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

import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.DownloadProperties;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Created by imedina on 06/11/15.
 */
public class VariationBuilder extends CellBaseBuilder {

    private DbSnpBuilder dbSnpBuilder;

    public VariationBuilder(Path downloadVariationPath, CellBaseFileSerializer fileSerializer, CellBaseConfiguration configuration) {
        super(fileSerializer);

        // dbSNP
        DownloadProperties.URLProperties dbSnpUrlProperties = configuration.getDownload().getDbSNP();
        dbSnpBuilder = new DbSnpBuilder(downloadVariationPath, dbSnpUrlProperties, fileSerializer);

        logger = LoggerFactory.getLogger(VariationBuilder.class);
    }

    @Override
    public void parse() throws Exception {
        // Parsing dbSNP data
        dbSnpBuilder.parse();
    }
}
