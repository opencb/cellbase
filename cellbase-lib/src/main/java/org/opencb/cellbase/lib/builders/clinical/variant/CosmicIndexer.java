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

import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.biodata.formats.variant.cosmic.CosmicParser;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.lib.EtlCommons;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.IOException;
import java.nio.file.Path;


public class CosmicIndexer extends ClinicalIndexer {

    private final Path cosmicFile;
    private final String assembly;

    private static final String COSMIC_VERSION = "v95";

    public CosmicIndexer(Path cosmicFile, boolean normalize, Path genomeSequenceFilePath, String assembly, RocksDB rdb) throws IOException {
        super(genomeSequenceFilePath);

        this.cosmicFile = cosmicFile;
        this.normalize = normalize;
        this.assembly = assembly;
        this.rdb = rdb;
    }

    public void index() throws RocksDBException, CellBaseException {
        // Call COSMIC parser
        try {
            logger.info("Parsing cosmic file ...");
            CosmicIndexerCallback callback = new CosmicIndexerCallback(rdb, this);
            CosmicParser.parse(cosmicFile, COSMIC_VERSION, EtlCommons.COSMIC_DATA, assembly, callback);
        } catch (IOException | FileFormatException e) {
            throw new CellBaseException("Error parsing COSMIC file " + cosmicFile, e);
        }
    }
}
