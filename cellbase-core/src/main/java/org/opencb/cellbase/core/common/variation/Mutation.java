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

/**
 * This class models a Cosmic mutation, the attributes are found in the TXT file
 * User: imedina
 * Date: 9/21/13
 * Time: 7:37 PM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class Mutation {

    private String id;
    private String chromosome;
    private int start;
    private int end;
    private String strand;
    private String protein;
    private int proteinStart;
    private int proteinEnd;
    private String gene;
    private String transcriptId;
    private String hgncId;
    private String sampleId;
    private String sampleName;
    private String sampleSource;
    private String tumourId;
    private String primarySite;
    private String siteSubtype;
    private String primaryHistology;
    private String histologySubtype;
    private String genomeWideScreen;
    private String mutationCDS;
    private String mutationAA;
    private String mutationZygosity;
    private String status;
    private String pubmed;
    private String tumourOrigin;
    private String description;
    private String source;
//    private String comments;

    public Mutation() {

    }

    public Mutation(String id, String chromosome, int start, int end, String strand, String protein, int proteinStart,
                    int proteinEnd, String gene, String transcriptId, String hgncId, String sampleId, String sampleName,
                    String sampleSource, String tumourId, String primarySite, String siteSubtype, String primaryHistology,
                    String histologySubtype, String genomeWideScreen, String mutationCDS, String mutationAA, String mutationZygosity,
                    String status, String pubmed, String tumourOrigin, String description, String source) {
        this.id = id;
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.strand = strand;
        this.protein = protein;
        this.proteinStart = proteinStart;
        this.proteinEnd = proteinEnd;
        this.gene = gene;
        this.transcriptId = transcriptId;
        this.hgncId = hgncId;
        this.sampleId = sampleId;
        this.sampleName = sampleName;
        this.sampleSource = sampleSource;
        this.tumourId = tumourId;
        this.primarySite = primarySite;
        this.siteSubtype = siteSubtype;
        this.primaryHistology = primaryHistology;
        this.histologySubtype = histologySubtype;
        this.genomeWideScreen = genomeWideScreen;
        this.mutationCDS = mutationCDS;
        this.mutationAA = mutationAA;
        this.mutationZygosity = mutationZygosity;
        this.status = status;
        this.pubmed = pubmed;
        this.tumourOrigin = tumourOrigin;
        this.description = description;
        this.source = source;
    }


    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getProtein() {
        return protein;
    }

    public void setProtein(String protein) {
        this.protein = protein;
    }

    public int getProteinStart() {
        return proteinStart;
    }

    public void setProteinStart(int proteinStart) {
        this.proteinStart = proteinStart;
    }

    public int getProteinEnd() {
        return proteinEnd;
    }

    public void setProteinEnd(int proteinEnd) {
        this.proteinEnd = proteinEnd;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
    }

    public String getHgncId() {
        return hgncId;
    }

    public void setHgncId(String hgncId) {
        this.hgncId = hgncId;
    }

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    public String getSampleId() {
        return sampleId;
    }

    public void setSampleId(String sampleId) {
        this.sampleId = sampleId;
    }

    public String getTumourId() {
        return tumourId;
    }

    public void setTumourId(String tumourId) {
        this.tumourId = tumourId;
    }

    public String getPrimarySite() {
        return primarySite;
    }

    public void setPrimarySite(String primarySite) {
        this.primarySite = primarySite;
    }

    public String getSiteSubtype() {
        return siteSubtype;
    }

    public void setSiteSubtype(String siteSubtype) {
        this.siteSubtype = siteSubtype;
    }

    public String getPrimaryHistology() {
        return primaryHistology;
    }

    public void setPrimaryHistology(String primaryHistology) {
        this.primaryHistology = primaryHistology;
    }

    public String getHistologySubtype() {
        return histologySubtype;
    }

    public void setHistologySubtype(String histologySubtype) {
        this.histologySubtype = histologySubtype;
    }

    public String getGenomeWideScreen() {
        return genomeWideScreen;
    }

    public void setGenomeWideScreen(String genomeWideScreen) {
        this.genomeWideScreen = genomeWideScreen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMutationCDS() {
        return mutationCDS;
    }

    public void setMutationCDS(String mutationCDS) {
        this.mutationCDS = mutationCDS;
    }

    public String getMutationAA() {
        return mutationAA;
    }

    public void setMutationAA(String mutationAA) {
        this.mutationAA = mutationAA;
    }

    public String getMutationZygosity() {
        return mutationZygosity;
    }

    public void setMutationZygosity(String mutationZygosity) {
        this.mutationZygosity = mutationZygosity;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getPubmed() {
        return pubmed;
    }

    public void setPubmed(String pubmed) {
        this.pubmed = pubmed;
    }


    public String getSampleSource() {
        return sampleSource;
    }

    public void setSampleSource(String sampleSource) {
        this.sampleSource = sampleSource;
    }


    public String getTumourOrigin() {
        return tumourOrigin;
    }

    public void setTumourOrigin(String tumourOrigin) {
        this.tumourOrigin = tumourOrigin;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
