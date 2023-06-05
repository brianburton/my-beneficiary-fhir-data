package gov.cms.bfd.model.rif.samples;

import gov.cms.bfd.model.rif.RifFile;
import gov.cms.bfd.model.rif.RifFileType;
import gov.cms.bfd.model.rif.parse.RifParsingUtils;
import gov.cms.bfd.sharedutils.exceptions.BadCodeMonkeyException;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/** Enumerates the sample RIF resources available on the classpath. */
public enum StaticRifResource {
  /** Sample A Beneficiary static rif. */
  SAMPLE_A_BENES(
      resourceUrl("rif-static-samples/sample-a-beneficiaries.txt"), RifFileType.BENEFICIARY, 1),

  /** Sample A Beneficiary without a reference year static rif. */
  SAMPLE_A_BENES_WITHOUT_REFERENCE_YEAR(
      resourceUrl("rif-static-samples/sample-a-beneficiaries-without-reference-year.txt"),
      RifFileType.BENEFICIARY,
      1),

  /** Sample A Beneficiary with a backslash in the data static rif. */
  SAMPLE_A_BENES_WITH_BACKSLASH(
      resourceUrl("rif-static-samples/sample-a-beneficiaries-with-backslash.txt"),
      RifFileType.BENEFICIARY,
      1),

  /** Sample A Beneficiary History static rif. */
  SAMPLE_A_BENEFICIARY_HISTORY(
      resourceUrl("rif-static-samples/sample-a-beneficiaryhistory.txt"),
      RifFileType.BENEFICIARY_HISTORY,
      5),

  /** Sample A Carrier static rif. */
  SAMPLE_A_CARRIER(resourceUrl("rif-static-samples/sample-a-bcarrier.txt"), RifFileType.CARRIER, 1),

  /** Sample A Carrier static rif. */
  SAMPLE_A_CARRIER_MULTIPLE_LINES(
      resourceUrl("rif-static-samples/sample-a-bcarrier-multiple-lines.txt"),
      RifFileType.CARRIER,
      7),

  /** Sample A Inpatient static rif. */
  SAMPLE_A_INPATIENT(
      resourceUrl("rif-static-samples/sample-a-inpatient.txt"), RifFileType.INPATIENT, 1),

  /** Sample A Inpatient four character drg code static rif. */
  SAMPLE_A_INPATIENT_FOUR_CHARACTER_DRG_CODE(
      resourceUrl("rif-static-samples/sample-a-inpatient-with-four-character-drg-code.txt"),
      RifFileType.INPATIENT,
      1),

  /** Sample A Outpatient static rif. */
  SAMPLE_A_OUTPATIENT(
      resourceUrl("rif-static-samples/sample-a-outpatient.txt"), RifFileType.OUTPATIENT, 1),

  /** Sample A SNF static rif. */
  SAMPLE_A_SNF(resourceUrl("rif-static-samples/sample-a-snf.txt"), RifFileType.SNF, 1),

  /** Sample A SNF Four Character DRG Code static rif. */
  SAMPLE_A_SNF_FOUR_CHARACTER_DRG_CODE(
      resourceUrl("rif-static-samples/sample-a-snf-with-four-character-drg-code.txt"),
      RifFileType.SNF,
      1),

  /** Sample A Hospice static rif. */
  SAMPLE_A_HOSPICE(resourceUrl("rif-static-samples/sample-a-hospice.txt"), RifFileType.HOSPICE, 1),

  /** Sample A HHA static rif. */
  SAMPLE_A_HHA(resourceUrl("rif-static-samples/sample-a-hha.txt"), RifFileType.HHA, 1),

  /** Sample A DME static rif. */
  SAMPLE_A_DME(resourceUrl("rif-static-samples/sample-a-dme.txt"), RifFileType.DME, 1),

  /** Sample A PDE static rif. */
  SAMPLE_A_PDE(resourceUrl("rif-static-samples/sample-a-pde.txt"), RifFileType.PDE, 1),

