package org.opencb.cellbase.app.transform;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.opencb.biodata.models.core.Disease;
import org.opencb.cellbase.core.common.genedisease.Disgenet;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Class for parsing Disgenet files as the one that can be downloaded from
 * http://www.disgenet.org/ds/DisGeNET/results/all_gene_disease_associations.tar.gz.
 *
 * @author Javi Lopez fjlopez@ebi.ac.uk
 */
public class DisgenetParser extends CellBaseParser {

    private Path disgenetFilePath;

    public DisgenetParser(Path disgenetFilePath, CellBaseSerializer serializer) {
        super(serializer);
        this.disgenetFilePath = disgenetFilePath;
    }

    /**
     * Parses a Disgenet file ad the one that can be downloaded from
     * http://www.disgenet.org/ds/DisGeNET/results/all_gene_disease_associations.tar.gz and writes corresponding json
     * objects.
     */
    public void parse() {
        Map<String, Disgenet> disgenetMap = new HashMap<>();

        BufferedReader reader;
        try {
            // Disgenet file is usually downloaded as a .tar.gz file
            if (disgenetFilePath.toFile().getName().endsWith("tar.gz")) {
                TarArchiveInputStream tarInput = new TarArchiveInputStream(
                        new GzipCompressorInputStream(new FileInputStream(disgenetFilePath.toFile())));
//                TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
//                BufferedReader br = null;
                reader = new BufferedReader(new InputStreamReader(tarInput)); // Read directly from tarInput
            } else {
                reader = FileUtils.newBufferedReader(disgenetFilePath);
            }
//            if (disgenetFilePath.toFile().getName().endsWith("txt.gz")) {
//                reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(disgenetFilePath.toFile()))));
//            } else {
//                reader = Files.newBufferedReader(disgenetFilePath, Charset.defaultCharset());
//            }

            logger.info("Parsing Disgenet file " + disgenetFilePath + " ...");
            // first line is the header -> ignore it
            reader.readLine();
            long processedDisgenetLines = fillDisgenetMap(disgenetMap, reader);

            logger.info("Serializing parsed variants ...");
            Collection<Disgenet> allDisgenetRecords = disgenetMap.values();
            for (Disgenet disGeNetRecord : allDisgenetRecords) {
                serializer.serialize(disGeNetRecord);
            }
            logger.info("Done");
            this.printSummary(processedDisgenetLines, allDisgenetRecords.size());

        } catch (FileNotFoundException e) {
            logger.error("Disgenet file " + disgenetFilePath + " not found");
        } catch (IOException e) {
            logger.error("Error reading Disgenet file " + disgenetFilePath + ": " + e.getMessage());
        }
    }

    private void printSummary(long processedDisgenetLines, long serializedGenes) {
        logger.info("");
        logger.info("Summary");
        logger.info("=======");
        logger.info("Processed " + processedDisgenetLines + " disGeNet file lines");
        logger.info("Serialized " + serializedGenes + " genes");
    }

    /**
     * Loads a map {geneId -> Disgenet info} from the Disgenet file.
     *
     * @param disGeNetMap: Map where keys are Disgenet gene ids and values are Disgenet objects. Will be filled within
     *                     this method.
     * @param reader:      BufferedReader pointing to the first line containing actual Disgenet info (header assumed to be
     *                     skipped).
     * @throws IOException in case any problem occurs reading the file.
     */
    private long fillDisgenetMap(Map<String, Disgenet> disGeNetMap, BufferedReader reader) throws IOException {
        long linesProcessed = 0;

        String line;
        while ((line = reader.readLine()) != null) {
            String[] fields = line.split("\t");

            String geneId = fields[0];
            String geneSymbol = fields[1];
            String geneName = fields[2];
            String diseaseId = fields[3];
            String diseaseName = fields[4];
            Float score = Float.parseFloat(fields[5]);
            Integer numberOfPubmeds = Integer.parseInt(fields[6]);
            String associationType = fields[7];
            Set<String> sources = new HashSet<>(Arrays.asList(fields[8].split(", ")));

            if (geneId != null && !geneId.equals("")) {
                if (disGeNetMap.get(geneId) != null) {
                    updateElementDisgenetMap(disGeNetMap, geneId, diseaseId, diseaseName, score, numberOfPubmeds, associationType, sources);
                } else {
                    insertNewElementToDisgenetMap(disGeNetMap, geneId, geneSymbol, geneName, diseaseId, diseaseName,
                            score, numberOfPubmeds, associationType, sources);
                }
            }

            linesProcessed++;
            if ((linesProcessed % 10000) == 0) {
                logger.info("{} lines processed", linesProcessed);
            }
        }

        return linesProcessed;
    }

    private void insertNewElementToDisgenetMap(Map<String, Disgenet> disGeNetMap, String geneId, String geneSymbol,
                                               String geneName, String diseaseId, String diseaseName, Float score,
                                               Integer numberOfPubmeds, String associationType, Set<String> sources) {
        Disease diseaseToAddToNewGene =
                new Disease(diseaseId, diseaseName, "", score, numberOfPubmeds, associationType, sources, "disgenet");
        List<Disease> diseases = new ArrayList<>();
        diseases.add(diseaseToAddToNewGene);
        Disgenet disGeNet = new Disgenet(geneName, geneSymbol, diseases);
        disGeNetMap.put(geneId, disGeNet);
    }

    private void updateElementDisgenetMap(Map<String, Disgenet> disGeNetMap, String geneId, String diseaseId, String diseaseName,
                                          Float score, Integer numberOfPubmeds, String associationType, Set<String> sources) {
        Disgenet disGeNetRecord = disGeNetMap.get(geneId);
        boolean diseaseFound = false;
        for (int i = 0; i < disGeNetRecord.getDiseases().size(); i++) {
            if (disGeNetRecord.getDiseases().get(i).getId().equals(diseaseId)) {
                disGeNetRecord.getDiseases().get(i).getAssociationTypes().add(associationType);
                disGeNetRecord.getDiseases().get(i).getSources().addAll(sources);
                diseaseFound = true;
            }
        }
        if (!diseaseFound) {
            Disease diseaseToAddToExitsGene =
                    new Disease(diseaseId, diseaseName, "", score, numberOfPubmeds, associationType, sources, "disgenet");
            disGeNetRecord.getDiseases().add(diseaseToAddToExitsGene);
        }
    }

}
