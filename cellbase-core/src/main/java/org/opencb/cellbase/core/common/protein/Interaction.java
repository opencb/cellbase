package org.opencb.cellbase.core.common.protein;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 12/3/13
 * Time: 4:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class Interaction {
    private Interactor interactorA;
    private Interactor interactorB;

    private List<Psimi> type;
    private List<Psimi> detectionMethod;
    private String status;
    private List<Score> score;
    private List<XRef> xrefs;
    private List<Psimi> source;
    private String pubmed;
    private List<String> authors;

    public Interaction(){
    }

    public Interactor getInteractorA() {
        return interactorA;
    }

    public void setInteractorA(Interactor interactorA) {
        this.interactorA = interactorA;
    }

    public Interactor getInteractorB() {
        return interactorB;
    }

    public void setInteractorB(Interactor interactorB) {
        this.interactorB = interactorB;
    }

    public List<Psimi> getType() {
        return type;
    }

    public void setType(List<Psimi> type) {
        this.type = type;
    }

    public List<Psimi> getDetectionMethod() {
        return detectionMethod;
    }

    public void setDetectionMethod(List<Psimi> detectionMethod) {
        this.detectionMethod = detectionMethod;
    }

    public List<Score> getScore() {
        return score;
    }

    public void setScore(List<Score> score) {
        this.score = score;
    }

    public List<XRef> getXrefs() {
        return xrefs;
    }

    public void setXrefs(List<XRef> xrefs) {
        this.xrefs = xrefs;
    }

    public List<Psimi> getSource() {
        return source;
    }

    public void setSource(List<Psimi> source) {
        this.source = source;
    }

    public String getPubmed() {
        return pubmed;
    }

    public void setPubmed(String pubmed) {
        this.pubmed = pubmed;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

