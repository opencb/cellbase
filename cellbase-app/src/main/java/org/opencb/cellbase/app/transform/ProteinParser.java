/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.app.transform;

import org.opencb.biodata.formats.protein.uniprot.UniProtParser;
import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.*;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ProteinParser extends CellBaseParser {

    private Path uniprotFilesDir;
    private Path interproFilePath;
    private String species;

    private Map<String, Entry> proteinMap;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public ProteinParser(Path uniprotFilesDir, String species, CellBaseSerializer serializer) {
        this(uniprotFilesDir, null, species, serializer);
    }

    public ProteinParser(Path uniprotFilesDir, Path interproFilePath, String species, CellBaseSerializer serializer) {
        super(serializer);

        this.uniprotFilesDir = uniprotFilesDir;
        this.interproFilePath = interproFilePath;
        this.species = species;
    }

    @Override
    public void parse() throws IOException {

        if (uniprotFilesDir == null || !Files.exists(uniprotFilesDir)) {
            throw new IOException("File '" + uniprotFilesDir + "' not valid");
        }

        proteinMap = new HashMap<>(30000);
        UniProtParser up = new UniProtParser();
        try {
            File[] files = uniprotFilesDir.toFile().listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".xml") || name.endsWith(".xml.gz");
                }
            });

            for (File file : files) {
                Uniprot uniprot = (Uniprot) up.loadXMLInfo(file.toString(), UniProtParser.UNIPROT_CONTEXT_v201504);

                for (Entry entry : uniprot.getEntry()) {
                    String entryOrganism = null;
                    Iterator<OrganismNameType> iter = entry.getOrganism().getName().iterator();
                    while (iter.hasNext()) {
                        entryOrganism = iter.next().getValue();
                        if (entryOrganism.equals(species)) {
                            proteinMap.put(entry.getAccession().get(0), entry);
//                            serializer.serialize(entry);
                        }
                    }
                }
            }
            logger.debug("Number of proteins stored in map: '{}'", proteinMap.size());

            if (interproFilePath != null && Files.exists(interproFilePath)) {
                BufferedReader interproBuffereReader = FileUtils.newBufferedReader(interproFilePath);
                Set<String> hashSet = new HashSet<>(proteinMap.keySet());
                Set<String> visited = new HashSet<>(30000);

                int numInterProLinesProcessed = 0;
                int numUniqueProteinsProcessed = 0;
                String[] fields;
                String line;
                boolean iprAdded;
                while ((line = interproBuffereReader.readLine()) != null) {
                    fields = line.split("\t");

                    if (hashSet.contains(fields[0])) {
                        iprAdded = false;
                        BigInteger start = BigInteger.valueOf(Integer.parseInt(fields[4]));
                        BigInteger end = BigInteger.valueOf(Integer.parseInt(fields[5]));
                        for (FeatureType featureType : proteinMap.get(fields[0]).getFeature()) {
                            if (featureType.getLocation() != null && featureType.getLocation().getBegin() != null
                                    && featureType.getLocation().getBegin().getPosition() != null
                                    && featureType.getLocation().getEnd().getPosition() != null
                                    && featureType.getLocation().getBegin().getPosition().equals(start)
                                    && featureType.getLocation().getEnd().getPosition().equals(end)) {
                                featureType.setId(fields[1]);
                                featureType.setRef(fields[3]);
                                iprAdded = true;
                                break;
                            }
                        }

                        if (!iprAdded) {
                            FeatureType featureType = new FeatureType();
                            featureType.setId(fields[1]);
                            featureType.setDescription(fields[2]);
                            featureType.setRef(fields[3]);

                            LocationType locationType = new LocationType();
                            PositionType positionType = new PositionType();
                            positionType.setPosition(start);
                            locationType.setBegin(positionType);
                            PositionType positionType2 = new PositionType();
                            positionType2.setPosition(end);
                            locationType.setEnd(positionType2);
                            featureType.setLocation(locationType);

                            proteinMap.get(fields[0]).getFeature().add(featureType);
                        }

                        if (!visited.contains(fields[0])) {
                            visited.add(fields[0]);
                            numUniqueProteinsProcessed++;
                        }
                    }

                    if (++numInterProLinesProcessed % 10000000 == 0) {
                        logger.debug("{} InterPro lines processed. {} unique proteins processed",
                                numInterProLinesProcessed, numUniqueProteinsProcessed);
                    }
                }
                interproBuffereReader.close();
            }

            // Serialize and save results
            for (Entry entry : proteinMap.values()) {
                serializer.serialize(entry);
            }

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
