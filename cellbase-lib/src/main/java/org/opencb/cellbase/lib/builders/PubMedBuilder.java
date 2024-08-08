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

import org.opencb.biodata.formats.pubmed.PubMedParser;
import org.opencb.biodata.formats.pubmed.v233jaxb.PubmedArticle;
import org.opencb.biodata.formats.pubmed.v233jaxb.PubmedArticleSet;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.lib.download.PubMedDownloadManager;
import org.opencb.commons.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.PUBMED_DATA;
import static org.opencb.cellbase.lib.EtlCommons.getDataName;

public class PubMedBuilder extends AbstractBuilder {

    private Path pubMedDownloadPath;
    private CellBaseConfiguration configuration;

    public PubMedBuilder(Path pubMedDownloadPath, CellBaseFileSerializer serializer, CellBaseConfiguration configuration) {
        super(serializer);
        this.pubMedDownloadPath = pubMedDownloadPath;
        this.configuration = configuration;
    }

    @Override
    public void parse() throws Exception {
        logger.info(BUILDING_LOG_MESSAGE, getDataName(PUBMED_DATA));

        // Check input folder
        FileUtils.checkPath(pubMedDownloadPath);

        // Check PubMed files before parsing them
        List<String> pubMedFilenames = PubMedDownloadManager.getPubMedFilenames(configuration.getDownload().getPubmed());
        for (String pubMedFilename : pubMedFilenames) {
            Path pubMedPath = pubMedDownloadPath.resolve(pubMedFilename);
            if (!Files.exists(pubMedPath)) {
                throw new CellBaseException("Expected PubMed file " + pubMedFilename + ", but it was not found at " + pubMedDownloadPath);
            }
        }

        for (String pubMedFilename : pubMedFilenames) {
            Path pubMedPath = pubMedDownloadPath.resolve(pubMedFilename);
            String basename = pubMedFilename.split("\\.")[0];

            PubmedArticleSet pubmedArticleSet = (PubmedArticleSet) PubMedParser.loadXMLInfo(pubMedPath.toAbsolutePath().toString());

            List<Object> objects = pubmedArticleSet.getPubmedArticleOrPubmedBookArticle();
            logger.info(PARSING_LOG_MESSAGE, pubMedPath);
            int counter = 0;
            for (Object object : objects) {
                PubmedArticle pubmedArticle = (PubmedArticle) object;
                ((CellBaseFileSerializer) serializer).serialize(pubmedArticle, basename);
                if (++counter % 2000 == 0) {
                    logger.info("{} articles", counter);
                }
            }
            serializer.close();

            String logMsg = pubMedPath + " (" + counter + " articles)";
            logger.info(PARSING_DONE_LOG_MESSAGE, logMsg);
        }

        logger.info(BUILDING_DONE_LOG_MESSAGE, getDataName(PUBMED_DATA));
    }
}
