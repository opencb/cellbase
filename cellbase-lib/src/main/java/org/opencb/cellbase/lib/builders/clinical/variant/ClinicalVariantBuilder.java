/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.lib.builders.clinical.variant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.lib.builders.AbstractBuilder;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.opencb.cellbase.lib.EtlCommons.*;

/**
 * Created by fjlopez on 26/09/16.
 */
public class ClinicalVariantBuilder extends AbstractBuilder {

    private final Path clinicalVariantPath;
    private final String assembly;
    private final Path genomeSequenceFilePath;
    private boolean normalize;

    private Path clinvarFullReleaseFilePath;
    private Path clinvarSummaryFilePath;
    private Path clinvarVariationAlleleFilePath;
    private Path clinvarEFOFilePath;
    private Path cosmicFilePath;
    private Path hgmdFilePath;
    private Path gwasFilePath;
    private Path gwasDbSnpFilePath;

    private final CellBaseConfiguration configuration;

    public ClinicalVariantBuilder(Path clinicalVariantFolder, boolean normalize, Path genomeSequenceFilePath,
                                  String assembly, CellBaseConfiguration configuration, CellBaseSerializer serializer) {
        super(serializer);
        this.clinicalVariantPath = clinicalVariantFolder;
        this.normalize = normalize;
        this.genomeSequenceFilePath = genomeSequenceFilePath;
        this.assembly = assembly;
        this.configuration = configuration;
    }

    public void check() throws CellBaseException, IOException {
        if (checked) {
            return;
        }

        logger.info(CHECKING_BEFORE_BUILDING_LOG_MESSAGE, getDataName(CLINICAL_VARIANT_DATA));

        // Sanity check
        checkDirectory(clinicalVariantPath, getDataName(CLINICAL_VARIANT_DATA));
        if (!Files.exists(serializer.getOutdir())) {
            try {
                Files.createDirectories(serializer.getOutdir());
            } catch (IOException e) {
                throw new CellBaseException("Error creating folder " + serializer.getOutdir(), e);
            }
        }

        // Check genome file
        logger.info("Checking genome FASTA file ...");
        if (!Files.exists(genomeSequenceFilePath)) {
            throw new CellBaseException("Genome file path does not exist " + genomeSequenceFilePath);
        }
        logger.info(OK_LOG_MESSAGE);
        logger.info("Checking index for genome FASTA file ...");
        getIndexFastaReferenceGenome(genomeSequenceFilePath);
        logger.info(OK_LOG_MESSAGE);

        // Check ClinVar files
        clinvarFullReleaseFilePath = checkFile(CLINVAR_DATA, configuration.getDownload().getClinvar(), CLINVAR_FULL_RELEASE_FILE_ID,
                clinicalVariantPath).toPath();
        clinvarSummaryFilePath = checkFile(CLINVAR_DATA, configuration.getDownload().getClinvar(), CLINVAR_SUMMARY_FILE_ID,
                clinicalVariantPath).toPath();
        clinvarVariationAlleleFilePath = checkFile(CLINVAR_DATA, configuration.getDownload().getClinvar(), CLINVAR_ALLELE_FILE_ID,
                clinicalVariantPath).toPath();
        clinvarEFOFilePath = checkFile(CLINVAR_DATA, configuration.getDownload().getClinvar(), CLINVAR_EFO_TERMS_FILE_ID,
                clinicalVariantPath).toPath();

        // Check COSMIC file
        cosmicFilePath = checkFiles(COSMIC_DATA, clinicalVariantPath, 1).get(0).toPath();

        // Check HGMD file
        hgmdFilePath = checkFiles(HGMD_DATA, clinicalVariantPath, 1).get(0).toPath();

        // Check GWAS files
        gwasFilePath = checkFiles(GWAS_DATA, clinicalVariantPath, 1).get(0).toPath();
        String dbSnpFilename = Paths.get(configuration.getDownload().getGwasCatalog().getFiles().get(GWAS_DBSNP_FILE_ID)).getFileName()
                .toString();
        gwasDbSnpFilePath = clinicalVariantPath.resolve(dbSnpFilename);
        if (!Files.exists(gwasDbSnpFilePath)) {
            throw new CellBaseException("Could not build clinical variants: the dbSNP file " + dbSnpFilename + " is missing at "
                    + clinicalVariantPath);
        }
        if (!Files.exists(clinicalVariantPath.resolve(dbSnpFilename + TBI_EXTENSION))) {
            throw new CellBaseException("Could not build clinical variants: the dbSNP tabix file " + dbSnpFilename + TBI_EXTENSION
                    + " is missing at " + clinicalVariantPath);
        }

        logger.info(CHECKING_DONE_BEFORE_BUILDING_LOG_MESSAGE, getDataName(CLINICAL_VARIANT_DATA));
        checked = true;
    }

