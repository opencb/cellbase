/*
 * Copyright 2015-2019 OpenCB
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

package org.opencb.cellbase.app.transform.clinical.variant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.app.cli.EtlCommons;
import org.opencb.cellbase.app.transform.CellBaseParser;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
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
public class ClinicalVariantParser extends CellBaseParser {

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
    private boolean normalize = true;


    public ClinicalVariantParser(Path clinicalVariantFolder, boolean normalize, Path genomeSequenceFilePath,
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
                normalize,
                genomeSequenceFilePath, assembly, serializer);
    }

    public ClinicalVariantParser(Path clinvarXMLFile, Path clinvarSummaryFile, Path clinvarVariationAlleleFile,
                                 Path clinvarEFOFile, Path cosmicFile, Path gwasFile, Path dbsnpFile,
                                 Path iarctp53GermlineFile, Path iarctp53GermlineReferencesFile,
                                 Path iarctp53SomaticFile, Path iarctp53SomaticReferencesFile, Path docmFile,
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
        this.normalize = normalize;
        this.genomeSequenceFilePath = genomeSequenceFilePath;
        this.assembly = assembly;
    }

    public void parse() throws IOException, RocksDBException {

        RocksDB rdb = null;
        Options dbOption = null;
        String dbLocation = null;

        try {
            Object[] dbConnection = getDBConnection(clinvarXMLFile.getParent().toString() + "/integration.idx", true);
            rdb = (RocksDB) dbConnection[0];
            dbOption = (Options) dbConnection[1];
            dbLocation = (String) dbConnection[2];

            if (this.clinvarXMLFile != null && this.clinvarSummaryFile != null
                    && this.clinvarVariationAlleleFile != null && Files.exists(clinvarXMLFile)
                    && Files.exists(clinvarSummaryFile) && Files.exists(clinvarVariationAlleleFile)) {
                ClinVarIndexer clinvarIndexer = new ClinVarIndexer(clinvarXMLFile, clinvarSummaryFile,
                        clinvarVariationAlleleFile, clinvarEFOFile, normalize, genomeSequenceFilePath, assembly, rdb);
                clinvarIndexer.index();
            } else {
                logger.warn("One or more of required ClinVar files are missing. Skipping ClinVar data.\n"
                        + "Please, ensure that these two files exist:\n"
                        + "{}\n"
                        + "{}", this.clinvarXMLFile.toString(), this.clinvarSummaryFile.toString());
            }

            if (this.cosmicFile != null && Files.exists(this.cosmicFile)) {
                CosmicIndexer cosmicIndexer = new CosmicIndexer(cosmicFile, normalize, genomeSequenceFilePath,
                        assembly, rdb);
                cosmicIndexer.index();
            } else {
                logger.warn("Cosmic file {} missing. Skipping Cosmic data", cosmicFile);
            }
            // TODO: write GWAS indexer as soon as it's needed (GRCh38 update)
//            if (this.gwasFile != null) {
//                GwasIndexer cosmicIndexer = new GwasIndexer(gwasFile, rdb);
//                cosmicIndexer.index();
//            }
            if (this.iarctp53GermlineFile != null && this.iarctp53SomaticFile != null
                    && Files.exists(iarctp53GermlineFile) && Files.exists(iarctp53SomaticFile)) {
                IARCTP53Indexer iarctp53Indexer = new IARCTP53Indexer(iarctp53GermlineFile,
                        iarctp53GermlineReferencesFile, iarctp53SomaticFile, iarctp53SomaticReferencesFile,
                        normalize, genomeSequenceFilePath, assembly, rdb);
                iarctp53Indexer.index();
            } else {
                logger.warn("One or more of required IARCTP53 files are missing. Skipping IARCTP53 data.");
            }

            if (this.docmFile != null && Files.exists(docmFile)) {
                DOCMIndexer docmIndexer = new DOCMIndexer(docmFile, normalize, genomeSequenceFilePath, assembly, rdb);
                docmIndexer.index();
            } else {
                logger.warn("The DOCM file {} is missing. Skipping DOCM data.", docmFile);
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
        logger.info("Reading from RoocksDB index and serializing to {}.json.gz",
                serializer.getOutdir().resolve(serializer.getFileName()));
        int counter = 0;
        for (rocksIterator.seekToFirst(); rocksIterator.isValid(); rocksIterator.next()) {
            VariantAnnotation variantAnnotation
                    = mapper.readValue(rocksIterator.value(), VariantAnnotation.class);
//            List<EvidenceEntry> evidenceEntryList
//                    = mapper.readValue(rocksIterator.value(), List.class);
            Variant variant = parseVariantFromVariantId(new String(rocksIterator.key()));
//            VariantAnnotation variantAnnotation = new VariantAnnotation();
//            variantAnnotation.setTraitAssociation(evidenceEntryList);
            variant.setAnnotation(variantAnnotation);
            serializer.serialize(variant);
            counter++;
            if (counter % 10000 == 0) {
                logger.info("{} written", counter);
            }
        }
        serializer.close();
        logger.info("Done.");
    }

    private Variant parseVariantFromVariantId(String variantId) {
        String[] parts = variantId.split(":", -1); // -1 to include empty fields
        return new Variant(parts[0].trim(), Integer.valueOf(parts[1].trim()), parts[2], parts[3]);
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
