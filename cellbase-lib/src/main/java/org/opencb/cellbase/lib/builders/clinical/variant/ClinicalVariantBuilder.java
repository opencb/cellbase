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

    private final Path clinicalVariantFolder;
    private final String assembly;
    private final Path genomeSequenceFilePath;
    private boolean normalize;

    private final CellBaseConfiguration configuration;

    public ClinicalVariantBuilder(Path clinicalVariantFolder, boolean normalize, Path genomeSequenceFilePath,
                                  String assembly, CellBaseConfiguration configuration, CellBaseSerializer serializer) {
        super(serializer);
        this.clinicalVariantFolder = clinicalVariantFolder;
        this.normalize = normalize;
        this.genomeSequenceFilePath = genomeSequenceFilePath;
        this.assembly = assembly;
        this.configuration = configuration;
    }

    public void parse() throws IOException, RocksDBException, CellBaseException {
        RocksDB rdb = null;
        Options dbOption = null;
        String dbLocation = null;

        try {
            Object[] dbConnection = getDBConnection(clinicalVariantFolder.toString() + "/integration.idx", true);
            rdb = (RocksDB) dbConnection[0];
            dbOption = (Options) dbConnection[1];
            dbLocation = (String) dbConnection[2];

            // COSMIC
            // IMPORTANT: COSMIC must be indexed first (before ClinVar, IARC TP53, DOCM, HGMD,...)!!!
            Path cosmicFile = clinicalVariantFolder.resolve(configuration.getDownload().getCosmic().getFiles().get(0));
            if (cosmicFile != null && Files.exists(cosmicFile)) {
                CosmicIndexer cosmicIndexer = new CosmicIndexer(cosmicFile, configuration.getDownload().getCosmic().getVersion(),
                        normalize, genomeSequenceFilePath, assembly, rdb);
                cosmicIndexer.index();
            } else {
                throw new CellBaseException("Could not build clinical variants: the COSMIC file " + cosmicFile + " is missing");
            }

            // ClinVar
            Path clinvarXMLFile = getPathFromHost(configuration.getDownload().getClinvar().getHost());
            Path clinvarSummaryFile = getPathFromHost(configuration.getDownload().getClinvarSummary().getHost());
            Path clinvarVariationAlleleFile = getPathFromHost(configuration.getDownload().getClinvarVariationAllele().getHost());
            Path clinvarEFOFile = getPathFromHost(configuration.getDownload().getClinvarEfoTerms().getHost());
            ClinVarIndexer clinvarIndexer = new ClinVarIndexer(clinvarXMLFile.getParent().resolve("clinvar_chunks"), clinvarSummaryFile,
                    clinvarVariationAlleleFile, clinvarEFOFile, configuration.getDownload().getClinvar().getVersion(), normalize,
                    genomeSequenceFilePath, assembly, rdb);
            clinvarIndexer.index();

            // HGMD
            Path hgmdFile = clinicalVariantFolder.resolve(configuration.getDownload().getHgmd().getFiles().get(0));
            if (hgmdFile != null && Files.exists(hgmdFile)) {
                HGMDIndexer hgmdIndexer = new HGMDIndexer(hgmdFile, configuration.getDownload().getHgmd().getVersion(), normalize,
                        genomeSequenceFilePath, assembly, rdb);
                hgmdIndexer.index();
            } else {
                throw new CellBaseException("Could not build clinical variants: the HGMD file " + hgmdFile + " is missing");
            }

            // GWAS catalog
            Path gwasFile = clinicalVariantFolder.resolve(Paths.get(configuration.getDownload().getGwasCatalog().getHost()).getFileName());
            if (gwasFile != null && Files.exists(gwasFile)) {
                Path dbsnpFile = clinicalVariantFolder.resolve(configuration.getDownload().getHgmd().getFiles().get(0));
                if (dbsnpFile != null && Files.exists(dbsnpFile)) {
                    Path tabixFile = Paths.get(dbsnpFile.toAbsolutePath() + ".tbi");
                    if (tabixFile != null && Files.exists(tabixFile)) {
                        GwasIndexer gwasIndexer = new GwasIndexer(gwasFile, dbsnpFile, genomeSequenceFilePath, assembly, rdb);
                        gwasIndexer.index();
                    } else {
                        throw new CellBaseException("Could not build clinical variants: the dbSNP tabix file " + tabixFile + " is missing");
                    }
                } else {
                    throw new CellBaseException("Could not build clinical variants: the dbSNP file " + dbsnpFile + " is missing");
                }
            } else {
                throw new CellBaseException("Could not build clinical variants: the GWAS catalog file " + gwasFile + " is missing");
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

    private Path getPathFromHost(String host) throws CellBaseException {
        Path path = clinicalVariantFolder.resolve(Paths.get(host).getFileName());
        if (!Files.exists(path)) {
            throw new CellBaseException("Could not build clinical variants. The file " + path + " is missing");
        }
        return path;
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
