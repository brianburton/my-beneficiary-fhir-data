package gov.cms.bfd.pipeline.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

import gov.cms.bfd.DataSourceComponents;
import gov.cms.bfd.DatabaseTestUtils;
import gov.cms.bfd.ProcessOutputConsumer;
import gov.cms.bfd.model.rif.RifFileType;
import gov.cms.bfd.model.rif.samples.StaticRifResource;
import gov.cms.bfd.pipeline.AbstractLocalStackS3Test;
import gov.cms.bfd.pipeline.ccw.rif.CcwRifLoadJob;
import gov.cms.bfd.pipeline.ccw.rif.extract.s3.DataSetManifest;
import gov.cms.bfd.pipeline.ccw.rif.extract.s3.DataSetManifest.DataSetManifestEntry;
import gov.cms.bfd.pipeline.ccw.rif.extract.s3.DataSetTestUtilities;
import gov.cms.bfd.pipeline.ccw.rif.load.CcwRifLoadTestUtils;
import gov.cms.bfd.pipeline.ccw.rif.load.LoadAppOptions;
import gov.cms.bfd.pipeline.rda.grpc.RdaFissClaimLoadJob;
import gov.cms.bfd.pipeline.rda.grpc.RdaMcsClaimLoadJob;
import gov.cms.bfd.pipeline.rda.grpc.server.RandomClaimGeneratorConfig;
import gov.cms.bfd.pipeline.rda.grpc.server.RdaMessageSourceFactory;
import gov.cms.bfd.pipeline.rda.grpc.server.RdaServer;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.apache.commons.codec.binary.Hex;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.utils.StringUtils;

/**
 * Integration tests for {@link PipelineApplication}.
 *
 * <p>These tests require the application pipeline assembly to be built and available. Accordingly,
 * they may not run correctly in Eclipse: if the assembly isn't built yet, they'll just fail, but if
 * an older assembly exists (because you haven't rebuilt it), it'll run using the old code, which
 * probably isn't what you want.
 */
public final class PipelineApplicationIT extends AbstractLocalStackS3Test {
  private static final Logger LOGGER = LoggerFactory.getLogger(PipelineApplicationIT.class);

  /** The POSIX signal number for the <code>SIGTERM</code> signal. */
  private static final int SIGTERM = 15;

  /**
   * Verifies that {@link PipelineApplication} exits as expected when launched with no configuration
   * environment variables.
   *
   * @throws IOException (indicates a test error)
   * @throws InterruptedException (indicates a test error)
   */
  @Test
  public void missingConfig() throws IOException, InterruptedException {
    // Start the app with no config env vars.
    ProcessBuilder appRunBuilder = createCcwRifAppProcessBuilder("foo");
    String javaHome = System.getenv("JAVA_HOME");
    appRunBuilder.environment().clear();
    appRunBuilder.environment().put("JAVA_HOME", javaHome);
    appRunBuilder.redirectErrorStream(true);
    Process appProcess = appRunBuilder.start();

    // Read the app's output.
    ProcessOutputConsumer appRunConsumer = new ProcessOutputConsumer(appProcess);
    Thread appRunConsumerThread = new Thread(appRunConsumer);
    appRunConsumerThread.start();

    // Wait for it to exit with an error.
    appProcess.waitFor(1, TimeUnit.MINUTES);
    appRunConsumerThread.join();
    // Verify that the application exited as expected.
    assertEquals(PipelineApplication.EXIT_CODE_BAD_CONFIG, appProcess.exitValue());
  }

  /**
   * Verifies that {@link PipelineApplication} works as expected when asked to run against an S3
   * bucket that doesn't exist. This test case isn't so much needed to test that one specific
   * failure case, but to instead verify that the application logs and keeps running as expected
   * when a job fails.
   *
   * @throws IOException (indicates a test error)
   * @throws InterruptedException (indicates a test error)
   */
  @Test
  public void missingBucket() throws IOException, InterruptedException {
    Process appProcess = null;
    try {
      // Start the app.
      ProcessBuilder appRunBuilder = createCcwRifAppProcessBuilder("foo");
      appRunBuilder.redirectErrorStream(true);
      appProcess = appRunBuilder.start();

      // Read the app's output.
      ProcessOutputConsumer appRunConsumer = new ProcessOutputConsumer(appProcess);
      Thread appRunConsumerThread = new Thread(appRunConsumer);
      appRunConsumerThread.start();

      // Wait for it to start scanning.
      Awaitility.await()
          .atMost(Durations.ONE_MINUTE)
          .until(() -> hasCcwRifLoadJobFailed(appRunConsumer));

      // Stop the application.
      sendSigterm(appProcess);
      appProcess.waitFor(1, TimeUnit.MINUTES);
      appRunConsumerThread.join();
    } finally {
      if (appProcess != null) appProcess.destroyForcibly();
    }
  }

