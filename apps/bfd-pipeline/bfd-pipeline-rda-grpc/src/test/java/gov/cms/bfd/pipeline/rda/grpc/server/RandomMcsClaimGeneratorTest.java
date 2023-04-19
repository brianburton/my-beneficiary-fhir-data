package gov.cms.bfd.pipeline.rda.grpc.server;

import static gov.cms.bfd.pipeline.rda.grpc.server.AbstractRandomClaimGeneratorTest.countDistinctFieldValues;
import static gov.cms.bfd.pipeline.rda.grpc.server.AbstractRandomClaimGeneratorTest.maxFieldLength;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import gov.cms.mpsm.rda.v1.mcs.McsClaim;
import gov.cms.mpsm.rda.v1.mcs.McsDiagnosisCode;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/** Tests the {@link RandomMcsClaimGenerator}. */
public class RandomMcsClaimGeneratorTest {
  /**
   * Verifies when a single MCS claim is generated using a seed, it's fields match the expected
   * values.
   *
   * @throws InvalidProtocolBufferException indicates test failure (failure to parse to json for
   *     comparison)
   */
  @Test
  public void randomClaim() throws InvalidProtocolBufferException {
    final Clock july1 = Clock.fixed(Instant.ofEpochMilli(1625172944844L), ZoneOffset.UTC);
    final RandomMcsClaimGenerator generator =
        new RandomMcsClaimGenerator(
            RandomClaimGeneratorConfig.builder()
                .seed(1)
                .optionalOverride(true)
                .clock(july1)
                .build());
    final McsClaim claim = generator.randomClaim();
    final String json = JsonFormat.printer().print(claim);
    assertEquals(claim.getIdrDtlCnt(), claim.getMcsDetailsList().size());
    for (McsDiagnosisCode diagnosisCode : claim.getMcsDiagnosisCodesList()) {
      assertEquals(claim.getIdrClmHdIcn(), diagnosisCode.getIdrClmHdIcn());
    }
    assertEquals(
        "{\n"
            + "  \"idrClmHdIcn\": \"212941673334303\",\n"
            + "  \"idrContrId\": \"2\",\n"
            + "  \"idrHic\": \"922470\",\n"
            + "  \"idrDtlCnt\": 3,\n"
            + "  \"idrBeneLast16\": \"xm\",\n"
            + "  \"idrBeneFirstInit\": \"g\",\n"
            + "  \"idrBeneMidInit\": \"c\",\n"
            + "  \"idrStatusCodeEnum\": \"STATUS_CODE_APPROVED_AND_PAID_D\",\n"
            + "  \"idrStatusDate\": \"2021-06-16\",\n"
            + "  \"idrBillProvNpi\": \"d6fdchd69\",\n"
            + "  \"idrBillProvNum\": \"6285\",\n"
            + "  \"idrBillProvEin\": \"zvzf\",\n"
            + "  \"idrBillProvType\": \"rx\",\n"
            + "  \"idrBillProvSpec\": \"q7\",\n"
            + "  \"idrBillProvPriceSpec\": \"0\",\n"
            + "  \"idrBillProvCounty\": \"n\",\n"
            + "  \"idrBillProvLoc\": \"39\",\n"
            + "  \"idrTotAllowed\": \"326.33\",\n"
            + "  \"idrCoinsurance\": \"1709.09\",\n"
            + "  \"idrDeductible\": \"72395.23\",\n"
            + "  \"idrTotBilledAmt\": \"65.06\",\n"
            + "  \"idrClaimReceiptDate\": \"2021-03-31\",\n"
            + "  \"idrClaimMbi\": \"978t9bxjj24\",\n"
            + "  \"mcsDiagnosisCodes\": [{\n"
            + "    \"idrClmHdIcn\": \"212941673334303\",\n"
            + "    \"idrDiagIcdTypeEnum\": \"DIAGNOSIS_ICD_TYPE_ICD10\",\n"
            + "    \"idrDiagCode\": \"v4j\",\n"
            + "    \"rdaPosition\": 1\n"
            + "  }, {\n"
            + "    \"idrClmHdIcn\": \"212941673334303\",\n"
            + "    \"idrDiagIcdTypeEnum\": \"DIAGNOSIS_ICD_TYPE_ICD9\",\n"
            + "    \"idrDiagCode\": \"bsr\",\n"
            + "    \"rdaPosition\": 2\n"
            + "  }, {\n"
            + "    \"idrClmHdIcn\": \"212941673334303\",\n"
            + "    \"idrDiagCode\": \"kn\",\n"
            + "    \"idrDiagIcdTypeUnrecognized\": \"r\",\n"
            + "    \"rdaPosition\": 3\n"
            + "  }, {\n"
            + "    \"idrClmHdIcn\": \"212941673334303\",\n"
            + "    \"idrDiagCode\": \"1p34\",\n"
            + "    \"idrDiagIcdTypeUnrecognized\": \"j\",\n"
            + "    \"rdaPosition\": 4\n"
            + "  }, {\n"
            + "    \"idrClmHdIcn\": \"212941673334303\",\n"
            + "    \"idrDiagIcdTypeEnum\": \"DIAGNOSIS_ICD_TYPE_ICD10\",\n"
            + "    \"idrDiagCode\": \"jb0m2\",\n"
            + "    \"rdaPosition\": 5\n"
            + "  }, {\n"
            + "    \"idrClmHdIcn\": \"212941673334303\",\n"
            + "    \"idrDiagCode\": \"k\",\n"
            + "    \"idrDiagIcdTypeUnrecognized\": \"s\",\n"
            + "    \"rdaPosition\": 6\n"
            + "  }],\n"
            + "  \"mcsDetails\": [{\n"
            + "    \"idrDtlFromDate\": \"2021-04-22\",\n"
            + "    \"idrDtlToDate\": \"2021-04-22\",\n"
            + "    \"idrProcCode\": \"q2\",\n"
            + "    \"idrModOne\": \"dt\",\n"
            + "    \"idrModTwo\": \"xq\",\n"
            + "    \"idrModThree\": \"t\",\n"
            + "    \"idrModFour\": \"tm\",\n"
            + "    \"idrDtlPrimaryDiagCode\": \"8x4k854\",\n"
            + "    \"idrKPosLnameOrg\": \"jgnvgswfffxcpqqqhjwfhftprhxnwtmvcdkkzz\",\n"
            + "    \"idrKPosFname\": \"svsctnnkjjqnqr\",\n"
            + "    \"idrKPosMname\": \"fvfrbj\",\n"
            + "    \"idrKPosAddr1\": \"48sscv0475m4pbj5x2p0k1qbtnnb\",\n"
            + "    \"idrKPosAddr21st\": \"22hdc64zvgh7qx988jm3zsrc1mh\",\n"
            + "    \"idrKPosAddr22nd\": \"w44zwdj030xphz87p7p7\",\n"
            + "    \"idrKPosCity\": \"qncmshzqgcdgfwngzwtvvwtdnjrwrr\",\n"
            + "    \"idrKPosState\": \"td\",\n"
            + "    \"idrKPosZip\": \"31974158965575\",\n"
            + "    \"idrDtlDiagIcdTypeUnrecognized\": \"p\",\n"
            + "    \"idrDtlStatusUnrecognized\": \"w\",\n"
            + "    \"idrTosEnum\": \"TYPE_OF_SERVICE_SECOND_OPINION_ON_ELECTIVE_SURGERY\",\n"
            + "    \"idrTwoDigitPosEnum\": \"TWO_DIGIT_PLAN_OF_SERVICE_INDEPENDENT_CLINIC\",\n"
            + "    \"idrDtlRendType\": \"9\",\n"
            + "    \"idrDtlRendSpec\": \"4\",\n"
            + "    \"idrDtlRendNpi\": \"0dq9g\",\n"
            + "    \"idrDtlRendProv\": \"8ftzksvsh\",\n"
            + "    \"idrKDtlFacProvNpi\": \"k9nh13zws\",\n"
            + "    \"idrDtlAmbPickupAddres1\": \"0vp9qf9\",\n"
            + "    \"idrDtlAmbPickupAddres2\": \"w5x\",\n"
            + "    \"idrDtlAmbPickupCity\": \"vtf0px\",\n"
            + "    \"idrDtlAmbPickupState\": \"c\",\n"
            + "    \"idrDtlAmbPickupZipcode\": \"w\",\n"
            + "    \"idrDtlAmbDropoffName\": \"hdbpsrhg8s1frw35dg1mp47\",\n"
            + "    \"idrDtlAmbDropoffAddrL1\": \"gfzxjm5pq6dmw8bt\",\n"
            + "    \"idrDtlAmbDropoffAddrL2\": \"3q\",\n"
            + "    \"idrDtlAmbDropoffCity\": \"874r8sm4m2ppm42\",\n"
            + "    \"idrDtlAmbDropoffState\": \"8\",\n"
            + "    \"idrDtlAmbDropoffZipcode\": \"1h5qrgv\",\n"
            + "    \"idrDtlNumber\": 1\n"
            + "  }, {\n"
            + "    \"idrDtlStatusEnum\": \"DETAIL_STATUS_PENDING\",\n"
            + "    \"idrDtlFromDate\": \"2021-01-12\",\n"
            + "    \"idrDtlToDate\": \"2021-01-12\",\n"
            + "    \"idrProcCode\": \"2t7\",\n"
            + "    \"idrModOne\": \"s6\",\n"
            + "    \"idrModTwo\": \"mt\",\n"
            + "    \"idrModThree\": \"hm\",\n"
            + "    \"idrModFour\": \"6\",\n"
            + "    \"idrDtlPrimaryDiagCode\": \"bd3jz\",\n"
            + "    \"idrKPosLnameOrg\": \"cgkpbdpwcschkgfdjnjshsqkhdffrzbkbhtkmjqcbcfcgz\",\n"
            + "    \"idrKPosFname\": \"jddsbtsbkqnwvzcgnvjdp\",\n"
            + "    \"idrKPosMname\": \"ctxghttkhdpngbghfpnb\",\n"
            + "    \"idrKPosAddr1\": \"5g2v7qv6qn3770rh6xpr\",\n"
            + "    \"idrKPosAddr21st\": \"mggmhsdxm19213cfs\",\n"
            + "    \"idrKPosAddr22nd\": \"5qt65kbghfg3kwqqczs6vvhkz\",\n"
            + "    \"idrKPosCity\": \"wwbwdmcpgbxkmjxdqvctwqmz\",\n"
            + "    \"idrKPosState\": \"gv\",\n"
            + "    \"idrKPosZip\": \"768036076\",\n"
            + "    \"idrDtlDiagIcdTypeUnrecognized\": \"c\",\n"
            + "    \"idrTosUnrecognized\": \"n\",\n"
            + "    \"idrTwoDigitPosUnrecognized\": \"c0\",\n"
            + "    \"idrDtlRendType\": \"w\",\n"
            + "    \"idrDtlRendSpec\": \"t\",\n"
            + "    \"idrDtlRendNpi\": \"0850gpz2qw\",\n"
            + "    \"idrDtlRendProv\": \"nn46pf7hw\",\n"
            + "    \"idrKDtlFacProvNpi\": \"f7\",\n"
            + "    \"idrDtlAmbPickupAddres1\": \"8x48c935s\",\n"
            + "    \"idrDtlAmbPickupAddres2\": \"x58hbk9snz32jh8xz\",\n"
            + "    \"idrDtlAmbPickupCity\": \"32mhgt3ww4wvmgz9m4\",\n"
            + "    \"idrDtlAmbPickupState\": \"c\",\n"
            + "    \"idrDtlAmbPickupZipcode\": \"rcjbw9\",\n"
            + "    \"idrDtlAmbDropoffName\": \"hq307m0jbtpbn0vc5b\",\n"
            + "    \"idrDtlAmbDropoffAddrL1\": \"zgv4fvmzz7q\",\n"
            + "    \"idrDtlAmbDropoffAddrL2\": \"np136vsmv7g51z\",\n"
            + "    \"idrDtlAmbDropoffCity\": \"bw37q1\",\n"
            + "    \"idrDtlAmbDropoffState\": \"1v\",\n"
            + "    \"idrDtlAmbDropoffZipcode\": \"z\",\n"
            + "    \"idrDtlNumber\": 2\n"
            + "  }, {\n"
            + "    \"idrDtlFromDate\": \"2021-02-02\",\n"
            + "    \"idrDtlToDate\": \"2021-02-02\",\n"
            + "    \"idrProcCode\": \"6w\",\n"
            + "    \"idrModOne\": \"t\",\n"
            + "    \"idrModTwo\": \"7\",\n"
            + "    \"idrModThree\": \"pg\",\n"
            + "    \"idrModFour\": \"6\",\n"
            + "    \"idrDtlPrimaryDiagCode\": \"sjjct\",\n"
            + "    \"idrKPosLnameOrg\": \"bvmspqfpqzqmzkvwhcfzcmwrqzdsqnpbjpfctzfnbdhtttrpdgmbxxmrjq\",\n"
            + "    \"idrKPosFname\": \"mjvmfthnrnqqnhzndxvhgkccphzsbt\",\n"
            + "    \"idrKPosMname\": \"qxjmbth\",\n"
            + "    \"idrKPosAddr1\": \"vx342s\",\n"
            + "    \"idrKPosAddr21st\": \"wzp0t7gd13\",\n"
            + "    \"idrKPosAddr22nd\": \"89nc2sp6bqq83f2bb2\",\n"
            + "    \"idrKPosCity\": \"dqfbrjzvkgjzmc\",\n"
            + "    \"idrKPosState\": \"k\",\n"
            + "    \"idrKPosZip\": \"87915092362\",\n"
            + "    \"idrDtlDiagIcdTypeUnrecognized\": \"t\",\n"
            + "    \"idrDtlStatusUnrecognized\": \"z\",\n"
            + "    \"idrTosUnrecognized\": \"t\",\n"
            + "    \"idrTwoDigitPosUnrecognized\": \"n0\",\n"
            + "    \"idrDtlRendType\": \"w\",\n"
            + "    \"idrDtlRendSpec\": \"n\",\n"
            + "    \"idrDtlRendNpi\": \"pvf7q\",\n"
            + "    \"idrDtlRendProv\": \"0\",\n"
            + "    \"idrKDtlFacProvNpi\": \"xsf4\",\n"
            + "    \"idrDtlAmbPickupAddres1\": \"544gdh59g\",\n"
            + "    \"idrDtlAmbPickupAddres2\": \"qb3zd1mgj\",\n"
            + "    \"idrDtlAmbPickupCity\": \"z\",\n"
            + "    \"idrDtlAmbPickupState\": \"c8\",\n"
            + "    \"idrDtlAmbPickupZipcode\": \"6w\",\n"
            + "    \"idrDtlAmbDropoffName\": \"bwph7wkksmfhg814s9x\",\n"
            + "    \"idrDtlAmbDropoffAddrL1\": \"2j9f9p90pq41118fz97cqh\",\n"
            + "    \"idrDtlAmbDropoffAddrL2\": \"ms8xjxrhq94xbb00nb\",\n"
            + "    \"idrDtlAmbDropoffCity\": \"mzf5phf\",\n"
            + "    \"idrDtlAmbDropoffState\": \"5\",\n"
            + "    \"idrDtlAmbDropoffZipcode\": \"9dm\",\n"
            + "    \"idrDtlNumber\": 3\n"
            + "  }],\n"
            + "  \"idrClaimTypeUnrecognized\": \"c\",\n"
            + "  \"idrBeneSexUnrecognized\": \"j\",\n"
            + "  \"idrBillProvGroupIndUnrecognized\": \"b\",\n"
            + "  \"idrBillProvStatusCdUnrecognized\": \"v\",\n"
            + "  \"idrHdrFromDos\": \"2021-01-12\",\n"
            + "  \"idrHdrToDos\": \"2021-04-22\",\n"
            + "  \"idrAssignmentEnum\": \"CLAIM_ASSIGNMENT_NON_ASSIGNED_LAB_SERVICES\",\n"
            + "  \"idrClmLevelIndEnum\": \"CLAIM_LEVEL_INDICATOR_VOID\",\n"
            + "  \"idrHdrAudit\": 9360,\n"
            + "  \"idrHdrAuditIndEnum\": \"AUDIT_INDICATOR_AUDIT_NUMBER\",\n"
            + "  \"idrUSplitReasonEnum\": \"SPLIT_REASON_CODE_2ND_OPINION\",\n"
            + "  \"idrJReferringProvNpi\": \"jhjfvs9\",\n"
            + "  \"idrJFacProvNpi\": \"f\",\n"
            + "  \"idrUDemoProvNpi\": \"j7\",\n"
            + "  \"idrUSuperNpi\": \"pbdckqr7\",\n"
            + "  \"idrUFcadjBilNpi\": \"cw0pskjw\",\n"
            + "  \"idrAmbPickupAddresLine1\": \"jtvnhjmznt60hzcz5w7t\",\n"
            + "  \"idrAmbPickupAddresLine2\": \"c4tdhr36pp\",\n"
            + "  \"idrAmbPickupCity\": \"4p\",\n"
            + "  \"idrAmbPickupState\": \"50\",\n"
            + "  \"idrAmbPickupZipcode\": \"cpmb3\",\n"
            + "  \"idrAmbDropoffName\": \"7dghwmqnx3\",\n"
            + "  \"idrAmbDropoffAddrLine1\": \"njxn8639h559w\",\n"
            + "  \"idrAmbDropoffAddrLine2\": \"ht6d8d87j\",\n"
            + "  \"idrAmbDropoffCity\": \"nnnpqb892nks\",\n"
            + "  \"idrAmbDropoffState\": \"g6\",\n"
            + "  \"idrAmbDropoffZipcode\": \"bsrrw\",\n"
            + "  \"mcsAudits\": [{\n"
            + "    \"idrJAuditNum\": 30026,\n"
            + "    \"idrJAuditIndUnrecognized\": \"9\",\n"
            + "    \"idrJAuditDispUnrecognized\": \"4\",\n"
            + "    \"rdaPosition\": 1\n"
            + "  }, {\n"
            + "    \"idrJAuditNum\": 16367,\n"
            + "    \"idrJAuditIndUnrecognized\": \"n\",\n"
            + "    \"idrJAuditDispEnum\": \"CUTBACK_AUDIT_DISPOSITION_DENY\",\n"
            + "    \"rdaPosition\": 2\n"
            + "  }, {\n"
            + "    \"idrJAuditNum\": 30113,\n"
            + "    \"idrJAuditIndUnrecognized\": \"7\",\n"
            + "    \"idrJAuditDispUnrecognized\": \"4\",\n"
            + "    \"rdaPosition\": 3\n"
            + "  }, {\n"
            + "    \"idrJAuditNum\": 15286,\n"
            + "    \"idrJAuditIndUnrecognized\": \"s\",\n"
            + "    \"idrJAuditDispEnum\": \"CUTBACK_AUDIT_DISPOSITION_AUDIT_OVERRIDDEN\",\n"
            + "    \"rdaPosition\": 4\n"
            + "  }, {\n"
            + "    \"idrJAuditNum\": 20021,\n"
            + "    \"idrJAuditIndUnrecognized\": \"p\",\n"
            + "    \"idrJAuditDispEnum\": \"CUTBACK_AUDIT_DISPOSITION_EOMB_MESSAGE_ONLY\",\n"
            + "    \"rdaPosition\": 5\n"
            + "  }, {\n"
            + "    \"idrJAuditNum\": 14325,\n"
            + "    \"idrJAuditIndUnrecognized\": \"j\",\n"
            + "    \"idrJAuditDispUnrecognized\": \"z\",\n"
            + "    \"rdaPosition\": 6\n"
            + "  }],\n"
            + "  \"mcsLocations\": [{\n"
            + "    \"idrLocClerk\": \"gh\",\n"
            + "    \"idrLocCode\": \"8\",\n"
            + "    \"idrLocDate\": \"2021-01-28\",\n"
            + "    \"idrLocActvCodeEnum\": \"LOCATION_ACTIVITY_CODE_FINANCIAL_RESPONSE_ACTIVITY\",\n"
            + "    \"rdaPosition\": 1\n"
            + "  }, {\n"
            + "    \"idrLocClerk\": \"m\",\n"
            + "    \"idrLocCode\": \"b4r\",\n"
            + "    \"idrLocDate\": \"2021-01-18\",\n"
            + "    \"idrLocActvCodeEnum\": \"LOCATION_ACTIVITY_CODE_MAINTENANCE_APPLIED_FROM_WORKSHEET_F\",\n"
            + "    \"rdaPosition\": 2\n"
            + "  }, {\n"
            + "    \"idrLocClerk\": \"378k\",\n"
            + "    \"idrLocCode\": \"k74\",\n"
            + "    \"idrLocDate\": \"2021-03-30\",\n"
            + "    \"idrLocActvCodeUnrecognized\": \"0\",\n"
            + "    \"rdaPosition\": 3\n"
            + "  }, {\n"
            + "    \"idrLocClerk\": \"t\",\n"
            + "    \"idrLocCode\": \"4\",\n"
            + "    \"idrLocDate\": \"2021-06-21\",\n"
            + "    \"idrLocActvCodeEnum\": \"LOCATION_ACTIVITY_CODE_ACTIVATION_ACTIVITY\",\n"
            + "    \"rdaPosition\": 4\n"
            + "  }],\n"
            + "  \"mcsAdjustments\": [{\n"
            + "    \"idrAdjDate\": \"2021-03-11\",\n"
            + "    \"idrXrefIcn\": \"xmxj\",\n"
            + "    \"idrAdjClerk\": \"320\",\n"
            + "    \"idrInitCcn\": \"t96h\",\n"
            + "    \"idrAdjChkWrtDt\": \"2021-06-16\",\n"
            + "    \"idrAdjBEombAmt\": \"10.13\",\n"
            + "    \"idrAdjPEombAmt\": \"194.27\",\n"
            + "    \"rdaPosition\": 1\n"
            + "  }, {\n"
            + "    \"idrAdjDate\": \"2021-01-21\",\n"
            + "    \"idrXrefIcn\": \"z270m\",\n"
            + "    \"idrAdjClerk\": \"qb\",\n"
            + "    \"idrInitCcn\": \"f0g03sj\",\n"
            + "    \"idrAdjChkWrtDt\": \"2021-04-21\",\n"
            + "    \"idrAdjBEombAmt\": \"2185.24\",\n"
            + "    \"idrAdjPEombAmt\": \"48855.78\",\n"
            + "    \"rdaPosition\": 2\n"
            + "  }, {\n"
            + "    \"idrAdjDate\": \"2021-06-28\",\n"
            + "    \"idrXrefIcn\": \"kq4rbc3\",\n"
            + "    \"idrAdjClerk\": \"dz6\",\n"
            + "    \"idrInitCcn\": \"sq2kh\",\n"
            + "    \"idrAdjChkWrtDt\": \"2021-06-02\",\n"
            + "    \"idrAdjBEombAmt\": \"16.13\",\n"
            + "    \"idrAdjPEombAmt\": \"8943.66\",\n"
            + "    \"rdaPosition\": 3\n"
            + "  }, {\n"
            + "    \"idrAdjDate\": \"2021-03-28\",\n"
            + "    \"idrXrefIcn\": \"qt3z\",\n"
            + "    \"idrAdjClerk\": \"k5\",\n"
            + "    \"idrInitCcn\": \"zs6q6q8g158\",\n"
            + "    \"idrAdjChkWrtDt\": \"2021-01-17\",\n"
            + "    \"idrAdjBEombAmt\": \"713.08\",\n"
            + "    \"idrAdjPEombAmt\": \"7.06\",\n"
            + "    \"rdaPosition\": 4\n"
            + "  }, {\n"
            + "    \"idrAdjDate\": \"2021-01-03\",\n"
            + "    \"idrXrefIcn\": \"15ctbq7rb9fwhzt\",\n"
            + "    \"idrAdjClerk\": \"z\",\n"
            + "    \"idrInitCcn\": \"k5mvp3k\",\n"
            + "    \"idrAdjChkWrtDt\": \"2021-04-09\",\n"
            + "    \"idrAdjBEombAmt\": \"74.75\",\n"
            + "    \"idrAdjPEombAmt\": \"4985.31\",\n"
            + "    \"rdaPosition\": 5\n"
            + "  }, {\n"
            + "    \"idrAdjDate\": \"2021-02-05\",\n"
            + "    \"idrXrefIcn\": \"jvn64s5r4qb6\",\n"
            + "    \"idrAdjClerk\": \"r995\",\n"
            + "    \"idrInitCcn\": \"jkrd69\",\n"
            + "    \"idrAdjChkWrtDt\": \"2021-04-17\",\n"
            + "    \"idrAdjBEombAmt\": \"913.50\",\n"
            + "    \"idrAdjPEombAmt\": \"8.87\",\n"
            + "    \"rdaPosition\": 6\n"
            + "  }, {\n"
            + "    \"idrAdjDate\": \"2021-01-25\",\n"
            + "    \"idrXrefIcn\": \"9bq\",\n"
            + "    \"idrAdjClerk\": \"hk\",\n"
            + "    \"idrInitCcn\": \"6180\",\n"
            + "    \"idrAdjChkWrtDt\": \"2021-01-17\",\n"
            + "    \"idrAdjBEombAmt\": \"67.97\",\n"
            + "    \"idrAdjPEombAmt\": \"73.67\",\n"
            + "    \"rdaPosition\": 7\n"
            + "  }, {\n"
            + "    \"idrAdjDate\": \"2021-04-23\",\n"
            + "    \"idrXrefIcn\": \"s\",\n"
            + "    \"idrAdjClerk\": \"pj4j\",\n"
            + "    \"idrInitCcn\": \"wc\",\n"
            + "    \"idrAdjChkWrtDt\": \"2021-05-18\",\n"
            + "    \"idrAdjBEombAmt\": \"30262.95\",\n"
            + "    \"idrAdjPEombAmt\": \"16464.26\",\n"
            + "    \"rdaPosition\": 8\n"
            + "  }, {\n"
            + "    \"idrAdjDate\": \"2021-05-18\",\n"
            + "    \"idrXrefIcn\": \"5rnjvb7r8tqjnm2\",\n"
            + "    \"idrAdjClerk\": \"3b\",\n"
            + "    \"idrInitCcn\": \"f5w98zrjhcjsbw\",\n"
            + "    \"idrAdjChkWrtDt\": \"2021-03-05\",\n"
            + "    \"idrAdjBEombAmt\": \"20.31\",\n"
            + "    \"idrAdjPEombAmt\": \"5676.34\",\n"
            + "    \"rdaPosition\": 9\n"
            + "  }]\n"
            + "}",
        json);
  }

