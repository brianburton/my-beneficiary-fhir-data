package gov.cms.bfd.server.war.r4.providers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.TokenClientParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import gov.cms.bfd.model.codebook.data.CcwCodebookVariable;
import gov.cms.bfd.model.rif.Beneficiary;
import gov.cms.bfd.model.rif.BeneficiaryHistory;
import gov.cms.bfd.model.rif.samples.StaticRifResource;
import gov.cms.bfd.model.rif.samples.StaticRifResourceGroup;
import gov.cms.bfd.pipeline.PipelineTestUtils;
import gov.cms.bfd.server.war.ServerRequiredTest;
import gov.cms.bfd.server.war.ServerTestUtils;
import gov.cms.bfd.server.war.commons.CCWUtils;
import gov.cms.bfd.server.war.commons.RequestHeaders;
import gov.cms.bfd.server.war.commons.TransformerConstants;
import gov.cms.bfd.server.war.stu3.providers.ExtraParamsInterceptor;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Period;
import org.junit.jupiter.api.Test;

/** Integration tests for {@link R4PatientResourceProvider}. */
public final class R4PatientResourceProviderE2E extends ServerRequiredTest {

  /**
   * A list of expected historical mbis for adding to the sample A loaded data (as data coming back
   * from the endpoint will have this added in the resource provider).
   */
  private static final List<String> standardExpectedHistoricalMbis =
      List.of("9AB2WW3GR44", "543217066", "3456689");

  /**
   * Verifies that {@link R4PatientResourceProvider#read} works as expected for a {@link Patient}
   * that does exist in the DB.
   */
  @Test
  public void readExistingPatient() {
    List<Object> loadedRecords =
        ServerTestUtils.get()
            .loadData(Arrays.asList(StaticRifResourceGroup.SAMPLE_A.getResources()));
    IGenericClient fhirClient = ServerTestUtils.get().createFhirClientV2();

    Beneficiary beneficiary =
        loadedRecords.stream()
            .filter(r -> r instanceof Beneficiary)
            .map(r -> (Beneficiary) r)
            .findFirst()
            .get();

    Patient patient =
        fhirClient.read().resource(Patient.class).withId(beneficiary.getBeneficiaryId()).execute();

    comparePatient(beneficiary, patient, standardExpectedHistoricalMbis);
  }

  /**
   * Verifies that {@link R4PatientResourceProvider#read} returns a {@link Patient} when the
   * beneficiary exists in the DB but has no {@link BeneficiaryHistory} or {@link
   * MedicareBeneficiaryIdHistory} records. Primarily this checks that the table joins do not cause
   * any issue retrieving the patient when there is nothing found in the history table.
   */
  @Test
  public void readExistingPatientWithNoHistory() {
    List<Object> loadedRecords =
        ServerTestUtils.get().loadData(Arrays.asList(StaticRifResource.SAMPLE_A_BENES));
    RequestHeaders requestHeader =
        RequestHeaders.getHeaderWrapper(
            R4PatientResourceProvider.HEADER_NAME_INCLUDE_ADDRESS_FIELDS, "true");
    IGenericClient fhirClient = createFhirClient(requestHeader);

    Beneficiary beneficiary =
        loadedRecords.stream()
            .filter(r -> r instanceof Beneficiary)
            .map(r -> (Beneficiary) r)
            .findFirst()
            .get();
    Patient patient =
        fhirClient.read().resource(Patient.class).withId(beneficiary.getBeneficiaryId()).execute();

    comparePatient(beneficiary, patient, requestHeader, new ArrayList<>());

    /*
     * Ensure the unhashed value for MBI is present.
     */
    boolean mbiUnhashedPresent = false;
    for (Identifier identifier : patient.getIdentifier()) {
      if (identifier.getSystem().equals(TransformerConstants.CODING_BBAPI_BENE_ID)) {
        mbiUnhashedPresent = true;
      }
    }

    assertTrue(mbiUnhashedPresent);

    // Check only the bene id and unhashed mbi is returned (no history)
    assertEquals(2, patient.getIdentifier().size());
  }

