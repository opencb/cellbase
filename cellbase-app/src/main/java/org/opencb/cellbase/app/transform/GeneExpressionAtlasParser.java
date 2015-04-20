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

package org.opencb.cellbase.app.transform;

import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.app.transform.formats.GeneExpressionAtlas;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by antonior on 10/16/14.
 */
public class GeneExpressionAtlasParser extends CellBaseParser  {
    private  Path gene_atlas_directory_path;


    public GeneExpressionAtlasParser(Path gene_atlas_directory_path, CellBaseSerializer serializer) {
        super(serializer);
        this.gene_atlas_directory_path = gene_atlas_directory_path;

    }

    public void parse() {
        Map<String, GeneExpressionAtlas> geneAtlasMap = new HashMap<>();
        try {
            String Experiment1 = "EncodeCellLines";
            readFile(geneAtlasMap, Experiment1);

            String Experiment2 = "IlluminaBodyMap";
            readFile(geneAtlasMap, Experiment2);

            String Experiment3 = "MammalianTissues";
            readFile(geneAtlasMap, Experiment3);

            String Experiment4 = "TwentySevenTissues";
            readFile(geneAtlasMap, Experiment4);

            Collection <GeneExpressionAtlas> allGeneAtlasRecords = geneAtlasMap.values();
            for (GeneExpressionAtlas one_atlas_gene : allGeneAtlasRecords) {
                serializer.serialize(one_atlas_gene);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readFile(Map<String, GeneExpressionAtlas> geneAtlasMap, String experiment) throws IOException {
        DirectoryStream<Path> directoryStream = Files.newDirectoryStream(this.gene_atlas_directory_path.resolve(experiment));
        for (Path file_path : directoryStream) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file_path.toFile())));
            String line;
            String Metainfo="";
            String [] header = null;
            while ((line = reader.readLine()) != null){

                if (line.startsWith("#")){
                    Metainfo = Metainfo+line;
                }

                if (line.startsWith("Gene ID")){
                    header = line.split("\t");
                }
                else{
                    updateGeneAtlasMap(geneAtlasMap, experiment, line, header);
                }
            }
        }
    }

    private void updateGeneAtlasMap(Map<String, GeneExpressionAtlas> geneAtlasMap, String experiment, String line, String[] header) {
        List<String> fields = Arrays.asList(line.split("\t"));
        String geneid = fields.get(0);
        String genename = fields.get(1);
        List<GeneExpressionAtlas.Tissue> tissueList = new ArrayList<>();

        for (int i = 2; i < fields.size(); i++ ){
            GeneExpressionAtlas.Tissue tissueToAddList = new GeneExpressionAtlas.Tissue(header[i], experiment,Float.parseFloat(fields.get(i)));
            tissueList.add(tissueToAddList);
        }

        if (geneAtlasMap.get(geneid) != null){
            geneAtlasMap.get(geneid).getTissues().addAll(tissueList);

        }
        else {

            GeneExpressionAtlas geneAtlasInstance = new GeneExpressionAtlas(geneid, genename, tissueList);
            geneAtlasMap.put(geneid,geneAtlasInstance);

        }
    }


}