  /** Sample A Multiple Rows Same Beneficiary static rif. */
  SAMPLE_A_MULTIPLE_ROWS_SAME_BENE(
      resourceUrl("rif-static-samples/sample-a-multiple-entries-same-bene.txt"),
      RifFileType.BENEFICIARY,
      6),

  /** Sample U Benficiary static rif. */
  SAMPLE_U_BENES(
      resourceUrl("rif-static-samples/sample-u-beneficiaries.txt"), RifFileType.BENEFICIARY, 1),

  /** Sample U Benficiary Changed With 8 months of history static rif. */
  SAMPLE_U_BENES_CHANGED_WITH_8_MONTHS(
      resourceUrl("rif-static-samples/sample-u-with-8-months.txt"), RifFileType.BENEFICIARY, 1),

  /** Sample U Benficiary Changed With 9 months of history static rif. */
  SAMPLE_U_BENES_CHANGED_WITH_9_MONTHS(
      resourceUrl("rif-static-samples/sample-u-with-9-months.txt"), RifFileType.BENEFICIARY, 1),

  /** Sample U Benficiary Unchanged static rif. */
  SAMPLE_U_BENES_UNCHANGED(
      resourceUrl("rif-static-samples/sample-u-unchanged-beneficiaries.txt"),
      RifFileType.BENEFICIARY,
      1),

  /** Sample U Carrier static rif. */
  SAMPLE_U_CARRIER(resourceUrl("rif-static-samples/sample-u-bcarrier.txt"), RifFileType.CARRIER, 1),

