package org.opencb.cellbase.core.common;

import java.util.List;

public class ConservedRegionFeature {

    private String chromosome;
    private int start;
    private int end;
    private String type;
    private List<Float> values;

    public ConservedRegionFeature(String chromosome, int start, int end, String type, List<Float> values) {
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.type = type;
        this.values = values;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Float> getValues() {
        return values;
    }

    public void setValues(List<Float> values) {
        this.values = values;
    }
}
