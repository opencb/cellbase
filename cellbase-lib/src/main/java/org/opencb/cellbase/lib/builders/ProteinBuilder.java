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
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataSource;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.opencb.cellbase.lib.EtlCommons.*;

public class ProteinBuilder extends AbstractBuilder {

    private Path proteinPath;
    private String species;

    public static final String PROTEIN_OUTPUT_FILENAME = PROTEIN_DATA + ".json.gz";

    public ProteinBuilder(Path proteinPath, String species, CellBaseSerializer serializer) {
        super(serializer);

        this.proteinPath = proteinPath;
        this.species = species;
    }

    @Override
    public void parse() throws CellBaseException, IOException {
        // Sanity check
        checkDirectory(proteinPath, getDataName(PROTEIN_DATA));

        // Check UniProt file
        DataSource dataSource = dataSourceReader.readValue(proteinPath.resolve(UNIPROT_DATA).resolve(getDataVersionFilename(UNIPROT_DATA))
                .toFile());
        List<File> uniProtFiles = checkFiles(dataSource, proteinPath.resolve(UNIPROT_DATA), getDataCategory(UNIPROT_DATA) + "/"
                + getDataName(UNIPROT_DATA));
        if (uniProtFiles.size() != 1) {
            throw new CellBaseException(getMismatchNumFilesErrorMessage(getDataName(UNIPROT_DATA), uniProtFiles.size()));
        }

        // Check InterPro file
        dataSource = dataSourceReader.readValue(proteinPath.resolve(INTERPRO_DATA).resolve(getDataVersionFilename(INTERPRO_DATA)).toFile());
        List<File> interProFiles = checkFiles(dataSource, proteinPath.resolve(INTERPRO_DATA), getDataCategory(INTERPRO_DATA) + "/"
                + getDataName(INTERPRO_DATA));
        if (interProFiles.size() != 1) {
            throw new CellBaseException(getMismatchNumFilesErrorMessage(getDataName(INTERPRO_DATA), interProFiles.size()));
        }

        // Prepare UniProt data by splitting data in chunks
        Path uniProtChunksPath = serializer.getOutdir().resolve(UNIPROT_CHUNKS_SUBDIRECTORY);
        logger.info("Split {} file {} into chunks at {}", getDataName(UNIPROT_DATA), uniProtFiles.get(0).getName(), uniProtChunksPath);
        Files.createDirectories(uniProtChunksPath);
        splitUniprot(proteinPath.resolve(UNIPROT_DATA).resolve(uniProtFiles.get(0).getName()), uniProtChunksPath);

        // Prepare RocksDB
        RocksDB rocksDb = getDBConnection(uniProtChunksPath);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        ObjectWriter jsonObjectWriter = mapper.writerFor(Entry.class);

        Map<String, Entry> proteinMap = new HashMap<>(30000);

        // Parsing files
        try {
            File[] files = uniProtChunksPath.toFile().listFiles((dir, name) -> name.endsWith(".xml") || name.endsWith(".xml.gz"));


            for (File file : files) {
                logger.info(PARSING_LOG_MESSAGE, file);
                Uniprot uniprot = (Uniprot) UniProtParser.loadXMLInfo(file.toString(), UniProtParser.UNIPROT_CONTEXT);

                for (Entry entry : uniprot.getEntry()) {
                    String entryOrganism;
                    for (OrganismNameType organismNameType : entry.getOrganism().getName()) {
                        entryOrganism = organismNameType.getValue();
                        if (entryOrganism.equals(species)) {
                            proteinMap.put(entry.getAccession().get(0), entry);

                            // Update RocksDB
                            rocksDb.put(entry.getAccession().get(0).getBytes(), jsonObjectWriter.writeValueAsBytes(entry));
                        }
                    }
                }
                logger.info(PARSING_DONE_LOG_MESSAGE);
            }
            logger.info("Number of proteins stored in map: '{}'", proteinMap.size());
            if (proteinMap.size() > 10) {
                logger.info("First 10 protein IDs in map: {}", proteinMap.keySet().stream().collect(Collectors.toList()).subList(0, 10));
            }

            logger.info(PARSING_LOG_MESSAGE, interProFiles.get(0));
            String interproName = getDataName(INTERPRO_DATA);
            int numLine = 0;
            int numInterProLinesProcessed = 0;
            int numUniqueProteinsProcessed = 0;
            try (BufferedReader interproBuffereReader = FileUtils.newBufferedReader(interProFiles.get(0).toPath())) {

                Set<String> hashSet = proteinMap.keySet();
                Set<String> visited = new HashSet<>(proteinMap.size());

                String[] fields;
                String line;
                boolean iprAdded;
                while ((line = interproBuffereReader.readLine()) != null) {
                    numLine++;
                    fields = line.split("\t");

                    if (hashSet.contains(fields[0])) {
                        iprAdded = false;
                        BigInteger start = BigInteger.valueOf(Integer.parseInt(fields[4]));
                        BigInteger end = BigInteger.valueOf(Integer.parseInt(fields[5]));
                        byte[] bytes = rocksDb.get(fields[0].getBytes());
                        Entry entry = mapper.readValue(bytes, Entry.class);
                        for (FeatureType featureType : entry.getFeature()) {
                            if (featureType.getLocation() != null && featureType.getLocation().getBegin() != null
                                    && featureType.getLocation().getBegin().getPosition() != null
                                    && featureType.getLocation().getEnd().getPosition() != null
                                    && featureType.getLocation().getBegin().getPosition().equals(start)
                                    && featureType.getLocation().getEnd().getPosition().equals(end)) {
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

                            bytes = rocksDb.get(fields[0].getBytes());
                            entry = mapper.readValue(bytes, Entry.class);
                            entry.getFeature().add(featureType);

                            if (fields[0].equalsIgnoreCase(entry.getAccession().get(0))) {
                                // Update RocksDB
                                rocksDb.put(fields[0].getBytes(), jsonObjectWriter.writeValueAsBytes(entry));
                            } else {
                                logger.info("Something wrong happen: interpro fields[0] = {} vs entry.getAccession().get(0) = {}",
                                        fields[0], entry.getAccession().get(0));
                            }
                        }

                        if (!visited.contains(fields[0])) {
                            visited.add(fields[0]);
                            numUniqueProteinsProcessed++;
                        }
                    }

                    if (++numInterProLinesProcessed % 10000000 == 0) {
                        printInfoLogs(numInterProLinesProcessed, numUniqueProteinsProcessed, interproName);
                    }
                }
                printInfoLogs(numInterProLinesProcessed, numUniqueProteinsProcessed, interproName);

                logger.info(PARSING_DONE_LOG_MESSAGE);
            } catch (IOException e) {
                logger.error("Error parsing {} file: {}. Num. line = {}. Error stack trace = {}", interproName, interProFiles.get(0),
                        numLine, Arrays.toString(e.getStackTrace()));
                printInfoLogs(numInterProLinesProcessed, numUniqueProteinsProcessed, interproName);
            }

            // Serialize and save results
            RocksIterator rocksIterator = rocksDb.newIterator();
            for (rocksIterator.seekToFirst(); rocksIterator.isValid(); rocksIterator.next()) {
                Entry entry = mapper.readValue(rocksIterator.value(), Entry.class);
                serializer.serialize(entry);
            }

            rocksDb.close();
        } catch (JAXBException | RocksDBException | IOException e) {
            throw new CellBaseException("Error parsing " + getDataName(PROTEIN_DATA) + " files", e);
        }

        logger.info(BUILDING_DONE_LOG_MESSAGE, getDataName(PROTEIN_DATA));
    }

    private RocksDB getDBConnection(Path uniProtChunksPath) throws CellBaseException {
        // A static method that loads the RocksDB C++ library
        RocksDB.loadLibrary();
        // The Options class contains a set of configurable DB options that determines the behavior of a database
        Options options = new Options().setCreateIfMissing(true);
        try {
            return RocksDB.open(options, uniProtChunksPath.resolve("integration.idx").toString());
        } catch (RocksDBException e) {
            throw new CellBaseException("Error preparing RocksDB", e);
        }
    }

    private void splitUniprot(Path uniprotFilePath, Path splitOutdirPath) throws IOException {
        PrintWriter pw = null;
        try (BufferedReader br = FileUtils.newBufferedReader(uniprotFilePath)) {
            StringBuilder header = new StringBuilder();
            boolean beforeEntry = true;
            boolean inEntry = false;
            int count = 0;
            int chunk = 0;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().startsWith("<entry ")) {
                    inEntry = true;
                    beforeEntry = false;
                    if (count % 10000 == 0) {
                        pw = new PrintWriter(Files.newOutputStream(splitOutdirPath.resolve("chunk_" + chunk + ".xml").toFile().toPath()));
                        pw.println(header.toString().trim());
                    }
                    count++;
                }

                if (beforeEntry) {
                    header.append(line).append("\n");
                }

                if (inEntry) {
                    pw.println(line);
                }

                if (line.trim().startsWith("</entry>")) {
                    inEntry = false;
                    if (count % 10000 == 0) {
                        if (pw != null) {
                            pw.print("</uniprot>");
                            pw.close();
                        }
                        chunk++;
                    }
                }
            }
            pw.print("</uniprot>");
            pw.close();
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    private String getMismatchNumFilesErrorMessage(String dataName, int numFiles) {
        return "Only one " + dataName + " file is expected, but currently there are " + numFiles + " files";
    }

    private void printInfoLogs(int numInterProLinesProcessed, int numUniqueProteinsProcessed, String dataName) {
        logger.info("{}: {} lines processed", dataName, numInterProLinesProcessed);
        logger.info("{}: {} unique proteins processed", dataName, numUniqueProteinsProcessed);
    }

}
