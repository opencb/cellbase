package org.opencb.cellbase.mongodb.serializer.converters;

import com.google.common.base.Joiner;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.opencb.biodata.models.variant.annotation.*;
import org.opencb.cellbase.core.serializer.CellBaseTypeConverter;

import java.util.*;

/**
 * Created by imedina on 17/06/14.
 */
public class VariantEffectConverter implements CellBaseTypeConverter<VariantAnnotation, DBObject> {

    private static final BiMap<String, String> featureTypes = HashBiMap.create();
    private static final BiMap<String, String> featureBiotypes = HashBiMap.create();

    static {
        featureTypes.put("MotifFeature", "MF");
        featureTypes.put("RegulatoryFeature", "RF");
        featureTypes.put("Transcript", "T");

        featureBiotypes.put("processed_transcript", "p_t");
        featureBiotypes.put("unprocessed_pseudogene", "u_p");
        featureBiotypes.put("transcribed_unprocessed_pseudogene", "t_u_p");
        featureBiotypes.put("protein_coding", "p_c");
        featureBiotypes.put("processed_pseudogene", "p_p");
        featureBiotypes.put("retained_intron", "r_i");
        featureBiotypes.put("lincRNA", "l_R");
        featureBiotypes.put("miRNA", "m_R");
    }

