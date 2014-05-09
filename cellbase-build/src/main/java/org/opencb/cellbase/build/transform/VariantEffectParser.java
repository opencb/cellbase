package org.opencb.cellbase.build.transform;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.models.variant.effect.ConsequenceType;
import org.opencb.biodata.models.variant.effect.ConsequenceTypeMappings;
import org.opencb.biodata.models.variant.effect.ProteinSubstitutionScores;
import org.opencb.biodata.models.variant.effect.VariantEffect;
import org.opencb.cellbase.build.transform.serializers.json.JsonSerializer;
import org.opencb.commons.io.DataWriter;


/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class VariantEffectParser {
    
    private DataWriter serializer;
    
    public VariantEffectParser(DataWriter serializer) {
        this.serializer = serializer;
    }
    
    public int parse(Path file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file.toFile())));
        String line;
        VariantEffect currentEffect = null;
        String currentAllele = null;
        
        int numEffectsWritten = 0;
        
        while((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                continue; // Header will just be ignored
            }
            
            String[] fields = line.split("\t");
            String[] positionFields = fields[0].split("[\\_\\/]");
        
            if (positionFields.length < 4) {
                // Only entries chr_pos_ref/alt will be parsed, ie, 1_909238_G/C or 3_361464_A/-
                // Entries like 5_121187650_duplication will be ignored
                continue;
            }
            
            if (isNewVariant(positionFields[0], Integer.parseInt(positionFields[1]), positionFields[2], positionFields[3], currentEffect, currentAllele)) {
                if (currentEffect != null && serializer != null) {
                    if (serializer.write(currentEffect)) {
                        numEffectsWritten++;
                    }
                }
                
                currentEffect = new VariantEffect(positionFields[0], Integer.parseInt(positionFields[1]), Integer.parseInt(positionFields[1]), positionFields[2]);
                currentAllele = positionFields[3];
            } else if (isNewAllele(positionFields[0], Integer.parseInt(positionFields[1]), positionFields[2], positionFields[3], currentEffect, currentAllele)) {
                currentAllele = positionFields[3];
            }
            
            parseLine(fields, currentEffect, currentAllele);
        }
        
        // Don't forget to serialize the last effect read!
        if (currentEffect != null && serializer != null) {
            if (serializer.write(currentEffect)) {
                numEffectsWritten++;
            }
        }
        
        return numEffectsWritten;
    }
    
    private boolean isNewVariant(String chromosome, int start, String referenceAllele, String alternateAllele, 
            VariantEffect current, String currentAllele) {
        if (current == null) {
            return true;
        }
        
        return !chromosome.equals(current.getChromosome())
                || start != current.getStart()
                || !referenceAllele.equals(current.getReferenceAllele())
//                || !alternateAllele.equals(currentAllele)
                ;
    }
    
    private boolean isNewAllele(String chromosome, int start, String referenceAllele, String alternateAllele, 
            VariantEffect current, String currentAllele) {
        if (current == null) {
            return true;
        }
        
        return chromosome.equals(current.getChromosome())
                && start == current.getStart()
                && referenceAllele.equals(current.getReferenceAllele())
                && !alternateAllele.equals(currentAllele);
    }
    
    private void parseLine(String[] fields, VariantEffect effect, String alternateAllele) {
        ConsequenceType ct = new ConsequenceType(alternateAllele);
        effect.addConsequenceType(alternateAllele, ct);
        
        // Gene and feature fields can be empty (marked with "-")
        if (!"-".equals(fields[3])) {
            ct.setGeneId(fields[3]);
        }
        if (!"-".equals(fields[4])) {
            ct.setFeatureId(fields[4]);
        }
        if (!"-".equals(fields[5])) {
            ct.setFeatureType(fields[5]);
        }
        
        // List of consequence types as SO codes
        String[] consequencesName = fields[6].split(",");
        int[] consequencesSo = new int[consequencesName.length];
        for (int i = 0; i < consequencesName.length; i++) {
            Integer so = ConsequenceTypeMappings.termToAccession.get(consequencesName[i]);
            if (so != null) {
                consequencesSo[i] = so;
            } else {
                Logger.getLogger(JsonSerializer.class.getName()).log(Level.WARNING, "{0} is not a valid consequence type", consequencesName[i]);
            }
        }
        ct.setConsequenceTypes(consequencesSo);
        
        // Fields related to position can be empty (marked with "-")
        if (!"-".equals(fields[7]) && StringUtils.isNumeric(fields[7])) {
            ct.setcDnaPosition(Integer.parseInt(fields[7]));
        }
        if (!"-".equals(fields[8]) && StringUtils.isNumeric(fields[8])) {
            ct.setCdsPosition(Integer.parseInt(fields[8]));
        }
        if (!"-".equals(fields[9]) && StringUtils.isNumeric(fields[9])) {
            ct.setProteinPosition(Integer.parseInt(fields[9]));
        }
        
        // Fields related to AA and codon changes can also be empty (marked with "-")
        if (!"-".equals(fields[10])) {
            ct.setAminoacidChange(fields[10]);
        }
        if (!"-".equals(fields[11])) {
            ct.setCodonChange(fields[11]);
        }
        
        // Variant ID
        if (!"-".equals(fields[12])) {
            ct.setVariationId(fields[12]);
        }
        
        parseExtraFields(fields[13], effect, ct);
    }

    private void parseExtraFields(String extra, VariantEffect effect, ConsequenceType ct) {
        for (String field : extra.split(";")) {
            String[] keyValue = field.split("=");
            
            switch (keyValue[0].toLowerCase()) {
                case "aa_maf":
                    effect.getFrequencies().setMafNhlbiEspAfricanAmerican(Float.parseFloat(keyValue[1]));
                    break;
                case "afr_maf":
                    effect.getFrequencies().setMaf1000GAfrican(Float.parseFloat(keyValue[1]));
                    break;
                case "amr_maf":
                    effect.getFrequencies().setMaf1000GAmerican(Float.parseFloat(keyValue[1]));
                    break;
                case "asn_maf":
                    effect.getFrequencies().setMaf1000GAsian(Float.parseFloat(keyValue[1]));
                    break;
                case "biotype":
                    ct.setFeatureBiotype(keyValue[1]);
                    break;
                case "canonical":
                    ct.setCanonical(keyValue[1].equalsIgnoreCase("YES") || keyValue[1].equalsIgnoreCase("Y"));
                    break;
                case "ccds":
                    ct.setCcdsId(keyValue[1]);
                    break;
                case "cell_type":
                    effect.getRegulatoryEffect().setCellType(keyValue[1]);
                    break;
                case "clin_sig":
                    ct.setClinicalSignificance(keyValue[1]);
                    break;
                case "distance":
                    ct.setVariantToTranscriptDistance(Integer.parseInt(keyValue[1]));
                    break;
                case "domains":
                    ct.setProteinDomains(keyValue[1].split(","));
                    break;
                case "ea_maf":
                    effect.getFrequencies().setMafNhlbiEspEuropeanAmerican(Float.parseFloat(keyValue[1]));
                    break;
                case "ensp":
                    ct.setProteinId(keyValue[1]);
                    break;
                case "eur_maf":
                    effect.getFrequencies().setMaf1000GEuropean(Float.parseFloat(keyValue[1]));
                    break;
                case "exon":
                    ct.setExonNumber(keyValue[1]);
                    break;
                case "gmaf": // Format is GMAF=G:0.2640
                    String[] gmafFields = keyValue[1].split(":");
                    effect.getFrequencies().setAllele1000g(gmafFields[0]);
                    effect.getFrequencies().setMaf1000G(Float.parseFloat(gmafFields[1]));
                    break;
                case "hgvsc":
                    ct.setHgvsc(keyValue[1]);
                    break;
                case "hgvsp":
                    ct.setHgvsp(keyValue[1]);
                    break;
                case "high_inf_pos":
                    effect.getRegulatoryEffect().setHighInformationPosition(keyValue[1].equalsIgnoreCase("YES") || keyValue[1].equalsIgnoreCase("Y"));
                    break;
                case "intron":
                    ct.setIntronNumber(keyValue[1]);
                    break;
                case "motif_name":
                    effect.getRegulatoryEffect().setMotifName(keyValue[1]);
                    break;
                case "motif_pos":
                    effect.getRegulatoryEffect().setMotifPosition(Integer.parseInt(keyValue[1]));
                    break;
                case "motif_score_change":
                    effect.getRegulatoryEffect().setMotifScoreChange(Float.parseFloat(keyValue[1]));
                    break;
                case "polyphen": // Format is PolyPhen=possibly_damaging(0.859)
                    String[] polyphenFields = keyValue[1].split("[\\(\\)]");
                    effect.getProteinSubstitutionScores().setPolyphenEffect(ProteinSubstitutionScores.PolyphenEffect.valueOf(polyphenFields[0].toUpperCase()));
                    effect.getProteinSubstitutionScores().setPolyphenScore(Float.parseFloat(polyphenFields[1]));
                    break;
                case "pubmed":
                    ct.setPubmed(keyValue[1].split(","));
                    break;
                case "sift": // Format is SIFT=tolerated(0.07)
                    String[] siftFields = keyValue[1].split("[\\(\\)]");
                    effect.getProteinSubstitutionScores().setSiftEffect(ProteinSubstitutionScores.SiftEffect.valueOf(siftFields[0].toUpperCase()));
                    effect.getProteinSubstitutionScores().setSiftScore(Float.parseFloat(siftFields[1]));
                    break;
                case "strand":
                    ct.setFeatureStrand(keyValue[1]);
                    break;
                case "sv":
                    ct.setStructuralVariantsId(keyValue[1].split(","));
                    break;
                case "symbol":
                    ct.setGeneName(keyValue[1]);
                    break;
                case "symbol_source":
                    ct.setGeneNameSource(keyValue[1]);
                    break;
                default:
                    // ALLELE_NUM, FREQS, IND, ZYG
                    break;
            }
        }
        
    }
}
