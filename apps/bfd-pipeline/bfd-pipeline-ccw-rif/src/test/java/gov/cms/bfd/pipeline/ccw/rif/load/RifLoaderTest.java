package gov.cms.bfd.pipeline.ccw.rif.load;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import gov.cms.bfd.model.rif.Beneficiary;
import gov.cms.bfd.pipeline.sharedutils.IdHasher;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Unit tests for {@link RifLoader}. */
public final class RifLoaderTest {
  private static final Logger LOGGER = LoggerFactory.getLogger(RifLoaderTest.class);

  /**
   * Runs a couple of fake HICNs through {@link RifLoader#computeHicnHash} to verify that the
   * expected result is produced.
   */
  @Test
  public void computeHicnHash() {
    LoadAppOptions options = CcwRifLoadTestUtils.getLoadOptions();
    options =
        new LoadAppOptions(
            new IdHasher.Config(1000, "nottherealpepper".getBytes(StandardCharsets.UTF_8)),
            options.isIdempotencyRequired(),
            false,
            new LoadAppOptions.PerformanceSettings(
                options.getBeneficiaryPerformanceSettings().getLoaderThreads(), 100, 3),
            new LoadAppOptions.PerformanceSettings(
                options.getBeneficiaryPerformanceSettings().getLoaderThreads(), 100, 3));
    LOGGER.info("salt/pepper: {}", Arrays.toString(options.getIdHasherConfig().getHashPepper()));
    LOGGER.info("hash iterations: {}", options.getIdHasherConfig().getHashIterations());
    IdHasher hasher = new IdHasher(options.getIdHasherConfig());

    /*
     * These are the two samples from `dev/design-decisions-readme.md` that
     * the frontend and backend both have tests to verify the result of.
     */
    assertEquals(
        "d95a418b0942c7910fb1d0e84f900fe12e5a7fd74f312fa10730cc0fda230e9a",
        RifLoader.computeHicnHash(hasher, "123456789A"));
    assertEquals(
        "6357f16ebd305103cf9f2864c56435ad0de5e50f73631159772f4a4fcdfe39a5",
        RifLoader.computeHicnHash(hasher, "987654321E"));
  }

  /**
   * Runs a couple of fake MBIs through {@link RifLoader#computeMbiHash} to verify that the expected
   * result is produced.
   */
  @Test
  public void computeMbiHash() {
    LoadAppOptions options = CcwRifLoadTestUtils.getLoadOptions();
    options =
        new LoadAppOptions(
            new IdHasher.Config(1000, "nottherealpepper".getBytes(StandardCharsets.UTF_8)),
            options.isIdempotencyRequired(),
            false,
            new LoadAppOptions.PerformanceSettings(
                options.getBeneficiaryPerformanceSettings().getLoaderThreads(), 100, 3),
            new LoadAppOptions.PerformanceSettings(
                options.getBeneficiaryPerformanceSettings().getLoaderThreads(), 100, 3));
    LOGGER.info("salt/pepper: {}", Arrays.toString(options.getIdHasherConfig().getHashPepper()));
    LOGGER.info("hash iterations: {}", options.getIdHasherConfig().getHashIterations());

    IdHasher hasher = new IdHasher(options.getIdHasherConfig());
    /*
     * These are the two samples from `dev/design-decisions-readme.md` that
     * the frontend and backend both have tests to verify the result of.
     */
    assertEquals(
        "ec49dc08f8dd8b4e189f623ab666cfc8b81f201cc94fe6aef860a4c3bd57f278",
        RifLoader.computeMbiHash(hasher, "3456789"));
    assertEquals(
        "742086db6bf338dedda6175ea3af8ca5e85b81fda9cc7078004a4d3e4792494b",
        RifLoader.computeMbiHash(hasher, "2456689"));
  }

