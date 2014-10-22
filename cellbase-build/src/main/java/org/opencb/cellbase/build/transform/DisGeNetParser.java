package org.opencb.cellbase.build.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.biodata.models.feature.DisGeNet;
import org.opencb.cellbase.build.serializers.CellBaseSerializer;
;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by antonior on 10/15/14.
 */
public class DisGeNetParser  extends CellBaseParser {
    public Path disGeNetFilePath;
    private Path EntrezIdToEnsemblIdFile;


    public DisGeNetParser(CellBaseSerializer serializer, Path disGeNetFilePath, Path EntrezIdToEnsemblIdFile) {
        super(serializer);
        this.disGeNetFilePath = disGeNetFilePath;
        this.EntrezIdToEnsemblIdFile = EntrezIdToEnsemblIdFile;
    }


    private Map<String, String> parserEntrezIdToEnsemblIdFile(Path EntrezIdToEnsemblIdFile) {
        Map<String, String> entrezIdToEnsemblId = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(EntrezIdToEnsemblIdFile.toFile())));
            String line;
            reader.readLine(); // First line is the header -> ignore it
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\t");
                entrezIdToEnsemblId.put(fields[1], fields[0]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entrezIdToEnsemblId;
    }


    public void parse() {
        Map<String, String> entrezIdToEnsemblIdMap = this.parserEntrezIdToEnsemblIdFile(this.EntrezIdToEnsemblIdFile);
        Map<String,DisGeNet> disGeNetMap = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this.disGeNetFilePath.toFile())));
            reader.readLine(); // First line is the header -> ignore it


            FillDisGeNetMap(entrezIdToEnsemblIdMap, disGeNetMap, reader);

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

    private void FillDisGeNetMap(Map<String, String> entrezIdToEnsemblIdMap, Map<String, DisGeNet> disGeNetMap, BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null){
            String[] fields = line.split("\t");

            String geneId = entrezIdToEnsemblIdMap.get(fields[0]);
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
                    UpdateElementDisGeNetMap(disGeNetMap, geneId, diseaseId, diseaseName, score, numberOfPubmeds, associationType, sources);

                }
                else {
                    InsertNewElementToDisGeNetMap(disGeNetMap, geneId, geneSymbol, geneName, diseaseId, diseaseName, score, numberOfPubmeds, associationType, sources);

                }

            }

        }
    }

    private void InsertNewElementToDisGeNetMap(Map<String, DisGeNet> disGeNetMap, String geneId, String geneSymbol, String geneName, String diseaseId, String diseaseName, Float score, Integer numberOfPubmeds, String associationType, Set<String> sources) {
        DisGeNet.Disease diseaseToAddToNewGene = new DisGeNet.Disease (diseaseId,diseaseName,score,numberOfPubmeds,associationType,sources);
        List<DisGeNet.Disease> listofDisease =  new ArrayList<>();
        listofDisease.add(diseaseToAddToNewGene);
        DisGeNet disGeNet = new DisGeNet(geneId,geneName,geneSymbol,listofDisease);
        disGeNetMap.put(geneId,disGeNet);
    }

    private void UpdateElementDisGeNetMap(Map<String, DisGeNet> disGeNetMap, String geneId, String diseaseId, String diseaseName, Float score, Integer numberOfPubmeds, String associationType, Set<String> sources) {
        DisGeNet disGeNetRecord = disGeNetMap.get(geneId);
        Boolean disease_found = false;
        for (int i = 0; i < disGeNetRecord.getDiseases().size(); i++){
            if (disGeNetRecord.getDiseases().get(i).getDiseaseId().equals(diseaseId)){
                disGeNetRecord.getDiseases().get(i).getAssociationTypes().add(associationType);
                disGeNetRecord.getDiseases().get(i).getSources().addAll(sources);
                disGeNetMap.put(geneId,disGeNetRecord);

                disease_found=true;
            }
        if (disease_found == false) {
            DisGeNet.Disease diseaseToAddToExitsGene = new DisGeNet.Disease (diseaseId,diseaseName,score,numberOfPubmeds,associationType,sources);
            disGeNetRecord.getDiseases().add(diseaseToAddToExitsGene);

            }

        }
    }
}
