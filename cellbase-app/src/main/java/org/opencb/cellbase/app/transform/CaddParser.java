package org.opencb.cellbase.app.transform;

import org.broad.tribble.readers.TabixReader;

import org.opencb.cellbase.app.serializers.CellBaseSerializer;
import org.opencb.cellbase.app.transform.formats.Cadd;

import java.io.*;
import java.nio.file.Path;

/**
 * @author Antonio Rueda
 * @author Luis Miguel Cruz.
 * @since October 08, 2014 
 */
public class CaddParser extends CellBaseParser{


    private final Path caddFilePath;
    private final String chrName;

    public CaddParser(Path caddFilePath, String chrName, CellBaseSerializer serializer){
    	super(serializer);
        this.caddFilePath = caddFilePath;
        this.chrName = chrName;
    }

    public void parse(){
        try {
            TabixReader inputReader = new TabixReader(caddFilePath.toString());
            TabixReader.Iterator caddIterator = inputReader.query(chrName);
            Cadd caddVariant = null;
            for (String line; (line = caddIterator.next()) != null;) {
                String[] fields = line.split("\t");
                String chr = fields[0];
                String ref = fields[2];
                String alt = fields[4];

                int pos = Integer.parseInt(fields[1]);

                // If the variant is the same as the last iteration variant, don't print it
                if (sameVariant(caddVariant, chr, pos, ref, alt)) {
                    caddVariant.addCaddValues(Float.parseFloat(fields[88]), Float.parseFloat(fields[89]), fields[68]);
                } else {
                    if (caddVariant != null) {
                        serializer.serialize(caddVariant);
                    }
                    caddVariant = createCaddVariant(fields);
                }
            }
            serializer.serialize(caddVariant);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private boolean sameVariant(Cadd caddVariant, String chr, int pos, String ref, String alt) {
        return caddVariant != null &&
                (caddVariant.getChromosome().equals(chr) && caddVariant.getStart() == pos &&
                        caddVariant.getReference().equals(ref) && caddVariant.getAlternate().equals(alt));
    }
    
    private Cadd createCaddVariant(String[] fields){
    	String ref = fields[2], alt = fields[4], chr = fields[0];
        int pos = Integer.parseInt(fields[1]);
    	
        Float encExp = stringToFloat(fields[29]);
        Float encH3K27Ac = stringToFloat(fields[30]);
        Float encH3K4Me1 = stringToFloat(fields[31]);
        Float encH3K4Me3 = stringToFloat(fields[32]);
        Float encNucleo = stringToFloat(fields[33]);

        Integer encOCC = null;
        if (!fields[34].equals("NA")){
            encOCC = Integer.parseInt(fields[34]);
        }

        Float encOCCombPVal = stringToFloat(fields[35]);
        Float encOCDNasePVal = stringToFloat(fields[36]);
        Float encOCFairePVal = stringToFloat(fields[37]);
        Float encOCpolIIPVal = stringToFloat(fields[38]);
        Float encOCctcfPVal = stringToFloat(fields[39]);
        Float encOCmycPVal = stringToFloat(fields[40]);
        Float encOCDNaseSig = stringToFloat(fields[41]);
        Float encOCFaireSig = stringToFloat(fields[42]);
        Float encOCpolIISig = stringToFloat(fields[43]);
        Float encOCctcfSig = stringToFloat(fields[44]);
        Float encOCmycSig = stringToFloat(fields[45]);

        Cadd caddVariant = new Cadd(
                alt, ref, chr, pos, pos, encExp, encH3K27Ac,
                encH3K4Me1, encH3K4Me3, encNucleo, encOCC,
                encOCCombPVal, encOCDNasePVal, encOCFairePVal,
                encOCpolIIPVal, encOCctcfPVal, encOCmycPVal,
                encOCDNaseSig, encOCFaireSig, encOCpolIISig,
                encOCctcfSig, encOCmycSig);

    	caddVariant.addCaddValues(Float.parseFloat(fields[88]), Float.parseFloat(fields[89]), fields[68]);
    	return caddVariant;
    }
    
    private Float stringToFloat(String floatName){
        if (floatName.equals("NA")){
            return null;
        } else{
            return Float.valueOf(floatName);
        }
    }
}