  /**
   * The ({@code SAMPLE_SYNTHEA_*}) set of test fixture files were generated using <a href=
   * "https://github.com/synthetichealth/synthea/wiki/Getting-Started">Synthea</a>. To recreate
   * these files, perform the following steps after installing Synthea as per the developer
   * instructions in the linked site above:
   *
   * <ol>
   *   <li>Generate the files: {@code ./run_synthea -s 1010 -cs 0 -r 20210520 -e 20210520 -p 20
   *       --exporter.fhir.export=false --exporter.bfd.export=true --exporter.years_of_history=10
   *       --generate.only_alive_patients=true -a 70-80}
   *   <li>Minimize the files: {@code ./gradlew rifMinimize}
   *   <li>Copy the files to the bfd resource dir: {@code cp output/bfd_min/*
   *       $BFD_HOME/apps/bfd-model/bfd-model-rif-samples/src/main/resources/rif-synthea}
   * </ol>
   */
  SAMPLE_SYNTHEA_BENES2011(
      resourceUrl("rif-synthea/beneficiary_2011.csv"), RifFileType.BENEFICIARY, -1),
  /** Sample Synthea Benes 2012 Resource. */
  SAMPLE_SYNTHEA_BENES2012(
      resourceUrl("rif-synthea/beneficiary_2012.csv"), RifFileType.BENEFICIARY, -1),
  /** Sample Synthea Benes 2013 Resource. */
  SAMPLE_SYNTHEA_BENES2013(
      resourceUrl("rif-synthea/beneficiary_2013.csv"), RifFileType.BENEFICIARY, -1),
  /** Sample Synthea Benes 2014 Resource. */
  SAMPLE_SYNTHEA_BENES2014(
      resourceUrl("rif-synthea/beneficiary_2014.csv"), RifFileType.BENEFICIARY, -1),
  /** Sample Synthea Benes 2015 Resource. */
  SAMPLE_SYNTHEA_BENES2015(
      resourceUrl("rif-synthea/beneficiary_2015.csv"), RifFileType.BENEFICIARY, -1),
  /** Sample Synthea Benes 2016 Resource. */
  SAMPLE_SYNTHEA_BENES2016(
      resourceUrl("rif-synthea/beneficiary_2016.csv"), RifFileType.BENEFICIARY, -1),
  /** Sample Synthea Benes 2017 Resource. */
  SAMPLE_SYNTHEA_BENES2017(
      resourceUrl("rif-synthea/beneficiary_2017.csv"), RifFileType.BENEFICIARY, -1),
  /** Sample Synthea Benes 2018 Resource. */
  SAMPLE_SYNTHEA_BENES2018(
      resourceUrl("rif-synthea/beneficiary_2018.csv"), RifFileType.BENEFICIARY, -1),
  /** Sample Synthea Benes 2019 Resource. */
  SAMPLE_SYNTHEA_BENES2019(
      resourceUrl("rif-synthea/beneficiary_2019.csv"), RifFileType.BENEFICIARY, -1),
  /** Sample Synthea Benes 2020 Resource. */
  SAMPLE_SYNTHEA_BENES2020(
      resourceUrl("rif-synthea/beneficiary_2020.csv"), RifFileType.BENEFICIARY, -1),
  /** Sample Synthea Benes 2021 Resource. */
  SAMPLE_SYNTHEA_BENES2021(
      resourceUrl("rif-synthea/beneficiary_2021.csv"), RifFileType.BENEFICIARY, -1),
  /** Sample Synthea Carrier Resource. */
  SAMPLE_SYNTHEA_CARRIER(resourceUrl("rif-synthea/carrier.csv"), RifFileType.CARRIER, -1),
  /** Sample Synthea Inpatient Resource. */
  SAMPLE_SYNTHEA_INPATIENT(resourceUrl("rif-synthea/inpatient.csv"), RifFileType.INPATIENT, -1),
  /** Sample Synthea Outpatient Resource. */
  SAMPLE_SYNTHEA_OUTPATIENT(resourceUrl("rif-synthea/outpatient.csv"), RifFileType.OUTPATIENT, -1),
  /** Sample Synthea SNF Resource. */
  SAMPLE_SYNTHEA_SNF(resourceUrl("rif-synthea/snf.csv"), RifFileType.SNF, -1),
  /** Sample Synthea Hospice Resource. */
  SAMPLE_SYNTHEA_HOSPICE(resourceUrl("rif-synthea/hospice.csv"), RifFileType.HOSPICE, -1),
  /** Sample Synthea HHAt Resource. */
  SAMPLE_SYNTHEA_HHA(resourceUrl("rif-synthea/hha.csv"), RifFileType.HHA, -1),
  /** Sample Synthea DME Resource. */
  SAMPLE_SYNTHEA_DME(resourceUrl("rif-synthea/dme.csv"), RifFileType.DME, -1),
  /** Sample Synthea PDE Resource. */
  SAMPLE_SYNTHEA_PDE(resourceUrl("rif-synthea/pde.csv"), RifFileType.PDE, -1),
  /** Sample Synthea Benehistory Resource. */
  SAMPLE_SYNTHEA_BENEHISTORY(
      resourceUrl("rif-synthea/beneficiary_history.csv"), RifFileType.BENEFICIARY_HISTORY, -1),