  /**
   * Verifies that {@link R4PatientResourceProvider#searchByLogicalId} works as expected for a
   * {@link Patient} that does exist in the DB, including identifiers to return the unhashed MBI.
   */
  @Test
  public void searchForExistingPatientByLogicalId() {
    List<Object> loadedRecords =
        ServerTestUtils.get()
            .loadData(Arrays.asList(StaticRifResourceGroup.SAMPLE_A.getResources()));
    IGenericClient fhirClient = createFhirClient("true");

    Beneficiary beneficiary =
        loadedRecords.stream()
            .filter(r -> r instanceof Beneficiary)
            .map(r -> (Beneficiary) r)
            .findFirst()
            .get();
    Bundle searchResults =
        fhirClient
            .search()
            .forResource(Patient.class)
            .where(
                Patient.RES_ID
                    .exactly()
                    .systemAndIdentifier(null, String.valueOf(beneficiary.getBeneficiaryId())))
            .returnBundle(Bundle.class)
            .execute();

    assertNotNull(searchResults);
    Patient patientFromSearchResult = (Patient) searchResults.getEntry().get(0).getResource();

    /*
     * Ensure the unhashed values for MBI is present.
     */
    Boolean mbiUnhashedPresent = false;
    Iterator<Identifier> identifiers = patientFromSearchResult.getIdentifier().iterator();
    while (identifiers.hasNext()) {
      Identifier identifier = identifiers.next();
      if (identifier
          .getSystem()
          .equals(TransformerConstants.CODING_BBAPI_MEDICARE_BENEFICIARY_ID_UNHASHED)) {
        mbiUnhashedPresent = true;
      }
    }

    assertTrue(mbiUnhashedPresent);
  }

  /**
   * Verifies that {@link R4PatientResourceProvider#searchByIdentifier} works as expected for a
   * {@link Patient} that does exist in the DB.
   */
  @Test
  public void searchForExistingPatientByMbiHash() {
    List<Object> loadedRecords =
        ServerTestUtils.get()
            .loadData(Arrays.asList(StaticRifResourceGroup.SAMPLE_A.getResources()));
    IGenericClient fhirClient = ServerTestUtils.get().createFhirClientV2();

    Beneficiary beneficiary =
        loadedRecords.stream()
            .filter(r -> r instanceof Beneficiary)
            .map(r -> (Beneficiary) r)
            .findFirst()
            .get();

    Bundle searchResults =
        fhirClient
            .search()
            .forResource(Patient.class)
            .where(
                Patient.IDENTIFIER
                    .exactly()
                    .systemAndIdentifier(
                        TransformerConstants.CODING_BBAPI_BENE_MBI_HASH,
                        beneficiary.getMbiHash().get()))
            .returnBundle(Bundle.class)
            .execute();

    assertNotNull(searchResults);

    assertEquals(1, searchResults.getTotal());
    Patient patientFromSearchResult = (Patient) searchResults.getEntry().get(0).getResource();

    comparePatient(
        beneficiary,
        patientFromSearchResult,
        getRHwithIncldAddrFldHdr("false"),
        standardExpectedHistoricalMbis);

    String mbiIdentifier =
        patientFromSearchResult.getIdentifier().stream()
            .filter(
                identifier ->
                    identifier
                        .getSystem()
                        .equals(TransformerConstants.CODING_BBAPI_MEDICARE_BENEFICIARY_ID_UNHASHED))
            .findFirst()
            .get()
            .getValue();

    assertEquals(
        beneficiary.getMedicareBeneficiaryId().get(), mbiIdentifier, "mbiHash identifier exists");
  }