  /**
   * Verifies that {@link PipelineApplication} works as expected when no data is made available for
   * it to process. Basically, it should just sit there and wait for data, doing nothing.
   *
   * @throws IOException (indicates a test error)
   * @throws InterruptedException (indicates a test error)
   */
  @Test
  public void noRifData() throws IOException, InterruptedException {
    skipOnUnsupportedOs();

    String bucket = null;
    Process appProcess = null;
    try {
      // Create the (empty) bucket to run against.
      bucket = s3Dao.createTestBucket();

      // Start the app.
      ProcessBuilder appRunBuilder = createCcwRifAppProcessBuilder(bucket);
      appRunBuilder.redirectErrorStream(true);
      appProcess = appRunBuilder.start();

      // Read the app's output.
      ProcessOutputConsumer appRunConsumer = new ProcessOutputConsumer(appProcess);
      Thread appRunConsumerThread = new Thread(appRunConsumer);
      appRunConsumerThread.start();

      // Wait for it to start scanning.
      try {
        Awaitility.await()
            .atMost(Durations.ONE_MINUTE)
            .until(() -> hasCcwRifLoadJobCompleted(appRunConsumer));
      } catch (ConditionTimeoutException e) {
        throw new RuntimeException(
            "Pipeline application failed to start scanning within timeout, STDOUT:\n"
                + appRunConsumer.getStdoutContents(),
            e);
      }

      // Stop the application.
      sendSigterm(appProcess);
      appProcess.waitFor(1, TimeUnit.MINUTES);
      appRunConsumerThread.join();

      // Verify that the application exited as expected.
      verifyExitValueMatchesSignal(SIGTERM, appProcess);
    } finally {
      if (appProcess != null) appProcess.destroyForcibly();
      if (StringUtils.isNotBlank(bucket)) s3Dao.deleteTestBucket(bucket);
    }
  }

  /**
   * Verifies that {@link PipelineApplication} works as expected against a small amount of data. We
   * trust that other tests elsewhere are covering the ETL results' correctness; here we're just
   * verifying the overall flow. Does it find the data set, process it, and then not find a data set
   * anymore?
   *
   * @throws IOException (indicates a test error)
   * @throws InterruptedException (indicates a test error)
   */
  @Test
  public void smallAmountOfRifData() throws IOException, InterruptedException {
    skipOnUnsupportedOs();

    String bucket = null;
    Process appProcess = null;
    try {
      /*
       * Create the (empty) bucket to run against, and populate it with a
       * data set.
       */
      bucket = s3Dao.createTestBucket();
      DataSetManifest manifest =
          new DataSetManifest(
              Instant.now(),
              0,
              false,
              CcwRifLoadJob.S3_PREFIX_PENDING_DATA_SETS,
              CcwRifLoadJob.S3_PREFIX_COMPLETED_DATA_SETS,
              new DataSetManifestEntry("beneficiaries.rif", RifFileType.BENEFICIARY),
              new DataSetManifestEntry("carrier.rif", RifFileType.CARRIER));
      DataSetTestUtilities.putObject(s3Dao, bucket, manifest);
      DataSetTestUtilities.putObject(
          s3Dao,
          bucket,
          manifest,
          manifest.getEntries().get(0),
          StaticRifResource.SAMPLE_A_BENES.getResourceUrl());
      DataSetTestUtilities.putObject(
          s3Dao,
          bucket,
          manifest,
          manifest.getEntries().get(1),
          StaticRifResource.SAMPLE_A_CARRIER.getResourceUrl());

      // Start the app.
      ProcessBuilder appRunBuilder = createCcwRifAppProcessBuilder(bucket);
      appRunBuilder.redirectErrorStream(true);
      appProcess = appRunBuilder.start();
      appProcess.getOutputStream().close();

      // Read the app's output.
      ProcessOutputConsumer appRunConsumer = new ProcessOutputConsumer(appProcess);
      Thread appRunConsumerThread = new Thread(appRunConsumer);
      appRunConsumerThread.start();

      try {
        // Wait for it to process a data set.
        Awaitility.await()
            .atMost(Durations.ONE_MINUTE)
            .until(() -> hasADataSetBeenProcessed(appRunConsumer));
      } catch (ConditionTimeoutException e) {
        throw new RuntimeException(
            "Failed to process data within time, STDOUT:\n" + appRunConsumer.getStdoutContents(),
            e);
      }

      // Stop the application.
      sendSigterm(appProcess);
      appProcess.waitFor(1, TimeUnit.MINUTES);
      appRunConsumerThread.join();

      // Verify that the application exited as expected.
      verifyExitValueMatchesSignal(SIGTERM, appProcess);
    } finally {
      if (appProcess != null) appProcess.destroyForcibly();
      if (StringUtils.isNotBlank(bucket)) s3Dao.deleteTestBucket(bucket);
    }
  }

