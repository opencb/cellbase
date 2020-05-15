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


package org.opencb.cellbase.lib.builders;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GeneBuilderIndexer {

    private RocksDB rocksdb;
    private static final String DESCRIPTION_SUFFIX = "_description";
    private RocksDbManager rocksDbManager;
    protected Logger logger;
    private Options dbOption = null;
    private String dbLocation = null;

    public GeneBuilderIndexer(Path geneDirectoryPath) {
        init(geneDirectoryPath);
    }

    private void init(Path geneDirectoryPath) {
        rocksDbManager = new RocksDbManager();
        dbLocation = geneDirectoryPath.toString() + "/integration.idx";
        rocksdb = rocksDbManager.getDBConnection(dbLocation);
        dbOption = new Options().setCreateIfMissing(true);

        logger = LoggerFactory.getLogger(this.getClass());
    }

    public void index(Path geneDescriptionFile, Path xrefsFile, Path uniprotIdMappingFile)
            throws IOException, RocksDBException {
        logger.error("loading gene descriptions");
        indexDescriptions(geneDescriptionFile);
        logger.error("DONE -- loading gene descriptions");
    }

    public String getDescription(String id) throws RocksDBException {
        String key = id + DESCRIPTION_SUFFIX;
        byte[] value = rocksdb.get(key.getBytes());
        if (value != null) {
            return new String(value);
        }
        return null;
    }

//    private void indexTfbs(path) {
//        rocksdb.put(key + DESCRIPTION_TFBS, value);
//    }

    private void indexDescriptions(Path geneDescriptionFile) throws IOException, RocksDBException {
        logger.info("Loading gene description data...");
        String[] fields;
        if (geneDescriptionFile != null && Files.exists(geneDescriptionFile) && Files.size(geneDescriptionFile) > 0) {
            List<String> lines = Files.readAllLines(geneDescriptionFile, Charset.forName("ISO-8859-1"));
            for (String line : lines) {
                fields = line.split("\t", -1);
                rocksDbManager.update(rocksdb, fields[0] + DESCRIPTION_SUFFIX, fields[1]);
            }
        } else {
            logger.warn("Gene description file " + geneDescriptionFile + " not found");
            logger.warn("Gene description data not loaded");
        }
    }

    protected void close() throws IOException {
        rocksDbManager.closeIndex(rocksdb, dbOption, dbLocation);
    }
}
