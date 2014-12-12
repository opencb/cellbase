package org.opencb.cellbase.core.common.clinical.gwas;

/**
 * Created by lcruz on 26/05/14.
 */

import java.util.ArrayList;
import java.util.List;

/** @author Luis Miguel Cruz
 *  @version 1.2.3
 *  @since October 08, 2014  */
public class Gwas {

    private String chromosome;
    private Integer start;
    private Integer end;
    private String reference;
    private String alternate;
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

    public Gwas() {}

    public Gwas(String chromosome, Integer start, Integer end, String reference, String alternate, String region,
                String reportedGenes, String mappedGene, String upstreamGeneId, String downstreamGeneId, String snpGeneIds,
                String upstreamGeneDistance, String downstreamGeneDistance, String strongestSNPRiskAllele, String snpId,
                String merged, String snpIdCurrent, String context, String intergenic, Float riskAlleleFrequency, String cnv) {
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.reference = reference;
        this.alternate = alternate;
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
        this.chromosome = other.chromosome;
        this.start = other.start;
        this.end = other.end;
        this.reference = other.reference;
        this.alternate = other.alternate;
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

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
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

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getAlternate() {
        return alternate;
    }

    public void setAlternate(String alternate) {
        this.alternate = alternate;
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

    public String toString(){
        StringBuilder result = new StringBuilder();

        result.append("-------- GWAS OBJECT -------\n");
        result.append("\t Region: \t"+region+"\n");
        result.append("\t Chromosome_id: \t"+chromosome+"\n");
        result.append("\t Start: \t"+start+"\n");
        result.append("\t End: \t"+end+"\n");
        result.append("\t Reference: \t"+reference+"\n");
        result.append("\t Alternate: \t"+alternate+"\n");
        result.append("\t Reported Gene(s): \t"+reportedGenes+"\n");
        result.append("\t Mapped_gene: \t"+mappedGene+"\n");
        result.append("\t Upstream_gene_id: \t"+upstreamGeneId+"\n");
        result.append("\t Downstream_gene_id: \t"+downstreamGeneId+"\n");
        result.append("\t Snp_gene_ids: \t"+snpGeneIds+"\n");
        result.append("\t Upstream_gene_distance: \t"+upstreamGeneDistance+"\n");
        result.append("\t Downstream_gene_distance: \t"+downstreamGeneDistance+"\n");
        result.append("\t Strongest SNP-Risk Allele: \t"+strongestSNPRiskAllele+"\n");
        result.append("\t SNPs: \t"+ snpId +"\n");
        result.append("\t Merged: \t"+merged+"\n");
        result.append("\t Snp_id_current: \t"+snpIdCurrent+"\n");
        result.append("\t Context: \t"+context+"\n");
        result.append("\t Intergenic: \t"+intergenic+"\n");
        result.append("\t Risk Allele Frequency: \t"+riskAlleleFrequency+"\n");
        result.append("\t CNV: \t"+cnv+"\n");
        result.append("\t-------- STUDIES -------\n");
        for (GwasStudy study : studies) {
            result.append("\t\t-------- Study: -------\n");
            result.append("\t\t PUBMEDID: \t"+ study.getPubmedId() +"\n");
            result.append("\t\t First Author: \t"+ study.getFirstAuthor() +"\n");
            result.append("\t\t Date: \t"+ study.getDate() +"\n");
            result.append("\t\t Journal: \t"+ study.getJournal() +"\n");
            result.append("\t\t Link: \t"+ study.getLink() +"\n");
            result.append("\t\t Study: \t"+ study.getStudy() +"\n");

            result.append("\t\t Initial Sample Size: \t"+ study.getInitialSampleSize() +"\n");
            result.append("\t\t Replication Sample Size: \t"+ study.getReplicationSampleSize() +"\n");
            result.append("\t\t Platform [SNPs passing QC]: \t"+ study.getPlatform() +"\n");
            result.append("\t\t-------- TRAITS -------\n");
            for (GwasTrait trait : study.getTraits()) {
                result.append("\t\t\t-------- Trait: -------\n");
                result.append("\t\t\t Disease/Trait: \t"+ trait.getDiseaseTrait() +"\n");
                result.append("\t\t\t Date Added to Catalog: \t"+ trait.getDateAddedToCatalog() +"\n");
                result.append("\t\t\t-------- TESTS -------\n");
                for (GwasTest test : trait.getTests()) {
                    result.append("\t\t\t\t-------- Test: -------\n");
                    result.append("\t\t\t\t p-Value: \t"+ test.getpValue() +"\n");
                    result.append("\t\t\t\t Pvalue_mlog: \t"+ test.getpValueMlog() +"\n");
                    result.append("\t\t\t\t p-Value (text): \t"+ test.getpValueText() +"\n");
                    result.append("\t\t\t\t OR or beta: \t"+ test.getOrBeta() +"\n");
                    result.append("\t\t\t\t 95% CI (text): \t"+ test.getPercentCI() +"\n");
                }
            }
        }
        result.append("----------------------------\n");

        return result.toString();
    }

}
