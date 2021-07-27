package gov.cms.bfd.pipeline.rda.grpc.sink;

import static gov.cms.bfd.pipeline.sharedutils.PipelineApplicationState.RDA_PERSISTENCE_UNIT_NAME;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;
import gov.cms.bfd.pipeline.rda.grpc.AbstractRdaLoadJob;
import gov.cms.bfd.pipeline.rda.grpc.RdaLoadOptions;
import gov.cms.bfd.pipeline.rda.grpc.source.GrpcRdaSource;
import gov.cms.bfd.pipeline.sharedutils.DatabaseOptions;
import gov.cms.bfd.pipeline.sharedutils.IdHasher;
import gov.cms.bfd.pipeline.sharedutils.PipelineApplicationState;
import gov.cms.bfd.pipeline.sharedutils.PipelineJob;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.time.Duration;
import java.util.Optional;
import java.util.Properties;

/**
 * Simple application that invokes an RDA API server and writes FISS claims to a database using
 * RdaLoadJob. This program was written for testing purposes and is not planned to be executed in a
 * production environment.
 *
 * <p>Two command line options are required when running the program:
 *
 * <ol>
 *   <li>file: path to a properties file containing configuration settings
 *   <li>claimType: either fiss or mcs to specify type of claims to download
 * </ol>
 */
public class DirectRdaLoadApp {
  public static void main(String[] args) throws Exception {
    if (args.length != 2) {
      System.err.printf("usage: %s configfile claimType%n", DirectRdaLoadApp.class.getSimpleName());
      System.exit(1);
    }
    Properties props = new Properties();
    try (Reader in = new BufferedReader(new FileReader(args[0]))) {
      props.load(in);
    }
    final String claimType = Strings.nullToEmpty(args[1]);

    final RdaLoadOptions jobConfig = readRdaLoadOptionsFromProperties(props);
    final DatabaseOptions databaseConfig = readDatabaseOptions(props);
    final PipelineApplicationState appState =
        new PipelineApplicationState(
            new MetricRegistry(), databaseConfig, RDA_PERSISTENCE_UNIT_NAME);
    final Optional<PipelineJob<?>> job = createPipelineJob(jobConfig, appState, claimType);
    if (!job.isPresent()) {
      System.err.printf("error: invalid claim type: '%s' expected 'fiss' or 'mcs'%n", claimType);
      System.exit(1);
    }
    job.get().call();
  }

  private static Optional<PipelineJob<?>> createPipelineJob(
      RdaLoadOptions jobConfig, PipelineApplicationState appState, String claimType) {
    switch (claimType.toLowerCase()) {
      case "fiss":
        return Optional.of(jobConfig.createFissClaimsLoadJob(appState));
      case "mcs":
        return Optional.of(jobConfig.createMcsClaimsLoadJob(appState));
      default:
        return Optional.empty();
    }
  }

  private static DatabaseOptions readDatabaseOptions(Properties props) {
    return new DatabaseOptions(
        props.getProperty("database.url"),
        props.getProperty("database.user"),
        props.getProperty("database.password"),
        10);
  }

  private static RdaLoadOptions readRdaLoadOptionsFromProperties(Properties props) {
    final IdHasher.Config idHasherConfig =
        new IdHasher.Config(
            getIntOrDefault(props, "hash.iterations", 100),
            props.getProperty("hash.pepper", "notarealpepper"));
    final AbstractRdaLoadJob.Config jobConfig =
        new AbstractRdaLoadJob.Config(
            Duration.ofDays(1), getIntOrDefault(props, "job.batchSize", 1));
    final GrpcRdaSource.Config grpcConfig =
        new GrpcRdaSource.Config(
            props.getProperty("api.host", "localhost"),
            getIntOrDefault(props, "api.port", 5003),
            Duration.ofSeconds(getIntOrDefault(props, "job.idleSeconds", Integer.MAX_VALUE)));
    return new RdaLoadOptions(jobConfig, grpcConfig, idHasherConfig);
  }

  private static int getIntOrDefault(Properties props, String key, int defaultValue) {
    String strValue = props.getProperty(key);
    return strValue != null ? Integer.parseInt(strValue) : defaultValue;
  }
}
