package org.opencb.cellbase.lib.mongodb.serializer.converter;

import com.google.common.base.Joiner;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.opencb.biodata.models.variant.effect.ConsequenceType;
import org.opencb.biodata.models.variant.effect.VariantEffect;
import org.opencb.cellbase.core.serializer.CellBaseTypeConverter;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by imedina on 17/06/14.
 */
public class VariantEffectConverter implements CellBaseTypeConverter<VariantEffect, DBObject> {

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
    public DBObject convertToStorageSchema(VariantEffect variantEffect) {
        BasicDBObject mongoDbSchema = new BasicDBObject("chr", variantEffect.getChromosome())
                .append("start", variantEffect.getStart())
                .append("end", variantEffect.getEnd())
                .append("ref", variantEffect.getReferenceAllele());



        // All consequence types for the different ALT are stored
        // in an array. A compression using '*' is implemented.
        BasicDBList consequenceTypeSchemaList = new BasicDBList();

        // 'keys' contains all the ALT alleles simulated
        Set<String> keys= variantEffect.getConsequenceTypes().keySet();
        Iterator<String> iterator = keys.iterator();

        // If a critical position 4 ALT alleles are pre-computed: one of A, C, G or T, and the '-'
        // No more are possible can be found (in theory), but maybe some funny SNPs like AA/TTA exist so
        // if "size >= 4" then all possible values are stored.
        if(keys.size() >= 4) {
            while(iterator.hasNext()) {
                String key = iterator.next();
                List<ConsequenceType> consequenceTypes = variantEffect.getConsequenceTypes().get(key);

                BasicDBObject consequenceTypeDBObject = new BasicDBObject("alt", key);
                BasicDBList consequenceTypeDBList = new BasicDBList();
                for(ConsequenceType consequenceType: consequenceTypes) {
                    BasicDBObject consequenceTypeItemDBObject = parseConsequenceTypeToDBObject(consequenceType);
                    consequenceTypeDBList.add(consequenceTypeItemDBObject);
                }
                consequenceTypeDBObject.append("val", consequenceTypeDBList);

                // Add Consequence Type object of this ALT to the List
                consequenceTypeSchemaList.add(consequenceTypeDBObject);
            }
        // If less than the 4 ALT alleles are found means that either 1 ALT allele and the '-' allele were pre-computed
        // (to minimize the number of calls to VEP), or a few multiallelic SNPs were found at that position.
        // ALT allele must be stored as '*' allele when possible.
        }else {
            // If '-' is found then an allele independent position has been found and 1 allele and '-' are expected
            if(keys.contains("-")) {
            System.out.println(keys+" "+variantEffect.getStart()+"-"+variantEffect.getEnd());
                boolean commonFound = false;
                while(iterator.hasNext()) {
                    String key = iterator.next();
                    if(key.equals("-")) {
                        List<ConsequenceType> consequenceTypes = variantEffect.getConsequenceTypes().get(key);

                        BasicDBObject consequenceTypeDBObject = new BasicDBObject("alt", key);
                        BasicDBList consequenceTypeDBList = new BasicDBList();
                        for(ConsequenceType consequenceType: consequenceTypes) {
                            BasicDBObject consequenceTypeItemDBObject = parseConsequenceTypeToDBObject(consequenceType);
                            consequenceTypeDBList.add(consequenceTypeItemDBObject);
                        }
                        consequenceTypeDBObject.append("val", consequenceTypeDBList);

                        // Add Consequence Type object of this ALT to the List
                        consequenceTypeSchemaList.add(consequenceTypeDBObject);
                    }else {
                        // Only the first non '-' is stored
                        if(!commonFound) {
                            commonFound = true;
                            List<ConsequenceType> consequenceTypes = variantEffect.getConsequenceTypes().get(key);

                            BasicDBObject consequenceTypeDBObject = new BasicDBObject("alt", "*");
                            BasicDBList consequenceTypeDBList = new BasicDBList();
                            for(ConsequenceType consequenceType: consequenceTypes) {
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
            }
            // If no '-' is found we process all of them.
            else {
                // 1077844
                while(iterator.hasNext()) {
                    String key = iterator.next();
                    List<ConsequenceType> consequenceTypes = variantEffect.getConsequenceTypes().get(key);

                    BasicDBObject consequenceTypeDBObject = new BasicDBObject("alt", key);
                    BasicDBList consequenceTypeDBList = new BasicDBList();
                    for(ConsequenceType consequenceType: consequenceTypes) {
                        BasicDBObject consequenceTypeItemDBObject = parseConsequenceTypeToDBObject(consequenceType);
                        consequenceTypeDBList.add(consequenceTypeItemDBObject);
                    }
                    consequenceTypeDBObject.append("val", consequenceTypeDBList);

                    // Add Consequence Type object of this ALT to the List
                    consequenceTypeSchemaList.add(consequenceTypeDBObject);
                }
            }
        }
        mongoDbSchema.append("ct", consequenceTypeSchemaList);

        return mongoDbSchema;
    }

    @Override
    public VariantEffect convertToDataModel(DBObject dbObject) {
        return null;
    }





    private BasicDBObject parseConsequenceTypeToDBObject(ConsequenceType consequenceType) {
        // We can save some disk not storing the ALT allele in each document: ("alt", consequenceType.getAllele())
        // This ALT allele MUST BE the same than the 'key'.
        // BasicDBObject consequenceTypeItemDBObject = new BasicDBObject("alt", consequenceType.getAllele());
        BasicDBObject consequenceTypeItemDBObject = new BasicDBObject();
        consequenceTypeItemDBObject.append("gId", consequenceType.getGeneId())
                .append("gName", consequenceType.getGeneName())
                .append("gSrc", consequenceType.getGeneNameSource())
                .append("ftId", consequenceType.getFeatureId())
                .append("ftType", (featureTypes.containsKey(consequenceType.getFeatureType())) ? featureTypes.get(consequenceType.getFeatureType()) : consequenceType.getFeatureType())
                .append("ftBio", (featureBiotypes.containsKey(consequenceType.getFeatureBiotype()) ? featureBiotypes.get(consequenceType.getFeatureBiotype()) : consequenceType.getFeatureBiotype()))
                .append("ftStr", consequenceType.getFeatureStrand());

        if(consequenceType.getcDnaPosition() != -1)
            consequenceTypeItemDBObject.append("cDnaPos", consequenceType.getcDnaPosition());
        if(consequenceType.getCcdsId() != null)
            consequenceTypeItemDBObject.append("ccdsId", consequenceType.getCcdsId());
        if(consequenceType.getCdsPosition() != -1)
            consequenceTypeItemDBObject.append("cdsPos", consequenceType.getCdsPosition());
        if(consequenceType.getProteinId() != null)
            consequenceTypeItemDBObject.append("pId", consequenceType.getProteinId());
        if(consequenceType.getProteinPosition() != -1)
            consequenceTypeItemDBObject.append("pPos", consequenceType.getProteinPosition());
        if(consequenceType.getProteinDomains() != null)
            consequenceTypeItemDBObject.append("pDom", Joiner.on(",").join(consequenceType.getProteinDomains()));
        if(consequenceType.getAminoacidChange() != null)
            consequenceTypeItemDBObject.append("aaCh", consequenceType.getAminoacidChange());
        if(consequenceType.getCodonChange() != null)
            consequenceTypeItemDBObject.append("codCh", consequenceType.getCodonChange());
        if(consequenceType.getVariationId() != null)
            consequenceTypeItemDBObject.append("snpId", consequenceType.getVariationId());
        if(consequenceType.getStructuralVariantsId() != null)
            consequenceTypeItemDBObject.append("svIds", Joiner.on(",").join(consequenceType.getStructuralVariantsId()));
        if(consequenceType.getConsequenceTypes() != null)
            consequenceTypeItemDBObject.append("ctTypes", consequenceType.getConsequenceTypes());
        if(consequenceType.isCanonical() != false)
            consequenceTypeItemDBObject.append("isCcan", consequenceType.isCanonical());
        if(consequenceType.getHgvsc() != null)
            consequenceTypeItemDBObject.append("hgvsc", consequenceType.getHgvsc());
        if(consequenceType.getHgvsp() != null)
            consequenceTypeItemDBObject.append("hgvsp", consequenceType.getHgvsp());
        if(consequenceType.getIntronNumber() != null)
            consequenceTypeItemDBObject.append("inNum", consequenceType.getIntronNumber());
        if(consequenceType.getExonNumber() != null)
            consequenceTypeItemDBObject.append("exNum", consequenceType.getExonNumber());
        if(consequenceType.getVariantToTranscriptDistance() != -1)
            consequenceTypeItemDBObject.append("varTrDist", consequenceType.getVariantToTranscriptDistance());
        if(consequenceType.getClinicalSignificance() != null)
            consequenceTypeItemDBObject.append("clinSig", consequenceType.getClinicalSignificance());
        if(consequenceType.getPubmed() != null)
            consequenceTypeItemDBObject.append("pubmeds", Joiner.on(",").join(consequenceType.getPubmed()));;

        return consequenceTypeItemDBObject;
    }



}
