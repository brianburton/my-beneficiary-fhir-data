package gov.cms.bfd.pipeline.ccw.rif;

import com.codahale.metrics.MetricRegistry;
import gov.cms.bfd.model.rif.RifFilesEvent;
import gov.cms.bfd.pipeline.ccw.rif.extract.ExtractionOptions;
import gov.cms.bfd.pipeline.ccw.rif.extract.s3.DataSetManifest;
import gov.cms.bfd.pipeline.ccw.rif.extract.s3.DataSetManifest.DataSetManifestEntry;
import gov.cms.bfd.pipeline.ccw.rif.extract.s3.DataSetManifest.DataSetManifestId;
import gov.cms.bfd.pipeline.ccw.rif.extract.s3.DataSetMonitorListener;
import gov.cms.bfd.pipeline.ccw.rif.extract.s3.DataSetQueue;
import gov.cms.bfd.pipeline.ccw.rif.extract.s3.S3RifFile;
import gov.cms.bfd.pipeline.ccw.rif.extract.s3.task.DataSetMoveTask;
import gov.cms.bfd.pipeline.ccw.rif.extract.s3.task.S3TaskManager;
import gov.cms.bfd.pipeline.sharedutils.PipelineApplicationState;
import gov.cms.bfd.pipeline.sharedutils.PipelineJob;
import gov.cms.bfd.pipeline.sharedutils.PipelineJobOutcome;
import gov.cms.bfd.pipeline.sharedutils.PipelineJobSchedule;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link PipelineJob} checks for and, if found, processes data that has been pushed from CMS'
 * Chronic Conditions Data (CCW) into an AWS S3 bucket. The data in S3 will be structured as
 * follows:
 *
 * <ul>
 *   <li>Amazon S3 Bucket: <code>&lt;s3-bucket-name&gt;</code>
 *       <ul>
 *         <li><code>1997-07-16T19:20:30Z</code>
 *             <ul>
 *               <li><code>Incoming</code>
 *                   <ul>
 *                     <li><code>23_manifest.xml</code>
 *                     <li><code>beneficiaries_42.rif</code>
 *                     <li><code>bcarrier_58.rif</code>
 *                     <li><code>pde_93.rif</code>
 *                   </ul>
 *               <li><code>Done</code>
 *                   <ul>
 *                     <li><code>64_manifest.xml</code>
 *                     <li><code>beneficiaries_45.rif</code>
 *                   </ul>
 *             </ul>
 *       </ul>
 * </ul>
 *
 * <p>In that structure, there will be one top-level directory in the bucket for each data set that
 * has yet to be completely processed by the ETL pipeline. Its name will be an <a
 * href="https://www.w3.org/TR/NOTE-datetime">ISO 8601 date and time</a> expressed in UTC, to a
 * precision of at least seconds. This will represent (roughly) the time that the data set was
 * created. Within each of those directories will be manifest files and the RIF files that they
 * reference.
 */
public final class CcwRifLoadJob implements PipelineJob {
  private static final Logger LOGGER = LoggerFactory.getLogger(CcwRifLoadJob.class);

  /** Shortcut for calculating GIGA (filesize). */
  private static final int GIGA = 1000 * 1000 * 1000;

  /** The directory name that pending/incoming RIF data sets will be pulled from in S3. */
  public static final String S3_PREFIX_PENDING_DATA_SETS = "Incoming";

  /**
   * The directory name that pending/incoming synthetic RIF data sets can be pulled from in S3. In
   * essence this is just a second Incoming folder we can use for organization; it is not
   * functionally different from {@link #S3_PREFIX_PENDING_DATA_SETS}.
   */
  public static final String S3_PREFIX_PENDING_SYNTHETIC_DATA_SETS = "Synthetic/Incoming";

  /** The directory name that completed/done RIF data sets will be moved to in S3. */
  public static final String S3_PREFIX_COMPLETED_DATA_SETS = "Done";

  /**
   * The directory name that completed/done RIF data sets loaded from {@link
   * #S3_PREFIX_PENDING_SYNTHETIC_DATA_SETS} will be moved to in S3.
   */
  public static final String S3_PREFIX_COMPLETED_SYNTHETIC_DATA_SETS = "Synthetic/Done";