  /** Synthetic Benficiary 1999 Remote S3 data. */
  SYNTHETIC_BENEFICIARY_1999(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-beneficiary-1999.rif"),
      RifFileType.BENEFICIARY,
      10000),
  /** Synthetic Benficiary 2000 Remote S3 data. */
  SYNTHETIC_BENEFICIARY_2000(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-beneficiary-2000.rif"),
      RifFileType.BENEFICIARY,
      10000),
  /** Synthetic Benficiary 2014 Remote S3 data. */
  SYNTHETIC_BENEFICIARY_2014(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-beneficiary-2014.rif"),
      RifFileType.BENEFICIARY,
      10000),
  /** Synthetic Carrier 1999_1999 Remote S3 data. */
  SYNTHETIC_CARRIER_1999_1999(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-carrier-1999-1999.rif"),
      RifFileType.CARRIER,
      102617),
  /** Synthetic Carrier1999_2000 Remote S3 data. */
  SYNTHETIC_CARRIER_1999_2000(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-carrier-1999-2000.rif"),
      RifFileType.CARRIER,
      107665),
  /** Synthetic Carrier 1999_2001 Remote S3 data. */
  SYNTHETIC_CARRIER_1999_2001(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-carrier-1999-2001.rif"),
      RifFileType.CARRIER,
      113604),
  /** Synthetic Carrier 2000_2000 Remote S3 data. */
  SYNTHETIC_CARRIER_2000_2000(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-carrier-2000-2000.rif"),
      RifFileType.CARRIER,
      102178),
  /** Synthetic Carrier 2000_2001 Remote S3 data. */
  SYNTHETIC_CARRIER_2000_2001(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-carrier-2000-2001.rif"),
      RifFileType.CARRIER,
      108801),
  /** Synthetic Carrier 2000_2002 Remote S3 data. */
  SYNTHETIC_CARRIER_2000_2002(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-carrier-2000-2002.rif"),
      RifFileType.CARRIER,
      113806),
  /** Synthetic Carrier 2014_2014 Remote S3 data. */
  SYNTHETIC_CARRIER_2014_2014(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-carrier-2014-2014.rif"),
      RifFileType.CARRIER,
      108172),
  /** Synthetic Carrier 2014_2015 Remote S3 data. */
  SYNTHETIC_CARRIER_2014_2015(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-carrier-2014-2015.rif"),
      RifFileType.CARRIER,
      106577),
  /** Synthetic Carrier 2014_2016 Remote S3 data. */
  SYNTHETIC_CARRIER_2014_2016(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-carrier-2014-2016.rif"),
      RifFileType.CARRIER,
      86736),
  /** Synthetic Inpatient 1999_1999 Remote S3 data. */
  SYNTHETIC_INPATIENT_1999_1999(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-inpatient-1999-1999.rif"),
      RifFileType.INPATIENT,
      650),
  /** Synthetic Inpatient 1999_2000 Remote S3 data. */
  SYNTHETIC_INPATIENT_1999_2000(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-inpatient-1999-2000.rif"),
      RifFileType.INPATIENT,
      646),
  /** Synthetic Inpatient 1999_2001 Remote S3 data. */
  SYNTHETIC_INPATIENT_1999_2001(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-inpatient-1999-2001.rif"),
      RifFileType.INPATIENT,
      700),
  /** Synthetic Inpatient 2000_2000 Remote S3 data. */
  SYNTHETIC_INPATIENT_2000_2000(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-inpatient-2000-2000.rif"),
      RifFileType.INPATIENT,
      706),
  /** Synthetic Inpatient 2000_2001 Remote S3 data. */
  SYNTHETIC_INPATIENT_2000_2001(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-inpatient-2000-2001.rif"),
      RifFileType.INPATIENT,
      641),
  /** Synthetic Inpatient 2000_2002 Remote S3 data. */
  SYNTHETIC_INPATIENT_2000_2002(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-inpatient-2000-2002.rif"),
      RifFileType.INPATIENT,
      680),
  /** Synthetic Inpatient 2014_2014 Remote S3 data. */
  SYNTHETIC_INPATIENT_2014_2014(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-inpatient-2014-2014.rif"),
      RifFileType.INPATIENT,
      352),
  /** Synthetic Inpatient 2014_2015 Remote S3 data. */
  SYNTHETIC_INPATIENT_2014_2015(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-inpatient-2014-2015.rif"),
      RifFileType.INPATIENT,
      309),
  /** Synthetic Inpatient 2014_2016 Remote S3 data. */
  SYNTHETIC_INPATIENT_2014_2016(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-inpatient-2014-2016.rif"),
      RifFileType.INPATIENT,
      387),
  /** Synthetic PDE 2014 Remote S3 data. */
  SYNTHETIC_PDE_2014(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-pde-2014.rif"),
      RifFileType.PDE,
      127643),
  /** Synthetic PDE 2015 Remote S3 data. */
  SYNTHETIC_PDE_2015(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-pde-2015.rif"),
      RifFileType.PDE,
      140176),
  /** Synthetic PDE 2016 Remote S3 data. */
  SYNTHETIC_PDE_2016(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-pde-2016.rif"),
      RifFileType.PDE,
      145526),
  /** Synthetic Outpatient 1999_1999 Remote S3 data. */
  SYNTHETIC_OUTPATIENT_1999_1999(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-outpatient-1999-1999.rif"),
      RifFileType.OUTPATIENT,
      20744),
  /** Synthetic Outpatient 2000_1999 Remote S3 data. */
  SYNTHETIC_OUTPATIENT_2000_1999(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-outpatient-2000-1999.rif"),
      RifFileType.OUTPATIENT,
      22439),
  /** Synthetic Outpatient 2001_1999 Remote S3 data. */
  SYNTHETIC_OUTPATIENT_2001_1999(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-outpatient-2001-1999.rif"),
      RifFileType.OUTPATIENT,
      23241),
  /** Synthetic Outpatient 2002_2000 Remote S3 data. */
  SYNTHETIC_OUTPATIENT_2002_2000(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-outpatient-2002-2000.rif"),
      RifFileType.OUTPATIENT,
      24575),
  /** Synthetic Outpatient 2014_2014 Remote S3 data. */
  SYNTHETIC_OUTPATIENT_2014_2014(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-outpatient-2014-2014.rif"),
      RifFileType.OUTPATIENT,
      25194),
  /** Synthetic Outpatient 2015_2014 Remote S3 data. */
  SYNTHETIC_OUTPATIENT_2015_2014(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-outpatient-2015-2014.rif"),
      RifFileType.OUTPATIENT,
      26996),
  /** Synthetic Outpatient 2016_2014 Remote S3 data. */
  SYNTHETIC_OUTPATIENT_2016_2014(
      remoteS3Data(TestDataSetLocation.SYNTHETIC_DATA, "synthetic-outpatient-2016-2014.rif"),
      RifFileType.OUTPATIENT,
      27955),
  /** Synthetic Hicn Multiple Benes S3 data. */
  SAMPLE_HICN_MULT_BENES(
      resourceUrl("rif-static-samples/sample-hicn-mult-bene-beneficiaries.txt"),
      RifFileType.BENEFICIARY,
      10),
  /** Synthetic Hicn Multiple Beneficiary History S3 data. */
  SAMPLE_HICN_MULT_BENES_BENEFICIARY_HISTORY(
      resourceUrl("rif-static-samples/sample-hicn-mult-bene-beneficiaryhistory.txt"),
      RifFileType.BENEFICIARY_HISTORY,
      7);

