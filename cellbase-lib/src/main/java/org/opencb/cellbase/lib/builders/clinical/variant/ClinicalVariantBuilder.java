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
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.lib.builders.CellBaseBuilder;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by fjlopez on 26/09/16.
 */
public class ClinicalVariantBuilder extends CellBaseBuilder {

    private final Path clinvarXMLFile;
    private final Path clinvarSummaryFile;
    private final Path clinvarVariationAlleleFile;
    private final Path clinvarEFOFile;
    private final Path cosmicFile;
    private final Path gwasFile;
    private final Path dbsnpFile;
    private final String assembly;
    private final Path iarctp53GermlineFile;
    private final Path iarctp53SomaticFile;
    private final Path iarctp53GermlineReferencesFile;
    private final Path iarctp53SomaticReferencesFile;
    private final Path genomeSequenceFilePath;
    private final Path docmFile;
    private final Path hgmdFile;
    private boolean normalize = true;

    public ClinicalVariantBuilder(Path clinicalVariantFolder, boolean normalize, Path genomeSequenceFilePath,
                                  String assembly, CellBaseSerializer serializer) {
        this(clinicalVariantFolder.resolve(EtlCommons.CLINVAR_XML_FILE),
                clinicalVariantFolder.resolve(EtlCommons.CLINVAR_SUMMARY_FILE),
                clinicalVariantFolder.resolve(EtlCommons.CLINVAR_VARIATION_ALLELE_FILE),
                clinicalVariantFolder.resolve(EtlCommons.CLINVAR_EFO_FILE),
                clinicalVariantFolder.resolve(EtlCommons.COSMIC_FILE),
                clinicalVariantFolder.resolve(EtlCommons.GWAS_FILE),
                clinicalVariantFolder.resolve(EtlCommons.DBSNP_FILE),
                clinicalVariantFolder.resolve("datasets/" + EtlCommons.IARCTP53_GERMLINE_FILE),
                clinicalVariantFolder.resolve("datasets/" + EtlCommons.IARCTP53_GERMLINE_REFERENCES_FILE),
                clinicalVariantFolder.resolve("datasets/" + EtlCommons.IARCTP53_SOMATIC_FILE),
                clinicalVariantFolder.resolve("datasets/" + EtlCommons.IARCTP53_SOMATIC_REFERENCES_FILE),
                clinicalVariantFolder.resolve(EtlCommons.DOCM_FILE),
                clinicalVariantFolder.resolve(EtlCommons.HGMD_FILE),
                normalize,
                genomeSequenceFilePath, assembly, serializer);
    }

    public ClinicalVariantBuilder(Path clinvarXMLFile, Path clinvarSummaryFile, Path clinvarVariationAlleleFile,
                                  Path clinvarEFOFile, Path cosmicFile, Path gwasFile, Path dbsnpFile,
                                  Path iarctp53GermlineFile, Path iarctp53GermlineReferencesFile,
                                  Path iarctp53SomaticFile, Path iarctp53SomaticReferencesFile, Path docmFile, Path hgmdFile,
                                  boolean normalize, Path genomeSequenceFilePath, String assembly,
                                  CellBaseSerializer serializer) {
        super(serializer);
        this.clinvarXMLFile = clinvarXMLFile;
        this.clinvarSummaryFile = clinvarSummaryFile;
        this.clinvarVariationAlleleFile = clinvarVariationAlleleFile;
        this.clinvarEFOFile = clinvarEFOFile;
        this.cosmicFile = cosmicFile;
        this.gwasFile = gwasFile;
        this.dbsnpFile = dbsnpFile;
        this.iarctp53GermlineFile = iarctp53GermlineFile;
        this.iarctp53GermlineReferencesFile = iarctp53GermlineReferencesFile;
        this.iarctp53SomaticFile = iarctp53SomaticFile;
        this.iarctp53SomaticReferencesFile = iarctp53SomaticReferencesFile;
        this.docmFile = docmFile;
        this.hgmdFile = hgmdFile;
        this.normalize = normalize;
        this.genomeSequenceFilePath = genomeSequenceFilePath;
        this.assembly = assembly;
    }

