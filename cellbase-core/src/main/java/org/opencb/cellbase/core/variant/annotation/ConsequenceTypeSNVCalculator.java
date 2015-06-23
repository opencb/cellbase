package org.opencb.cellbase.core.variant.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.feature.Region;
import org.opencb.biodata.models.variant.annotation.ConsequenceType;
import org.opencb.biodata.models.variant.annotation.ExpressionValue;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by fjlopez on 22/06/15.
 */
public class ConsequenceTypeSNVCalculator implements ConsequenceTypeCalculator {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    private HashSet<String> SoNames = new HashSet<>();
    private ConsequenceType consequenceType;
    private Gene gene;
    private GenomicVariant variant;

    public ConsequenceTypeSNVCalculator() {
    }

    public List<ConsequenceType> run(GenomicVariant inputVariant, List<Gene> geneList, List<Region> regulatoryRegionList) {

        variant = inputVariant;
        for(Gene currentGene: geneList) {
            gene = currentGene;
            for(Transcript currentTranscript : gene.getTranscripts()) {
                consequenceType = new ConsequenceType();
                consequenceType.setGeneName(gene.getName());
                consequenceType.setEnsemblGeneId(gene.getId());
                consequenceType.setEnsemblTranscriptId(currentTranscript.getId());
                consequenceType.setStrand(currentTranscript.getStrand());
                consequenceType.setBiotype(currentTranscript.getBiotype());
                SoNames.clear();

                if(currentTranscript.getStrand().equals("+")) {
                    // Check variant overlaps transcript start/end coordinates
                    if(variant.getPosition()>=currentTranscript.getStart() &&
                            variant.getPosition()<=currentTranscript.getEnd()) {
                        switch (currentTranscript.getBiotype()) {
                            /**
                             * Coding biotypes
                             */
                            case VariantAnnotationUtils.NONSENSE_MEDIATED_DECAY:
                                SoNames.add("NMD_transcript_variant");
                            case VariantAnnotationUtils.IG_C_GENE:
                            case VariantAnnotationUtils.IG_D_GENE:
                            case VariantAnnotationUtils.IG_J_GENE:
                            case VariantAnnotationUtils.IG_V_GENE:
                            case VariantAnnotationUtils.TR_C_GENE:  // TR_C_gene
                            case VariantAnnotationUtils.TR_D_GENE:  // TR_D_gene
                            case VariantAnnotationUtils.TR_J_GENE:  // TR_J_gene
                            case VariantAnnotationUtils.TR_V_GENE:  // TR_V_gene
                            case VariantAnnotationUtils.POLYMORPHIC_PSEUDOGENE:
                            case VariantAnnotationUtils.PROTEIN_CODING:    // protein_coding
                            case VariantAnnotationUtils.NON_STOP_DECAY:    // non_stop_decay
                            case VariantAnnotationUtils.TRANSLATED_PROCESSED_PSEUDOGENE:
                            case VariantAnnotationUtils.TRANSLATED_UNPROCESSED_PSEUDOGENE:    // translated_unprocessed_pseudogene
                            case VariantAnnotationUtils.LRG_GENE:    // LRG_gene
                                solveCodingPositiveTranscript();
                                consequenceType.setSoTermsFromSoNames(new ArrayList<>(SoNames));
                                break;
                            /**
                             * pseudogenes, antisense should not be annotated as non-coding genes
                             */
                            case VariantAnnotationUtils.SNORNA_PSEUDOGENE:
                            case VariantAnnotationUtils.SNRNA_PSEUDOGENE:
                            case VariantAnnotationUtils.SCRNA_PSEUDOGENE:
                            case VariantAnnotationUtils.RRNA_PSEUDOGENE:
                            case VariantAnnotationUtils.MISC_RNA_PSEUDOGENE:
                            case VariantAnnotationUtils.MIRNA_PSEUDOGENE:
                            case VariantAnnotationUtils.TRANSCRIBED_UNITARY_PSEUDOGENE:
                                solveNonCodingPositiveTranscript(isInsertion, variant, SoNames, transcriptInfo,
                                        transcriptStart, transcriptEnd, null, variantStart, variantEnd,
                                        consequenceTypeTemplate);
                                consequenceTypeList.add(new ConsequenceType(consequenceTypeTemplate.getGeneName(),
                                        consequenceTypeTemplate.getEnsemblGeneId(),
                                        consequenceTypeTemplate.getEnsemblTranscriptId(),
                                        consequenceTypeTemplate.getStrand(),
                                        consequenceTypeTemplate.getBiotype(),
                                        consequenceTypeTemplate.getcDnaPosition(), new ArrayList<>(SoNames),
                                        consequenceTypeTemplate.getExpressionValues()));
                                break;
                            /**
                             * Non-coding biotypes
                             */
                            case VariantAnnotationUtils.MIRNA:  // miRNA
                                miRnaInfo = (BasicDBObject) geneInfo.get("mirna");
                            default:
                                solveNonCodingPositiveTranscript(isInsertion, variant, SoNames, transcriptInfo,
                                        transcriptStart, transcriptEnd, miRnaInfo, variantStart, variantEnd,
                                        consequenceTypeTemplate);
                                consequenceTypeList.add(new ConsequenceType(consequenceTypeTemplate.getGeneName(),
                                        consequenceTypeTemplate.getEnsemblGeneId(),
                                        consequenceTypeTemplate.getEnsemblTranscriptId(),
                                        consequenceTypeTemplate.getStrand(),
                                        consequenceTypeTemplate.getBiotype(),
                                        consequenceTypeTemplate.getcDnaPosition(), new ArrayList<>(SoNames),
                                        consequenceTypeTemplate.getExpressionValues()));
                                break;
                        }
                    } else {
                        solveTranscriptFlankingRegions(SoNames, transcriptStart, transcriptEnd, variantStart, variantEnd,
                                "upstream_gene_variant", "downstream_gene_variant");
                        if (SoNames.size() > 0) { // Variant does not overlap gene region, just may have upstream/downstream annotations
                            consequenceTypeList.add(new ConsequenceType(consequenceTypeTemplate.getGeneName(),
                                    consequenceTypeTemplate.getEnsemblGeneId(),
                                    consequenceTypeTemplate.getEnsemblTranscriptId(),
                                    consequenceTypeTemplate.getStrand(),
                                    consequenceTypeTemplate.getBiotype(), new ArrayList<>(SoNames),
                                    consequenceTypeTemplate.getExpressionValues()));
                        }
                    }

                } else {
                    // Check overlaps transcript start/end coordinates
                    if(variant.getPosition()>=currentTranscript.getStart() &&
                            variant.getPosition()<=currentTranscript.getEnd()) {
                        switch (currentTranscript.getBiotype()) {
                            /**
                             * Coding biotypes
                             */
                            case VariantAnnotationUtils.NONSENSE_MEDIATED_DECAY:
                                SoNames.add("NMD_transcript_variant");
                            case VariantAnnotationUtils.IG_C_GENE:
                            case VariantAnnotationUtils.IG_D_GENE:
                            case VariantAnnotationUtils.IG_J_GENE:
                            case VariantAnnotationUtils.IG_V_GENE:
                            case VariantAnnotationUtils.TR_C_GENE:  // TR_C_gene
                            case VariantAnnotationUtils.TR_D_GENE:  // TR_D_gene
                            case VariantAnnotationUtils.TR_J_GENE:  // TR_J_gene
                            case VariantAnnotationUtils.TR_V_GENE:  // TR_V_gene
                            case VariantAnnotationUtils.POLYMORPHIC_PSEUDOGENE:
                            case VariantAnnotationUtils.PROTEIN_CODING:    // protein_coding
                            case VariantAnnotationUtils.NON_STOP_DECAY:    // non_stop_decay
                            case VariantAnnotationUtils.TRANSLATED_PROCESSED_PSEUDOGENE:
                            case VariantAnnotationUtils.TRANSLATED_UNPROCESSED_PSEUDOGENE:    // translated_unprocessed_pseudogene
                            case VariantAnnotationUtils.LRG_GENE:    // LRG_gene
                                solveCodingNegativeTranscript(isInsertion, variant, SoNames, transcriptInfo, transcriptStart,
                                        transcriptEnd, variantStart, variantEnd, cdsLength, transcriptFlags,
                                        consequenceTypeTemplate);
                                consequenceTypeList.add(new ConsequenceType(consequenceTypeTemplate.getGeneName(),
                                        consequenceTypeTemplate.getEnsemblGeneId(),
                                        consequenceTypeTemplate.getEnsemblTranscriptId(),
                                        consequenceTypeTemplate.getStrand(),
                                        consequenceTypeTemplate.getBiotype(),
                                        consequenceTypeTemplate.getcDnaPosition(),
                                        consequenceTypeTemplate.getCdsPosition(),
                                        consequenceTypeTemplate.getAaPosition(),
                                        consequenceTypeTemplate.getAaChange(),
                                        consequenceTypeTemplate.getCodon(),
                                        consequenceTypeTemplate.getProteinSubstitutionScores(),
                                        new ArrayList<>(SoNames),
                                        consequenceTypeTemplate.getExpressionValues()));
                                break;
                            /**
                             * pseudogenes, antisense should not be annotated as non-coding genes
                             */
                            case VariantAnnotationUtils.SNORNA_PSEUDOGENE:
                            case VariantAnnotationUtils.SNRNA_PSEUDOGENE:
                            case VariantAnnotationUtils.SCRNA_PSEUDOGENE:
                            case VariantAnnotationUtils.RRNA_PSEUDOGENE:
                            case VariantAnnotationUtils.MISC_RNA_PSEUDOGENE:
                            case VariantAnnotationUtils.MIRNA_PSEUDOGENE:
                            case VariantAnnotationUtils.TRANSCRIBED_UNITARY_PSEUDOGENE:
                                solveNonCodingNegativeTranscript(isInsertion, variant, SoNames, transcriptInfo,
                                        transcriptStart, transcriptEnd, null, variantStart, variantEnd, consequenceTypeTemplate);
                                consequenceTypeList.add(new ConsequenceType(consequenceTypeTemplate.getGeneName(),
                                        consequenceTypeTemplate.getEnsemblGeneId(),
                                        consequenceTypeTemplate.getEnsemblTranscriptId(),
                                        consequenceTypeTemplate.getStrand(),
                                        consequenceTypeTemplate.getBiotype(),
                                        consequenceTypeTemplate.getcDnaPosition(), new ArrayList<>(SoNames),
                                        consequenceTypeTemplate.getExpressionValues()));
                                break;
                            /**
                             * Non-coding biotypes
                             */
                            case VariantAnnotationUtils.MIRNA:  // miRNA
                                miRnaInfo = (BasicDBObject) geneInfo.get("mirna");
                            default:
                                solveNonCodingNegativeTranscript(isInsertion, variant, SoNames, transcriptInfo,
                                        transcriptStart, transcriptEnd, miRnaInfo, variantStart, variantEnd, consequenceTypeTemplate);
                                consequenceTypeList.add(new ConsequenceType(consequenceTypeTemplate.getGeneName(),
                                        consequenceTypeTemplate.getEnsemblGeneId(),
                                        consequenceTypeTemplate.getEnsemblTranscriptId(),
                                        consequenceTypeTemplate.getStrand(),
                                        consequenceTypeTemplate.getBiotype(),
                                        consequenceTypeTemplate.getcDnaPosition(), new ArrayList<>(SoNames),
                                        consequenceTypeTemplate.getExpressionValues()));
                                break;
                        }
                    } else {
                        solveTranscriptFlankingRegions(SoNames, transcriptStart, transcriptEnd, variantStart,
                                variantEnd, "downstream_gene_variant", "upstream_gene_variant");
                        if (SoNames.size() > 0) { // Variant does not overlap gene region, just has upstream/downstream annotations
                            consequenceTypeList.add(new ConsequenceType(consequenceTypeTemplate.getGeneName(),
                                    consequenceTypeTemplate.getEnsemblGeneId(),
                                    consequenceTypeTemplate.getEnsemblTranscriptId(),
                                    consequenceTypeTemplate.getStrand(),
                                    consequenceTypeTemplate.getBiotype(), new ArrayList<>(SoNames),
                                    consequenceTypeTemplate.getExpressionValues()));
                        }
                    }

                }
            }
        }

        if(consequenceTypeList.size() == 0) {
            consequenceTypeList.add(new ConsequenceType("intergenic_variant"));
        }

        LinkedList regulatoryInfoList = (LinkedList) regulatoryQueryResult.getResult();
//        BasicDBList regulatoryInfoList = (BasicDBList) regulatoryQueryResult.getResult();
        if(!regulatoryInfoList.isEmpty()) {
            consequenceTypeList.add(new ConsequenceType("regulatory_region_variant"));
            i = 0;
            do {
                regulatoryInfo = (BasicDBObject) regulatoryInfoList.get(i);
                regulatoryType = (String) regulatoryInfo.get("featureType");
                TFBSFound = regulatoryType.equals("TF_binding_site") || regulatoryType.equals("TF_binding_site_motif");
                i++;
            } while(i<regulatoryInfoList.size() && !TFBSFound);
            if(TFBSFound) {
                consequenceTypeList.add(new ConsequenceType("TF_binding_site_variant"));
            }
        } else {
            int b;
            b = 1;
        }

//        if(transcriptInfoList == null) {
//            consequenceTypeList.add(new ConsequenceType("intergenic_variant"));
//        }

//        consequenceTypeList = filterConsequenceTypesBySoTerms(consequenceTypeList, options.getAsStringList("so"));
        // setting queryResult fields
        queryResult.setId(variant.toString());
        queryResult.setDbTime(Long.valueOf(dbTimeEnd - dbTimeStart).intValue());
        queryResult.setNumResults(consequenceTypeList.size());
        queryResult.setResult(consequenceTypeList);

        return queryResult;

    }

}
