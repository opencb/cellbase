package org.opencb.cellbase.core.common.clinical;

import org.opencb.biodata.formats.variant.clinvar.v19jaxb.PublicSetType;

/**
 * Created by parce on 10/29/14.
 */
public class ClinvarPublicSet {
    private String chromosome;
    private int start;
    private int end;
    private String reference;
    private String alternate;
    private PublicSetType clinvarSet;

    public ClinvarPublicSet(String chromosome, int start, int end, String reference, String alternate, PublicSetType clinvarSet) {
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.reference = reference;
        this.alternate = alternate;
        this.clinvarSet = clinvarSet;
    }

    public ClinvarPublicSet() {}

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

    public PublicSetType getClinvarSet() {
        return clinvarSet;
    }

    public void setClinvarSet(PublicSetType clinvarSet) {
        this.clinvarSet = clinvarSet;
    }
}
