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

package org.opencb.cellbase.core.common.variation;

@Deprecated
public class GenomicVariantEffect {

    private String chromosome;
    private int position;
    private String referenceAllele;
    private String alternativeAllele;

    private String featureId;
    private String featureName;
    private String featureType;
    private String featureBiotype;
    private String featureChromosome;
    private int featureStart;
    private int featureEnd;
    private String featureStrand;

    private String snpId;
    private String snpRefAltAlleles;
    private String snpAncestralAllele;
    // TODO: MAF frequencies

    private String geneId;
    private String transcriptId;
    private String geneName;

    private String consequenceTypeSoAccession;
    private String consequenceTypeSoTerm;
    private String consequenceTypeSoDescription;
    private String consequenceTypeCategory;

    private int aaPosition;
    private String aminoacidChange;
    private String codonChange;

    private float polyphenScore;
    private String polyphenPrediction;
    private float siftScore;
    private String siftPrediciton;

    private float phastConsScore;
    private float phyloPScore;


    public GenomicVariantEffect() {
    }

    public GenomicVariantEffect(String chromosome, int position, String referenceAllele, String alternativeAllele,
                                String featureId, String featureName, String featureType, String featureBiotype,
                                String featureChromosome, int featureStart,
                                int featureEnd, String featureStrand, String snpId,
                                String snpRefAltAlleles, String snpAncestralAllele, String geneId,
                                String transcriptId, String geneName, String consequenceTypeSoAccession,
                                String consequenceTypeSoTerm, String consequenceTypeSoDescription,
                                String consequenceTypeCategory, int aaPosition, String aminoacidChange, String codonChange,
                                float polyphenScore, String polyphenPrediction, float siftScore, String siftPrediciton,
                                float phastConsScore, float phyloPScore) {
        this.chromosome = chromosome;
        this.position = position;
        this.referenceAllele = referenceAllele;
        this.alternativeAllele = alternativeAllele;
        this.featureId = featureId;
        this.featureName = featureName;
        this.featureType = featureType;
        this.featureBiotype = featureBiotype;
        this.featureChromosome = featureChromosome;
        this.featureStart = featureStart;
        this.featureEnd = featureEnd;
        this.featureStrand = featureStrand;
        this.snpId = snpId;
        this.snpAncestralAllele = snpAncestralAllele;
        this.snpRefAltAlleles = snpRefAltAlleles;
        this.geneId = geneId;
        this.transcriptId = transcriptId;
        this.geneName = geneName;
        this.consequenceTypeSoAccession = consequenceTypeSoAccession;
        this.consequenceTypeSoTerm = consequenceTypeSoTerm;
        this.consequenceTypeSoDescription = consequenceTypeSoDescription;
        this.consequenceTypeCategory = consequenceTypeCategory;
        this.aaPosition = aaPosition;
        this.aminoacidChange = aminoacidChange;
        this.codonChange = codonChange;
        this.polyphenScore = polyphenScore;
        this.polyphenPrediction = polyphenPrediction;
        this.siftScore = siftScore;
        this.siftPrediciton = siftPrediciton;
        this.phastConsScore = phastConsScore;
        this.phyloPScore = phyloPScore;
    }


