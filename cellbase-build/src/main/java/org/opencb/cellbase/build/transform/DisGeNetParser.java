package org.opencb.cellbase.build.transform;

import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.build.transform.formats.DisGeNet;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by antonior on 10/15/14.
 */
public class DisGeNetParser  extends CellBaseParser {
    public Path disGeNetFilePath;
    private Path entrezIdToEnsemblIdFile;


    public DisGeNetParser(Path disGeNetFilePath, Path entrezIdToEnsemblIdFile, CellBaseSerializer serializer) {
        super(serializer);
        this.disGeNetFilePath = disGeNetFilePath;
        this.entrezIdToEnsemblIdFile = entrezIdToEnsemblIdFile;
    }

    public void parse() {
        Map<String, List<String>> entrezIdToEnsemblIdMap = parseEntrezIdToEnsemblIdFile(entrezIdToEnsemblIdFile);
        Map<String, DisGeNet> disGeNetMap = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(disGeNetFilePath.toFile())))) {
            logger.info("Parsing DisGeNet file " + disGeNetFilePath + " ...");
            reader.readLine(); // First line is the header -> ignore it

            long processedDisGeNetLines = fillDisGeNetMap(entrezIdToEnsemblIdMap, disGeNetMap, reader);
            logger.info("Done");

            logger.info("Seializing parsed variants ...");
            Collection <DisGeNet> allDisGeNetRecords = disGeNetMap.values();
            for (DisGeNet disGeNetRecord : allDisGeNetRecords){
                serialize(disGeNetRecord);
            }
            logger.info("Done");
            this.printSummary(processedDisGeNetLines, allDisGeNetRecords.size());

        } catch (FileNotFoundException e) {
            logger.error("DisGeNet file " + disGeNetFilePath + " not found");
        } catch (IOException e) {
            logger.error("Error reading DisGeNet file " + disGeNetFilePath + ": " + e.getMessage());
        }
    }

    private void printSummary(long processedDisGeNetLines, long serializedGenes) {
        logger.info("");
        logger.info("Summary");
        logger.info("=======");
        logger.info("Processed " + processedDisGeNetLines + " disGeNet file lines");
        logger.info("Serialized " + serializedGenes + " genes");
    }

    private Map<String, List<String>> parseEntrezIdToEnsemblIdFile(Path entrezIdToEnsemblIdFile) {
        logger.info("Parsing \"entrezId to Ensembl Id\" file " + entrezIdToEnsemblIdFile + " ...");
        Map<String, List<String>> entrezIdToEnsemblId = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(entrezIdToEnsemblIdFile.toFile())))) {
            reader.readLine(); // First line is the header -> ignore it
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
            logger.error("Ensembl Ids won't be added to DisGeNet objects");
        } catch (IOException e) {
            logger.error("Error reading "+ entrezIdToEnsemblIdFile + ": " + e.getMessage());
            logger.error("Ensembl Ids won't be added to DisGeNet objects");
        }
        return entrezIdToEnsemblId;
    }

    private long fillDisGeNetMap(Map<String, List<String>> entrezIdToEnsemblIdMap, Map<String, DisGeNet> disGeNetMap, BufferedReader reader) throws IOException {
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

            if (geneId != null && geneId !=""){
                if (disGeNetMap.get(geneId) != null){
                    updateElementDisGeNetMap(disGeNetMap, geneId, diseaseId, diseaseName, score, numberOfPubmeds, associationType, sources);
                } else {
                    insertNewElementToDisGeNetMap(disGeNetMap, geneId, ensemblGeneIds, geneSymbol, geneName, diseaseId, diseaseName, score, numberOfPubmeds, associationType, sources);
                }
            }

            linesProcessed++;
        }

        return linesProcessed;
    }

    private void insertNewElementToDisGeNetMap(Map<String, DisGeNet> disGeNetMap, String geneId, List<String> ensemblGeneIds, String geneSymbol, String geneName, String diseaseId, String diseaseName, Float score, Integer numberOfPubmeds, String associationType, Set<String> sources) {
        DisGeNet.Disease diseaseToAddToNewGene =
                new DisGeNet.Disease(diseaseId, diseaseName, score, numberOfPubmeds, associationType, sources);
        List<DisGeNet.Disease> diseases = new ArrayList<>();
        diseases.add(diseaseToAddToNewGene);
        DisGeNet disGeNet = new DisGeNet(ensemblGeneIds, geneName, geneSymbol, diseases);
        disGeNetMap.put(geneId, disGeNet);
    }

    private void updateElementDisGeNetMap(Map<String, DisGeNet> disGeNetMap, String geneId, String diseaseId, String diseaseName, Float score, Integer numberOfPubmeds, String associationType, Set<String> sources) {
        DisGeNet disGeNetRecord = disGeNetMap.get(geneId);
        Boolean disease_found = false;
        for (int i = 0; i < disGeNetRecord.getDiseases().size(); i++){
            if (disGeNetRecord.getDiseases().get(i).getDiseaseId().equals(diseaseId)){
                disGeNetRecord.getDiseases().get(i).getAssociationTypes().add(associationType);
                disGeNetRecord.getDiseases().get(i).getSources().addAll(sources);
                disease_found = true;
            }
        }
        if (disease_found == false) {
            DisGeNet.Disease diseaseToAddToExitsGene =
                    new DisGeNet.Disease(diseaseId, diseaseName, score, numberOfPubmeds, associationType, sources);
            disGeNetRecord.getDiseases().add(diseaseToAddToExitsGene);
        }
    }
}
