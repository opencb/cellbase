package org.opencb.cellbase.core.common.variation;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 11/4/13
 * Time: 12:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConsequenceType {

    private String soTerm;
    private String soAccesion;
    private String soDescription;
    private String ensemblConsequenceType;

    public ConsequenceType() {

    }

    public ConsequenceType(String soTerm, String soAccesion, String soDescription, String ensemblConsequenceType) {
        this.soTerm = soTerm;
        this.soAccesion = soAccesion;
        this.soDescription = soDescription;
        this.ensemblConsequenceType = ensemblConsequenceType;
    }

    @Override
    public String toString() {
        return soTerm + "\t" + soAccesion + "\t" + soDescription + "\t" + ensemblConsequenceType;
    }


    public String getSoTerm() {
        return soTerm;
    }

    public void setSoTerm(String soTerm) {
        this.soTerm = soTerm;
    }


    public String getSoAccesion() {
        return soAccesion;
    }

    public void setSoAccesion(String soAccesion) {
        this.soAccesion = soAccesion;
    }


    public String getSoDescription() {
        return soDescription;
    }

    public void setSoDescription(String soDescription) {
        this.soDescription = soDescription;
    }


    public String getEnsemblConsequenceType() {
        return ensemblConsequenceType;
    }

    public void setEnsemblConsequenceType(String ensemblConsequenceType) {
        this.ensemblConsequenceType = ensemblConsequenceType;
    }

}
