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

import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class SpliceBuilder extends CellBaseBuilder {

    private Path genePath;
    private Path genomeInfoPath;
    private Path fastaPath;

    private static final int DISTANCE = 50;
    private static final int CHUNK_SIZE = 5000;

    public SpliceBuilder(Path genePath, Path genomeInfoPath, Path fastaPath, CellBaseFileSerializer serializer) {
        super(serializer);

        this.genePath = genePath;
        this.genomeInfoPath = genomeInfoPath;
        this.fastaPath = fastaPath;

        logger = LoggerFactory.getLogger(SpliceBuilder.class);
    }

    @Override
    public void parse() throws Exception {
        FileUtils.checkPath(genePath);
        FileUtils.checkPath(genomeInfoPath);
        FileUtils.checkPath(fastaPath);

        logger.info("Computing splice scores...");

        SpliceAIBuildExecutor executor = new SpliceAIBuildExecutor(genePath, genomeInfoPath, fastaPath, DISTANCE, CHUNK_SIZE, serializer);

        executor.prepareInput();
//        executor.runSpliceAI();
//        executor.parseOutput();
//        executor.clean();

        logger.info("Computing splice scores finished.");
    }

}
