package org.opencb.cellbase.app.transform;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.tools.tar.TarInputStream;
import org.opencb.cellbase.core.common.genedisease.Disease;
import org.opencb.cellbase.core.common.genedisease.Disgenet;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by fjlopez on 19/05/15.
 */
public class DisgenetParser extends CellBaseParser {

    public Path disgenetFilePath;

    public DisgenetParser(Path disgenetFilePath, CellBaseSerializer serializer) {
        super(serializer);
        this.disgenetFilePath = disgenetFilePath;
    }

    public void parse() {
        Map<String, Disgenet> disgenetMap = new HashMap<>();

        BufferedReader reader;
        try {
            if (disgenetFilePath.toFile().getName().endsWith("tar.gz")) {
                TarArchiveInputStream tarInput = new TarArchiveInputStream(new GzipCompressorInputStream(new FileInputStream(disgenetFilePath.toFile())));
                TarArchiveEntry currentEntry = tarInput.getNextTarEntry();
                BufferedReader br = null;
                reader = new BufferedReader(new InputStreamReader(tarInput)); // Read directly from tarInput
            } else if (disgenetFilePath.toFile().getName().endsWith(".gz")) {
                reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(disgenetFilePath.toFile()))));
            } else {
                reader = Files.newBufferedReader(disgenetFilePath, Charset.defaultCharset());
            }

            logger.info("Parsing Disgenet file " + disgenetFilePath + " ...");

            // first line is the header -> ignore it
            reader.readLine();

            long processedDisgenetLines = fillDisgenetMap(disgenetMap, reader);
            logger.info("Done");

            logger.info("Serializing parsed variants ...");
            Collection <Disgenet> allDisgenetRecords = disgenetMap.values();
            for (Disgenet disGeNetRecord : allDisgenetRecords){
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

    private long fillDisgenetMap(Map<String, Disgenet> disGeNetMap, BufferedReader reader) throws IOException {
        long linesProcessed = 0;

        String line;
        while ((line = reader.readLine()) != null){
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

            if (geneId != null && !geneId.equals("")){
                if (disGeNetMap.get(geneId) != null){
                    updateElementDisgenetMap(disGeNetMap, geneId, diseaseId, diseaseName, score, numberOfPubmeds, associationType, sources);
                } else {
                    insertNewElementToDisgenetMap(disGeNetMap, geneId, geneSymbol, geneName, diseaseId, diseaseName,
                            score, numberOfPubmeds, associationType, sources);
                }
            }

            linesProcessed++;
            if((linesProcessed%10000)==0) {
                logger.info("{} lines processed", linesProcessed);
            }
        }

        return linesProcessed;
    }

    private void insertNewElementToDisgenetMap(Map<String, Disgenet> disGeNetMap, String geneId, String geneSymbol,
                                               String geneName, String diseaseId, String diseaseName, Float score,
                                               Integer numberOfPubmeds, String associationType, Set<String> sources) {
        Disease diseaseToAddToNewGene =
                new Disease(diseaseId, diseaseName, score, numberOfPubmeds, associationType, sources);
        List<Disease> diseases = new ArrayList<>();
        diseases.add(diseaseToAddToNewGene);
        Disgenet disGeNet = new Disgenet(geneName, geneSymbol, diseases);
        disGeNetMap.put(geneId, disGeNet);
    }

    private void updateElementDisgenetMap(Map<String, Disgenet> disGeNetMap, String geneId, String diseaseId, String diseaseName, Float score, Integer numberOfPubmeds, String associationType, Set<String> sources) {
        Disgenet disGeNetRecord = disGeNetMap.get(geneId);
        Boolean disease_found = false;
        for (int i = 0; i < disGeNetRecord.getDiseases().size(); i++){
            if (disGeNetRecord.getDiseases().get(i).getDiseaseId().equals(diseaseId)){
                disGeNetRecord.getDiseases().get(i).getAssociationTypes().add(associationType);
                disGeNetRecord.getDiseases().get(i).getSources().addAll(sources);
                disease_found = true;
            }
        }
        if (!disease_found) {
            Disease diseaseToAddToExitsGene =
                    new Disease(diseaseId, diseaseName, score, numberOfPubmeds, associationType, sources);
            disGeNetRecord.getDiseases().add(diseaseToAddToExitsGene);
        }
    }

}
