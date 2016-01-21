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

package org.opencb.cellbase.core.common.clinical.gwas;

/**
 * Created by lcruz on 26/05/14.
 */

import org.opencb.cellbase.core.common.clinical.ClinicalVariant;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Luis Miguel Cruz
 * @version 1.2.3
 * @since October 08, 2014
 */
public class Gwas extends ClinicalVariant {

    private String region;
    private String reportedGenes;
    private String mappedGene;
    private String upstreamGeneId;
    private String downstreamGeneId;
    private String snpGeneIds;
    private String upstreamGeneDistance;
    private String downstreamGeneDistance;
    private String strongestSNPRiskAllele;
    private String snpId;
    private String merged;
    private String snpIdCurrent;
    private String context;
    private String intergenic;
    private Float riskAlleleFrequency;
    private String cnv;
    private List<GwasStudy> studies;

    public Gwas(String chromosome, Integer start, Integer end, String reference, String alternate, String region,
                String reportedGenes, String mappedGene, String upstreamGeneId, String downstreamGeneId, String snpGeneIds,
                String upstreamGeneDistance, String downstreamGeneDistance, String strongestSNPRiskAllele, String snpId,
                String merged, String snpIdCurrent, String context, String intergenic, Float riskAlleleFrequency, String cnv) {
        super(chromosome, start, end, reference, alternate, "gwas");
        this.region = region;
        this.reportedGenes = reportedGenes;
        this.mappedGene = mappedGene;
        this.upstreamGeneId = upstreamGeneId;
        this.downstreamGeneId = downstreamGeneId;
        this.snpGeneIds = snpGeneIds;
        this.upstreamGeneDistance = upstreamGeneDistance;
        this.downstreamGeneDistance = downstreamGeneDistance;
        this.strongestSNPRiskAllele = strongestSNPRiskAllele;
        this.snpId = snpId;
        this.merged = merged;
        this.snpIdCurrent = snpIdCurrent;
        this.context = context;
        this.intergenic = intergenic;
        this.riskAlleleFrequency = riskAlleleFrequency;
        this.cnv = cnv;
        this.studies = new ArrayList<>();
    }

    public Gwas(Gwas other) {
        super(other.getChromosome(), other.getStart(), other.getEnd(), other.getReference(), other.getAlternate(), "gwas");
        this.region = other.region;
        this.reportedGenes = other.reportedGenes;
        this.mappedGene = other.mappedGene;
        this.upstreamGeneId = other.upstreamGeneId;
        this.downstreamGeneId = other.downstreamGeneId;
        this.snpGeneIds = other.snpGeneIds;
        this.upstreamGeneDistance = other.upstreamGeneDistance;
        this.downstreamGeneDistance = other.downstreamGeneDistance;
        this.strongestSNPRiskAllele = other.strongestSNPRiskAllele;
        this.snpId = other.snpId;
        this.merged = other.merged;
        this.snpIdCurrent = other.snpIdCurrent;
        this.context = other.context;
        this.intergenic = other.intergenic;
        this.riskAlleleFrequency = other.riskAlleleFrequency;
        this.cnv = other.cnv;
        this.studies = other.studies;
    }

    // ---------------------------------- GETTERS / SETTERS ------------------------------

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getReportedGenes() {
        return reportedGenes;
    }

    public void setReportedGenes(String reportedGenes) {
        this.reportedGenes = reportedGenes;
    }

    public String getMappedGene() {
        return mappedGene;
    }

    public void setMappedGene(String mappedGene) {
        this.mappedGene = mappedGene;
    }

    public String getUpstreamGeneId() {
        return upstreamGeneId;
    }

    public void setUpstreamGeneId(String upstreamGeneId) {
        this.upstreamGeneId = upstreamGeneId;
    }

    public String getDownstreamGeneId() {
        return downstreamGeneId;
    }

    public void setDownstreamGeneId(String downstreamGeneId) {
        this.downstreamGeneId = downstreamGeneId;
    }

    public String getSnpGeneIds() {
        return snpGeneIds;
    }

    public void setSnpGeneIds(String snpGeneIds) {
        this.snpGeneIds = snpGeneIds;
    }

    public String getUpstreamGeneDistance() {
        return upstreamGeneDistance;
    }

    public void setUpstreamGeneDistance(String upstreamGeneDistance) {
        this.upstreamGeneDistance = upstreamGeneDistance;
    }

    public String getDownstreamGeneDistance() {
        return downstreamGeneDistance;
    }

    public void setDownstreamGeneDistance(String downstreamGeneDistance) {
        this.downstreamGeneDistance = downstreamGeneDistance;
    }

    public String getStrongestSNPRiskAllele() {
        return strongestSNPRiskAllele;
    }

    public void setStrongestSNPRiskAllele(String strongestSNPRiskAllele) {
        this.strongestSNPRiskAllele = strongestSNPRiskAllele;
    }

    public String getSnpId() {
        return snpId;
    }

    public void setSnpId(String snpId) {
        this.snpId = snpId;
    }

    public String getMerged() {
        return merged;
    }

    public void setMerged(String merged) {
        this.merged = merged;
    }

    public String getSnpIdCurrent() {
        return snpIdCurrent;
    }

    public void setSnpIdCurrent(String snpIdCurrent) {
        this.snpIdCurrent = snpIdCurrent;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getIntergenic() {
        return intergenic;
    }

    public void setIntergenic(String intergenic) {
        this.intergenic = intergenic;
    }

    public Float getRiskAlleleFrequency() {
        return riskAlleleFrequency;
    }

    public void setRiskAlleleFrequency(Float riskAlleleFrequency) {
        this.riskAlleleFrequency = riskAlleleFrequency;
    }

    public String getCnv() {
        return cnv;
    }

    public void setCnv(String cnv) {
        this.cnv = cnv;
    }

    public List<GwasStudy> getStudies() {
        return this.studies;
    }

    public void setStudies(List<GwasStudy> studies) {
        this.studies = studies;
    }

    public void addStudies(List<GwasStudy> studies) {
        for (GwasStudy study : studies) {
            this.addStudy(study);
        }
    }

    public void addStudy(GwasStudy study) {
        if (this.studies.contains(study)) {
            int studyIndex = this.studies.indexOf(study);
            this.studies.get(studyIndex).addTraits(study.getTraits());
        } else {
            this.studies.add(study);
        }
    }
}