  /**
   * Verifies that the correct bene id or exception is returned when an MBI points to more than one
   * bene id in either the Beneficiaries and/or BeneficiariesHistory table.
   */
  @Test
  public void searchForExistingPatientByMbiHashWithBeneDupes() {
    List<Object> loadedRecords =
        ServerTestUtils.get()
            .loadData(Arrays.asList(StaticRifResourceGroup.SAMPLE_A.getResources()));

    // load additional Beneficiary and Beneficiary History records for
    // testing
    loadedRecords.addAll(
        ServerTestUtils.get()
            .loadData(Arrays.asList(StaticRifResourceGroup.SAMPLE_HICN_MULT_BENES.getResources())));

    IGenericClient fhirClient = ServerTestUtils.get().createFhirClientV2();

    Stream<Beneficiary> beneficiariesStream =
        loadedRecords.stream().filter(r -> r instanceof Beneficiary).map(r -> (Beneficiary) r);
    List<Beneficiary> beneficiariesList = beneficiariesStream.collect(Collectors.toList());

    Stream<BeneficiaryHistory> beneficiariesHistoryStream =
        loadedRecords.stream()
            .filter(r -> r instanceof BeneficiaryHistory)
            .map(r -> (BeneficiaryHistory) r);
    List<BeneficiaryHistory> beneficiariesHistoryList =
        beneficiariesHistoryStream.collect(Collectors.toList());

    boolean useMbiFromBeneficiaryTable;
    boolean expectsSingleBeneMatch;

    /*
     * The following scenario tests when the same mbi is in the
     * Beneficiaries table but points to different bene ids.
     */
    useMbiFromBeneficiaryTable = true;
    expectsSingleBeneMatch = false;
    assertPatientByHashTypeMatch(
        fhirClient,
        beneficiariesList,
        beneficiariesHistoryList,
        567834L,
        "3456789",
        useMbiFromBeneficiaryTable,
        expectsSingleBeneMatch,
        standardExpectedHistoricalMbis);

    /*
     * The following scenario tests when only one mbi is in the
     * Beneficiaries table.
     */
    useMbiFromBeneficiaryTable = true;
    expectsSingleBeneMatch = true;
    assertPatientByHashTypeMatch(
        fhirClient,
        beneficiariesList,
        beneficiariesHistoryList,
        -123456L,
        "3456789N",
        useMbiFromBeneficiaryTable,
        expectsSingleBeneMatch,
        new ArrayList<>());

    /*
     * The following scenario tests when the same mbi is in the
     * Beneficiaries and also in the BeneficiariesHistory table. The bene id
     * is different between the tables so the bene record from the
     * Beneficiaries table should be used.
     *
     * bene id=BENE1234 mbi=SAMEMBI rfrnc_yr=2019 should be pulled back.
     */
    useMbiFromBeneficiaryTable = true;
    expectsSingleBeneMatch = false;
    assertPatientByHashTypeMatch(
        fhirClient,
        beneficiariesList,
        beneficiariesHistoryList,
        1234L,
        "SAMEMBI",
        useMbiFromBeneficiaryTable,
        expectsSingleBeneMatch,
        List.of("HISTMBI"));

    /*
     * The following scenario tests when the requested mbi is only in the
     * BeneficiariesHistory table. Use the bene id from the
     * BeneficiariesHistory table to then read the Beneficiaries table.
     */
    useMbiFromBeneficiaryTable = false;
    expectsSingleBeneMatch = true;
    assertPatientByHashTypeMatch(
        fhirClient,
        beneficiariesList,
        beneficiariesHistoryList,
        55555L,
        "HISTMBI",
        useMbiFromBeneficiaryTable,
        expectsSingleBeneMatch,
        List.of("HISTMBI"));

    /*
     * The following scenario tests when the requested mbi is only in the
     * BeneficiariesHistory table but this mbi points to more than one bene
     * id in history.
     */
    useMbiFromBeneficiaryTable = false;
    expectsSingleBeneMatch = false;
    assertPatientByHashTypeMatch(
        fhirClient,
        beneficiariesList,
        beneficiariesHistoryList,
        66666L,
        "DUPHISTMBI",
        useMbiFromBeneficiaryTable,
        expectsSingleBeneMatch,
        List.of("HISTMBI"));

    /*
     * The following scenario tests when a mbi is not found in the
     * Beneficiaries and BeneficiariesHistory table.
     *
     */
    Bundle searchResults =
        fhirClient
            .search()
            .forResource(Patient.class)
            .where(
                Patient.IDENTIFIER
                    .exactly()
                    .systemAndIdentifier(
                        TransformerConstants.CODING_BBAPI_BENE_MBI_HASH, "notfoundmbi"))
            .returnBundle(Bundle.class)
            .execute();
    assertEquals(0, searchResults.getTotal());
  }