    @Override
    public DBObject convertToStorageSchema(VariantAnnotation variantAnnotation) {
        BasicDBObject mongoDbSchema = new BasicDBObject("chr", variantAnnotation.getChromosome())
                .append("start", variantAnnotation.getStart())
                .append("end", variantAnnotation.getEnd())
                .append("ref", variantAnnotation.getReferenceAllele());



        // All consequence types for the different ALT are stored
        // in an array. A compression using '*' is implemented.
        BasicDBList consequenceTypeSchemaList = new BasicDBList();

        // 'keys' contains all the ALT alleles simulated
        Set<String> keys= variantAnnotation.getEffects().keySet();
        Iterator<String> iterator = keys.iterator();

        // Note:
        // During the simulation 2 types of variants are simulated:
        // - common: which means only one allele is simulated since no different results are produced by VEP. This is
        //          the case of UPSTREAM, DOWNSTREAM and INTRONS variants
        // - all: the 3 possible SNV and '-' are simulated. This is done for EXONS and REGULATORY regions
//        System.out.println("num. alleles:\t"+keys.size());
//        if(keys.size() == 3) {
//            System.out.println("==3: "+variantAnnotation+":"+variantAnnotation.getStart()+" alleles: "+keys+"\n");
//        }
//        if(keys.size() == 2 && !keys.contains("T") && !keys.contains("C")) {
//            System.out.println("==2: "+variantAnnotation+":"+variantAnnotation.getStart()+" alleles: "+keys+"\n");
//        }

        // If the different allele at this genomic position can produce different results the 4 possible SNV ALT alleles
        // are pre-computed: one of these A, C, G or T; and '-'
        // No more are possible can be found (in theory), but maybe some funny SNPs like AA/TTA exist so
        // if "size >= 4" then all possible values are stored.
        if(keys.size() >= 4) {
//            System.out.println(">=4: "+variantAnnotation+":"+variantAnnotation.getStart()+" alleles: "+keys+"\n");
            while(iterator.hasNext()) {
                String key = iterator.next();
                List<VariantEffect> consequenceTypes = variantAnnotation.getEffects().get(key);

                BasicDBObject consequenceTypeDBObject = new BasicDBObject("alt", key);
                BasicDBList consequenceTypeDBList = new BasicDBList();
                for(VariantEffect consequenceType: consequenceTypes) {
                    BasicDBObject consequenceTypeItemDBObject = parseConsequenceTypeToDBObject(consequenceType);
                    consequenceTypeDBList.add(consequenceTypeItemDBObject);
                }
                consequenceTypeDBObject.append("val", consequenceTypeDBList);

                // Add Consequence Type object of this ALT to the List
                consequenceTypeSchemaList.add(consequenceTypeDBObject);
            }
        // If less than the 4 ALT alleles are found means that either 1 ALT allele and the '-' allele were pre-computed
        // (to minimize the number of calls to VEP), or a few multiallelic SNPs were found at that position.
        // ALT allele must be stored as '*' allele when possible to save space.
        }else {
            boolean commonFound = false;
            while(iterator.hasNext()) {
                String key = iterator.next();
                if(key.equals("-")) {
                    List<VariantEffect> consequenceTypes = variantAnnotation.getEffects().get(key);

                    BasicDBObject consequenceTypeDBObject = new BasicDBObject("alt", key);
                    BasicDBList consequenceTypeDBList = new BasicDBList();
                    for(VariantEffect consequenceType: consequenceTypes) {
                        BasicDBObject consequenceTypeItemDBObject = parseConsequenceTypeToDBObject(consequenceType);
                        consequenceTypeDBList.add(consequenceTypeItemDBObject);
                    }
                    consequenceTypeDBObject.append("val", consequenceTypeDBList);

                    // Add Consequence Type object of this ALT to the List
                    consequenceTypeSchemaList.add(consequenceTypeDBObject);
                }else {
                    // Only the first non '-' is stored using '*'
                    if(!commonFound) {
                        commonFound = true;
                        List<VariantEffect> consequenceTypes = variantAnnotation.getEffects().get(key);

                        BasicDBObject consequenceTypeDBObject = new BasicDBObject("alt", "*");
                        BasicDBList consequenceTypeDBList = new BasicDBList();
                        for(VariantEffect consequenceType: consequenceTypes) {
                            // HGVS must be encoded with '*'
                            if(consequenceType.getHgvsc() != null) {
                                if(consequenceType.getFeatureStrand().equals("1")) {
                                    String s = consequenceType.getHgvsc().replace(">"+key, ">*");
                                    consequenceType.setHgvsc(s);
                                }else {
                                    String complement = "";
                                    switch (key) {
                                        case "A": complement = "T"; break;
                                        case "T": complement = "A"; break;
                                        case "C": complement = "G"; break;
                                        case "G": complement = "C"; break;
                                    }
                                    String s = consequenceType.getHgvsc().replace(">"+complement, ">*");
                                    consequenceType.setHgvsc(s);
                                }
                            }
                            BasicDBObject consequenceTypeItemDBObject = parseConsequenceTypeToDBObject(consequenceType);
                            consequenceTypeDBList.add(consequenceTypeItemDBObject);
                        }
                        consequenceTypeDBObject.append("val", consequenceTypeDBList);

                        // Add Consequence Type object of this ALT to the List
                        consequenceTypeSchemaList.add(consequenceTypeDBObject);
                    }
                }
            }

//            // If '-' is found then an allele independent position has been found and 1 allele and '-' are expected
//            if(keys.contains("-")) {
//                System.out.println("if: "+variantAnnotation+":"+variantAnnotation.getStart()+" alleles: "+keys+"\n");
////                System.out.println(keys+" "+variantAnnotation.getStart()+"-"+variantAnnotation.getEnd());
//                boolean commonFound = false;
//                while(iterator.hasNext()) {
//                    String key = iterator.next();
//                    if(key.equals("-")) {
//                        List<VariantEffect> consequenceTypes = variantAnnotation.getEffects().get(key);
//
//                        BasicDBObject consequenceTypeDBObject = new BasicDBObject("alt", key);
//                        BasicDBList consequenceTypeDBList = new BasicDBList();
//                        for(VariantEffect consequenceType: consequenceTypes) {
//                            BasicDBObject consequenceTypeItemDBObject = parseConsequenceTypeToDBObject(consequenceType);
//                            consequenceTypeDBList.add(consequenceTypeItemDBObject);
//                        }
//                        consequenceTypeDBObject.append("val", consequenceTypeDBList);
//
//                        // Add Consequence Type object of this ALT to the List
//                        consequenceTypeSchemaList.add(consequenceTypeDBObject);
//                    }else {
//                        // Only the first non '-' is stored using '*'
//                        if(!commonFound) {
//                            commonFound = true;
//                            List<VariantEffect> consequenceTypes = variantAnnotation.getEffects().get(key);
//
//                            BasicDBObject consequenceTypeDBObject = new BasicDBObject("alt", "*");
//                            BasicDBList consequenceTypeDBList = new BasicDBList();
//                            for(VariantEffect consequenceType: consequenceTypes) {
//                                // HGVS must be encoded with '*'
//                                if(consequenceType.getHgvsc() != null) {
//                                    if(consequenceType.getFeatureStrand().equals("1")) {
//                                        String s = consequenceType.getHgvsc().replace(">"+key, ">*");
//                                        consequenceType.setHgvsc(s);
//                                    }else {
//                                        String complement = "";
//                                        switch (key) {
//                                            case "A": complement = "T"; break;
//                                            case "T": complement = "A"; break;
//                                            case "C": complement = "G"; break;
//                                            case "G": complement = "C"; break;
//                                        }
//                                        String s = consequenceType.getHgvsc().replace(">"+complement, ">*");
//                                        consequenceType.setHgvsc(s);
//                                    }
//                                }
//                                BasicDBObject consequenceTypeItemDBObject = parseConsequenceTypeToDBObject(consequenceType);
//                                consequenceTypeDBList.add(consequenceTypeItemDBObject);
//                            }
//                            consequenceTypeDBObject.append("val", consequenceTypeDBList);
//
//                            // Add Consequence Type object of this ALT to the List
//                            consequenceTypeSchemaList.add(consequenceTypeDBObject);
//                        }
//                    }
//                }
//            }
//            // If no '-' is found we process all of them.
//            else {
//                // 1077844
//                System.out.println("else: "+variantAnnotation+":"+variantAnnotation.getStart()+" alleles: "+keys+"\n");
//                while(iterator.hasNext()) {
//                    String key = iterator.next();
//                    List<VariantEffect> consequenceTypes = variantAnnotation.getEffects().get(key);
//
//                    BasicDBObject consequenceTypeDBObject = new BasicDBObject("alt", key);
//                    BasicDBList consequenceTypeDBList = new BasicDBList();
//                    for(VariantEffect consequenceType: consequenceTypes) {
//                        BasicDBObject consequenceTypeItemDBObject = parseConsequenceTypeToDBObject(consequenceType);
//                        consequenceTypeDBList.add(consequenceTypeItemDBObject);
//                    }
//                    consequenceTypeDBObject.append("val", consequenceTypeDBList);
//
//                    // Add Consequence Type object of this ALT to the List
//                    consequenceTypeSchemaList.add(consequenceTypeDBObject);
//                }
//            }
        }
        mongoDbSchema.append("eff", consequenceTypeSchemaList);

        // Parsing Frequencies from VEP
        BasicDBObject frequencyDBObject = parseFrequencies(variantAnnotation.getFrequencies());
        if(frequencyDBObject.size() != 0) {
            mongoDbSchema.append("freqs", frequencyDBObject);
        }

        // Parsing ProteinSubstitutionScores from VEP
        BasicDBObject proteinSubstitutionScoresDBObject = parseProteinSubstituionScores(variantAnnotation.getProteinSubstitutionScores());
        if(proteinSubstitutionScoresDBObject.size() != 0) {
            mongoDbSchema.append("pss", proteinSubstitutionScoresDBObject);
        }

        // Parsing ProteinSubstitutionScores from VEP
        BasicDBObject regulatoryEffectDBObject = parseRegulatoryEffect(variantAnnotation.getRegulatoryEffect());
        if(regulatoryEffectDBObject.size() != 0) {
            mongoDbSchema.append("reg", regulatoryEffectDBObject);
        }

        // Parsing ProteinSubstitutionScores from VEP
//        BasicDBObject genesDBObject = parseGenes(variantAnnotation.getEffects());
//        if(genesDBObject.size() != 0) {
//            mongoDbSchema.append("gn", genesDBObject);
//        }

        return mongoDbSchema;
    }

