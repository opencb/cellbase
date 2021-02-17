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

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.biodata.formats.protein.uniprot.UniProtParser;
import org.opencb.biodata.formats.protein.uniprot.v202003jaxb.*;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProteinBuilder extends CellBaseBuilder {

    private Path uniprotFilesDir;
    private Path interproFilePath;
    private String species;

    private Map<String, Entry> proteinMap;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public ProteinBuilder(Path uniprotFilesDir, String species, CellBaseSerializer serializer) {
        this(uniprotFilesDir, null, species, serializer);
    }

    public ProteinBuilder(Path uniprotFilesDir, Path interproFilePath, String species, CellBaseSerializer serializer) {
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

        RocksDB rocksDb = getDBConnection();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        ObjectWriter jsonObjectWriter = mapper.writerFor(Entry.class);

        proteinMap = new HashMap<>(30000);
//        UniProtParser up = new UniProtParser();
        try {
            File[] files = uniprotFilesDir.toFile().listFiles((dir, name) -> name.endsWith(".xml") || name.endsWith(".xml.gz"));

            for (File file : files) {
                Uniprot uniprot = (Uniprot) UniProtParser.loadXMLInfo(file.toString(), UniProtParser.UNIPROT_CONTEXT);

                for (Entry entry : uniprot.getEntry()) {
                    String entryOrganism;
                    for (OrganismNameType organismNameType : entry.getOrganism().getName()) {
                        entryOrganism = organismNameType.getValue();
                        if (entryOrganism.equals(species)) {
//                            proteinMap.put(entry.getAccession().get(0), entry);
                            rocksDb.put(entry.getAccession().get(0).getBytes(), jsonObjectWriter.writeValueAsBytes(entry));
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
//                        for (FeatureType featureType : proteinMap.get(fields[0]).getFeature()) {
                        byte[] bytes = rocksDb.get(fields[0].getBytes());
                        Entry entry = mapper.readValue(bytes, Entry.class);
                        for (FeatureType featureType : entry.getFeature()) {
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

//                            proteinMap.get(fields[0]).getFeature().add(featureType);
                            bytes = rocksDb.get(fields[0].getBytes());
                            entry = mapper.readValue(bytes, Entry.class);
                            entry.getFeature().add(featureType);
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
            RocksIterator rocksIterator = rocksDb.newIterator();
            for (rocksIterator.seekToFirst(); rocksIterator.isValid(); rocksIterator.next()) {
                Entry entry = mapper.readValue(rocksIterator.value(), Entry.class);
                serializer.serialize(entry);
            }

            rocksDb.close();
        } catch (JAXBException | RocksDBException e) {
            e.printStackTrace();
        }
    }

    private RocksDB getDBConnection() {
        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();
        // the Options class contains a set of configurable DB options
        // that determines the behavior of a database.
        Options options = new Options().setCreateIfMissing(true);
        try {
            return RocksDB.open(options, uniprotFilesDir.resolve("integration.idx").toString());
        } catch (RocksDBException e) {
            // do some error handling
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