  /**
   * Verifies that {@link R4PatientResourceProvider#searchByIdentifier} works as expected for MBIs
   * that should be present as a {@link BeneficiaryHistory} record.
   */
  @Test
  public void searchForExistingPatientByHistoricalMbiHash() {
    List<Object> loadedRecords =
        ServerTestUtils.get()
            .loadData(Arrays.asList(StaticRifResourceGroup.SAMPLE_A.getResources()));
    IGenericClient fhirClient = ServerTestUtils.get().createFhirClientV2();

    loadedRecords.stream()
        .filter(r -> r instanceof BeneficiaryHistory)
        .map(r -> (BeneficiaryHistory) r)
        .forEach(
            h -> {
              Bundle searchResults =
                  fhirClient
                      .search()
                      .forResource(Patient.class)
                      .where(
                          Patient.IDENTIFIER
                              .exactly()
                              .systemAndIdentifier(
                                  TransformerConstants.CODING_BBAPI_BENE_MBI_HASH,
                                  h.getMbiHash().get()))
                      .returnBundle(Bundle.class)
                      .execute();

              assertNotNull(searchResults);
              assertEquals(1, searchResults.getTotal());
              Patient patientFromSearchResult =
                  (Patient) searchResults.getEntry().get(0).getResource();
              assertEquals(
                  String.valueOf(h.getBeneficiaryId()),
                  patientFromSearchResult.getIdElement().getIdPart());
            });
  }

  /**
   * Verifies that {@link R4PatientResourceProvider#searchByIdentifier} works as expected for MBIs
   * associated with {@link Beneficiary}s that have <strong>no</strong> {@link BeneficiaryHistory}
   * records.
   */
  @Test
  public void searchForExistingPatientByMbiWithNoHistory() {
    List<Object> loadedRecords =
        ServerTestUtils.get().loadData(Arrays.asList(StaticRifResource.SAMPLE_A_BENES));
    IGenericClient fhirClient = ServerTestUtils.get().createFhirClientV2();

    loadedRecords.stream()
        .filter(r -> r instanceof Beneficiary)
        .map(r -> (Beneficiary) r)
        .forEach(
            h -> {
              Bundle searchResults =
                  fhirClient
                      .search()
                      .forResource(Patient.class)
                      .where(
                          Patient.IDENTIFIER
                              .exactly()
                              .systemAndIdentifier(
                                  TransformerConstants.CODING_BBAPI_BENE_MBI_HASH,
                                  h.getMbiHash().get()))
                      .returnBundle(Bundle.class)
                      .execute();

              assertNotNull(searchResults);
              assertEquals(1, searchResults.getTotal());
              Patient patientFromSearchResult =
                  (Patient) searchResults.getEntry().get(0).getResource();
              assertEquals(
                  String.valueOf(h.getBeneficiaryId()),
                  patientFromSearchResult.getIdElement().getIdPart());
            });
  }

  /**
   * Verifies that searching by a known existing part D contract number (with 'include identifiers'
   * in the header) returns a result as expected. Also ensures the unhashed MBI values are returned
   * by default.
   */
  @Test
  public void searchForExistingPatientByPartDContractNum() {
    List<Object> loadedRecords =
        ServerTestUtils.get().loadData(Arrays.asList(StaticRifResource.SAMPLE_A_BENES));
    IGenericClient fhirClient = createFhirClientWithIncludeIdentifiersMbi();

    // Should return a single match
    Bundle searchResults =
        fhirClient
            .search()
            .forResource(Patient.class)
            .where(
                new TokenClientParam("_has:Coverage.extension")
                    .exactly()
                    .systemAndIdentifier(
                        CCWUtils.calculateVariableReferenceUrl(CcwCodebookVariable.PTDCNTRCT01),
                        "S4607"))
            .where(
                new TokenClientParam("_has:Coverage.rfrncyr")
                    .exactly()
                    .systemAndIdentifier(
                        CCWUtils.calculateVariableReferenceUrl(CcwCodebookVariable.RFRNC_YR),
                        "2018"))
            .returnBundle(Bundle.class)
            .execute();

    assertNotNull(searchResults);
    assertEquals(1, searchResults.getEntry().size());
    Patient patientFromSearchResult = (Patient) searchResults.getEntry().get(0).getResource();

    Beneficiary expectedBene = (Beneficiary) loadedRecords.get(0);
    assertEquals(
        String.valueOf(expectedBene.getBeneficiaryId()),
        patientFromSearchResult.getIdElement().getIdPart());

    /*
     * Ensure the unhashed values for MBI is present.
     */
    boolean mbiUnhashedPresent = false;
    for (Identifier identifier : patientFromSearchResult.getIdentifier()) {
      if (identifier
          .getSystem()
          .equals(TransformerConstants.CODING_BBAPI_MEDICARE_BENEFICIARY_ID_UNHASHED)) {
        mbiUnhashedPresent = true;
      }
    }

    assertTrue(mbiUnhashedPresent);
  }

