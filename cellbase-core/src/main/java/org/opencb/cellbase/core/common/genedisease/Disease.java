package org.opencb.cellbase.core.common.genedisease;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by fjlopez on 20/05/15.
 */
public class Disease {
    private String diseaseId;
    private String diseaseName;
    private Float score;
    private Integer numberOfPubmeds;
    private List<String> associationTypes;
    private Set<String> sources;

    public Disease(String diseaseId, String diseaseName, Float score, Integer numberOfPubmeds, String associationType, Set<String> sources) {
        this.diseaseId = diseaseId;
        this.diseaseName = diseaseName;
        this.score = score;
        this.numberOfPubmeds = numberOfPubmeds;
        this.associationTypes = new ArrayList<>();
        this.associationTypes.add(associationType);
        this.sources = sources;
    }

    public String getDiseaseId() {
        return diseaseId;
    }

    public void setDiseaseId(String diseaseId) {
        this.diseaseId = diseaseId;
    }

    public String getDiseaseName() {
        return diseaseName;
    }

    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Integer getNumberOfPubmeds() {
        return numberOfPubmeds;
    }

    public void setNumberOfPubmeds(Integer numberOfPubmeds) {
        this.numberOfPubmeds = numberOfPubmeds;
    }

    public List<String> getAssociationTypes() {
        return associationTypes;
    }

    public void setAssociationTypes(List<String> associationTypes) {
        this.associationTypes = associationTypes;
    }

    public Set<String> getSources() {
        return sources;
    }

    public void setSources(Set<String> sources) {
        sources = sources;
    }
}