    public void parse() throws IOException, RocksDBException, CellBaseException {
        check();

        // Prepare ClinVar chunk files before building (if necessary)
        Path chunksPath = serializer.getOutdir().resolve(CLINVAR_CHUNKS_SUBDIRECTORY);
        if (Files.notExists(chunksPath)) {
            Files.createDirectories(chunksPath);
            logger.info("Splitting CliVar file {} in {} ...", clinvarFullReleaseFilePath, chunksPath);
            splitClinvar(clinvarFullReleaseFilePath, chunksPath);
            logger.info(OK_LOG_MESSAGE);
        }

        RocksDB rdb = null;
        Options dbOption = null;
        String dbLocation = null;

        try {
            Object[] dbConnection = getDBConnection(clinicalVariantPath.toString() + "/integration.idx", true);
            rdb = (RocksDB) dbConnection[0];
            dbOption = (Options) dbConnection[1];
            dbLocation = (String) dbConnection[2];

            // COSMIC
            // IMPORTANT: COSMIC must be indexed first (before ClinVar, HGMD,...)!!!
            CosmicIndexer cosmicIndexer = new CosmicIndexer(cosmicFilePath, configuration.getDownload().getCosmic().getVersion(),
                    normalize, genomeSequenceFilePath, assembly, rdb);
            cosmicIndexer.index();

            // ClinVar
            ClinVarIndexer clinvarIndexer = new ClinVarIndexer(serializer.getOutdir().resolve(CLINVAR_CHUNKS_SUBDIRECTORY),
                    clinvarSummaryFilePath, clinvarVariationAlleleFilePath, clinvarEFOFilePath, configuration.getDownload().getClinvar()
                    .getVersion(), normalize, genomeSequenceFilePath, assembly, rdb);
            clinvarIndexer.index();

            // HGMD
            HGMDIndexer hgmdIndexer = new HGMDIndexer(hgmdFilePath, configuration.getDownload().getHgmd().getVersion(), normalize,
                    genomeSequenceFilePath, assembly, rdb);
            hgmdIndexer.index();

            // GWAS catalog
            GwasIndexer gwasIndexer = new GwasIndexer(gwasFilePath, gwasDbSnpFilePath, genomeSequenceFilePath, assembly, rdb);
            gwasIndexer.index();

            // Serialize
            serializeRDB(rdb);
            closeIndex(rdb, dbOption, dbLocation);
            serializer.close();
        } catch (Exception e) {
            closeIndex(rdb, dbOption, dbLocation);
            serializer.close();
            throw e;
        }
    }

    private void serializeRDB(RocksDB rdb) throws IOException {
        // DO NOT change the name of the rocksIterator variable - for some unexplainable reason Java VM crashes if it's
        // named "iterator"
        RocksIterator rocksIterator = rdb.newIterator();

        ObjectMapper mapper = new ObjectMapper();
        logger.info("Reading from RocksDB index and serializing to {}.json.gz", serializer.getOutdir().resolve(serializer.getFileName()));
        int counter = 0;
        for (rocksIterator.seekToFirst(); rocksIterator.isValid(); rocksIterator.next()) {
            Variant variant = parseVariantFromVariantId(new String(rocksIterator.key()));
            if (variant != null) {
                VariantAnnotation variantAnnotation = mapper.readValue(rocksIterator.value(), VariantAnnotation.class);
                variant.setAnnotation(variantAnnotation);
                serializer.serialize(variant);
                counter++;
                if (counter % 10000 == 0) {
                    logger.info("{} written", counter);
                }
            }
        }
        serializer.close();
        logger.info("Done.");
    }

