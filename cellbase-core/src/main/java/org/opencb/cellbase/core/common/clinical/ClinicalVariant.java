package org.opencb.cellbase.core.common.clinical;

/**
 * Created by pol on 20/08/15.
 */
public abstract class ClinicalVariant {

    private String chromosome;
    private int start;
    private int end;
    private String reference;
    private String alternate;
    private String source;

    public ClinicalVariant(String source) {
        this.source = source;
    }

    public ClinicalVariant(String chromosome, int start, int end, String reference, String alternate, String source) {
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.reference = reference;
        this.alternate = alternate;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
