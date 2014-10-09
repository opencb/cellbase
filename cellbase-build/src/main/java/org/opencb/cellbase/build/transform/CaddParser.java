package org.opencb.cellbase.build.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.broad.tribble.readers.TabixReader;
import org.opencb.biodata.models.variant.cadd.Cadd;
import org.opencb.biodata.models.variant.cadd.CaddValues;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Antonio Rueda
 * @author Luis Miguel Cruz.
 * @since October 08, 2014 
 */
public class CaddParser {
	public Path caddFilePath = null;
	public Path outputFilePath = null;
    
    public CaddParser(){
    	this.caddFilePath = null;
    	this.outputFilePath = null;
    }
    
    public CaddParser(Path caddFilePath, Path outputFilePath) {
		this.caddFilePath = caddFilePath;
		this.outputFilePath = outputFilePath;
	}
    

    public void parse(String chrName){
        Cadd caddVariant = new Cadd ();

        try {
        	String line, ref, alt, chr;
            int pos;
            
            PrintWriter writer = new PrintWriter(
            		new BufferedWriter(
            				new FileWriter(outputFilePath.toString())));
            
            try{
            	TabixReader t = new TabixReader(caddFilePath.toString());
        		TabixReader.Iterator tabixIterator;
        		
            	tabixIterator = t.query(chrName);
    			line = tabixIterator.next();
    			Boolean hasElements = false;
    			
    			while(line != null){
    				if (!line.startsWith("##")){
                        String[] fields = line.split("\t");
                        ref = fields[2];
                        alt = fields[4];
                        chr = fields[0];
                        pos = Integer.parseInt(fields[1]);

                        // If the variant is the same as the last iteration variant, don't print it
                        if (caddVariant.getChromosome() != null &&
                        		(caddVariant.getChromosome().equals(chr) && caddVariant.getStart() == pos &&
                        		 caddVariant.getReference().equals(ref) && caddVariant.getAlternate().equals(alt))) {
                            List <CaddValues> caddinfo = caddVariant.getValuesCadd();

                            CaddValues values = 
                            		new CaddValues(Float.parseFloat(fields[88]), Float.parseFloat(fields[89]), fields[68]);
                            caddinfo.add(values);
                            caddVariant.setValuesCadd(caddinfo);
                            hasElements = true;
                        } else {
                            if (caddVariant.getChromosome() != null) {
                                ObjectMapper jsonMapper = new ObjectMapper();
                                jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                                writer.write(/*caddVariant.getChr() + "\t" +
                                		Integer.toString(caddVariant.getPos()) + "\t" +
                                		caddVariant.getReference() + "\t" + 
                                		caddVariant.getAllele() + "\t" + */
                                		jsonMapper.writeValueAsString(caddVariant)+"\n");
                            }

                            List<CaddValues> caddValuesList = new ArrayList<CaddValues>();

                            CaddValues values = new CaddValues(
                            		Float.parseFloat(fields[88]), Float.parseFloat(fields[89]), fields[68]);
                            caddValuesList.add(values);

                            caddVariant = createCaddVariant(fields, caddValuesList);
                            hasElements = true;
                        }
                    }
                    
                    line = tabixIterator.next();
    			}
    			
    			// Print the last element if the variant list is not empty
    			if(hasElements){
    				ObjectMapper jsonMapper = new ObjectMapper();
                    jsonMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                    writer.write(/*caddVariant.getChr() + "\t" +
                    		Integer.toString(caddVariant.getPos()) + "\t" + 
                    		caddVariant.getReference() + "\t" + 
                    		caddVariant.getAllele() + "\t" + */
                    		jsonMapper.writeValueAsString(caddVariant) + "\n");	
    			}
            } catch (ArrayIndexOutOfBoundsException e) {
				e.printStackTrace();
			} finally {
				writer.close();
			}
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private Cadd createCaddVariant(String[] fields, List<CaddValues> caddValuesList){
    	String ref = fields[2], alt = fields[4], chr = fields[0];
        int pos = Integer.parseInt(fields[1]);
    	
        Float EncExp = stringToFloat(fields[29]);
        Float EncH3K27Ac = stringToFloat(fields[30]);
        Float EncH3K4Me1 = stringToFloat(fields[31]);
        Float EncH3K4Me3 = stringToFloat(fields[32]);
        Float EncNucleo = stringToFloat(fields[33]);

        Integer EncOCC = null;
        if (!fields[34].equals("NA")){
            EncOCC = Integer.parseInt(fields[34]);
        }

        Float EncOCCombPVal = stringToFloat(fields[35]);
        Float EncOCDNasePVal = stringToFloat(fields[36]);
        Float EncOCFairePVal = stringToFloat(fields[37]);
        Float EncOCpolIIPVal = stringToFloat(fields[38]);
        Float EncOCctcfPVal = stringToFloat(fields[39]);
        Float EncOCmycPVal = stringToFloat(fields[40]);
        Float EncOCDNaseSig = stringToFloat(fields[41]);
        Float EncOCFaireSig = stringToFloat(fields[42]);
        Float EncOCpolIISig = stringToFloat(fields[43]);
        Float EncOCctcfSig = stringToFloat(fields[44]);
        Float EncOCmycSig = stringToFloat(fields[45]);
    	
    	Cadd caddVariant = new Cadd(
        		alt, ref, chr, pos, pos, EncExp, EncH3K27Ac, 
        		EncH3K4Me1, EncH3K4Me3, EncNucleo, EncOCC,
        		EncOCCombPVal, EncOCDNasePVal, EncOCFairePVal, 
        		EncOCpolIIPVal, EncOCctcfPVal, EncOCmycPVal,
        		EncOCDNaseSig, EncOCFaireSig, EncOCpolIISig,
        		EncOCctcfSig, EncOCmycSig, caddValuesList);
    	
    	return caddVariant;
    }
    
    private Float stringToFloat(String floatName){
        if (floatName.equals("NA")){
            return null;
        }
        else{
            return Float.parseFloat(floatName);
        }
    }
}