    private Variant parseVariantFromVariantId(String variantId) {
        try {
            String[] parts = variantId.split(":", -1); // -1 to include empty fields
            if (parts[1].contains("-")) {
                String[] pos = parts[1].split("-");
                return new Variant(parts[0].trim(), Integer.parseInt(pos[0].trim()), Integer.parseInt(pos[1].trim()), parts[2], parts[3]);
            } else {
                return new Variant(parts[0].trim(), Integer.parseInt(parts[1].trim()), parts[2], parts[3]);
            }
        } catch (Exception e) {
            logger.warn("{}. Impossible to create the variant object from the variant ID: {}", e.getMessage(), variantId);
            return null;
        }
    }

    private void closeIndex(RocksDB rdb, Options dbOption, String dbLocation) throws IOException {
        if (rdb != null) {
            rdb.close();
        }
        if (dbOption != null) {
            dbOption.dispose();
        }
        if (dbLocation != null && Files.exists(Paths.get(dbLocation))) {
            org.apache.commons.io.FileUtils.deleteDirectory(new File(dbLocation));
        }
    }

    private Object[] getDBConnection(String dbLocation, boolean forceCreate) {
        boolean indexingNeeded = forceCreate || !Files.exists(Paths.get(dbLocation));
        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();
        // the Options class contains a set of configurable DB options
        // that determines the behavior of a database.
        Options options = new Options().setCreateIfMissing(true);

//        options.setMaxBackgroundCompactions(4);
//        options.setMaxBackgroundFlushes(1);
//        options.setCompressionType(CompressionType.NO_COMPRESSION);
//        options.setMaxOpenFiles(-1);
//        options.setIncreaseParallelism(4);
//        options.setCompactionStyle(CompactionStyle.LEVEL);
//        options.setLevelCompactionDynamicLevelBytes(true);

        RocksDB db = null;
        try {
            // a factory method that returns a RocksDB instance
            if (indexingNeeded) {
                db = RocksDB.open(options, dbLocation);
            } else {
                db = RocksDB.openReadOnly(options, dbLocation);
            }
            // do something
        } catch (RocksDBException e) {
            // do some error handling
            e.printStackTrace();
            System.exit(1);
        }

        return new Object[]{db, options, dbLocation, indexingNeeded};

    }

    private void splitClinvar(Path clinvarXmlFilePath, Path splitOutdirPath) throws IOException {
        PrintWriter pw = null;
        try (BufferedReader br = FileUtils.newBufferedReader(clinvarXmlFilePath)) {
            StringBuilder header = new StringBuilder();
            boolean beforeEntry = true;
            boolean inEntry = false;
            int count = 0;
            int chunk = 0;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("<ClinVarSet ")) {
                    inEntry = true;
                    beforeEntry = false;
                    if (count % 10000 == 0) {
                        pw = new PrintWriter(new FileOutputStream(splitOutdirPath.resolve("chunk_" + chunk + ".xml").toFile()));
                        pw.println(header.toString().trim());
                    }
                    count++;
                }

                if (beforeEntry) {
                    header.append(line).append("\n");
                }

                if (inEntry) {
                    pw.println(line);
                }

                if (line.trim().startsWith("</ClinVarSet>")) {
                    inEntry = false;
                    if (count % 10000 == 0) {
                        if (pw != null) {
                            pw.print("</ReleaseSet>");
                            pw.close();
                        }
                        chunk++;
                    }
                }
            }
            if (pw != null) {
                pw.print("</ReleaseSet>");
                pw.close();
            }
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }
}