  /**
   * Regression test for part of BFD-525, which verifies that duplicate entries are not returned
   * when 1) plain-text identifiers are requested, 2) a beneficiary has multiple historical
   * identifiers, and 3) paging is requested. (This oddly specific combo had been bugged earlier and
   * was quite tricky to resolve).
   */
  @Test
  public void searchForExistingPatientByPartDContractNumWithPagingAndMultipleMbis() {
    ServerTestUtils.get()
        .loadData(
            Arrays.asList(
                StaticRifResource.SAMPLE_A_BENES, StaticRifResource.SAMPLE_A_BENEFICIARY_HISTORY));
    IGenericClient fhirClient = createFhirClientWithIncludeIdentifiersMbi();

    // Should return a single match
    Bundle searchResults =
        fhirClient
            .search()
            .forResource(Patient.class)
            .where(
                new TokenClientParam("_has:Coverage.extension")
                    .exactly()
                    .systemAndIdentifier(
                        CCWUtils.calculateVariableReferenceUrl(CcwCodebookVariable.PTDCNTRCT01),
                        "S4607"))
            .where(
                new TokenClientParam("_has:Coverage.rfrncyr")
                    .exactly()
                    .systemAndIdentifier(
                        CCWUtils.calculateVariableReferenceUrl(CcwCodebookVariable.RFRNC_YR),
                        "2018"))
            .count(1)
            .returnBundle(Bundle.class)
            .execute();

    // Verify that the bene wasn't duplicated.
    assertNotNull(searchResults);
    assertEquals(1, searchResults.getEntry().size());

    // Double-check that the bene has multiple identifiers.
    Patient patientFromSearchResult = (Patient) searchResults.getEntry().get(0).getResource();
    assertEquals(
        1, // was 4
        patientFromSearchResult.getIdentifier().stream()
            .filter(
                i ->
                    TransformerConstants.CODING_BBAPI_MEDICARE_BENEFICIARY_ID_UNHASHED.equals(
                        i.getSystem()))
            .count());
  }

  /**
   * Regression test for part of BFD-525, which verifies that duplicate entries are not returned
   * when 1) plain-text identifiers are requested, 2) a beneficiary has multiple historical
   * identifiers, and 3) paging is not requested. (This oddly specific combo had been bugged earlier
   * and was quite tricky to resolve).
   */
  @Test
  public void searchForExistingPatientByPartDContractNumWithNoPagingAndMultipleMbis() {
    ServerTestUtils.get()
        .loadData(
            Arrays.asList(
                StaticRifResource.SAMPLE_A_BENES, StaticRifResource.SAMPLE_A_BENEFICIARY_HISTORY));
    IGenericClient fhirClient = createFhirClient("true");

    Bundle searchResults = null;
    // Should return a single match
    searchResults =
        fhirClient
            .search()
            .forResource(Patient.class)
            .where(
                new TokenClientParam("_has:Coverage.extension")
                    .exactly()
                    .systemAndIdentifier(
                        CCWUtils.calculateVariableReferenceUrl(CcwCodebookVariable.PTDCNTRCT01),
                        "S4607"))
            .where(
                new TokenClientParam("_has:Coverage.rfrncyr")
                    .exactly()
                    .systemAndIdentifier(
                        CCWUtils.calculateVariableReferenceUrl(CcwCodebookVariable.RFRNC_YR),
                        "2018"))
            .returnBundle(Bundle.class)
            .execute();

    // Verify that the bene wasn't duplicated.
    assertNotNull(searchResults);
    assertEquals(1, searchResults.getEntry().size());

    // Double-check that the bene has multiple identifiers.
    Patient patientFromSearchResult = (Patient) searchResults.getEntry().get(0).getResource();
    assertEquals(
        1, // was 4
        patientFromSearchResult.getIdentifier().stream()
            .filter(
                i ->
                    TransformerConstants.CODING_BBAPI_MEDICARE_BENEFICIARY_ID_UNHASHED.equals(
                        i.getSystem()))
            .count());
  }

