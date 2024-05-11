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

package org.opencb.cellbase.lib.download;

import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.DownloadProperties;
import org.opencb.cellbase.core.exception.CellBaseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class SpliceScoreDownloadManager extends AbstractDownloadManager {

    public SpliceScoreDownloadManager(String species, String assembly, Path outdir, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, outdir, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException, CellBaseException {
        logger.info(DOWNLOADING_LOG_MESSAGE, getDataName(SPLICE_SCORE_DATA));
        if (!speciesConfiguration.getScientificName().equals(HOMO_SAPIENS_NAME)) {
            logger.info("{} not supported for the species {}", getDataName(SPLICE_SCORE_DATA),
                    speciesConfiguration.getScientificName());
            return Collections.emptyList();
        }

        // Create splice score directory
        Path spliceScorePath = downloadFolder.resolve(SPLICE_SCORE_DATA).toAbsolutePath();
        Files.createDirectories(spliceScorePath);

        // SpliceAI
        saveSpliceScoreSource(SPLICEAI_DATA, configuration.getDownload().getSpliceAi(), spliceScorePath);

        // MMSplice
        saveSpliceScoreSource(MMSPLICE_DATA, configuration.getDownload().getMmSplice(), spliceScorePath);

        logger.info(DOWNLOADING_DONE_LOG_MESSAGE, getDataName(SPLICE_SCORE_DATA));
        return Collections.emptyList();
    }

    private void saveSpliceScoreSource(String data, DownloadProperties.URLProperties props, Path spliceScorePath)
            throws CellBaseException, IOException {
        logger.warn("{} files must be downloaded manually !", getDataName(data));
        saveDataSource(data, props.getVersion(), getTimeStamp(), Collections.singletonList(props.getHost()),
                spliceScorePath.resolve(getDataVersionFilename(data)));
    }
}