    @Override
    public VariantAnnotation convertToDataModel(DBObject dbObject) {
        return null;
    }




    private BasicDBObject parseConsequenceTypeToDBObject(VariantEffect variantEffect) {
        // We can save some disk not storing the ALT allele in each document: ("alt", consequenceType.getAllele())
        // This ALT allele MUST BE the same than the 'key'.
        // BasicDBObject consequenceTypeItemDBObject = new BasicDBObject("alt", consequenceType.getAllele());
        BasicDBObject consequenceTypeItemDBObject = new BasicDBObject();
        consequenceTypeItemDBObject.append("gId", variantEffect.getGeneId())
                .append("gName", variantEffect.getGeneName())
                .append("gSrc", variantEffect.getGeneNameSource())
                .append("ftId", variantEffect.getFeatureId())
                .append("ftType", (featureTypes.containsKey(variantEffect.getFeatureType())) ? featureTypes.get(variantEffect.getFeatureType()) : variantEffect.getFeatureType())
                .append("ftBio", (featureBiotypes.containsKey(variantEffect.getFeatureBiotype()) ? featureBiotypes.get(variantEffect.getFeatureBiotype()) : variantEffect.getFeatureBiotype()))
                .append("ftStr", variantEffect.getFeatureStrand());

        if(variantEffect.getcDnaPosition() != -1)
            consequenceTypeItemDBObject.append("cDnaPos", variantEffect.getcDnaPosition());
        if(variantEffect.getCcdsId() != null)
            consequenceTypeItemDBObject.append("ccdsId", variantEffect.getCcdsId());
        if(variantEffect.getCdsPosition() != -1)
            consequenceTypeItemDBObject.append("cdsPos", variantEffect.getCdsPosition());
        if(variantEffect.getProteinId() != null)
            consequenceTypeItemDBObject.append("pId", variantEffect.getProteinId());
        if(variantEffect.getProteinPosition() != -1)
            consequenceTypeItemDBObject.append("pPos", variantEffect.getProteinPosition());
        if(variantEffect.getProteinDomains() != null)
            consequenceTypeItemDBObject.append("pDom", Joiner.on(",").join(variantEffect.getProteinDomains()));
        if(variantEffect.getAminoacidChange() != null)
            consequenceTypeItemDBObject.append("aaCh", variantEffect.getAminoacidChange());
        if(variantEffect.getCodonChange() != null)
            consequenceTypeItemDBObject.append("codCh", variantEffect.getCodonChange());
        if(variantEffect.getVariationId() != null)
            consequenceTypeItemDBObject.append("snpId", variantEffect.getVariationId());
        if(variantEffect.getStructuralVariantsId() != null)
            consequenceTypeItemDBObject.append("svIds", Joiner.on(",").join(variantEffect.getStructuralVariantsId()));
        if(variantEffect.getConsequenceTypes() != null)
            consequenceTypeItemDBObject.append("ctTypes", variantEffect.getConsequenceTypes());
        if(variantEffect.isCanonical())
            consequenceTypeItemDBObject.append("isCan", variantEffect.isCanonical());
        if(variantEffect.getHgvsc() != null)
            consequenceTypeItemDBObject.append("hgvsc", variantEffect.getHgvsc());
        if(variantEffect.getHgvsp() != null)
            consequenceTypeItemDBObject.append("hgvsp", variantEffect.getHgvsp());
        if(variantEffect.getIntronNumber() != null)
            consequenceTypeItemDBObject.append("inNum", variantEffect.getIntronNumber());
        if(variantEffect.getExonNumber() != null)
            consequenceTypeItemDBObject.append("exNum", variantEffect.getExonNumber());
        if(variantEffect.getVariantToTranscriptDistance() != -1)
            consequenceTypeItemDBObject.append("varTrDist", variantEffect.getVariantToTranscriptDistance());
        if(variantEffect.getClinicalSignificance() != null)
            consequenceTypeItemDBObject.append("clinSig", variantEffect.getClinicalSignificance());
        if(variantEffect.getPubmed() != null)
            consequenceTypeItemDBObject.append("pubmeds", Joiner.on(",").join(variantEffect.getPubmed()));

        return consequenceTypeItemDBObject;
    }