  /**
   * Verifies that searching by lastUpdated with its various supported prefixes returns results as
   * expected.
   */
  @Test
  public void searchWithLastUpdated() {
    List<Object> loadedRecords =
        ServerTestUtils.get()
            .loadData(Arrays.asList(StaticRifResourceGroup.SAMPLE_A.getResources()));
    IGenericClient fhirClient = ServerTestUtils.get().createFhirClientV2();

    Beneficiary beneficiary =
        loadedRecords.stream()
            .filter(r -> r instanceof Beneficiary)
            .map(r -> (Beneficiary) r)
            .findFirst()
            .get();

    // Build up a list of lastUpdatedURLs that return > all values values
    String nowDateTime = new DateTimeDt(Date.from(Instant.now().plusSeconds(1))).getValueAsString();
    String earlyDateTime = "2019-10-01T00:00:00+00:00";
    List<String> allUrls =
        Arrays.asList(
            "_lastUpdated=gt" + earlyDateTime,
            "_lastUpdated=ge" + earlyDateTime,
            "_lastUpdated=le" + nowDateTime,
            "_lastUpdated=ge" + earlyDateTime + "&_lastUpdated=le" + nowDateTime,
            "_lastUpdated=gt" + earlyDateTime + "&_lastUpdated=lt" + nowDateTime);
    testLastUpdatedUrls(fhirClient, beneficiary.getBeneficiaryId(), allUrls, 1);

    // Empty searches
    List<String> emptyUrls =
        Arrays.asList("_lastUpdated=lt" + earlyDateTime, "_lastUpdated=le" + earlyDateTime);
    testLastUpdatedUrls(fhirClient, beneficiary.getBeneficiaryId(), emptyUrls, 0);
  }

  /**
   * Returns a header with include address fields.
   *
   * @param value of all include address fields values
   * @return RequestHeaders instance derived from value
   */
  public static RequestHeaders getRHwithIncldAddrFldHdr(String value) {
    return RequestHeaders.getHeaderWrapper(
        R4PatientResourceProvider.HEADER_NAME_INCLUDE_ADDRESS_FIELDS, value);
  }

  /**
   * Creates a FHIR client for testing with the specified header.
   *
   * @param requestHeader the request header
   * @return the client with extra params registered
   */
  public static IGenericClient createFhirClient(RequestHeaders requestHeader) {
    IGenericClient fhirClient = ServerTestUtils.get().createFhirClientV2();
    if (requestHeader != null) {
      ExtraParamsInterceptor extraParamsInterceptor = new ExtraParamsInterceptor();
      extraParamsInterceptor.setHeaders(requestHeader);
      fhirClient.registerInterceptor(extraParamsInterceptor);
    }
    return fhirClient;
  }

  /**
   * Creates a FHIR client for testing with the specified 'include address' header values.
   *
   * @param addrHdrVal includeAddressFields header value
   * @return the client
   */
  public static IGenericClient createFhirClient(String addrHdrVal) {
    RequestHeaders requestHeader =
        RequestHeaders.getHeaderWrapper(
            R4PatientResourceProvider.HEADER_NAME_INCLUDE_ADDRESS_FIELDS, addrHdrVal);
    return createFhirClient(requestHeader);
  }

