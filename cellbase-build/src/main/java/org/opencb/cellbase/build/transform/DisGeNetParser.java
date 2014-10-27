package org.opencb.cellbase.build.transform;

import org.opencb.biodata.models.feature.DisGeNet;
import org.opencb.cellbase.build.serializers.CellBaseSerializer;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by antonior on 10/15/14.
 */
public class DisGeNetParser  extends CellBaseParser {
    public Path disGeNetFilePath;
    private Path entrezIdToEnsemblIdFile;


    public DisGeNetParser(CellBaseSerializer serializer, Path disGeNetFilePath, Path entrezIdToEnsemblIdFile) {
        super(serializer);
        this.disGeNetFilePath = disGeNetFilePath;
        this.entrezIdToEnsemblIdFile = entrezIdToEnsemblIdFile;
    }


    private Map<String, List<String>> parseEntrezIdToEnsemblIdFile(Path entrezIdToEnsemblIdFile) {
        Map<String, List<String>> entrezIdToEnsemblId = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(entrezIdToEnsemblIdFile.toFile())));
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // TODO: treat exceptions
        return entrezIdToEnsemblId;
    }

    public void parse() {
        Map<String, List<String>> entrezIdToEnsemblIdMap = this.parseEntrezIdToEnsemblIdFile(this.entrezIdToEnsemblIdFile);
        Map<String,DisGeNet> disGeNetMap = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.disGeNetFilePath.toFile())));
            reader.readLine(); // First line is the header -> ignore it

            fillDisGeNetMap(entrezIdToEnsemblIdMap, disGeNetMap, reader);

            Collection <DisGeNet> allDisGeNetRecords = disGeNetMap.values();
            for (DisGeNet one_disgenet : allDisGeNetRecords){
                serialize(one_disgenet);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillDisGeNetMap(Map<String, List<String>> entrezIdToEnsemblIdMap, Map<String, DisGeNet> disGeNetMap, BufferedReader reader) throws IOException {
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
                    updateElementDisGeNetMap(disGeNetMap, geneId, ensemblGeneIds, diseaseId, diseaseName, score, numberOfPubmeds, associationType, sources);
                } else {
                    insertNewElementToDisGeNetMap(disGeNetMap, geneId, ensemblGeneIds, geneSymbol, geneName, diseaseId, diseaseName, score, numberOfPubmeds, associationType, sources);
                }
            }
        }
    }

    private void insertNewElementToDisGeNetMap(Map<String, DisGeNet> disGeNetMap, String geneId, List<String> ensemblGeneIds, String geneSymbol, String geneName, String diseaseId, String diseaseName, Float score, Integer numberOfPubmeds, String associationType, Set<String> sources) {
        DisGeNet.Disease diseaseToAddToNewGene =
                new DisGeNet.Disease(diseaseId, diseaseName, score, numberOfPubmeds, associationType, sources);
        List<DisGeNet.Disease> diseases = new ArrayList<>();
        diseases.add(diseaseToAddToNewGene);
        DisGeNet disGeNet = new DisGeNet(ensemblGeneIds, geneName, geneSymbol, diseases);
        disGeNetMap.put(geneId, disGeNet);
    }

    private void updateElementDisGeNetMap(Map<String, DisGeNet> disGeNetMap, String geneId, List<String> ensemblGeneIds, String diseaseId, String diseaseName, Float score, Integer numberOfPubmeds, String associationType, Set<String> sources) {
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
