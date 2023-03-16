package gov.cms.bfd.pipeline.rda.grpc.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.cms.bfd.model.rda.RdaFissClaim;
import gov.cms.bfd.pipeline.rda.grpc.server.JsonMessageSource;
import gov.cms.bfd.pipeline.rda.grpc.server.RandomFissClaimSource;
import gov.cms.bfd.pipeline.rda.grpc.server.RdaServer;
import gov.cms.bfd.pipeline.rda.grpc.server.RdaService;
import gov.cms.bfd.pipeline.rda.grpc.server.WrappedClaimSource;
import gov.cms.bfd.pipeline.rda.grpc.sink.direct.MbiCache;
import gov.cms.bfd.pipeline.sharedutils.IdHasher;
import gov.cms.mpsm.rda.v1.FissClaimChange;
import io.grpc.CallOptions;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/** Integration tests for the {@link FissClaimStreamCaller}. */
public class FissClaimStreamCallerIT {

  /** Example paid claim. */
  private static final String CLAIM_1 =
      "{"
          + "  \"rdaClaimKey\": \"63843470id\","
          + "  \"dcn\": \"63843470\","
          + "  \"intermediaryNb\": \"53412\","
          + "  \"hicNo\": \"916689703543\","
          + "  \"currStatusEnum\": \"CLAIM_STATUS_PAID\","
          + "  \"currLoc1Enum\": \"PROCESSING_TYPE_MANUAL\","
          + "  \"currLoc2Unrecognized\": \"uma\","
          + "  \"totalChargeAmount\": \"3.75\","
          + "  \"currTranDtCymd\": \"2021-03-20\","
          + "  \"principleDiag\": \"uec\","
          + "  \"mbi\": \"c1ihk7q0g3i\","
          + "  \"fissProcCodes\": [],"
          + "  \"medaProvId\": \"oducjgzt67joc\","
          + "  \"clmTypIndEnum\": \"CLAIM_TYPE_INPATIENT\""
          + "}";
  /** Example rejected claim. */
  private static final String CLAIM_2 =
      "{"
          + "  \"rdaClaimKey\": \"2643602id\","
          + "  \"dcn\": \"2643602\","
          + "  \"intermediaryNb\": \"24153\","
          + "  \"hicNo\": \"640930211775\","
          + "  \"currStatusEnum\": \"CLAIM_STATUS_REJECT\","
          + "  \"currLoc1Enum\": \"PROCESSING_TYPE_OFFLINE\","
          + "  \"currLoc2Unrecognized\": \"p6s\","
          + "  \"totalChargeAmount\": \"55.91\","
          + "  \"recdDtCymd\": \"2021-05-14\","
          + "  \"currTranDtCymd\": \"2020-12-21\","
          + "  \"principleDiag\": \"egnj\","
          + "  \"npiNumber\": \"5764657700\","
          + "  \"mbi\": \"0vtc7u321x0\","
          + "  \"fedTaxNb\": \"2845244764\","
          + "  \"fissProcCodes\": [],"
          + "  \"clmTypIndEnum\": \"CLAIM_TYPE_OUTPATIENT\""
          + "}";

  /** Clock for creating for consistent values in JSON (2021-06-03T18:02:37Z). */
  private final Clock clock = Clock.fixed(Instant.ofEpochMilli(1622743357000L), ZoneOffset.UTC);
  /** The test hasher. */
  private final IdHasher hasher = new IdHasher(new IdHasher.Config(10, "justsomestring"));
  /** The transformer to create results for correctness verification. */
  private final FissClaimTransformer transformer =
      new FissClaimTransformer(clock, MbiCache.computedCache(hasher.getConfig()));

  /**
   * Verifies the caller can respond to a basic request and the results contain the expected values.
   *
   * @throws Exception indicates a test failure / setup issue
   */
  @Test
  public void basicCall() throws Exception {
    RdaServer.InProcessConfig.builder()
        .serverName(getClass().getSimpleName())
        .fissSourceFactory(
            sequenceNumber ->
                WrappedClaimSource.wrapFissClaims(
                    new JsonMessageSource<>(
                        CLAIM_1 + System.lineSeparator() + CLAIM_2,
                        JsonMessageSource::parseFissClaim),
                    clock,
                    sequenceNumber))
        .build()
        .runWithChannelParam(
            channel -> {
              final FissClaimStreamCaller caller = new FissClaimStreamCaller();
              assertEquals(
                  RdaService.RDA_PROTO_VERSION,
                  caller.callVersionService(channel, CallOptions.DEFAULT));

              final GrpcResponseStream<FissClaimChange> results =
                  caller.callService(channel, CallOptions.DEFAULT, 0L);
              assertTrue(results.hasNext());

              RdaFissClaim claim = transform(results.next());
              assertEquals("63843470id", claim.getClaimId());
              assertEquals("63843470", claim.getDcn());
              assertEquals(Long.valueOf(1), claim.getSequenceNumber());
              assertTrue(results.hasNext());

              claim = transform(results.next());
              assertEquals("2643602id", claim.getClaimId());
              assertEquals("2643602", claim.getDcn());
              assertEquals(Long.valueOf(2), claim.getSequenceNumber());
              assertFalse(results.hasNext());
            });
  }

  /**
   * Verifies the caller's results have sequential sequence numbers.
   *
   * @throws Exception indicates a test failure / setup issue
   */
  @Test
  public void sequenceNumbers() throws Exception {
    RdaServer.InProcessConfig.builder()
        .serverName(getClass().getSimpleName())
        .fissSourceFactory(
            sequenceNumber ->
                new RandomFissClaimSource(1000L, 15).toClaimChanges().skip(sequenceNumber - 1))
        .build()
        .runWithChannelParam(
            channel -> {
              final FissClaimStreamCaller caller = new FissClaimStreamCaller();
              final GrpcResponseStream<FissClaimChange> results =
                  caller.callService(channel, CallOptions.DEFAULT, 10L);
              assertEquals(Long.valueOf(10), transform(results.next()).getSequenceNumber());
              assertEquals(Long.valueOf(11), transform(results.next()).getSequenceNumber());
              assertEquals(Long.valueOf(12), transform(results.next()).getSequenceNumber());
              assertEquals(Long.valueOf(13), transform(results.next()).getSequenceNumber());
              assertEquals(Long.valueOf(14), transform(results.next()).getSequenceNumber());
              assertFalse(results.hasNext());
            });
  }

  /**
   * Transforms a {@link FissClaimChange} to a {@link RdaFissClaim}.
   *
   * @param change the change to transform
   * @return the resulting RDA Fiss claim
   */
  private RdaFissClaim transform(FissClaimChange change) {
    return transformer.transformClaim(change).getClaim();
  }
}
