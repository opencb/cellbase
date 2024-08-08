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

import org.opencb.biodata.formats.obo.OboParser;
import org.opencb.biodata.models.core.OntologyTerm;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class OntologyBuilder extends AbstractBuilder {

    private Path oboDownloadPath;
    private SpeciesConfiguration speciesConfiguration;

    public static final String OBO_OUTPUT_BASENAME = "ontology";
    public static final String OBO_OUTPUT_FILENAME = OBO_OUTPUT_BASENAME + ".json.gz";

    public OntologyBuilder(Path oboDownloadPath, SpeciesConfiguration speciesConfiguration, CellBaseSerializer serializer) {
        super(serializer);

        this.oboDownloadPath = oboDownloadPath;
        this.speciesConfiguration = speciesConfiguration;
    }

    @Override
    public void parse() throws Exception {
        // Sanity check
        checkDirectory(oboDownloadPath, getDataName(ONTOLOGY_DATA));

        // Check ontology files
        List<File> hpoFiles = Collections.emptyList();
        List<File> doidFiles = Collections.emptyList();
        List<File> mondoFiles = Collections.emptyList();
        if (speciesConfiguration.getScientificName().equalsIgnoreCase(HOMO_SAPIENS)) {
            hpoFiles = checkOboFiles(HPO_OBO_DATA);
            doidFiles = checkOboFiles(DOID_OBO_DATA);
            mondoFiles = checkOboFiles(MONDO_OBO_DATA);
        }
        List<File> goFiles = checkOboFiles(GO_OBO_DATA);

        // Parse OBO files and build
        if (speciesConfiguration.getScientificName().equalsIgnoreCase(HOMO_SAPIENS)) {
            parseOboFile(hpoFiles.get(0), HPO_OBO_DATA);
            parseOboFile(doidFiles.get(0), DOID_OBO_DATA);
            parseOboFile(mondoFiles.get(0), MONDO_OBO_DATA);
        }
        parseOboFile(goFiles.get(0), GO_OBO_DATA);

        // Close serializer
        serializer.close();
    }

    private void parseOboFile(File oboFile, String data) throws IOException {
        logger.info(PARSING_LOG_MESSAGE, oboFile);
        try (BufferedReader bufferedReader = FileUtils.newBufferedReader(oboFile.toPath())) {
            OboParser parser = new OboParser();
            List<OntologyTerm> terms = parser.parseOBO(bufferedReader, data);
            for (OntologyTerm term : terms) {
                serializer.serialize(term);
            }
        }
        logger.info(PARSING_DONE_LOG_MESSAGE, oboFile);
    }

    private List<File> checkOboFiles(String data) throws IOException, CellBaseException {
        Path versionFilePath = oboDownloadPath.resolve(data).resolve(getDataVersionFilename(data));
        String name = getDataName(data);

        List<File> files = checkFiles(dataSourceReader.readValue(versionFilePath.toFile()), oboDownloadPath.resolve(data),
                getDataName(ONTOLOGY_DATA) + "/" + name);
        if (files.size() != 1) {
            throw new CellBaseException("One " + name + " file is expected, but currently there are " + files.size() + " files");
        }
        return files;
    }
}
