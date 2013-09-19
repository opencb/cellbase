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
        String version = o.get("version").getAsString().toUpperCase();

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
//                String speciesAssembly = species.get("assembly").getAsString();
//                String speciesAssemblyCode = speciesAssembly.replaceAll("[a-zA-Z_.-]", "");
//                if(speciesAssemblyCode.equals("")){
//                    speciesAssemblyCode = speciesAssembly.toLowerCase();
//                }

                String[] pair = speciesText.split(" ");
                String name;
                if(pair.length < 3){
                    name = (pair[0].substring(0,1)+pair[1]).toLowerCase();
                }else{
                    name = (pair[0].substring(0,1)+pair[1]+pair[pair.length-1].replaceAll("[/_().-]", "")).toLowerCase();
                }
                speciesList.add(name);

                String upperName = name.toUpperCase();

//              Generate Alias
                sbAlias = new StringBuilder();
                sbAlias.append(speciesText+",");
                sbAlias.append(speciesText.replace(" ","_")+",");
                sbAlias.append(name+",");
                sbAlias.append(name.substring(0,4)+",");
                sbAlias.append(name.substring(0,3));


//              Generate mongodb.conf application.properties species
                dbprop.append(upperName+".DEFAULT.VERSION = "+ version +"\n");
                dbprop.append(upperName+"."+version+".ALIAS = "+sbAlias+"\n");
                dbprop.append(upperName+"."+version+".DB = "+"PRIMARY_DB"+"\n");
//                dbprop.append(upperName+".V3.DATABASE = "+name+"_cdb_v3_"+speciesAssemblyCode+"\n");
                dbprop.append(upperName+"."+version+".DATABASE = "+name+"_cdb_"+version.toLowerCase()+"\n");
//                dbprop.append(upperName+"."+version+".MAX_POOL_SIZE = "+"10"+"\n");
//                dbprop.append(upperName+"."+version+".TIMEOUT = "+"10"+"\n");
                dbprop.append("\n");
            }

        }
        StringBuilder species = new StringBuilder();
        System.out.print("\n\n######################"+"\n");
        System.out.print("#  Available Species\n");
        System.out.print("######################"+"\n");
        species.append("SPECIES = ");
        for(String specie: speciesList){
            species.append(specie+",");
        }
        species.deleteCharAt(species.length()-1);
        species.append("\n");
        System.out.println(species);
//        System.out.println(sbAlias);
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