  /**
   * Verifies the RDA pipeline can be configured, started, and shut down successfully.
   *
   * @throws Exception indicates test failure
   */
  @Test
  public void rdaPipeline() throws Exception {
    skipOnUnsupportedOs();

    final AtomicReference<Process> appProcess = new AtomicReference<>();
    try {
      final var randomClaimConfig =
          RandomClaimGeneratorConfig.builder().seed(12345).maxToSend(30).build();
      final var serviceConfig =
          RdaMessageSourceFactory.Config.builder().randomClaimConfig(randomClaimConfig).build();
      RdaServer.LocalConfig.builder()
          .serviceConfig(serviceConfig)
          .build()
          .runWithPortParam(
              port -> {
                // Start the app.
                ProcessBuilder appRunBuilder = createRdaAppProcessBuilder(port);
                appRunBuilder.redirectErrorStream(true);
                appProcess.set(appRunBuilder.start());

                // Read the app's output.
                ProcessOutputConsumer appRunConsumer = new ProcessOutputConsumer(appProcess.get());
                Thread appRunConsumerThread = new Thread(appRunConsumer);
                appRunConsumerThread.start();

                // Wait for it to start scanning.
                try {
                  Awaitility.await()
                      .atMost(Durations.ONE_MINUTE)
                      .until(
                          () ->
                              hasRdaFissLoadJobCompleted(appRunConsumer)
                                  && hasRdaMcsLoadJobCompleted(appRunConsumer));
                } catch (ConditionTimeoutException e) {
                  throw new RuntimeException(
                      "Pipeline application failed to start scanning within timeout, STDOUT:\n"
                          + appRunConsumer.getStdoutContents(),
                      e);
                }

                assertTrue(
                    hasJobRecordMatching(
                        appRunConsumer,
                        line -> line.contains("processed 30 objects in"),
                        RdaFissClaimLoadJob.class),
                    "FISS job processed all claims");

                assertTrue(
                    hasJobRecordMatching(
                        appRunConsumer,
                        line -> line.contains("processed 30 objects in"),
                        RdaMcsClaimLoadJob.class),
                    "MCS job processed all claims");

                // Stop the application.
                sendSigterm(appProcess.get());
                appProcess.get().waitFor(1, TimeUnit.MINUTES);
                appRunConsumerThread.join();

                // Verify that the application exited as expected.
                verifyExitValueMatchesSignal(SIGTERM, appProcess.get());
              });
    } finally {
      if (appProcess.get() != null) appProcess.get().destroyForcibly();
    }
  }