  /**
   * The following method tests that a ResourceNotFoundException exception is thrown when there are
   * instances of one hash value (hicn or mbi) pointing to more than bene id between the
   * Beneficiaries and BeneficiariesHistory tables.
   *
   * <p>Or that single match is found when the expectsSingleBeneMatch param is = true.
   *
   * <p>The hashType param chooses which type of values/hash to use. This is either "hicn" or "mbi".
   *
   * @param fhirClient the fhir client
   * @param beneficiariesList the beneficiaries list
   * @param beneficiariesHistoryList the beneficiaries history list
   * @param beneficiaryId the beneficiary id
   * @param unhashedValue the unhashed value
   * @param useFromBeneficiaryTable the use from beneficiary table
   * @param expectsSingleBeneMatch if a single bene match is expected
   * @param expectedHistoricalMbis the expected historical mbis
   */
  private void assertPatientByHashTypeMatch(
      IGenericClient fhirClient,
      List<Beneficiary> beneficiariesList,
      List<BeneficiaryHistory> beneficiariesHistoryList,
      Long beneficiaryId,
      String unhashedValue,
      Boolean useFromBeneficiaryTable,
      Boolean expectsSingleBeneMatch,
      List<String> expectedHistoricalMbis) {

    Bundle searchResults = null;
    String mbiHash = "";

    if (useFromBeneficiaryTable) {
      Beneficiary beneficiaryMbiToMatchTo =
          beneficiariesList.stream()
              .filter(r -> unhashedValue.equals(r.getMedicareBeneficiaryId().get()))
              .findFirst()
              .get();

      mbiHash = beneficiaryMbiToMatchTo.getMbiHash().get();
    } else {
      BeneficiaryHistory beneficiaryHistoryMbiToMatchTo =
          beneficiariesHistoryList.stream()
              .filter(r -> unhashedValue.equals(r.getMedicareBeneficiaryId().get()))
              .findFirst()
              .get();

      mbiHash = beneficiaryHistoryMbiToMatchTo.getMbiHash().get();
    }

    try {
      // return bene record based on unhashedValue passed to this method
      searchResults =
          fhirClient
              .search()
              .forResource(Patient.class)
              .where(
                  Patient.IDENTIFIER
                      .exactly()
                      .systemAndIdentifier(
                          TransformerConstants.CODING_BBAPI_BENE_MBI_HASH, mbiHash))
              .returnBundle(Bundle.class)
              .execute();

      if (!expectsSingleBeneMatch) {
        // Should throw exception before here, so assert a failed test.
        fail("An exception was expected when there are duplicate bene id matches.");
      }
    } catch (ResourceNotFoundException e) {
      // Test passes if an exception was thrown.
    }

    // Validate result if a single match is expected for test.
    if (expectsSingleBeneMatch) {
      assertNotNull(searchResults);
      assertEquals(1, searchResults.getTotal());

      Beneficiary beneficiary =
          beneficiariesList.stream()
              .filter(r -> beneficiaryId.equals(r.getBeneficiaryId()))
              .findAny()
              .get();
      Patient patientFromSearchResult = (Patient) searchResults.getEntry().get(0).getResource();

      comparePatient(
          beneficiary,
          patientFromSearchResult,
          getRHwithIncldAddrFldHdr("false"),
          expectedHistoricalMbis);
    }
  }

  /**
   * Test the set of lastUpdated values.
   *
   * @param fhirClient to use
   * @param id the beneficiary id to use
   * @param urls is a list of lastUpdate values to test to find
   * @param expectedValue number of matches
   */
  private void testLastUpdatedUrls(
      IGenericClient fhirClient, Long id, List<String> urls, int expectedValue) {
    String baseResourceUrl = "Patient?_id=" + id + "&_format=application%2Fjson%2Bfhir";

    // Search for each lastUpdated value
    for (String lastUpdatedValue : urls) {
      String theSearchUrl = baseResourceUrl + "&" + lastUpdatedValue;
      Bundle searchResults =
          fhirClient.search().byUrl(theSearchUrl).returnBundle(Bundle.class).execute();
      assertEquals(
          expectedValue,
          searchResults.getTotal(),
          String.format(
              "Expected %s to filter resources using lastUpdated correctly", lastUpdatedValue));
    }
  }