    public void parse() throws IOException, RocksDBException, CellBaseException {

        RocksDB rdb = null;
        Options dbOption = null;
        String dbLocation = null;

        try {
            Object[] dbConnection = getDBConnection(clinvarXMLFile.getParent().toString() + "/integration.idx", true);
            rdb = (RocksDB) dbConnection[0];
            dbOption = (Options) dbConnection[1];
            dbLocation = (String) dbConnection[2];

            // COSMIC
            // IMPORTANT: COSMIC must be indexed first (before ClinVar, IARC TP53, DOCM, HGMD,...)!!!
            if (this.cosmicFile != null && Files.exists(this.cosmicFile)) {
                CosmicIndexer cosmicIndexer = new CosmicIndexer(cosmicFile, normalize, genomeSequenceFilePath, assembly, rdb);
                cosmicIndexer.index();
            } else {
                logger.warn("Cosmic file {} missing. Skipping Cosmic data", cosmicFile);
            }

            // ClinVar
            if (this.clinvarXMLFile != null && this.clinvarSummaryFile != null
                    && this.clinvarVariationAlleleFile != null && Files.exists(clinvarXMLFile)
                    && Files.exists(clinvarSummaryFile) && Files.exists(clinvarVariationAlleleFile)) {
              ClinVarIndexer clinvarIndexer = new ClinVarIndexer(clinvarXMLFile.getParent().resolve("clinvar_chunks"), clinvarSummaryFile,
                        clinvarVariationAlleleFile, clinvarEFOFile, normalize, genomeSequenceFilePath, assembly, rdb);
                clinvarIndexer.index();
            } else {
                logger.warn("One or more of required ClinVar files are missing. Skipping ClinVar data.\n"
                        + "Please, ensure that these two files exist:\n"
                        + "{}\n"
                        + "{}", this.clinvarXMLFile.toString(), this.clinvarSummaryFile.toString());
            }

            // IARC TP53
            if (this.iarctp53GermlineFile != null && this.iarctp53SomaticFile != null
                    && Files.exists(iarctp53GermlineFile) && Files.exists(iarctp53SomaticFile)) {
                IARCTP53Indexer iarctp53Indexer = new IARCTP53Indexer(iarctp53GermlineFile,
                        iarctp53GermlineReferencesFile, iarctp53SomaticFile, iarctp53SomaticReferencesFile,
                        normalize, genomeSequenceFilePath, assembly, rdb);
                iarctp53Indexer.index();
            } else {
                logger.warn("One or more of required IARCTP53 files are missing. Skipping IARCTP53 data.");
            }

            // DOCM
            if (this.docmFile != null && Files.exists(docmFile)) {
                DOCMIndexer docmIndexer = new DOCMIndexer(docmFile, normalize, genomeSequenceFilePath, assembly, rdb);
                docmIndexer.index();
            } else {
                logger.warn("The DOCM file {} is missing. Skipping DOCM data.", docmFile);
            }

            // HGMD
            if (this.hgmdFile != null && Files.exists(hgmdFile)) {
                HGMDIndexer hgmdIndexer = new HGMDIndexer(hgmdFile, normalize, genomeSequenceFilePath, assembly, rdb);
                hgmdIndexer.index();
            } else {
                logger.warn("The HGMD file {} is missing. Skipping HGMD data.", hgmdFile);
            }

            // GWAS catalog
            if (gwasFile != null && Files.exists(gwasFile)) {
                if (dbsnpFile != null && Files.exists(dbsnpFile)) {
                    Path tabixFile = Paths.get(dbsnpFile.toAbsolutePath() + ".tbi");
                    if (tabixFile != null && Files.exists(tabixFile)) {
                        GwasIndexer gwasIndexer = new GwasIndexer(gwasFile, dbsnpFile, genomeSequenceFilePath, assembly, rdb);
                        gwasIndexer.index();
                    } else {
                        logger.warn("The dbSNP tabix file {} is missing. Skipping GWAS catalog data.", tabixFile);
                    }
                } else {
                    logger.warn("The dbSNP file {} is missing. Skipping GWAS catalog data.", dbsnpFile);
                }
            } else {
                logger.warn("The GWAS catalog file {} is missing. Skipping GWAS catalog data.", gwasFile);
            }

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
            logger.warn(e.getMessage() + ". Impossible to create the variant object from the variant ID: " + variantId);
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

}
