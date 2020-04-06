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

import org.opencb.biodata.formats.feature.gff.Gff2;
import org.opencb.biodata.formats.feature.gff.io.Gff2Reader;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.lib.EtlCommons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class RegulatoryFeatureBuilder extends CellBaseBuilder  {

    private final Path gffFile;
    protected Set<Gff2> regulatoryFeatureSet;

    public RegulatoryFeatureBuilder(Path regulatoryDirectoryPath, CellBaseSerializer serializer) {
        super(serializer);
        gffFile = regulatoryDirectoryPath.resolve(EtlCommons.REGULATORY_FEATURES_FILE);
    }

    @Override
    public void parse() throws Exception {
        logger.info("Parsing regulatory features...");
        if (Files.exists(gffFile)) {
            parseGffFile(gffFile);
        } else {
            logger.warn("No regulatory features GFF file found {}", EtlCommons.REGULATORY_FEATURES_FILE);
            logger.warn("Skipping regulatory features GFF file parsing. Regulatory feature data models will not be built.");
        }
    }

    protected void parseGffFile(Path regulatoryFeatureFile) throws IOException, NoSuchMethodException, FileFormatException {
        regulatoryFeatureSet = new HashSet<>();
        if (regulatoryFeatureFile != null && Files.exists(regulatoryFeatureFile) && !Files.isDirectory(regulatoryFeatureFile)
                && Files.size(regulatoryFeatureFile) > 0) {
            Gff2Reader regulatoryFeatureReader = new Gff2Reader(regulatoryFeatureFile);
            Gff2 feature;
            while ((feature = regulatoryFeatureReader.read()) != null) {
                regulatoryFeatureSet.add(feature);
            }
            regulatoryFeatureReader.close();
        }

        int i = 0;
        // Serialize and save results
        for (Gff2 feature : regulatoryFeatureSet) {
            // ID=TF_binding_site:ENSR00000243312;
            String id = feature.getAttribute().split(";")[0].split(":")[1];
            RegulatoryFeature regulatoryFeature = new RegulatoryFeature(id, feature.getSequenceName(), feature.getFeature(),
                    feature.getStart(), feature.getEnd());
            serializer.serialize(regulatoryFeature);
        }
        serializer.close();
    }
}
