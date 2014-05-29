package org.opencb.cellbase.build.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.biodata.models.variant.CADD.Cadd;
import org.opencb.biodata.models.variant.CADD.CaddValues;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;



/**
 * Created by antonior on 5/22/14.
 */
public class CaddParser {

    public static Float TableFloatToString(String floatName){
        if (floatName.equals("NA")){

            return null;
        }
        else{
            return Float.parseFloat(floatName);
        }



    }


    public static void parse(Path caddFilePath, Path oFilePath){

        Cadd Caddvariant = new Cadd ();


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(caddFilePath.toFile())))) {
            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(oFilePath.toFile())))) {

                String line;
                String[] header;
                String ref;
                String alt;
                String chr;
                int pos;

                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("##")) {
                        continue; // Header will just be ignored
                    } else if (line.startsWith("#")) {
                        header = line.split("\t");

                    } else {
                        String[] fields = line.split("\t");
                        ref = fields[2];
                        alt = fields[4];
                        chr = fields[0];
                        pos = Integer.parseInt(fields[1]);

                        if (Caddvariant.getChr()!=null && (Caddvariant.getChr().equals(chr) && Caddvariant.getPos() == pos && Caddvariant.getReference().equals(ref) && Caddvariant.getAllele().equals(alt))) {
                            List <CaddValues> caddinfo = Caddvariant.getValuesCadd();

                            CaddValues values = new CaddValues(Float.parseFloat(fields[88]), Float.parseFloat(fields[89]), fields[68]);
                            caddinfo.add(values);
                            Caddvariant.setValuesCadd(caddinfo);

                        } else {
                            if (Caddvariant.getChr()!=null) {
                                ObjectMapper jsonMapper = new ObjectMapper();
                                jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                                writer.write(Caddvariant.getChr()+"\t"+Integer.toString(Caddvariant.getPos())+"\t"+Caddvariant.getReference()+"\t"+Caddvariant.getAllele()+"\t"+jsonMapper.writeValueAsString(Caddvariant)+"\n");

                                //print this cadd
                            }

                            Float EncExp = TableFloatToString(fields[29]);
                            Float EncH3K27Ac = TableFloatToString(fields[30]);
                            Float EncH3K4Me1 = TableFloatToString(fields[31]);
                            Float EncH3K4Me3 = TableFloatToString(fields[32]);
                            Float EncNucleo = TableFloatToString(fields[33]);

                            Integer EncOCC = null;
                            if (!fields[34].equals("NA")){
                                EncOCC = Integer.parseInt(fields[34]);
                            }



                            Float EncOCCombPVal = TableFloatToString(fields[35]);
                            Float EncOCDNasePVal = TableFloatToString(fields[36]);
                            Float EncOCFairePVal = TableFloatToString(fields[37]);
                            Float EncOCpolIIPVal = TableFloatToString(fields[38]);
                            Float EncOCctcfPVal = TableFloatToString(fields[39]);
                            Float EncOCmycPVal = TableFloatToString(fields[40]);
                            Float EncOCDNaseSig = TableFloatToString(fields[41]);
                            Float EncOCFaireSig = TableFloatToString(fields[42]);
                            Float EncOCpolIISig = TableFloatToString(fields[43]);
                            Float EncOCctcfSig = TableFloatToString(fields[44]);
                            Float EncOCmycSig = TableFloatToString(fields[45]);
                            List<CaddValues> caddValuesList = new ArrayList<>();


                            CaddValues values = new CaddValues(Float.parseFloat(fields[88]), Float.parseFloat(fields[89]), fields[68]);
                            caddValuesList.add(values);


                            Caddvariant = new Cadd(alt, ref, chr, pos, EncExp, EncH3K27Ac, EncH3K4Me1, EncH3K4Me3, EncNucleo, EncOCC, EncOCCombPVal, EncOCDNasePVal, EncOCFairePVal, EncOCpolIIPVal, EncOCctcfPVal, EncOCmycPVal, EncOCDNaseSig, EncOCFaireSig, EncOCpolIISig, EncOCctcfSig, EncOCmycSig, caddValuesList);

                        }
                    }
                }

                ObjectMapper jsonMapper = new ObjectMapper();
                jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                writer.write(Caddvariant.getChr()+"\t"+Integer.toString(Caddvariant.getPos())+"\t"+Caddvariant.getReference()+"\t"+Caddvariant.getAllele()+"\t"+jsonMapper.writeValueAsString(Caddvariant)+"\n");



            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
