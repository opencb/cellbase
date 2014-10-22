package org.opencb.cellbase.build.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.biodata.models.Expresion.GeneAtlas;
import org.opencb.biodata.models.feature.DisGeNet;
import org.opencb.cellbase.build.serializers.CellBaseSerializer;


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
public class GeneAtlasParser  extends CellBaseParser  {
    private  Path gene_atlas_directory_path;


    public GeneAtlasParser(CellBaseSerializer serializer, Path gene_atlas_directory_path) {
        super(serializer);
        this.gene_atlas_directory_path = gene_atlas_directory_path;

    }


    public void parse() {
        Map<String,GeneAtlas> geneAtlasMap = new HashMap<>();
        try {
            String Experiment1 = "EncodeCellLines";
            readFile(geneAtlasMap, Experiment1);

            String Experiment2 = "IlluminaBodyMap";
            readFile(geneAtlasMap, Experiment2);

            String Experiment3 = "MammalianTissues";
            readFile(geneAtlasMap, Experiment3);

            String Experiment4 = "TwentySevenTissues";
            readFile(geneAtlasMap, Experiment4);

            Collection <GeneAtlas> allGeneAtlasRecords = geneAtlasMap.values();
            for (GeneAtlas one_atlas_gene : allGeneAtlasRecords) {
                serialize(one_atlas_gene);

            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readFile(Map<String, GeneAtlas> geneAtlasMap, String experiment) throws IOException {
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

    private void updateGeneAtlasMap(Map<String, GeneAtlas> geneAtlasMap, String experiment, String line, String[] header) {
        List<String> fields = Arrays.asList(line.split("\t"));
        String geneid = fields.get(0);
        String genename = fields.get(1);
        List<GeneAtlas.tissue> tissueList = new ArrayList<>();

        for (int i = 2; i < fields.size(); i++ ){
            GeneAtlas.tissue tissueToAddList = new GeneAtlas.tissue(header[i], experiment,Float.parseFloat(fields.get(i)));
            tissueList.add(tissueToAddList);
        }

        if (geneAtlasMap.get(geneid) != null){
            geneAtlasMap.get(geneid).getTissues().addAll(tissueList);

        }
        else {

            GeneAtlas geneAtlasInstance = new GeneAtlas(geneid, genename, tissueList);
            geneAtlasMap.put(geneid,geneAtlasInstance);

        }
    }


}
