package org.opencb.cellbase.core.common.variation;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 9/21/13
 * Time: 7:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class Mutation {

    private String chromosome;
    private int start;
    private int end;
    private String strand;
    private String geneName;
    private String ensemblTranscriptId;
    private String hgncId;
    private String sampleName;
    private String sampleId;
    private String tumourId;
    private String primarySite;
    private String siteSubtype;
    private String primaryHistology;
    private String histologySubtype;
    private String genomeWideScreen;
    private String mutationID;
    private String mutationCDS;
    private String mutationAA;
    private String mutationDescription;
    private String mutationZygosity;
    private String mutationSomaticStatus;
    private String pubmedPMID;
    private String sampleSource;
    private String tumourOrigin;
    private String comments;

    public Mutation() {
    }

    public Mutation(String chromosome, int start, int end, String strand, String geneName, String ensemblTranscriptId, String hgncId, String sampleName, String sampleId, String tumourId, String primarySite, String siteSubtype, String primaryHistology, String histologySubtype, String genomeWideScreen, String mutationID, String mutationCDS, String mutationAA, String mutationDescription, String mutationZygosity, String mutationSomaticStatus, String pubmedPMID, String sampleSource, String tumourOrigin, String comments) {
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.strand = strand;
        this.geneName = geneName;
        this.ensemblTranscriptId = ensemblTranscriptId;
        this.hgncId = hgncId;
        this.sampleName = sampleName;
        this.sampleId = sampleId;
        this.tumourId = tumourId;
        this.primarySite = primarySite;
        this.siteSubtype = siteSubtype;
        this.primaryHistology = primaryHistology;
        this.histologySubtype = histologySubtype;
        this.genomeWideScreen = genomeWideScreen;
        this.mutationID = mutationID;
        this.mutationCDS = mutationCDS;
        this.mutationAA = mutationAA;
        this.mutationDescription = mutationDescription;
        this.mutationZygosity = mutationZygosity;
        this.mutationSomaticStatus = mutationSomaticStatus;
        this.pubmedPMID = pubmedPMID;
        this.sampleSource = sampleSource;
        this.tumourOrigin = tumourOrigin;
        this.comments = comments;
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

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getEnsemblTranscriptId() {
        return ensemblTranscriptId;
    }

    public void setEnsemblTranscriptId(String ensemblTranscriptId) {
        this.ensemblTranscriptId = ensemblTranscriptId;
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

    public String getMutationAA() {
        return mutationAA;
    }

    public void setMutationAA(String mutationAA) {
        this.mutationAA = mutationAA;
    }

    public String getMutationDescription() {
        return mutationDescription;
    }

    public void setMutationDescription(String mutationDescription) {
        this.mutationDescription = mutationDescription;
    }

    public String getMutationZygosity() {
        return mutationZygosity;
    }

    public void setMutationZygosity(String mutationZygosity) {
        this.mutationZygosity = mutationZygosity;
    }

    public String getMutationSomaticStatus() {
        return mutationSomaticStatus;
    }

    public void setMutationSomaticStatus(String mutationSomaticStatus) {
        this.mutationSomaticStatus = mutationSomaticStatus;
    }

    public String getPubmedPMID() {
        return pubmedPMID;
    }

    public void setPubmedPMID(String pubmedPMID) {
        this.pubmedPMID = pubmedPMID;
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

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
