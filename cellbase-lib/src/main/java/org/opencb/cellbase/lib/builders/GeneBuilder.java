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
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.opencb.cellbase.lib.EtlCommons.*;
import static org.opencb.cellbase.lib.builders.EnsemblGeneBuilder.ENSEMBL_GENE_BASENAME;
import static org.opencb.cellbase.lib.builders.RefSeqGeneBuilder.REFSEQ_GENE_BASENAME;
import static org.opencb.cellbase.lib.builders.RefSeqGeneBuilder.REFSEQ_GENE_OUTPUT_FILENAME;

public class GeneBuilder extends AbstractBuilder {

    private Path downloadPath;
    private EnsemblGeneBuilder ensemblGeneBuilder;
    private RefSeqGeneBuilder refSeqGeneBuilder;

    public GeneBuilder(Path downloadPath, Path buildPath, SpeciesConfiguration speciesConfiguration, boolean flexibleGTFParsing,
                       CellBaseConfiguration configuration) {
        super(null);

        this.downloadPath = downloadPath;

        // Create Ensembl gene builder
        CellBaseJsonFileSerializer ensemblGeneSerializer = new CellBaseJsonFileSerializer(buildPath, ENSEMBL_GENE_BASENAME);
        this.ensemblGeneBuilder = new EnsemblGeneBuilder(downloadPath.resolve(ENSEMBL_DATA), speciesConfiguration, flexibleGTFParsing,
                configuration, ensemblGeneSerializer);

        // Create RefSeq gene builder
        CellBaseJsonFileSerializer refSeqGeneSerializer = new CellBaseJsonFileSerializer(buildPath, REFSEQ_GENE_BASENAME);
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
        // Check folders and files before building
        check();

        // Build Ensembl genes
        ensemblGeneBuilder.parse();

        // Build RefSeq genes
        if (!Files.exists(downloadPath.resolve(REFSEQ_DATA).resolve(REFSEQ_GENE_OUTPUT_FILENAME))) {
            refSeqGeneBuilder.parse();
        } else {
            logger.info(DATA_ALREADY_BUILT, getDataName(REFSEQ_DATA) + " gene");
        }


        logger.info(BUILDING_DONE_LOG_MESSAGE, getDataName(GENE_DATA));
    }

    public static List<String> getCommonDataSources(SpeciesConfiguration speciesConfiguration, CellBaseConfiguration configuration) {
        List<String> dataList = new ArrayList<>();

        boolean isHSapiens = false;
        if (speciesConfiguration.getScientificName().equals(HOMO_SAPIENS)) {
            isHSapiens = true;
        }

        String prefixId = getConfigurationFileIdPrefix(speciesConfiguration.getScientificName());

        if (isHSapiens || isDataSupported(configuration.getDownload().getManeSelect(), prefixId)) {
            dataList.add(MANE_SELECT_DATA);
        }
        if (isHSapiens || isDataSupported(configuration.getDownload().getLrg(), prefixId)) {
            dataList.add(LRG_DATA);
        }
        if (isHSapiens || isDataSupported(configuration.getDownload().getCancerHotspot(), prefixId)) {
            dataList.add(CANCER_HOTSPOT_DATA);
        }
        if (isHSapiens || isDataSupported(configuration.getDownload().getDgidb(), prefixId)) {
            dataList.add(DGIDB_DATA);
        }
        if (isHSapiens || isDataSupported(configuration.getDownload().getHpo(), prefixId)) {
            dataList.add(HPO_DISEASE_DATA);
        }
        if (isHSapiens || isDataSupported(configuration.getDownload().getCancerHotspot(), prefixId)) {
            dataList.add(CANCER_GENE_CENSUS_DATA);
        }
        if (isHSapiens || isDataSupported(configuration.getDownload().getMiRTarBase(), prefixId)) {
            dataList.add(MIRTARBASE_DATA);
        }
        if (isHSapiens || isDataSupported(configuration.getDownload().getMirbase(), prefixId)) {
            dataList.add(MIRBASE_DATA);
        }

        return dataList;
    }
}