  /**
   * Compares an expected patient to a {@link Beneficiary} transformed into a patient.
   *
   * @param beneficiary the beneficiary to test
   * @param patient the expected patient
   * @param headers the headers to use while transforming the bene
   * @param expectedHistoricalMbis the expected historical mbis for the expected beneficiary
   */
  private void comparePatient(
      Beneficiary beneficiary,
      Patient patient,
      RequestHeaders headers,
      List<String> expectedHistoricalMbis) {
    assertNotNull(patient);

    BeneficiaryTransformerV2 beneficiaryTransformerV2 =
        new BeneficiaryTransformerV2(
            PipelineTestUtils.get().getPipelineApplicationState().getMetrics());

    Patient expected = beneficiaryTransformerV2.transform(beneficiary, headers);

    comparePatient(expected, patient, expectedHistoricalMbis);
  }

  /**
   * Compares an expected patient to a {@link Beneficiary} transformed into a patient.
   *
   * @param beneficiary the beneficiary
   * @param patient the patient
   * @param expectedHistoricalMbis the expected historical mbis for the beneficiary
   */
  private void comparePatient(
      Beneficiary beneficiary, Patient patient, List<String> expectedHistoricalMbis) {
    comparePatient(beneficiary, patient, RequestHeaders.getHeaderWrapper(), expectedHistoricalMbis);
  }

  /**
   * Compares a patient with another patient.
   *
   * @param expected the expected patient
   * @param patient the patient to test
   * @param expectedHistoricalMbis the expected historical mbis for the expected patient
   */
  private void comparePatient(
      Patient expected, Patient patient, List<String> expectedHistoricalMbis) {
    // The ID returned from the FHIR client differs from the transformer. It adds
    // URL information. Verify that the resource it is pointing to is the same,
    // and then set up to do a deep compare of the rest
    assertTrue(patient.getId().endsWith(expected.getId()));
    patient.setIdElement(expected.getIdElement());

    // Last updated time will also differ, so fix this before the deep compare
    assertNotNull(patient.getMeta().getLastUpdated());
    patient.getMeta().setLastUpdatedElement(expected.getMeta().getLastUpdatedElement());

    // Add the identifiers that wont be present in the expected due to not going
    // through the resource provider that adds historical mbis.
    addHistoricalExtensions(expected, expectedHistoricalMbis);
    assertTrue(expected.equalsDeep(patient));
  }

  /**
   * Adds a historical extension to a patient loaded from the sample A data for each mbi provided,
   * as if it had been transformed using the resource provider that would normally add them.
   *
   * @param patient the patient to add the Identifiers for historical MBIs to
   * @param historicalMbis the historical mbis to add as identifier extensions as historical mbis
   */
  private void addHistoricalExtensions(Patient patient, List<String> historicalMbis) {
    Period period = new Period();
    try {
      Date start = (new SimpleDateFormat("yyyy-MM-dd")).parse("2020-07-30");
      period.setStart(start, TemporalPrecisionEnum.DAY);
    } catch (Exception e) {
    }

    for (String historicalMbi : historicalMbis) {
      Extension extension =
          new Extension(
              "https://bluebutton.cms.gov/resources/codesystem/identifier-currency",
              new Coding(
                  "https://bluebutton.cms.gov/resources/codesystem/identifier-currency",
                  "historic",
                  "Historic"));

      Identifier histId = new Identifier();
      histId
          .setValue(historicalMbi)
          .setSystem("http://hl7.org/fhir/sid/us-mbi")
          .getType()
          .addCoding()
          .setCode("MC")
          .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
          .setDisplay("Patient's Medicare number")
          .addExtension(extension);
      patient.getIdentifier().add(histId);
    }
  }

  /**
   * Creates a FHIR client with 'include identifiers' set in the header.
   *
   * @return the client
   */
  public static IGenericClient createFhirClientWithIncludeIdentifiersMbi() {
    RequestHeaders requestHeader =
        RequestHeaders.getHeaderWrapper(
            R4PatientResourceProvider.HEADER_NAME_INCLUDE_IDENTIFIERS, "mbi");
    return createFhirClient(requestHeader);
  }
}