  /**
   * The directory name that failed RIF data sets are moved to {@link
   * #S3_PREFIX_FAILED_SYNTHETIC_DATA_SETS} will be moved to in S3.
   */
  public static final String S3_PREFIX_FAILED_SYNTHETIC_DATA_SETS = "Synthetic/Failed";

  /**
   * The {@link Logger} message that will be recorded if/when the {@link CcwRifLoadJob} goes and
   * looks, but doesn't find any data sets waiting to be processed.
   */
  public static final String LOG_MESSAGE_NO_DATA_SETS = "No data sets to process found.";

  /**
   * The {@link Logger} message that will be recorded if/when the {@link CcwRifLoadJob} starts
   * processing a data set.
   */
  public static final String LOG_MESSAGE_DATA_SET_READY = "Data set ready. Processing it...";

  /**
   * The {@link Logger} message that will be recorded if/when the {@link CcwRifLoadJob} completes
   * the processing of a data set.
   */
  public static final String LOG_MESSAGE_DATA_SET_COMPLETE = "Data set processing complete.";

  /**
   * A regex for {@link DataSetManifest} keys in S3. Provides capturing groups for the {@link
   * DataSetManifestId} fields.
   */
  public static final Pattern REGEX_PENDING_MANIFEST =
      Pattern.compile(
          "^("
              + S3_PREFIX_PENDING_DATA_SETS
              + "|"
              + S3_PREFIX_PENDING_SYNTHETIC_DATA_SETS
              + ")/(.*)/([0-9]+)_manifest\\.xml$");

  /**
   * A regex that can be used for checking for a manifest in the {@link
   * #S3_PREFIX_COMPLETED_DATA_SETS} location.
   */
  public static final Pattern REGEX_COMPLETED_MANIFEST =
      Pattern.compile(
          "^("
              + S3_PREFIX_COMPLETED_DATA_SETS
              + "|"
              + S3_PREFIX_COMPLETED_SYNTHETIC_DATA_SETS
              + ")/(.*)/([0-9]+)_manifest\\.xml$");

  /** The application metrics. */
  private final MetricRegistry appMetrics;
  /** The extraction options. */
  private final ExtractionOptions options;
  /** The data set listener for finding new files to load. */
  private final DataSetMonitorListener listener;
  /** The manager for taking actions with S3. */
  private final S3TaskManager s3TaskManager;
  /** The application state. */
  private final PipelineApplicationState appState;
  /** If the application is in idempotent mode. */
  private final boolean isIdempotentMode;
  /** The queue of S3 data to be processed. */
  private final DataSetQueue dataSetQueue;

  /**
   * Constructs a new {@link CcwRifLoadJob} instance.
   *
   * @param appState the {@link PipelineApplicationState} for the overall application
   * @param options the {@link ExtractionOptions} to use
   * @param s3TaskManager the {@link S3TaskManager} to use
   * @param listener the {@link DataSetMonitorListener} to send events to
   * @param isIdempotentMode the {@link boolean} TRUE if running in idenpotent mode
   */
  public CcwRifLoadJob(
      PipelineApplicationState appState,
      ExtractionOptions options,
      S3TaskManager s3TaskManager,
      DataSetMonitorListener listener,
      boolean isIdempotentMode) {
    this.appState = appState;
    this.appMetrics = appState.getMetrics();
    this.options = options;
    this.s3TaskManager = s3TaskManager;
    this.listener = listener;
    this.isIdempotentMode = isIdempotentMode;

    this.dataSetQueue = new DataSetQueue(appMetrics, options, s3TaskManager);
  }

  /** {@inheritDoc} */
  @Override
  public Optional<PipelineJobSchedule> getSchedule() {
    return Optional.of(new PipelineJobSchedule(1, ChronoUnit.SECONDS));
  }

  /** {@inheritDoc} */
  @Override
  public boolean isInterruptible() {
    /*
     * TODO This would be a good enhancement: making this class properly
     * interruptible. This would
     * allow us to shut things down quickly when a load is running.
     */
    return false;
  }

