package org.opencb.cellbase.core.common.clinical;

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
    private String snps;
    private String merged;
    private String snpIdCurrent;
    private String context;
    private String intergenic;
    private Float riskAlleleFrequency;
    private String cnv;
    private List<GwasStudy> studies;

    public Gwas() {
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
        this.snps = other.snps;
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

    public String getSnps() {
        return snps;
    }

    public void setSnps(String snps) {
        this.snps = snps;
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

    public List<GwasStudy> getStudies() {
        return this.studies;
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
        result.append("\t SNPs: \t"+snps+"\n");
        result.append("\t Merged: \t"+merged+"\n");
        result.append("\t Snp_id_current: \t"+snpIdCurrent+"\n");
        result.append("\t Context: \t"+context+"\n");
        result.append("\t Intergenic: \t"+intergenic+"\n");
        result.append("\t Risk Allele Frequency: \t"+riskAlleleFrequency+"\n");
        result.append("\t CNV: \t"+cnv+"\n");
        result.append("\t-------- STUDIES -------\n");
        for (GwasStudy study : studies) {
            result.append("\t\t-------- Study: -------\n");
            result.append("\t\t PUBMEDID: \t"+study.pubmedId+"\n");
            result.append("\t\t First Author: \t"+study.firstAuthor+"\n");
            result.append("\t\t Date: \t"+study.date+"\n");
            result.append("\t\t Journal: \t"+study.journal+"\n");
            result.append("\t\t Link: \t"+study.link+"\n");
            result.append("\t\t Study: \t"+study.study+"\n");

            result.append("\t\t Initial Sample Size: \t"+study.initialSampleSize+"\n");
            result.append("\t\t Replication Sample Size: \t"+study.replicationSampleSize+"\n");
            result.append("\t\t Platform [SNPs passing QC]: \t"+study.platform+"\n");
            result.append("\t\t-------- TRAITS -------\n");
            for (GwasStudy.GwasTrait trait : study.getTraits()) {
                result.append("\t\t\t-------- Trait: -------\n");
                result.append("\t\t\t Disease/Trait: \t"+trait.diseaseTrait+"\n");
                result.append("\t\t\t Date Added to Catalog: \t"+trait.dateAddedToCatalog+"\n");
                result.append("\t\t\t-------- TESTS -------\n");
                for (GwasStudy.GwasTrait.GwasTest test : trait.tests) {
                    result.append("\t\t\t\t-------- Test: -------\n");
                    result.append("\t\t\t\t p-Value: \t"+test.pValue+"\n");
                    result.append("\t\t\t\t Pvalue_mlog: \t"+test.pValueMlog+"\n");
                    result.append("\t\t\t\t p-Value (text): \t"+test.pValueText+"\n");
                    result.append("\t\t\t\t OR or beta: \t"+test.orBeta+"\n");
                    result.append("\t\t\t\t 95% CI (text): \t"+test.percentCI+"\n");
                }
            }
        }
        result.append("----------------------------\n");

        return result.toString();
    }

    public static class GwasStudy {
        private String pubmedId;
        private String firstAuthor;
        private String date;
        private String journal;
        private String link;
        private String study;
        private String initialSampleSize;
        private String replicationSampleSize;
        private String platform;
        private List<GwasTrait> traits;

        public GwasStudy() {
            this.traits = new ArrayList<>();
        }

        public String getPubmedId() {
            return pubmedId;
        }

        public void setPubmedId(String pubmedId) {
            this.pubmedId = pubmedId;
        }

        public String getFirstAuthor() {
            return firstAuthor;
        }

        public void setFirstAuthor(String firstAuthor) {
            this.firstAuthor = firstAuthor;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getJournal() {
            return journal;
        }

        public void setJournal(String journal) {
            this.journal = journal;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getStudy() {
            return study;
        }

        public void setStudy(String study) {
            this.study = study;
        }

        public String getInitialSampleSize() {
            return initialSampleSize;
        }

        public void setInitialSampleSize(String initialSampleSize) {
            this.initialSampleSize = initialSampleSize;
        }

        public String getReplicationSampleSize() {
            return replicationSampleSize;
        }

        public void setReplicationSampleSize(String replicationSampleSize) {
            this.replicationSampleSize = replicationSampleSize;
        }

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public List<GwasTrait> getTraits() {
            return traits;
        }

        public void setTraits(List<GwasTrait> traits) {
            this.traits = traits;
        }

        public void addTraits(List<GwasTrait> traits) {
            for (GwasTrait trait : traits) {
                addTrait(trait);
            }
        }

        public void addTrait(GwasTrait trait) {
            if (this.traits.contains(trait)) {
                int traitIndex = this.traits.indexOf(trait);
                this.traits.get(traitIndex).addTests(trait.getTests());
            } else {
                this.traits.add(trait);
            }
        }

        @Override
        public boolean equals(Object study) {
            boolean equals = false;
            if (study instanceof GwasStudy) {
                 equals = this.pubmedId.equals(((GwasStudy)study).getPubmedId());
            }
            return equals;
        }

        // TODO: as equals has been overriden, hashCode should be overriden too

        public static class GwasTrait {
            private String diseaseTrait;
            private String dateAddedToCatalog;
            private List<GwasTest> tests;

            public GwasTrait() {
                this.tests = new ArrayList<>();
            }

            public String getDiseaseTrait() {
                return diseaseTrait;
            }

            public void setDiseaseTrait(String diseaseTrait) {
                this.diseaseTrait = diseaseTrait;
            }


            public String getDateAddedToCatalog() {
                return dateAddedToCatalog;
            }

            public void setDateAddedToCatalog(String dateAddedToCatalog) {
                this.dateAddedToCatalog = dateAddedToCatalog;
            }

            public List<GwasTest> getTests() {
                return this.tests;
            }

            public void setTests(List<GwasTest> tests) {
                this.tests = tests;
            }

            public void addTests(List<GwasTest> tests) {
                this.tests.addAll(tests);
            }

            public void addTest(GwasTest test) {
                this.tests.add(test);
            }

            @Override
            public boolean equals(Object o) {
                boolean equals = false;
                if (o instanceof GwasTrait) {
                    equals = this.diseaseTrait.equals(((GwasTrait)o).getDiseaseTrait());
                }
                return equals;
            }

            // TODO: as equals has been overriden, hashCode should be overriden too

            public static class GwasTest {
                private Float pValue;
                private Float pValueMlog;
                private String pValueText;
                private String orBeta;
                private String percentCI;

                public GwasTest() {
                }

                public Float getpValue() {
                    return pValue;
                }

                public void setpValue(Float pValue) {
                    this.pValue = pValue;
                }

                public Float getpValueMlog() {
                    return pValueMlog;
                }

                public void setpValueMlog(Float pValueMlog) {
                    this.pValueMlog = pValueMlog;
                }

                public String getpValueText() {
                    return pValueText;
                }

                public void setpValueText(String pValueText) {
                    this.pValueText = pValueText;
                }

                public String getOrBeta() {
                    return orBeta;
                }

                public void setOrBeta(String orBeta) {
                    this.orBeta = orBeta;
                }

                public String getPercentCI() {
                    return percentCI;
                }

                public void setPercentCI(String percentCI) {
                    this.percentCI = percentCI;
                }

            }
        }
    }
}
