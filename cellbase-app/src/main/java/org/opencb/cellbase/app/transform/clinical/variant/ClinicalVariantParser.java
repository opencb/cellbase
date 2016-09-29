package org.opencb.cellbase.app.transform.clinical.variant;

import org.opencb.cellbase.app.cli.EtlCommons;
import org.opencb.cellbase.app.transform.CellBaseParser;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

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
    private final Path clinvarEFOFile;
    private final Path cosmicFile;
    private final Path gwasFile;
    private final Path dbsnpFile;
    private final String assembly;

    public ClinicalVariantParser(Path clinicalVariantFolder, String assembly, CellBaseSerializer serializer) {
        this(clinicalVariantFolder.resolve(EtlCommons.CLINVAR_XML_FILE),
                clinicalVariantFolder.resolve(EtlCommons.CLINVAR_SUMMARY_FILE),
                clinicalVariantFolder.resolve(EtlCommons.CLINVAR_EFO_FILE),
                clinicalVariantFolder.resolve(EtlCommons.COSMIC_FILE),
                clinicalVariantFolder.resolve(EtlCommons.GWAS_FILE),
                clinicalVariantFolder.resolve(EtlCommons.DBSNP_FILE), assembly, serializer);
    }

    public ClinicalVariantParser(Path clinvarXMLFile, Path clinvarSummaryFile, Path clinvarEFOFile,
                                 Path cosmicFile, Path gwasFile, Path dbsnpFile, String assembly,
                                 CellBaseSerializer serializer) {
        super(serializer);
        this.clinvarXMLFile = clinvarXMLFile;
        this.clinvarSummaryFile = clinvarSummaryFile;
        this.clinvarEFOFile = clinvarEFOFile;
        this.cosmicFile = cosmicFile;
        this.gwasFile = gwasFile;
        this.dbsnpFile = dbsnpFile;
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
            if (this.clinvarXMLFile != null) {
                ClinVarIndexer clinvarIndexer = new ClinVarIndexer(clinvarXMLFile, clinvarSummaryFile,
                        clinvarEFOFile, assembly, rdb);
                clinvarIndexer.index();
            }
            if (this.cosmicFile != null) {
                CosmicIndexer cosmicIndexer = new CosmicIndexer(cosmicFile, rdb);
                cosmicIndexer.index();
            }
            if (this.gwasFile != null) {
                GwasIndexer cosmicIndexer = new GwasIndexer(gwasFile, rdb);
                cosmicIndexer.index();
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
