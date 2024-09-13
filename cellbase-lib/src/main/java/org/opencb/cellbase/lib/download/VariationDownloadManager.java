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
import org.opencb.cellbase.lib.EtlCommons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class VariationDownloadManager extends AbstractDownloadManager {

    public VariationDownloadManager(String species, String assembly, Path targetDirectory, CellBaseConfiguration configuration)
            throws IOException, CellBaseException {
        super(species, assembly, targetDirectory, configuration);
    }

    @Override
    public List<DownloadFile> download() throws IOException, InterruptedException {
        return Collections.singletonList(downloadDbSnp());
    }

    public DownloadFile downloadDbSnp() throws IOException, InterruptedException {
        if (!speciesHasInfoToDownload(speciesConfiguration, VARIATION_DATA)) {
            return null;
        }
        if (speciesConfiguration.getScientificName().equals(EtlCommons.HOMO_SAPIENS_NAME)) {
            logger.info("Downloading dbSNP information ...");

            Path variation = downloadFolder.resolve(VARIATION_DATA);
            Files.createDirectories(variation);

            DownloadProperties.URLProperties dbSNP = configuration.getDownload().getDbSNP();
            String url = dbSNP.getHost();
            saveVersionData(VARIATION_DATA, DBSNP_NAME, dbSNP.getVersion(), getTimeStamp(),
                    Collections.singletonList(url), variation.resolve(DBSNP_VERSION_FILENAME));

            Path outPath = variation.resolve(Paths.get(url).getFileName());
            logger.info("Downloading {} to {} ...", url, outPath);
            return downloadFile(url, outPath.toString());
        }
        return null;
    }
}
