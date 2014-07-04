package org.opencb.cellbase.lib.mongodb.serializer.converter;

import com.google.common.base.Joiner;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.opencb.biodata.models.variant.effect.VariantAnnotation;
import org.opencb.biodata.models.variant.effect.VariantEffect;
import org.opencb.cellbase.core.serializer.CellBaseTypeConverter;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

        // If a critical position 4 ALT alleles are pre-computed: one of A, C, G or T, and the '-'
        // No more are possible can be found (in theory), but maybe some funny SNPs like AA/TTA exist so
        // if "size >= 4" then all possible values are stored.
        if(keys.size() >= 4) {
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
        // ALT allele must be stored as '*' allele when possible.
        }else {
            // If '-' is found then an allele independent position has been found and 1 allele and '-' are expected
            if(keys.contains("-")) {
            System.out.println(keys+" "+variantAnnotation.getStart()+"-"+variantAnnotation.getEnd());
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
                        // Only the first non '-' is stored
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
            }
            // If no '-' is found we process all of them.
            else {
                // 1077844
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
            }
        }
        mongoDbSchema.append("ct", consequenceTypeSchemaList);

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
        if(variantEffect.isCanonical() != false)
            consequenceTypeItemDBObject.append("isCcan", variantEffect.isCanonical());
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
            consequenceTypeItemDBObject.append("pubmeds", Joiner.on(",").join(variantEffect.getPubmed()));;

        return consequenceTypeItemDBObject;
    }



}
