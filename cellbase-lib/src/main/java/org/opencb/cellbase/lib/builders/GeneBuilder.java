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
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;

import java.nio.file.Path;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class GeneBuilder extends CellBaseBuilder {

    private EnsemblGeneBuilder ensemblGeneBuilder;
    private RefSeqGeneBuilder refSeqGeneBuilder;

    public GeneBuilder(Path downloadPath, Path buildPath, SpeciesConfiguration speciesConfiguration, boolean flexibleGTFParsing,
                       CellBaseConfiguration configuration)
            throws CellBaseException {
        super(null);

        // Create Ensembl gene builder
        CellBaseJsonFileSerializer ensemblGeneSerializer = new CellBaseJsonFileSerializer(buildPath.resolve(ENSEMBL_DATA),
                ENSEMBL_GENE_BASENAME);
        this.ensemblGeneBuilder = new EnsemblGeneBuilder(downloadPath.resolve(ENSEMBL_DATA), speciesConfiguration, flexibleGTFParsing,
                configuration, ensemblGeneSerializer);

        // Create RefSeq gene builder
        CellBaseJsonFileSerializer refSeqGeneSerializer = new CellBaseJsonFileSerializer(buildPath.resolve(REFSEQ_DATA),
                REFSEQ_GENE_BASENAME);
        this.refSeqGeneBuilder = new RefSeqGeneBuilder(downloadPath.resolve(REFSEQ_DATA), speciesConfiguration, configuration,
                refSeqGeneSerializer);
    }

    public void check() throws Exception {
        // Check Ensembl requirements
        ensemblGeneBuilder.check();

        // Check RefSeq requirements
        refSeqGeneBuilder.check();
    }

    @Override
    public void parse() throws Exception {
        logger.info(BUILDING_LOG_MESSAGE, getDataName(GENE_DATA));

        // Check folders and files before building
        check();

//        // Build Ensembl/RefSeq genes
        ensemblGeneBuilder.parse();
        refSeqGeneBuilder.parse();

        logger.info(BUILDING_DONE_LOG_MESSAGE, getDataName(GENE_DATA));
    }
}