  /**
   * Runs a couple of fake HICNs through {@link RifLoader#computeHicnHash} to verify that the
   * expected result is produced.
   */
  @Test
  public void isBeneficiaryHistoryEqual() {
    Beneficiary newBene = new Beneficiary();
    LocalDate birthDate = LocalDate.of(1960, 1, 8);
    String hicn = "2332j3l2";
    Optional<String> hicnUnhased = Optional.of("323232");
    char sex = 'M';
    Optional<String> medicareBeneficiaryId = Optional.of("beneficiaryId");
    Optional<String> mbiHash = Optional.of("mbiHash");
    Optional<LocalDate> mbiEffectiveDate = Optional.of(LocalDate.of(2020, 1, 1));
    Optional<LocalDate> mbiObsoleteDate = Optional.of(LocalDate.of(2020, 1, 8));

    newBene.setBirthDate(birthDate);
    newBene.setHicn(hicn);
    newBene.setHicnUnhashed(hicnUnhased);
    newBene.setSex(sex);
    newBene.setMedicareBeneficiaryId(medicareBeneficiaryId);
    newBene.setMbiHash(mbiHash);
    newBene.setMbiEffectiveDate(mbiEffectiveDate);
    newBene.setMbiObsoleteDate(mbiObsoleteDate);

    Beneficiary oldBene = new Beneficiary();
    oldBene.setBirthDate(birthDate);
    oldBene.setHicn(hicn);
    oldBene.setHicnUnhashed(hicnUnhased);
    oldBene.setSex(sex);
    oldBene.setMedicareBeneficiaryId(medicareBeneficiaryId);
    oldBene.setMbiHash(mbiHash);
    oldBene.setMbiEffectiveDate(mbiEffectiveDate);
    oldBene.setMbiObsoleteDate(mbiObsoleteDate);

    // Both old and new beneficiary have the same values return true
    assertTrue(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // New beneficiary birth date is not the same as old should assert false
    newBene.setBirthDate(LocalDate.of(1950, 1, 8));
    assertFalse(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // Undo New beneficiary birth date and set it back to old should assert true
    newBene.setBirthDate(birthDate);
    assertTrue(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // New beneficiary hicn is not the same as old should assert false
    newBene.setHicn("difHicn");
    assertFalse(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // Undo New beneficiary hicn and set it back to old should assert true
    newBene.setHicn(hicn);
    assertTrue(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // New beneficiary hicnunHashed is not the same as old should assert false
    newBene.setHicnUnhashed(Optional.of("difHicn"));
    assertFalse(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // Undo New beneficiary hicnUnhased and set it back to old should assert true
    newBene.setHicnUnhashed(hicnUnhased);
    assertTrue(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // New beneficiary sex is not the same as old should assert false
    newBene.setSex('F');
    assertFalse(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // Undo New beneficiary sex and set it back to old should assert true
    newBene.setSex(sex);
    assertTrue(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // New beneficiary mediciarybeneficiaryid is not the same as old should assert false
    newBene.setMedicareBeneficiaryId(Optional.of("diff"));
    assertFalse(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // Undo New beneficiary mediciarybeneficiaryid and set it back to old should assert true
    newBene.setMedicareBeneficiaryId(medicareBeneficiaryId);
    assertTrue(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // New beneficiary mbihash is not the same as old should assert false
    newBene.setMbiHash(Optional.of("mbihashdiff"));
    assertFalse(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // Undo New beneficiary mbihash and set it back to old should assert true
    newBene.setMbiHash(mbiHash);
    assertTrue(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // New beneficiary mbiEffectiveDate is not the same as old should assert false
    newBene.setMbiEffectiveDate(Optional.of(LocalDate.of(2020, 1, 8)));
    assertFalse(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // Undo New beneficiary mbiEffectiveDate and set it back to old should assert true
    newBene.setMbiEffectiveDate(mbiEffectiveDate);
    assertTrue(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // New beneficiary mbiObsoleteDate is not the same as old should assert true
    // becasue mbiObsoleteDate is no longer part of the equality check.
    newBene.setMbiObsoleteDate(Optional.of(LocalDate.of(2020, 1, 2)));
    assertTrue(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // Undo New beneficiary mbiObsoleteDate and set it back to old should assert true
    newBene.setMbiObsoleteDate(mbiObsoleteDate);
    assertTrue(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // Check for nulls on fields
    // New beneficiary hicnUnhashed is null and the return result should assert false
    newBene.setHicnUnhashed(Optional.empty());
    assertFalse(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // Undo New beneficiary hicnUnhashed and set it back to old should assert true
    newBene.setHicnUnhashed(hicnUnhased);
    assertTrue(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // New beneficiary sex is null and the return result should assert false
    newBene.setSex(Character.MIN_VALUE);
    assertFalse(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // Undo New beneficiary sex and set it back to old should assert true
    newBene.setSex(sex);
    assertTrue(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // New beneficiary mediciarybeneficiaryid is null and the return result should assert false
    newBene.setMedicareBeneficiaryId(Optional.empty());
    assertFalse(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // Undo beneficiary mediciarybeneficiaryid and set it back to old should assert true
    newBene.setMedicareBeneficiaryId(medicareBeneficiaryId);
    assertTrue(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // New beneficiary mbiEffectiveDate is null and the return result should assert false
    newBene.setMbiEffectiveDate(Optional.empty());
    assertFalse(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // old beneficiary mbiEffectiveDate was empty and new beneficiary has mbiEffectiveDate
    newBene.setMbiEffectiveDate(mbiEffectiveDate);
    oldBene.setMbiEffectiveDate(Optional.empty());
    assertFalse(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));

    // New beneficiary mbiObsoleteDate is null and the return result should assert true
    // since the test will ignore empty setMbiObsoleteDate.
    oldBene.setMbiEffectiveDate(mbiEffectiveDate);
    newBene.setMbiObsoleteDate(Optional.empty());
    assertTrue(RifLoader.isBeneficiaryHistoryEqual(newBene, oldBene));
  }
}
