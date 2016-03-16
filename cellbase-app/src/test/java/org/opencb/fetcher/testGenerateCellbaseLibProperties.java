/*
 * Copyright 2015 OpenCB
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

package org.opencb.fetcher;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

    @Test
    public void generateCellbaseProperties() throws IOException {
        Path p = Paths.get(USER_HOME + "/appl/cellbase/cellbase-build/installation-dir/bin/genome-fetcher/species_info.json");
        BufferedReader br = Files.newBufferedReader(p, Charset.defaultCharset());

        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        JsonParser jp = factory.createParser(br);
        JsonNode o = mapper.readTree(jp);

        ArrayNode phylos = (ArrayNode) o.get("items");
        String version = o.get("version").asText().toUpperCase();

        ArrayList<String> speciesList = new ArrayList<>();
        StringBuilder dbprop = new StringBuilder();
        StringBuilder sbAlias;

        System.out.println(version);
        for (JsonNode phylo : phylos) {

            String category = phylo.get("text").asText();
            ArrayNode phyloSpecies = (ArrayNode) phylo.get("items");

            dbprop.append("\n\n######################" + "\n");
            dbprop.append("#  " + category + "\n");
            dbprop.append("######################" + "\n");

            for (JsonNode species : phyloSpecies) {
                String speciesText = species.get("text").asText();
                String speciesAssembly = species.get("assembly").asText();
                String speciesAssemblyCode = speciesAssembly.replaceAll("[a-zA-Z_.-]", "");
                if(speciesAssemblyCode.equals("")){
                    speciesAssemblyCode = speciesAssembly.toLowerCase();
                }

                String[] pair = speciesText.split(" ");
                String name;
                if (pair.length < 3) {
                    name = (pair[0].substring(0, 1) + pair[1]).toLowerCase();
                } else {
                    name = (pair[0].substring(0, 1) + pair[1] + pair[pair.length - 1].replaceAll("[/_().-]", "")).toLowerCase();
                }
                speciesList.add(name);

                String upperName = name.toUpperCase();

//              Generate Alias
                sbAlias = new StringBuilder();
                sbAlias.append(speciesText + ",");
                sbAlias.append(speciesText.replace(" ", "_") + ",");
                sbAlias.append(name + ",");
                sbAlias.append(name.substring(0, 4) + ",");
                sbAlias.append(name.substring(0, 3));


//              Generate mongodb.conf application.properties species
                dbprop.append(upperName + ".DEFAULT.VERSION = " + version + "\n");
                dbprop.append(upperName + "." + version + ".ALIAS = " + sbAlias + "\n");
                dbprop.append(upperName + "." + version + ".DB = " + "PRIMARY_DB" + "\n");
//                dbprop.append(upperName+".V3.DATABASE = "+name+"_cdb_v3_"+speciesAssemblyCode+"\n");
                dbprop.append(upperName + "." + version + ".DATABASE = " + name + "_cdb_" + version.toLowerCase() + "_" + speciesAssemblyCode + "\n");
//                dbprop.append(upperName+"."+version+".MAX_POOL_SIZE = "+"10"+"\n");
//                dbprop.append(upperName+"."+version+".TIMEOUT = "+"10"+"\n");
                dbprop.append("\n");
            }
        }
        StringBuilder species = new StringBuilder();
        System.out.print("\n\n######################" + "\n");
        System.out.print("#  Available Species\n");
        System.out.print("######################" + "\n");
        species.append("SPECIES = ");
        for (String specie : speciesList) {
            species.append(specie + ",");
        }
        species.deleteCharAt(species.length() - 1);
        species.append("\n");
        System.out.println(species);
//        System.out.println(sbAlias);
        System.out.println(dbprop);
    }

}