    public String toString() {
        StringBuilder br = new StringBuilder();
        return br.append(chromosome).append("\t")
                .append(position).append("\t")
                .append(referenceAllele).append("\t")
                .append(alternativeAllele).append("\t")
                .append(featureId).append("\t")
                .append(featureName).append("\t")
                .append(featureType).append("\t")
                .append(featureBiotype).append("\t")
                .append(featureChromosome).append("\t")
                .append(featureStart).append("\t")
                .append(featureEnd).append("\t")
                .append(featureStrand).append("\t")
                .append(snpId).append("\t")
                .append(snpAncestralAllele).append("\t")
                .append(snpRefAltAlleles).append("\t")
                .append(geneId).append("\t")
                .append(transcriptId).append("\t")
                .append(geneName).append("\t")
                .append(consequenceTypeSoAccession).append("\t")
                .append(consequenceTypeSoTerm).append("\t")
                .append(consequenceTypeSoDescription).append("\t")
                .append(consequenceTypeCategory).append("\t")
                .append(aaPosition).append("\t")
                .append(aminoacidChange).append("\t")
                .append(codonChange).append("\t")
                .append(polyphenScore).append("\t")
                .append(polyphenPrediction).append("\t")
                .append(siftScore).append("\t")
                .append(siftPrediciton).append("\t")
                .append(phastConsScore).append("\t")
                .append(phyloPScore).toString();
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getReferenceAllele() {
        return referenceAllele;
    }

    public void setReferenceAllele(String referenceAllele) {
        this.referenceAllele = referenceAllele;
    }

    public String getAlternativeAllele() {
        return alternativeAllele;
    }

    public void setAlternativeAllele(String alternativeAllele) {
        this.alternativeAllele = alternativeAllele;
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public String getFeatureType() {
        return featureType;
    }

    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

    public String getFeatureBiotype() {
        return featureBiotype;
    }

    public void setFeatureBiotype(String featureBiotype) {
        this.featureBiotype = featureBiotype;
    }

    public String getFeatureChromosome() {
        return featureChromosome;
    }

    public void setFeatureChromosome(String featureChromosome) {
        this.featureChromosome = featureChromosome;
    }

    public int getFeatureStart() {
        return featureStart;
    }

    public void setFeatureStart(int featureStart) {
        this.featureStart = featureStart;
    }

    public int getFeatureEnd() {
        return featureEnd;
    }

    public void setFeatureEnd(int featureEnd) {
        this.featureEnd = featureEnd;
    }

    public String getFeatureStrand() {
        return featureStrand;
    }

    public void setFeatureStrand(String featureStrand) {
        this.featureStrand = featureStrand;
    }

    public String getSnpId() {
        return snpId;
    }

    public void setSnpId(String snpId) {
        this.snpId = snpId;
    }

    public String getSnpAncestralAllele() {
        return snpAncestralAllele;
    }

    public void setSnpAncestralAllele(String snpAncestralAllele) {
        this.snpAncestralAllele = snpAncestralAllele;
    }

    public String getSnpRefAltAlleles() {
        return snpRefAltAlleles;
    }

    public void setSnpRefAltAlleles(String snpRefAltAlleles) {
        this.snpRefAltAlleles = snpRefAltAlleles;
    }

    public String getGeneId() {
        return geneId;
    }

    public void setGeneId(String geneId) {
        this.geneId = geneId;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getConsequenceTypeSoAccession() {
        return consequenceTypeSoAccession;
    }

    public void setConsequenceTypeSoAccession(String consequenceTypeSoAccession) {
        this.consequenceTypeSoAccession = consequenceTypeSoAccession;
    }

    public String getConsequenceTypeSoTerm() {
        return consequenceTypeSoTerm;
    }

    public void setConsequenceTypeSoTerm(String consequenceTypeSoTerm) {
        this.consequenceTypeSoTerm = consequenceTypeSoTerm;
    }

    public String getConsequenceTypeSoDescription() {
        return consequenceTypeSoDescription;
    }

    public void setConsequenceTypeSoDescription(String consequenceTypeSoDescription) {
        this.consequenceTypeSoDescription = consequenceTypeSoDescription;
    }

    public String getConsequenceTypeCategory() {
        return consequenceTypeCategory;
    }

    public void setConsequenceTypeCategory(String consequenceTypeCategory) {
        this.consequenceTypeCategory = consequenceTypeCategory;
    }

    public int getAaPosition() {
        return aaPosition;
    }

    public void setAaPosition(int aaPosition) {
        this.aaPosition = aaPosition;
    }

    public String getAminoacidChange() {
        return aminoacidChange;
    }

    public void setAminoacidChange(String aminoacidChange) {
        this.aminoacidChange = aminoacidChange;
    }

    public String getCodonChange() {
        return codonChange;
    }

    public void setCodonChange(String codonChange) {
        this.codonChange = codonChange;
    }

    public float getPolyphenScore() {
        return polyphenScore;
    }

    public void setPolyphenScore(float polyphenScore) {
        this.polyphenScore = polyphenScore;
    }

    public String getPolyphenPrediction() {
        return polyphenPrediction;
    }

    public void setPolyphenPrediction(String polyphenPrediction) {
        this.polyphenPrediction = polyphenPrediction;
    }

    public float getSiftScore() {
        return siftScore;
    }

    public void setSiftScore(float siftScore) {
        this.siftScore = siftScore;
    }

    public String getSiftPrediciton() {
        return siftPrediciton;
    }

    public void setSiftPrediciton(String siftPrediciton) {
        this.siftPrediciton = siftPrediciton;
    }

    public float getPhastConsScore() {
        return phastConsScore;
    }

    public void setPhastConsScore(float phastConsScore) {
        this.phastConsScore = phastConsScore;
    }

    public float getPhyloPScore() {
        return phyloPScore;
    }

    public void setPhyloPScore(float phyloPScore) {
        this.phyloPScore = phyloPScore;
    }
}