    private BasicDBObject parseFrequencies(Map<String, Set<Frequency>> frequencies) {
        BasicDBObject frequencyItemDBObject = new BasicDBObject();
        if(frequencies != null) {
//            frequencyItemDBObject.append("allele1000g", frequencies.get("").getAllele1000g());
//            frequencyItemDBObject.append("maf1000G", frequencies.getMaf1000G());
//            frequencyItemDBObject.append("maf1000GAfrican", frequencies.getMaf1000GAfrican());
//            frequencyItemDBObject.append("maf1000GAmerican", frequencies.getMaf1000GAmerican());
//            frequencyItemDBObject.append("maf1000GAsian", frequencies.getMaf1000GAsian());
//            frequencyItemDBObject.append("maf1000GEuropean", frequencies.getMaf1000GEuropean());
//            frequencyItemDBObject.append("mafNhlbiEspAfricanAmerican", frequencies.getMafNhlbiEspAfricanAmerican());
//            frequencyItemDBObject.append("mafNhlbiEspEuropeanAmerican", frequencies.getMafNhlbiEspEuropeanAmerican());
        }
        return frequencyItemDBObject;
    }

    private BasicDBObject parseProteinSubstituionScores(ProteinSubstitutionScores proteinSubstitutionScores) {
        BasicDBObject proteinSubstitionScoresItemDBObject = new BasicDBObject();
        if(proteinSubstitutionScores.getPolyphenEffect() != null) {
            proteinSubstitionScoresItemDBObject.append("ps", proteinSubstitutionScores.getPolyphenScore());
            proteinSubstitionScoresItemDBObject.append("pe", proteinSubstitutionScores.getPolyphenEffect().name());

        }
        if(proteinSubstitutionScores.getSiftEffect() != null) {
            proteinSubstitionScoresItemDBObject.append("ss", proteinSubstitutionScores.getSiftScore());
            proteinSubstitionScoresItemDBObject.append("se", proteinSubstitutionScores.getSiftEffect().name());
        }
        return proteinSubstitionScoresItemDBObject;
    }