  /** Verifies that the overrides in the {@link RandomClaimGeneratorConfig} are enforced. */
  @Test
  public void testFieldOverrides() {
    final int maxClaimIds = 18;
    final int maxMbis = 14;
    final var normalConfig = RandomClaimGeneratorConfig.builder().seed(1).build();
    final var normalGenerator = new RandomMcsClaimGenerator(normalConfig);
    final var normalClaims =
        IntStream.range(1, 100)
            .mapToObj(i -> normalGenerator.randomClaim())
            .collect(Collectors.toList());

    // We expect to normally get many more unique values than the overrides will use.
    assertThat(
        countDistinctFieldValues(normalClaims, McsClaim::getIdrClaimMbi),
        Matchers.greaterThan((long) maxClaimIds));
    assertThat(
        countDistinctFieldValues(normalClaims, McsClaim::getIdrClmHdIcn),
        Matchers.greaterThan((long) maxMbis));

    // We expect these ids to normally fall within their normal field length.
    assertThat(
        maxFieldLength(normalClaims, McsClaim::getIdrContrId),
        Matchers.lessThan(RandomMcsClaimGenerator.ForcedErrorFieldLength));
    assertThat(
        maxFieldLength(normalClaims, McsClaim::getIdrHic),
        Matchers.lessThan(RandomMcsClaimGenerator.ForcedErrorFieldLength));

    final var overrideConfig =
        normalConfig.toBuilder()
            .maxUniqueClaimIds(maxClaimIds)
            .maxUniqueMbis(maxMbis)
            .randomErrorRate(10)
            .build();
    final var overrideGenerator = new RandomMcsClaimGenerator(overrideConfig);
    final var overrideClaims =
        IntStream.range(1, 100)
            .mapToObj(i -> overrideGenerator.randomClaim())
            .collect(Collectors.toList());

    // We expect to get exactly the specified number of unique ids.
    assertEquals(maxClaimIds, countDistinctFieldValues(overrideClaims, McsClaim::getIdrClmHdIcn));
    assertEquals(maxMbis, countDistinctFieldValues(overrideClaims, McsClaim::getIdrClaimMbi));

    // We expect these ids to sometimes have a value with the forced error length when we are
    // forcing errors.
    assertEquals(
        RandomMcsClaimGenerator.ForcedErrorFieldLength,
        maxFieldLength(overrideClaims, McsClaim::getIdrContrId));
    assertEquals(
        RandomMcsClaimGenerator.ForcedErrorFieldLength,
        maxFieldLength(overrideClaims, McsClaim::getIdrHic));
  }
}
