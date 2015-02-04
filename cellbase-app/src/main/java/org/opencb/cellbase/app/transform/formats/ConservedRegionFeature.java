package org.opencb.cellbase.app.transform.formats;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lcruz
 * @since 31/10/2014
 */
public class ConservedRegionFeature {
    private String chromosome;
    private int start;
    private int end;
    private int chunk;
    private List<ConservedRegionSource>sources;

    public ConservedRegionFeature(String chromosome, int start, int end, int chunk) {
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.chunk = chunk;
        this.sources = new ArrayList<>();
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

    public int getChunk() {
        return chunk;
    }

    public void setChunk(int chunk) {
        this.chunk = chunk;
    }

    public List<ConservedRegionSource> getSources() {
        return sources;
    }

    public void setSources(List<ConservedRegionSource> sources) {
        this.sources = sources;
    }

    public void addSource(String type, List<Float> values) {
        this.sources.add(new ConservedRegionSource(type, values));
    }

    public static class ConservedRegionSource{
        private String type;
        private List<Float>values;

        public ConservedRegionSource(String type, List<Float> values) {
            this.type = type;
            this.values = values;
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
}
