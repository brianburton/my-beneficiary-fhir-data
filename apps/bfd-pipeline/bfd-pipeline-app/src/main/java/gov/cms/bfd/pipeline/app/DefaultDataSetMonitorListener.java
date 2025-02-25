package gov.cms.bfd.pipeline.app;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.Timer;
import gov.cms.bfd.model.rif.RifFileEvent;
import gov.cms.bfd.model.rif.RifFileRecords;
import gov.cms.bfd.model.rif.RifFilesEvent;
import gov.cms.bfd.pipeline.ccw.rif.CcwRifLoadJob;
import gov.cms.bfd.pipeline.ccw.rif.extract.RifFilesProcessor;
import gov.cms.bfd.pipeline.ccw.rif.extract.s3.DataSetMonitorListener;
import gov.cms.bfd.pipeline.ccw.rif.load.RifLoader;
import gov.cms.bfd.pipeline.ccw.rif.load.RifRecordLoadResult;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link DataSetMonitorListener} implementation "glues together" the {@link CcwRifLoadJob}
 * with the {@link RifFilesProcessor} and the {@link RifLoader}: pulling all of the data sets out of
 * S3, parsing them, and then loading them into the BFD database.
 */
public final class DefaultDataSetMonitorListener implements DataSetMonitorListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDataSetMonitorListener.class);

  /** Metrics for this class. */
  private final MetricRegistry appMetrics;
  /** The application's error handler. */
  private final Consumer<Throwable> errorHandler;
  /** Handles processing of new RIF files. */
  private final RifFilesProcessor rifProcessor;
  /** Loads RIF files into the database. */
  private final RifLoader rifLoader;

  /**
   * Constructs a new {@link DataSetMonitorListener} instance.
   *
   * @param appMetrics the {@link MetricRegistry} for the application
   * @param errorHandler the application's error handler
   * @param rifProcessor the {@link RifFilesProcessor} for the application
   * @param rifLoader the {@link RifLoader} for the application
   */
  DefaultDataSetMonitorListener(
      MetricRegistry appMetrics,
      Consumer<Throwable> errorHandler,
      RifFilesProcessor rifProcessor,
      RifLoader rifLoader) {
    this.appMetrics = appMetrics;
    this.errorHandler = errorHandler;
    this.rifProcessor = rifProcessor;
    this.rifLoader = rifLoader;
  }

  /** {@inheritDoc} */
  @Override
  public void dataAvailable(RifFilesEvent rifFilesEvent) {
    Timer.Context timerDataSet =
        appMetrics
            .timer(
                MetricRegistry.name(
                    PipelineApplication.class.getSimpleName(), "dataSet", "processed"))
            .time();

    final var failed = new AtomicBoolean(false);
    Consumer<Throwable> errorHandler =
        error -> {
          /*
           * This will be called on the same thread used to run each
           * RifLoader task (probably a background one). This is not
           * the right place to do any error _recovery_ (that'd have
           * to be inside RifLoader itself), but it is likely the
           * right place to decide when/if a failure is "bad enough"
           * that the rest of processing should be stopped. Right now
           * we stop that way for _any_ failure, but we probably want
           * to be more discriminating than that.
           */
          failed.set(true);
          errorOccurred(error);
        };

    Consumer<RifRecordLoadResult> resultHandler =
        result -> {
          /*
           * Don't really *need* to do anything here. The RifLoader
           * already records metrics for each data set.
           */
        };

    /*
     * Each ETL stage produces a stream that will be handed off to
     * and processed by the next stage.
     */
    for (RifFileEvent rifFileEvent : rifFilesEvent.getFileEvents()) {
      if (failed.get()) {
        LOGGER.info("Stopping due to error.");
        break;
      }
      Slf4jReporter dataSetFileMetricsReporter =
          Slf4jReporter.forRegistry(rifFileEvent.getEventMetrics()).outputTo(LOGGER).build();
      dataSetFileMetricsReporter.start(2, TimeUnit.MINUTES);

      RifFileRecords rifFileRecords = rifProcessor.produceRecords(rifFileEvent);
      try {
        rifLoader.process(rifFileRecords, errorHandler, resultHandler);
      } catch (Exception e) {
        LOGGER.error("Exception while processing RIF file");
        // Stop the metrics to help avoid zombie threads
        dataSetFileMetricsReporter.stop();
        timerDataSet.stop();
        throw e;
      }

      dataSetFileMetricsReporter.stop();
      dataSetFileMetricsReporter.report();
    }
    timerDataSet.stop();
  }

  /** {@inheritDoc} */
  @Override
  public void errorOccurred(Throwable error) {
    errorHandler.accept(error);
  }

  /** Called when no RIF files are available to process. */
  @Override
  public void noDataAvailable() {
    // Nothing to do here.
  }
}