  /**
   * Verifies that when there is an exception while running the RDA jobs they complete normally
   * after logging their exception.
   *
   * @throws Exception indicates a test failure
   */
  @Test
  public void rdaPipelineServerFailure() throws Exception {
    skipOnUnsupportedOs();

    final AtomicReference<Process> appProcess = new AtomicReference<>();
    try {
      final var randomClaimConfig =
          RandomClaimGeneratorConfig.builder().seed(12345).maxToSend(100).build();
      final var serviceConfig =
          RdaMessageSourceFactory.Config.builder()
              .randomClaimConfig(randomClaimConfig)
              .throwExceptionAfterCount(25)
              .build();
      RdaServer.LocalConfig.builder()
          .serviceConfig(serviceConfig)
          .build()
          .runWithPortParam(
              port -> {
                // Start the app.
                ProcessBuilder appRunBuilder = createRdaAppProcessBuilder(port);
                appRunBuilder.redirectErrorStream(true);
                appProcess.set(appRunBuilder.start());

                // Read the app's output.
                ProcessOutputConsumer appRunConsumer = new ProcessOutputConsumer(appProcess.get());
                Thread appRunConsumerThread = new Thread(appRunConsumer);
                appRunConsumerThread.start();

                // Wait for it to start scanning.
                try {
                  Awaitility.await()
                      .atMost(Durations.ONE_MINUTE)
                      .until(
                          () ->
                              hasRdaFissLoadJobCompleted(appRunConsumer)
                                  && hasRdaMcsLoadJobCompleted(appRunConsumer));
                } catch (ConditionTimeoutException e) {
                  throw new RuntimeException(
                      "Pipeline application failed to start scanning within timeout, STDOUT:\n"
                          + appRunConsumer.getStdoutContents(),
                      e);
                }

                assertTrue(
                    hasJobRecordMatching(
                        appRunConsumer,
                        line -> line.contains("StatusRuntimeException"),
                        RdaFissClaimLoadJob.class),
                    "FISS job terminated by grpc exception");

                assertTrue(
                    hasJobRecordMatching(
                        appRunConsumer,
                        line -> line.contains("StatusRuntimeException"),
                        RdaMcsClaimLoadJob.class),
                    "MCS job terminated by grpc exception");

                // Stop the application.
                sendSigterm(appProcess.get());
                appProcess.get().waitFor(1, TimeUnit.MINUTES);
                appRunConsumerThread.join();

                // Verify that the application exited as expected.
                verifyExitValueMatchesSignal(SIGTERM, appProcess.get());
              });
    } finally {
      if (appProcess.get() != null) appProcess.get().destroyForcibly();
    }
  }

  /**
   * Throws an {@link TestAbortedException} if the OS doesn't support <strong>graceful</strong>
   * shutdowns via {@link Process#destroy()}.
   */
  private static void skipOnUnsupportedOs() {
    /*
     * The only OS I know for sure that handles this correctly is Linux,
     * because I've verified that there. However, the following project
     * seems to indicate that Linux really might be it:
     * https://github.com/zeroturnaround/zt-process-killer. Some further
     * research indicates that this could be supported on Windows for GUI
     * apps, but not console apps. If this lack of OS support ever proves to
     * be a problem, the best thing to do would be to enhance our
     * application such that it listens on a particular port for shutdown
     * requests, and handles them gracefully.
     */

    assumeTrue(
        Arrays.asList("Linux", "Mac OS X").contains(System.getProperty("os.name")),
        "Unsupported OS for this test case.");
  }

  /**
   * Checks if the CCW RIF load job has completed by checking the job records.
   *
   * @param appRunConsumer the {@link ProcessOutputConsumer} whose output should be checked
   * @return <code>true</code> if the application output indicates that data set scanning has
   *     started, <code>false</code> if not
   */
  private static boolean hasCcwRifLoadJobCompleted(ProcessOutputConsumer appRunConsumer) {
    return hasJobRecordMatching(
        appRunConsumer, PipelineJobRunner.JobRunSummary::isSuccessString, CcwRifLoadJob.class);
  }

  /**
   * Checks if the RDA Fiss load job has completed by checking the job records.
   *
   * @param appRunConsumer the {@link ProcessOutputConsumer} whose output should be checked
   * @return <code>true</code> if the application output indicates that data set scanning has
   *     started, <code>false</code> if not
   */
  private static boolean hasRdaFissLoadJobCompleted(ProcessOutputConsumer appRunConsumer) {
    return hasJobRecordMatching(
        appRunConsumer,
        PipelineJobRunner.JobRunSummary::isSuccessString,
        RdaFissClaimLoadJob.class);
  }

  /**
   * Checks if the RDA MCS load job has completed by checking the job records.
   *
   * @param appRunConsumer the {@link ProcessOutputConsumer} whose output should be checked
   * @return <code>true</code> if the application output indicates that data set scanning has
   *     started, <code>false</code> if not
   */
  private static boolean hasRdaMcsLoadJobCompleted(ProcessOutputConsumer appRunConsumer) {
    return hasJobRecordMatching(
        appRunConsumer, PipelineJobRunner.JobRunSummary::isSuccessString, RdaMcsClaimLoadJob.class);
  }

  /**
   * Checks if the CCW RIF load job has failed by checking the job records.
   *
   * @param appRunConsumer the {@link ProcessOutputConsumer} whose output should be checked
   * @return <code>true</code> if the application output indicates that the {@link CcwRifLoadJob}
   *     failed, <code>false</code> if not
   */
  private static boolean hasCcwRifLoadJobFailed(ProcessOutputConsumer appRunConsumer) {
    return hasJobRecordMatching(
        appRunConsumer, PipelineJobRunner.JobRunSummary::isFailureString, CcwRifLoadJob.class);
  }

