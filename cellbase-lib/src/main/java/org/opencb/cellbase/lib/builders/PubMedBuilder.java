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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.biodata.formats.pubmed.PubMedParser;
import org.opencb.biodata.formats.pubmed.generated.PubmedArticle;
import org.opencb.biodata.formats.pubmed.generated.PubmedArticleSet;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class PubMedBuilder extends CellBaseBuilder {

    private Path pubmedDir;
    private CellBaseFileSerializer fileSerializer;

    public PubMedBuilder(Path pubmedDir, CellBaseFileSerializer serializer) {
        super(serializer);

        this.fileSerializer = serializer;
        this.pubmedDir = pubmedDir;

        logger = LoggerFactory.getLogger(PubMedBuilder.class);
    }

    @Override
    public void parse() throws Exception {
        // Check input folder
        FileUtils.checkPath(pubmedDir);

        logger.info("Parsing PubMed files...");

        for (File file : pubmedDir.toFile().listFiles()) {
            if (file.isFile() && (file.getName().endsWith("gz") || file.getName().endsWith("xml"))) {
                String name = file.getName().split("\\.")[0];

                logger.info("Parsing PubMed file {} ...", file.getName());

                ObjectWriter objectWriter = new ObjectMapper().writerFor(PubmedArticle.class);
                PubmedArticleSet pubmedArticleSet = (PubmedArticleSet) PubMedParser.loadXMLInfo(file.getAbsolutePath());

                List<Object> objects = pubmedArticleSet.getPubmedArticleOrPubmedBookArticle();
                for (Object object : objects) {
                    PubmedArticle pubmedArticle = (PubmedArticle) object;
                    fileSerializer.serialize(pubmedArticle, name);
                }
//
//
//
//                try (BufferedWriter out = FileUtils.newBufferedWriter(jsonPath)) {
//                    List<Object> objects = pubmedArticleSet.getPubmedArticleOrPubmedBookArticle();
//                    for (Object object : objects) {
//                        PubmedArticle pubmedArticle = (PubmedArticle) object;
//                        out.write(objectWriter.writeValueAsString(pubmedArticle));
//                        out.write("\n");
//                    }
//                }
            }
        }

        logger.info("Parsing PubMed files finished.");
    }
}
