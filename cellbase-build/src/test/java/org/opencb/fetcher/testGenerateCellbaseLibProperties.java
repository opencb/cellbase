package org.opencb.fetcher;

import com.google.gson.*;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class testGenerateCellbaseLibProperties {
    String USER_HOME = System.getProperty("user.home");
    Gson gson = new Gson();
    @Test
    public void generateCellbaseProperties() throws IOException {
        Path p = Paths.get(USER_HOME + "/appl/cellbase/cellbase-build/installation-dir/bin/genome-fetcher/species_info.json");
        BufferedReader br = Files.newBufferedReader(p, Charset.defaultCharset());

        ArrayList<String> speciesList = new ArrayList<>();
        StringBuilder sbAlias = new StringBuilder();
        StringBuilder dbprop = new StringBuilder();

        JsonParser parser = new JsonParser();
        JsonObject o = (JsonObject)parser.parse(br).getAsJsonObject();
        JsonArray phylos = o.get("items").getAsJsonArray();

        for(JsonElement phyloElement: phylos){
            JsonObject phylo = phyloElement.getAsJsonObject();
            JsonArray phyloSpecies = phylo.get("items").getAsJsonArray();

            String category = phylo.get("text").getAsString();
            dbprop.append("\n\n######################"+"\n");
            dbprop.append("#  "+category+"\n");
            dbprop.append("######################"+"\n");

            for(JsonElement speciesElement: phyloSpecies){
                JsonObject species = speciesElement.getAsJsonObject();

                String speciesText = species.get("text").getAsString();
                String speciesAssembly = species.get("assembly").getAsString();
                String speciesAssemblyCode = speciesAssembly.replaceAll("[a-zA-Z_.-]", "");
                if(speciesAssemblyCode.equals("")){
                    speciesAssemblyCode = speciesAssembly.toLowerCase();
                }

                String[] pair = speciesText.split(" ");
                String name = (pair[0].substring(0,1)+pair[1]).toLowerCase();
                speciesList.add(name);

                String upperName = name.toUpperCase();

//              Generate Alias
                sbAlias.append(upperName+".ALIAS = ");
                sbAlias.append(speciesText+",");
                sbAlias.append(speciesText.replace(" ","_")+",");
                sbAlias.append(name+",");
                sbAlias.append(name.substring(0,4)+",");
                sbAlias.append(name.substring(0,3)+"\n");


//              Generate mongodb.conf application.properties species
                dbprop.append(upperName+".DEFAULT.VERSION = "+ "V3" +"\n");
                dbprop.append(upperName+".V3.DB = "+"PRIMARY_DB"+"\n");
                dbprop.append(upperName+".V3.DATABASE = "+name+"_cdb_v3_"+speciesAssemblyCode+"\n");
                dbprop.append(upperName+".V3.MAX_POOL_SIZE = "+"10"+"\n");
                dbprop.append(upperName+".V3.TIMEOUT = "+"10"+"\n\n");
//                MMUSCULUS.DEFAULT.VERSION = V3
//                MMUSCULUS.V3.DB = PRIMARY_DB
//                MMUSCULUS.V3.DATABASE = mmusculus_cdb_v3_38
//                MMUSCULUS.V3.MAX_POOL_SIZE = 10
//                MMUSCULUS.V3.TIMEOUT = 10
            }

        }
        StringBuilder species = new StringBuilder();
        species.append("SPECIES = ");
        for(String specie: speciesList){
            species.append(specie+",");
        }
        species.deleteCharAt(species.length()-1);
        species.append("\n");
        System.out.println(species);
        System.out.println(sbAlias);
        System.out.println(dbprop);


//        while ((line = br.readLine()) != null) {
//            if (line.startsWith("#")) {
//                String category = line.split(" ")[1];
//                dbprop.append("\n\n######################"+"\n");
//                dbprop.append("#  "+category+"\n");
//                dbprop.append("######################"+"\n");
//            }else{
//                String species = line.split("\t")[0];
//                String[] pair = species.split(" ");
//                String name = (pair[0].substring(0,1)+pair[1]).toLowerCase();
//                speciesList.add(name);
//
//
//                //Generate Alias
//                sbAlias.append(name.toUpperCase()+".ALIAS = ");
////                HSAPIENS.ALIAS=Homo sapiens,Homo_sapiens,hsapiens,hsap,hsa
//                sbAlias.append(species+",");
//                sbAlias.append(species.replace(" ","_")+",");
//                sbAlias.append(name+",");
//                sbAlias.append(name.substring(0,4)+",");
//                sbAlias.append(name.substring(0,3)+"\n");
//
//
//            }
//        }
//        br.close();
//        StringBuilder species = new StringBuilder();
//        species.append("SPECIES = ");
//        for(String specie: speciesList){
//            species.append(specie+",");
//        }
//        species.deleteCharAt(species.length()-1);
//        species.append("\n");
//        System.out.println(species);
//        System.out.println(sbAlias);
    }
}