  /**
   * Checks if a job has a job record matching a specified predicate.
   *
   * @param appRunConsumer the job to check
   * @param matcher {@link Predicate} used to find a target string
   * @param klass used to verify a target string contains the class name
   * @return {@code true} if the job had a record matching the specified predicate and class name
   */
  private static boolean hasJobRecordMatching(
      ProcessOutputConsumer appRunConsumer, Predicate<String> matcher, Class<?> klass) {
    return appRunConsumer.matches(
        line -> matcher.test(line) && line.contains(klass.getSimpleName()));
  }

  /**
   * Verifies a data set has been processed by the specified job by checking for a specific log
   * message.
   *
   * @param appRunConsumer the {@link ProcessOutputConsumer} whose output should be checked
   * @return <code>true</code> if the application output indicates that a data set has been
   *     processed, <code>false</code> if not
   */
  private static boolean hasADataSetBeenProcessed(ProcessOutputConsumer appRunConsumer) {
    return appRunConsumer.matches(
        line -> line.contains(CcwRifLoadJob.LOG_MESSAGE_DATA_SET_COMPLETE));
  }

  /**
   * Sends a <code>SIGTERM</code> to the specified {@link Process}, causing it to exit, but giving
   * it a chance to do so gracefully.
   *
   * @param process the {@link Process} to signal
   */
  private static void sendSigterm(Process process) {
    /*
     * We have to use reflection and external commands here to work around
     * this ridiculous JDK bug:
     * https://bugs.openjdk.java.net/browse/JDK-5101298.
     */
    if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
      try {
        Field pidField = process.getClass().getDeclaredField("pid");
        pidField.setAccessible(true);

        int processPid = pidField.getInt(process);

        ProcessBuilder killBuilder =
            new ProcessBuilder("/bin/kill", "--signal", "TERM", "" + processPid);
        int killBuilderExitCode = killBuilder.start().waitFor();
        if (killBuilderExitCode != 0) process.destroy();
      } catch (NoSuchFieldException
          | SecurityException
          | IllegalArgumentException
          | IllegalAccessException
          | InterruptedException
          | IOException e) {
        process.destroy();
        throw new RuntimeException(e);
      }
    } else {
      /*
       * Not sure if this bug exists on Windows or not (may cause test
       * cases to fail, if it does, because we wouldn't be able to read
       * all of the processes' output after they're stopped). If it does,
       * we could follow up on the ideas here to add a similar
       * platform-specific workaround:
       * https://stackoverflow.com/questions/140111/sending-an-arbitrary-
       * signal-in-windows.
       */
      process.destroy();
    }
  }

  /**
   * Verifies that the specified {@link Process} has exited, due to the specified signal.
   *
   * @param signalNumber the POSIX signal number to check for
   * @param process the {@link Process} to check the {@link Process#exitValue()} of
   */
  private static void verifyExitValueMatchesSignal(int signalNumber, Process process) {
    /*
     * Per POSIX (by way of http://unix.stackexchange.com/a/99143),
     * applications that exit due to a signal should return an exit code
     * that is 128 + the signal number.
     */
    assertEquals(128 + signalNumber, process.exitValue());
  }

  /**
   * Creates a ProcessBuilder with the common settings used by CCW/RIF and RDA tests.
   *
   * @return ProcessBuilder ready for more env vars to be added
   */
  private ProcessBuilder createAppProcessBuilder() {
    String[] command = createCommandForPipelineApp();
    ProcessBuilder appRunBuilder = new ProcessBuilder(command);
    appRunBuilder.redirectErrorStream(true);

    DataSource dataSource = DatabaseTestUtils.get().getUnpooledDataSource();
    DataSourceComponents dataSourceComponents = new DataSourceComponents(dataSource);

    // Remove inherited environment variables that could affect the test in some local environments.
    Map<String, String> environment = appRunBuilder.environment();
    List.of(
            AppConfiguration.ENV_VAR_KEY_CCW_RIF_JOB_ENABLED,
            AppConfiguration.ENV_VAR_KEY_RDA_JOB_ENABLED,
            AppConfiguration.ENV_VAR_KEY_RDA_GRPC_HOST,
            AppConfiguration.ENV_VAR_KEY_RDA_GRPC_PORT,
            AppConfiguration.ENV_VAR_KEY_RDA_GRPC_AUTH_TOKEN,
            AppConfiguration.ENV_VAR_KEY_RDA_GRPC_SERVER_TYPE)
        .forEach(environment::remove);

    environment.put(
        AppConfiguration.ENV_VAR_KEY_HICN_HASH_ITERATIONS,
        String.valueOf(CcwRifLoadTestUtils.HICN_HASH_ITERATIONS));
    environment.put(
        AppConfiguration.ENV_VAR_KEY_HICN_HASH_PEPPER,
        Hex.encodeHexString(CcwRifLoadTestUtils.HICN_HASH_PEPPER));
    environment.put(AppConfiguration.ENV_VAR_KEY_DATABASE_URL, dataSourceComponents.getUrl());
    environment.put(
        AppConfiguration.ENV_VAR_KEY_DATABASE_USERNAME, dataSourceComponents.getUsername());
    environment.put(
        AppConfiguration.ENV_VAR_KEY_DATABASE_PASSWORD, dataSourceComponents.getPassword());
    environment.put(
        AppConfiguration.ENV_VAR_KEY_LOADER_THREADS,
        String.valueOf(LoadAppOptions.DEFAULT_LOADER_THREADS));
    environment.put(
        AppConfiguration.ENV_VAR_KEY_IDEMPOTENCY_REQUIRED,
        String.valueOf(CcwRifLoadTestUtils.IDEMPOTENCY_REQUIRED));

    environment.put(
        AppConfiguration.ENV_VAR_KEY_S3_ENDPOINT_URI,
        localstack.getEndpointOverride(S3).toString());
    environment.put(AppConfiguration.ENV_VAR_KEY_S3_ACCESS_KEY, localstack.getAccessKey());
    environment.put(AppConfiguration.ENV_VAR_KEY_S3_SECRET_KEY, localstack.getSecretKey());

    /*
     * Note: Not explicitly providing AWS credentials here, as the child
     * process will inherit any that are present in this build/test process.
     */
    return appRunBuilder;
  }

  /**
   * Creates a ProcessBuilder configured for an CCS/RIF pipeline test.
   *
   * @param bucket the name of the S3 bucket that the application will be configured to pull RIF
   *     data from
   * @return a {@link ProcessBuilder} that can be used to launch the application
   */
  private ProcessBuilder createCcwRifAppProcessBuilder(String bucket) {
    ProcessBuilder appRunBuilder = createAppProcessBuilder();

    appRunBuilder.environment().put(AppConfiguration.ENV_VAR_KEY_BUCKET, bucket);
    appRunBuilder
        .environment()
        .put(
            AppConfiguration.ENV_VAR_KEY_RIF_FILTERING_NON_NULL_AND_NON_2023_BENES,
            Boolean.FALSE.toString());

    return appRunBuilder;
  }

  /**
   * Creates a ProcessBuilder configured for an RDA pipeline test.
   *
   * @param port the TCP/IP port that the RDA mock server is listening on
   * @return a {@link ProcessBuilder} that can be used to launch the application
   */
  private ProcessBuilder createRdaAppProcessBuilder(int port) {
    ProcessBuilder appRunBuilder = createAppProcessBuilder();

    appRunBuilder.environment().put(AppConfiguration.ENV_VAR_KEY_CCW_RIF_JOB_ENABLED, "false");
    appRunBuilder.environment().put(AppConfiguration.ENV_VAR_KEY_RDA_JOB_ENABLED, "true");
    appRunBuilder.environment().put(AppConfiguration.ENV_VAR_KEY_RDA_JOB_BATCH_SIZE, "10");
    appRunBuilder
        .environment()
        .put(AppConfiguration.ENV_VAR_KEY_RDA_GRPC_PORT, String.valueOf(port));

    return appRunBuilder;
  }

  /**
   * Creates a command to run the pipeline application.
   *
   * @return the command array for {@link ProcessBuilder#ProcessBuilder(String...)} that will launch
   *     the application via its <code>.x</code> assembly executable script
   */
  private String[] createCommandForPipelineApp() {
    try {
      Path assemblyDirectory =
          Files.list(Paths.get(".", "target", "pipeline-app"))
              .filter(f -> f.getFileName().toString().startsWith("bfd-pipeline-app-"))
              .findFirst()
              .get();
      Path pipelineAppScript = assemblyDirectory.resolve("bfd-pipeline-app.sh");

      return new String[] {pipelineAppScript.toAbsolutePath().toString()};
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
