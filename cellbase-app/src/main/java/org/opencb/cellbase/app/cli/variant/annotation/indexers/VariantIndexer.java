package org.opencb.cellbase.app.cli.variant.annotation.indexers;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.biodata.formats.variant.io.VariantReader;
import org.opencb.biodata.models.variant.Variant;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public abstract class VariantIndexer {

    protected ObjectMapper jsonObjectMapper;
    protected ObjectWriter jsonObjectWriter;

    private final Logger logger = LoggerFactory.getLogger(VariantIndexer.class);

    private final VariantReader variantReader;
    private final boolean forceCreate;
    private final int maxOpenFiles;

    protected RocksDB dbIndex;
    private Options dbOption;
    private String dbLocation;
    private boolean indexingNeeded;

    public VariantIndexer(VariantReader variantReader, int maxOpenFiles, boolean forceCreate) {
        this.variantReader = variantReader;
        this.forceCreate = forceCreate;
        this.maxOpenFiles = maxOpenFiles;

        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectWriter = jsonObjectMapper.writer();

    }

    public RocksDB getDbIndex() {
        return dbIndex;
    }

    public String getDbLocation() {
        return dbLocation;
    }

    public void open() {
        Object[] dbConnection = getDBConnection();
        dbIndex = (RocksDB) dbConnection[0];
        dbOption = (Options) dbConnection[1];
        dbLocation = (String) dbConnection[2];
        indexingNeeded = (boolean) dbConnection[3];

        variantReader.open();
        variantReader.pre();

    }

    private Object[] getDBConnection() {
        String dbLocation = variantReader.getVariantFileMetadata().getPath() + ".idx";
        boolean indexingNeeded = forceCreate || !Files.exists(Paths.get(dbLocation));
        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();
        // the Options class contains a set of configurable DB options
        // that determines the behavior of a database.
        Options options = new Options().setCreateIfMissing(true);
        if (maxOpenFiles > 0) {
            options.setMaxOpenFiles(maxOpenFiles);
        }

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

    public void run() throws IOException, RocksDBException {
        if (indexingNeeded) {
            int recordCounter = 0;
            List<Variant> variantList = variantReader.read();
            while (!variantList.isEmpty()) {
                try {
                    updateIndex(variantList);
                } catch (IOException | RocksDBException e) {
                    e.printStackTrace();
                    throw e;
                } catch (Exception e) {
                    if (recordCounter >= 0 && variantList != null) {
                        logger.error("Error fond while trying to parse {}",
                                variantList.stream().map(Variant::toString).collect(Collectors.joining()));
                    } else {
                        logger.error("Error found while parsing {}", variantReader.getVariantFileMetadata().getPath());
                    }
                    throw e;
                }

                recordCounter++;
                if (recordCounter % 100000 == 0) {
                    logger.info("{} records indexed", recordCounter);
                }
                variantList = variantReader.read();
            }
        } else {
            logger.info("Index already present. Skipping index creation for {}",
                    variantReader.getVariantFileMetadata().getPath());
        }

    }

    protected abstract void updateIndex(List<Variant> variantList) throws IOException, RocksDBException;

    public void close() {
        dbIndex.close();
        dbOption.dispose();

        variantReader.post();
        variantReader.close();
    }
}
