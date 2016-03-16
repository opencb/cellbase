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

package org.opencb.cellbase.core.common.clinical;

/**
 * @author by antonior on 5/22/14.
 * @author Luis Miguel Cruz.
 * @since October 08, 2014
 */
public class Cosmic extends ClinicalVariant {

    /**
     * geneName.
     */
    private String geneName;

    /**
     * Mutation GRCh37 strand.
     */
    private String mutationGRCh37Strand;

    /**
     * Primary site.
     */
    private String primarySite;

    /**
     * Mutation zygosity.
     */
    private String mutationZygosity;

    /**
     * Mutation AA.
     */
    private String mutationAA;

    /**
     * Tumour origin.
     */
    private String tumourOrigin;

    /**
     * Histology subtype.
     */
    private String histologySubtype;

    /**
     * Accession Number.
     */
    private String accessionNumber;

    /**
     * Mutation ID.
     */
    private String mutationID;

    /**
     * Mutation CDS.
     */
    private String mutationCDS;

    /**
     * Sample name..
     */
    private String sampleName;

    /**
     * Primary histology.
     */
    private String primaryHistology;

    /**
     * Mutation GRCh37 genome position.
     */
    private String mutationGRCh37GenomePosition;

    /**
     * Mutation Description.
     */
    private String mutationDescription;

    /**
     * Genome-wide screen.
     */
    private String genomeWideScreen;

    /**
     * idTumour.
     */
    private String idTumour;

    /**
     * idSample.
     */
    private String idSample;

    /**
     * Mutation somatic status.
     */
    private String mutationSomaticStatus;

    /**
     * Site subtype.
     */
    private String siteSubtype;

    /**
     * Gene CDS length.
     */
    private int geneCDSLength;

    /**
     * HGNC ID.
     */
    private String hgncId;

    /**
     * Pubmed PMID.
     */
    private String pubmedPMID;

    private String sampleSource;

    /**
     * Age (may be null).
     */
    private Float age;

    /**
     * Comments.
     */
    private String comments;

    private boolean snp;

    private String fathmmPrediction;

    private Integer idStudy;

    public Cosmic() {
        super("cosmic");
    }

    // ----------------------- GETTERS / SETTERS --------------------------------

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getMutationGRCh37Strand() {
        return mutationGRCh37Strand;
    }

    public void setMutationGRCh37Strand(String mutationGRCh37Strand) {
        this.mutationGRCh37Strand = mutationGRCh37Strand;
    }

    public String getPrimarySite() {
        return primarySite;
    }

    public void setPrimarySite(String primarySite) {
        this.primarySite = primarySite;
    }

    public String getMutationZygosity() {
        return mutationZygosity;
    }

    public void setMutationZygosity(String mutationZygosity) {
        this.mutationZygosity = mutationZygosity;
    }

    public String getMutationAA() {
        return mutationAA;
    }

    public void setMutationAA(String mutationAA) {
        this.mutationAA = mutationAA;
    }

    public String getTumourOrigin() {
        return tumourOrigin;
    }

    public void setTumourOrigin(String tumourOrigin) {
        this.tumourOrigin = tumourOrigin;
    }

    public String getHistologySubtype() {
        return histologySubtype;
    }

    public void setHistologySubtype(String histologySubtype) {
        this.histologySubtype = histologySubtype;
    }

    public String getAccessionNumber() {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber) {
        this.accessionNumber = accessionNumber;
    }

    public String getMutationID() {
        return mutationID;
    }

    public void setMutationID(String mutationID) {
        this.mutationID = mutationID;
    }

    public String getMutationCDS() {
        return mutationCDS;
    }

    public void setMutationCDS(String mutationCDS) {
        this.mutationCDS = mutationCDS;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public String getPrimaryHistology() {
        return primaryHistology;
    }

    public void setPrimaryHistology(String primaryHistology) {
        this.primaryHistology = primaryHistology;
    }

    public String getMutationGRCh37GenomePosition() {
        return mutationGRCh37GenomePosition;
    }

    public void setMutationGRCh37GenomePosition(String mutationGRCh37GenomePosition) {
        this.mutationGRCh37GenomePosition = mutationGRCh37GenomePosition;
    }

    public String getMutationDescription() {
        return mutationDescription;
    }

    public void setMutationDescription(String mutationDescription) {
        this.mutationDescription = mutationDescription;
    }

    public String getGenomeWideScreen() {
        return genomeWideScreen;
    }

    public void setGenomeWideScreen(String genomeWideScreen) {
        this.genomeWideScreen = genomeWideScreen;
    }

    public String getIdTumour() {
        return idTumour;
    }

    public void setIdTumour(String idTumour) {
        this.idTumour = idTumour;
    }

    public String getIdSample() {
        return idSample;
    }

    public void setIdSample(String idSample) {
        this.idSample = idSample;
    }

    public String getMutationSomaticStatus() {
        return mutationSomaticStatus;
    }

    public void setMutationSomaticStatus(String mutationSomaticStatus) {
        this.mutationSomaticStatus = mutationSomaticStatus;
    }

    public String getSiteSubtype() {
        return siteSubtype;
    }

    public void setSiteSubtype(String siteSubtype) {
        this.siteSubtype = siteSubtype;
    }

    public int getGeneCDSLength() {
        return geneCDSLength;
    }

    public void setGeneCDSLength(int geneCDSLength) {
        this.geneCDSLength = geneCDSLength;
    }

    public boolean isSnp() {
        return snp;
    }

    public void setSnp(boolean snp) {
        this.snp = snp;
    }

    public String getFathmmPrediction() {
        return fathmmPrediction;
    }

    public void setFathmmPrediction(String fathmmPrediction) {
        this.fathmmPrediction = fathmmPrediction;
    }

    public Integer getIdStudy() {
        return idStudy;
    }

    public void setIdStudy(Integer idStudy) {
        this.idStudy = idStudy;
    }

    public String getHgncId() {
        return hgncId;
    }

    public void setHgncId(String hgncId) {
        this.hgncId = hgncId;
    }

    public String getPubmedPMID() {
        return pubmedPMID;
    }

    public void setPubmedPMID(String pubmedPMID) {
        this.pubmedPMID = pubmedPMID;
    }

    public Float getAge() {
        return age;
    }

    public void setAge(Float age) {
        this.age = age;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getSampleSource() {
        return sampleSource;
    }

    public void setSampleSource(String sampleSource) {
        this.sampleSource = sampleSource;
    }

}