  /** The Resource URL Supplier for the different RIF files. */
  private final Supplier<URL> resourceUrlSupplier;
  /** The Rif File Type of the beneficiary files (EX: Beneficiary, Carrier, etc). */
  private final RifFileType rifFileType;
  /** The Record Count of the beneficiaries/claims/drug events in the RIF file. */
  private final int recordCount;
  /** The Resource URL of the resource's contents. */
  private URL resourceUrl;

  /**
   * Enum constant constructor.
   *
   * @param resourceUrlSupplier the value to use for {@link #getResourceSupplier()}
   * @param rifFileType the value to use for {@link #getRifFileType()}
   * @param recordCount the value to use for {@link #getRecordCount()}. If the supplied value is
   *     negative the size will be computed by scanning the file.
   */
  private StaticRifResource(
      Supplier<URL> resourceUrlSupplier, RifFileType rifFileType, int recordCount) {
    this.resourceUrlSupplier = resourceUrlSupplier;
    this.rifFileType = rifFileType;
    if (recordCount < 0) {
      this.recordCount = computeRecordCount();
    } else {
      this.recordCount = recordCount;
    }
  }

  /**
   * Gets the {@link #resourceUrl}.
   *
   * @return the {@link URL} to the resource's contents
   */
  public synchronized URL getResourceUrl() {
    if (resourceUrl == null) resourceUrl = resourceUrlSupplier.get();

    return resourceUrl;
  }

