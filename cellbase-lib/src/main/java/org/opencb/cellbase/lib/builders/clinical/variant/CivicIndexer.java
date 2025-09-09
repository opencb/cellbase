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
import org.opencb.biodata.formats.variant.civic.CivicParser;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.lib.EtlCommons;
import org.rocksdb.RocksDB;

import java.io.IOException;
import java.nio.file.Path;


public class CivicIndexer extends ClinicalIndexer {

    private Path civicVariantsFilePath;
    private Path civicFeaturesFilePath;
    private Path civicProfilesFilePath;
    private Path civicAssertionsFilePath;
    private Path civicEvidencesFilePath;

    public CivicIndexer(Path civicVariantsFilePath, Path civicFeaturesFilePath, Path civicProfilesFilePath, Path civicAssertionsFilePath,
                        Path civicEvidencesFilePath, String version, boolean normalize, Path genomeSequenceFilePath, String assembly,
                        RocksDB rdb) throws IOException {
        super(genomeSequenceFilePath);

        this.civicVariantsFilePath = civicVariantsFilePath;
        this.civicFeaturesFilePath = civicFeaturesFilePath;
        this.civicProfilesFilePath = civicProfilesFilePath;
        this.civicAssertionsFilePath = civicAssertionsFilePath;
        this.civicEvidencesFilePath = civicEvidencesFilePath;
        this.version = version;
        this.normalize = normalize;
        this.assembly = assembly;
        this.rdb = rdb;
    }

    public void index() throws CellBaseException {
        // Call CIViC parser
        String dataName = EtlCommons.getDataName(EtlCommons.CIVIC_DATA);
        try {
            logger.info("Parsing {} files ...", dataName);
            CivicIndexerCallback callback = new CivicIndexerCallback(rdb, this);
            CivicParser.parse(civicVariantsFilePath, civicFeaturesFilePath, civicProfilesFilePath, civicAssertionsFilePath,
                    civicEvidencesFilePath, version, callback);

            logger.info("{} parsing finished: {} variants passed", dataName, callback.getNumPassedVariants());
            logger.info("{} parsing finished: {} invalid lines by nucletiodes", dataName, callback.getNumInvalidBaseLines());
            logger.info("{} parsing finished: {} invalid lines by position", dataName, callback.getNumInvalidPositionLines());
        } catch (IOException | FileFormatException e) {
            throw new CellBaseException("Error parsing " + dataName + " files: " + civicVariantsFilePath + ", " + civicFeaturesFilePath
                    + civicProfilesFilePath + ", " + civicAssertionsFilePath + ", " + civicEvidencesFilePath, e);
        }
    }
}