  /** {@inheritDoc} */
  @Override
  public PipelineJobOutcome call() throws Exception {
    LOGGER.debug("Scanning for data sets to process...");

    // Update the queue from S3.
    dataSetQueue.updatePendingDataSets();

    // If no manifest was found, we're done (until next time).
    if (dataSetQueue.isEmpty()) {
      LOGGER.debug(LOG_MESSAGE_NO_DATA_SETS);
      listener.noDataAvailable();
      return PipelineJobOutcome.NOTHING_TO_DO;
    }

    // We've found the oldest manifest.
    DataSetManifest manifestToProcess = dataSetQueue.getNextDataSetToProcess().get();
    LOGGER.info(
        "Found data set to process: '{}'."
            + " There were '{}' total pending data sets and '{}' completed ones.",
        manifestToProcess.toString(),
        dataSetQueue.getPendingManifestsCount(),
        dataSetQueue.getCompletedManifestsCount().get());

    /*
     * We've got a data set to process. However, it might still be uploading
     * to S3, so we need to wait for that to complete before we start
     * processing it.
     */
    boolean alreadyLoggedWaitingEvent = false;
    while (!dataSetIsAvailable(manifestToProcess)) {
      /*
       * We're very patient here, so we keep looping, but it's prudent to
       * pause between each iteration. TODO should eventually time out,
       * once we know how long transfers might take
       */
      try {
        if (!alreadyLoggedWaitingEvent) {
          LOGGER.info("Data set not ready. Waiting for it to finish uploading...");
          alreadyLoggedWaitingEvent = true;
        }
        Thread.sleep(1000 * 1);
      } catch (InterruptedException e) {
        /*
         * Many Java applications use InterruptedExceptions to signal
         * that a thread should stop what it's doing ASAP. This app
         * doesn't, so this is unexpected, and accordingly, we don't
         * know what to do. Safest bet is to blow up.
         */
        throw new RuntimeException(e);
      }
    }

    /*
     * Huzzah! We've got a data set to process and we've verified it's all there
     * waiting for us in S3. Now convert it into a RifFilesEvent (containing a List
     * of asynchronously-downloading S3RifFiles.
     */
    LOGGER.info(LOG_MESSAGE_DATA_SET_READY);
    LOGGER.info(
        "Data set syntheticData indicator is: {}",
        String.valueOf(manifestToProcess.isSyntheticData()));

    /*
     * The {@link DataSetManifest} can have an optional element {@link
     * PreValidationProperties}
     * which contains elements that can be used to preform a pre-validation
     * verification prior
     * to beginning the actual processing (loading) of data.
     *
     * For example, checking if a range of bene_id(s) will cause a database key
     * constraint
     * violation during an INSERT operation. However, if running in idempotent
     * mode, it will be acceptable to run 'as is' since this is probably a re-run
     * of a previous load and does not represent a pure INSERT(ing) of data.
     */
    boolean preValidationOK = true;
    if (manifestToProcess.getPreValidationProperties().isPresent()) {
      preValidationOK = (isIdempotentMode || checkPreValidationProperties(manifestToProcess));
    }

    /*
     * If pre-validation succeeded, then normal processing continues; however, if it
     * has failed
     * (currently only Synthea has pre-validation), then we'll skip over the normal
     * processing
     * and go directly to where the manifest and associated RIF files are (re-)moved
     * from the
     * incoming bucket folder.
     */
    if (preValidationOK) {
      List<S3RifFile> rifFiles =
          manifestToProcess.getEntries().stream()
              .map(
                  manifestEntry ->
                      new S3RifFile(
                          appMetrics, manifestEntry, s3TaskManager.downloadAsync(manifestEntry)))
              .collect(Collectors.toList());

      RifFilesEvent rifFilesEvent =
          new RifFilesEvent(
              manifestToProcess.getTimestamp(),
              manifestToProcess.isSyntheticData(),
              new ArrayList<>(rifFiles));

      /*
       * To save time for the next data set, peek ahead at it. If it's available and
       * it looks like there's enough disk space, start downloading it early in the
       * background.
       */
      Optional<DataSetManifest> secondManifestToProcess = dataSetQueue.getSecondDataSetToProcess();
      if (secondManifestToProcess.isPresent()
          && dataSetIsAvailable(secondManifestToProcess.get())) {
        Path tmpdir = Paths.get(System.getProperty("java.io.tmpdir"));
        long usableFreeTempSpace;
        try {
          usableFreeTempSpace = Files.getFileStore(tmpdir).getUsableSpace();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }

        if (usableFreeTempSpace >= (50 * GIGA)) {
          secondManifestToProcess.get().getEntries().stream()
              .forEach(manifestEntry -> s3TaskManager.downloadAsync(manifestEntry));
        }
      }

      /*
       * Now we hand that off to the DataSetMonitorListener, to do the *real*
       * work of actually processing that data set. It's important that we
       * block until it's completed, in order to ensure that we don't end up
       * processing multiple data sets in parallel (which would lead to data
       * consistency problems).
       */
      listener.dataAvailable(rifFilesEvent);
      LOGGER.info(LOG_MESSAGE_DATA_SET_COMPLETE);

      /*
       * Now that the data set has been processed, we need to ensure that we
       * don't end up processing it again. We ensure this two ways: 1) we keep
       * a list of the data sets most recently processed, and 2) we rename the
       * S3 objects that comprise that data set. (#1 is required as S3
       * deletes/moves are only *eventually* consistent, so #2 may not take
       * effect right away.)
       */
      rifFiles.stream().forEach(f -> f.cleanupTempFile());
    } else {
      /*
       * If here, Synthea pre-validation has failed; we want to move the S3 incoming
       * files
       * to a failed folder; so instead of moving files to a done folder we'll just
       * replace
       * the manifest's notion of its Done folder to a Failed folder.
       */
      manifestToProcess.setManifestKeyDoneLocation(S3_PREFIX_FAILED_SYNTHETIC_DATA_SETS);
    }
    dataSetQueue.markProcessed(manifestToProcess);
    s3TaskManager.submit(new DataSetMoveTask(s3TaskManager, options, manifestToProcess));

    return PipelineJobOutcome.WORK_DONE;
  }