  /**
   * Gets the {@link #rifFileType}.
   *
   * @return the {@link RifFileType} of the RIF file
   */
  public RifFileType getRifFileType() {
    return rifFileType;
  }

  /**
   * Gets the {@link #recordCount}.
   *
   * @return the number of beneficiaries/claims/drug events in the RIF file excluding line items
   */
  public int getRecordCount() {
    return recordCount;
  }

  /**
   * Compute the number of records in the RIF file. Takes account of the configured id column so
   * that, e.g., the count would return the count of claims rather than the count of all claim
   * lines.
   *
   * @return the count of records
   */
  private int computeRecordCount() {
    RifFile file = toRifFile();
    String idColumn = null;
    if (getRifFileType().getIdColumn() != null) {
      idColumn = getRifFileType().getIdColumn().toString();
    }
    try {
      Iterable<CSVRecord> records =
          CSVFormat.RFC4180
              .withDelimiter('|')
              .withHeader()
              .parse(new InputStreamReader(file.open(), file.getCharset()));
      Set<String> uniqueIds = new HashSet<>();
      int i = 0;
      for (CSVRecord record : records) {
        if (idColumn != null) {
          uniqueIds.add(record.get(idColumn));
        } else {
          uniqueIds.add(Integer.toString(i++));
        }
      }
      return uniqueIds.size();
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Unable to open resource: " + resourceUrlSupplier.get().toString(), e);
    }
  }

  /**
   * Returns the Static Rif file as a {@link RifFile}.
   *
   * @return a {@link RifFile} based on this {@link StaticRifResource}
   */
  public RifFile toRifFile() {
    return new StaticRifFile(this);
  }

  /**
   * Gets the {@link Supplier} for the {@link URL} to the resource's contents.
   *
   * @param resourceName the name of the resource on the classpath (as might be passed to {@link
   *     ClassLoader#getResource(String)})
   * @return the resource url
   */
  private static Supplier<URL> resourceUrl(String resourceName) {
    return () -> {
      URL resource = Thread.currentThread().getContextClassLoader().getResource(resourceName);
      if (resource == null)
        throw new IllegalArgumentException("Unable to find resource: " + resourceName);

      return resource;
    };
  }

  /**
   * Get local copy of S3 data.
   *
   * @param dataSetLocation the {@link TestDataSetLocation} of the file to get a local copy of
   * @param fileName the name of the specific file in the specified {@link TestDataSetLocation} to
   *     get a local copy of, e.g. "beneficiaries.rif"
   * @return a {@link URL} to a local copy of the specified test data file from S3
   */
  private static Supplier<URL> localCopyOfS3Data(
      TestDataSetLocation dataSetLocation, String fileName) {
    return () -> {
      // Find the build output `target` directory.
      Path targetDir = Paths.get(".", "bfd-model-rif-samples", "target");
      if (!Files.exists(targetDir)) targetDir = Paths.get("..", "bfd-model-rif-samples", "target");
      if (!Files.exists(targetDir)) targetDir = Paths.get(".", "target");
      if (!Files.exists(targetDir))
        throw new IllegalStateException(
            "Unable to find directory: " + targetDir.toAbsolutePath().toString());

      // Build the path that the resources will be downloaded to.
      Path resourceDir =
          targetDir
              .resolve("test-data-from-s3")
              .resolve(dataSetLocation.getS3BucketName())
              .resolve(dataSetLocation.getS3KeyPrefix().replaceAll(":", "-"));
      Path resourceLocalCopy = resourceDir.resolve(fileName);

      /*
       * Implementation note: we have to carefully leverage
       * synchronization to ensure that we don't end up with multiple
       * copies of the same file. To avoid pegging dev systems, it's also
       * best to ensure that we're only grabbing one file at a time.
       * Locking on the static class object accomplishes these goals.
       */
      synchronized (StaticRifResource.class) {
        // Ensure the directory exists.
        if (!Files.exists(resourceDir)) {
          try {
            Files.createDirectories(resourceDir);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        }

        // Download the file, if needed.
        if (!Files.exists(resourceLocalCopy)) {
          downloadFromS3(dataSetLocation, fileName, resourceLocalCopy);
        }
      }

      // We now know the file exists, so return it.
      try {
        return resourceLocalCopy.toUri().toURL();
      } catch (MalformedURLException e) {
        throw new BadCodeMonkeyException(e);
      }
    };
  }

  /**
   * Downloads the specified S3 object to the specified local path.
   *
   * @param dataSetLocation the {@link TestDataSetLocation} of the S3 object to download
   * @param fileName the name of the specific object/file to be downloaded
   * @param downloadPath the {@link Path} to download the S3 object to
   */
  private static void downloadFromS3(
      TestDataSetLocation dataSetLocation, String fileName, Path downloadPath) {
    /*
     * To avoid dragging in the S3 client libraries, we require here that
     * the test data files be available publicly via HTTP.
     */

    URL s3DownloadUrl = remoteS3Data(dataSetLocation, fileName).get();
    download(s3DownloadUrl, downloadPath);
  }

  /**
   * Copies the contents of the specified {@link URL} to the specified local {@link Path}.
   *
   * @param url the {@link URL} to download the contents of
   * @param localPath the local {@link Path} to write to
   */
  private static void download(URL url, Path localPath) {
    FileOutputStream outputStream = null;
    try {
      ReadableByteChannel channel = Channels.newChannel(url.openStream());
      outputStream = new FileOutputStream(localPath.toFile());
      outputStream.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
    } catch (IOException e) {
      throw new UncheckedIOException("Unable to download URL: " + url, e);
    } finally {
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      }
    }
  }

