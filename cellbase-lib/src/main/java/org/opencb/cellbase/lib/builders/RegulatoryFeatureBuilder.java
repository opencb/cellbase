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
import org.apache.commons.lang3.StringUtils;
import org.opencb.biodata.formats.feature.gff.Gff2;
import org.opencb.biodata.formats.feature.gff.io.Gff2Reader;
import org.opencb.biodata.formats.io.FileFormatException;
import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.biodata.models.core.RegulatoryPfm;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataSource;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class RegulatoryFeatureBuilder extends AbstractBuilder {

    private Path regulationPath;

    private Set<Gff2> regulatoryFeatureSet;

    public RegulatoryFeatureBuilder(Path regulationPath, CellBaseSerializer serializer) {
        super(serializer);
        this.regulationPath = regulationPath;
    }

    @Override
    public void parse() throws Exception {
        logger.info(BUILDING_LOG_MESSAGE, getDataName(REGULATION_DATA));

        // Sanity check
        checkDirectory(regulationPath, getDataName(REGULATION_DATA));

        // Check build regulatory files
        DataSource dataSource = dataSourceReader.readValue(regulationPath.resolve(getDataVersionFilename(REGULATORY_BUILD_DATA)).toFile());
        List<File> regulatoryFiles = checkFiles(dataSource, regulationPath, getDataCategory(REGULATORY_BUILD_DATA) + "/"
                + getDataName(REGULATORY_BUILD_DATA));
        if (regulatoryFiles.size() != 1) {
            throw new CellBaseException("One " + getDataName(REGULATORY_BUILD_DATA) + " file is expected, but currently there are "
                    + regulatoryFiles.size() + " files");
        }

        // Check motif features files
        dataSource = dataSourceReader.readValue(regulationPath.resolve(getDataVersionFilename(MOTIF_FEATURES_DATA)).toFile());
        List<File> motifFeaturesFiles = checkFiles(dataSource, regulationPath, getDataCategory(MOTIF_FEATURES_DATA) + "/"
                + getDataName(MOTIF_FEATURES_DATA));
        if (motifFeaturesFiles.size() != 2) {
            throw new CellBaseException("Two " + getDataName(MOTIF_FEATURES_DATA) + " files are expected, but currently there are "
                    + motifFeaturesFiles.size() + " files");
        }

        // Downloading and building pfm matrices
        File motifFile = motifFeaturesFiles.get(0).getName().endsWith("tbi") ? motifFeaturesFiles.get(1) : motifFeaturesFiles.get(0);
        loadPfmMatrices(motifFile.toPath(), serializer.getOutdir());

        // Parse regulatory build features
        parseGffFile(regulatoryFiles.get(0).toPath());

        logger.info(BUILDING_DONE_LOG_MESSAGE, getDataName(REGULATION_DATA));
    }

    protected void parseGffFile(Path regulatoryFeatureFile) throws IOException, NoSuchMethodException, FileFormatException {
        logger.info(PARSING_LOG_MESSAGE, regulatoryFeatureFile);

        // Create and populate regulatory feature set
        regulatoryFeatureSet = new HashSet<>();
        try (Gff2Reader regulatoryFeatureReader = new Gff2Reader(regulatoryFeatureFile)) {
            Gff2 feature;
            while ((feature = regulatoryFeatureReader.read()) != null) {
                regulatoryFeatureSet.add(feature);
            }
        }

        // Serialize and save results
        for (Gff2 feature : regulatoryFeatureSet) {
            // In order to get the ID we split the attribute format: ID=TF_binding_site:ENSR00000243312; ....
            String id = feature.getAttribute().split(";")[0].split(":")[1];
            RegulatoryFeature regulatoryFeature = new RegulatoryFeature(id, feature.getSequenceName(), feature.getFeature(),
                    feature.getStart(), feature.getEnd());
            serializer.serialize(regulatoryFeature);
        }
        serializer.close();

        logger.info(PARSING_DONE_LOG_MESSAGE, regulatoryFeatureFile);
    }

    private void loadPfmMatrices(Path motifGffFile, Path buildFolder) throws IOException, NoSuchMethodException, FileFormatException,
            InterruptedException {
        Path regulatoryPfmPath = buildFolder.resolve(REGULATORY_PFM_BASENAME + ".json.gz");
        logger.info("Downloading and building PFM matrices in {} from {} ...", regulatoryPfmPath, motifGffFile);
        if (Files.exists(regulatoryPfmPath)) {
            logger.info("{} is already built", regulatoryPfmPath);
            return;
        }

        Set<String> motifIds = new HashSet<>();
        try (Gff2Reader motifsFeatureReader = new Gff2Reader(motifGffFile)) {
            Gff2 tfbsMotifFeature;
            Pattern filePattern = Pattern.compile("ENSPFM(\\d+)");
            while ((tfbsMotifFeature = motifsFeatureReader.read()) != null) {
                String pfmId = getMatrixId(filePattern, tfbsMotifFeature);
                if (StringUtils.isNotEmpty(pfmId)) {
                    motifIds.add(pfmId);
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        CellBaseSerializer serializer = new CellBaseJsonFileSerializer(buildFolder, REGULATORY_PFM_BASENAME, true);
        if (logger.isInfoEnabled()) {
            logger.info("Looking up {} PFMs", motifIds.size());
        }
        for (String pfmId : motifIds) {
            String urlString = "https://rest.ensembl.org/species/homo_sapiens/binding_matrix/" + pfmId
                    + "?unit=frequencies;content-type=application/json";
            URL url = new URL(urlString);
            RegulatoryPfm regulatoryPfm = mapper.readValue(url, RegulatoryPfm.class);
            serializer.serialize(regulatoryPfm);
            // https://github.com/Ensembl/ensembl-rest/wiki/Rate-Limits
            TimeUnit.MILLISECONDS.sleep(250);
        }
        serializer.close();

        logger.info("Downloading and building PFM matrices at {} done.", regulatoryPfmPath);
    }

    private String getMatrixId(Pattern pattern, Gff2 tfbsMotifFeature) {
        Matcher matcher = pattern.matcher(tfbsMotifFeature.getAttribute());
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    public Set<Gff2> getRegulatoryFeatureSet() {
        return regulatoryFeatureSet;
    }
}