    private BasicDBObject parseRegulatoryEffect(RegulatoryEffect regulatoryEffect) {
        BasicDBObject regulatoryItemDBObject = new BasicDBObject();
        if(regulatoryEffect.getMotifName() != null) {
            regulatoryItemDBObject.append("motifName", regulatoryEffect.getMotifName());
            regulatoryItemDBObject.append("motifPosition", regulatoryEffect.getMotifPosition());
            regulatoryItemDBObject.append("motifScoreChange", regulatoryEffect.getMotifScoreChange());
            regulatoryItemDBObject.append("highInformationPosition", regulatoryEffect.isHighInformationPosition());
            regulatoryItemDBObject.append("cellType", regulatoryEffect.getCellType());
        }
        return regulatoryItemDBObject;
    }

    private BasicDBObject parseGenes(Map<String, List<VariantEffect>> effects) {
        BasicDBObject regulatoryItemDBObject = new BasicDBObject();

        Set<String> visited = new HashSet<>();
        Set<String> keys = effects.keySet();
        Iterator<String> iterator = keys.iterator();
        while(iterator.hasNext()) {
            String next = iterator.next();
            if(!visited.contains(next)) {



                visited.add(next);
            }
        }


//        regulatoryItemDBObject.append("cellType", variantEffect.ge);
        return regulatoryItemDBObject;
    }

}