  /**
   * Get Remote S3 data.
   *
   * @param dataSetLocation the {@link TestDataSetLocation} of the file to get a local copy of
   * @param fileName the name of the specific file in the specified {@link TestDataSetLocation} to
   *     get a local copy of, e.g. "beneficiaries.rif"
   * @return a {@link URL} that can be used to download/stream the specified test data file from S3
   */
  private static Supplier<URL> remoteS3Data(TestDataSetLocation dataSetLocation, String fileName) {
    return () -> {
      try {
        return new URL(
            String.format(
                "http://%s.s3.amazonaws.com/%s/%s",
                dataSetLocation.getS3BucketName(), dataSetLocation.getS3KeyPrefix(), fileName));
      } catch (MalformedURLException e) {
        throw new BadCodeMonkeyException(e);
      }
    };
  }

  /**
   * A simple app driver that can be run to verify the record counts for each {@link
   * StaticRifResource}.
   *
   * @param args (not used)
   * @throws Exception Any {@link Exception}s encountered will cause this mini-app to terminate.
   */
  public static void main(String[] args) throws Exception {
    /*
     * Note: Because of the SAMPLE_C files' large size, this will take HOURS
     * to run.
     */

    for (StaticRifResource resource : StaticRifResource.values()) {
      Set<String> uniqueIds = new HashSet<>();
      Path tempDownloadPath = null;
      InputStream tempDownloadStream = null;
      try {
        tempDownloadPath = Files.createTempFile("bfd-test-data-", ".rif");
        download(resource.getResourceUrl(), tempDownloadPath);

        tempDownloadStream =
            new BufferedInputStream(new FileInputStream(tempDownloadPath.toFile()));
        CSVParser parser =
            RifParsingUtils.createCsvParser(
                RifParsingUtils.CSV_FORMAT, tempDownloadStream, StandardCharsets.UTF_8);
        parser.forEach(
            r -> {
              if (resource.getRifFileType().getIdColumn() != null)
                uniqueIds.add(r.get(resource.getRifFileType().getIdColumn()));
              else uniqueIds.add("" + r.getRecordNumber());
            });
      } finally {
        if (tempDownloadPath != null) Files.deleteIfExists(tempDownloadPath);
        if (tempDownloadStream != null) tempDownloadStream.close();
      }
      System.out.println(String.format("%s: %d", resource.name(), uniqueIds.size()));
    }
  }
}
