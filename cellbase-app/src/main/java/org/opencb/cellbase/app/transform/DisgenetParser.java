package org.opencb.cellbase.app.transform;

import org.opencb.cellbase.build.transform.formats.disgenet.Disease;
import org.opencb.cellbase.core.common.genedisease.Disgenet;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.build.transform.formats.disgenet.DisGeNet;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

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

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(disgenetFilePath.toFile())))) {
            logger.info("Parsing Disgenet file " + disgenetFilePath + " ...");

            // first line is the header -> ignore it
            reader.readLine();

            long processedDisgenetLines = fillDisgenetMap(entrezIdToEnsemblIdMap, disgenetMap, reader);
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

    private Map<String, List<String>> parseEntrezIdToEnsemblIdFile(Path entrezIdToEnsemblIdFile) {
        logger.info("Parsing \"entrezId to Ensembl Id\" file " + entrezIdToEnsemblIdFile + " ...");
        Map<String, List<String>> entrezIdToEnsemblId = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(entrezIdToEnsemblIdFile.toFile())))) {
            // first line is the header -> ignore it
            reader.readLine();
            for (String line; (line = reader.readLine()) != null;) {
                String[] fields = line.split("\t");
                List<String> ensemblIds = entrezIdToEnsemblId.get(fields[1]);
                if (ensemblIds == null) {
                    ensemblIds = new ArrayList<>();
                    ensemblIds.add(fields[0]);
                    entrezIdToEnsemblId.put(fields[1], ensemblIds);
                } else {
                    ensemblIds.add(fields[0]);
                }
            }
            reader.close();
            logger.info("Done");
        } catch (FileNotFoundException e) {
            logger.error("File " + entrezIdToEnsemblIdFile + " doesn't exist");
            logger.error("Ensembl Ids won't be added to Disgenet objects");
        } catch (IOException e) {
            logger.error("Error reading "+ entrezIdToEnsemblIdFile + ": " + e.getMessage());
            logger.error("Ensembl Ids won't be added to Disgenet objects");
        }
        return entrezIdToEnsemblId;
    }

    private long fillDisgenetMap(Map<String, List<String>> entrezIdToEnsemblIdMap, Map<String, Disgenet> disGeNetMap, BufferedReader reader) throws IOException {
        long linesProcessed = 0;

        String line;
        while ((line = reader.readLine()) != null){
            String[] fields = line.split("\t");

            String geneId = fields[0];
            List<String> ensemblGeneIds = entrezIdToEnsemblIdMap.get(geneId);
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
                    insertNewElementToDisgenetMap(disGeNetMap, geneId, ensemblGeneIds, geneSymbol, geneName, diseaseId, diseaseName, score, numberOfPubmeds, associationType, sources);
                }
            }

            linesProcessed++;
        }

        return linesProcessed;
    }

    private void insertNewElementToDisgenetMap(Map<String, Disgenet> disGeNetMap, String geneId, List<String> ensemblGeneIds, String geneSymbol, String geneName, String diseaseId, String diseaseName, Float score, Integer numberOfPubmeds, String associationType, Set<String> sources) {
        Disease diseaseToAddToNewGene =
                new Disease(diseaseId, diseaseName, score, numberOfPubmeds, associationType, sources);
        List<Disease> diseases = new ArrayList<>();
        diseases.add(diseaseToAddToNewGene);
        Disgenet disGeNet = new Disgenet(ensemblGeneIds, geneName, geneSymbol, diseases);
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