  /**
   * Determines if all the objects listed in the specified manifest can be found in S3.
   *
   * @param manifest the {@link DataSetManifest} that lists the objects to verify the presence of
   * @return <code>true</code> if all the objects listed can be found in S3
   */
  private boolean dataSetIsAvailable(DataSetManifest manifest) {
    /*
     * There are two ways to do this: 1) list all the objects in the data
     * set and verify the ones we're looking for are there after, or 2) try
     * to grab the metadata for each object. Option #2 *should* be simpler,
     * but isn't, because each missing object will result in an exception.
     * Exceptions-as-control-flow is a poor design choice, so we'll go with
     * option #1.
     */

    final String dataSetKeyPrefix =
        String.format(
            "%s/%s/", manifest.getManifestKeyIncomingLocation(), manifest.getTimestampText());

    /*
     * Pull the object names from the keys that were returned, by
     * stripping the timestamp prefix and slash from each of them.
     */

    final Set<String> dataSetObjectNames =
        s3TaskManager
            .getS3Dao()
            .listObjects(
                options.getS3BucketName(),
                Optional.of(dataSetKeyPrefix),
                options.getS3ListMaxKeys())
            .peek(
                o ->
                    LOGGER.debug("Found file: '{}', part of data set: '{}'.", o.getKey(), manifest))
            .map(o -> o.getKey().substring(dataSetKeyPrefix.length()))
            .collect(Collectors.toSet());

    for (DataSetManifestEntry manifestEntry : manifest.getEntries()) {
      if (!dataSetObjectNames.contains(manifestEntry.getName())) {
        LOGGER.debug(
            "Waiting for file '{}', part of data set: '{}'.", manifestEntry.getName(), manifest);
        return false;
      }
    }
    return true;
  }

  /**
   * Perform pre-validation for a data load if the {@link
   * DataSetManifest#getPreValidationProperties()} has content. At this time, only Synthea-based
   * {@link DataSetManifest} have content that can be used for pre-validation.
   *
   * @param manifest the {@link DataSetManifest} that lists pre-validation properties to verify
   * @return <code>true</code> if all of the pre-validation parameters listed in the manifest do not
   *     introduce potential data loading issues, <code>false</code> if not
   */
  private boolean checkPreValidationProperties(DataSetManifest manifest) throws Exception {
    LOGGER.info(
        "PreValidationProperties found in manifest, ID: {}; verifying efficacy...",
        manifest.getId());

    // for now only Synthea manifests will have PreValidationProperties
    if (!manifest.isSyntheticData()) {
      return true;
    }

    /* we are processing Synthea data...setup a pre-validation interface. */
    CcwRifLoadPreValidateInterface preValInterface = new CcwRifLoadPreValidateSynthea();
    // initialize the interface with the appState
    preValInterface.init(appState);

    // perform whatever vaidation is appropriate
    LOGGER.info(
        "Synthea pre-validation being performed by: {}...", preValInterface.getClass().getName());
    return preValInterface.isValid(manifest);
  }
}
