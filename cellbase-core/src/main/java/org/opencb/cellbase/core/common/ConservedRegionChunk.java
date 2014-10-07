package org.opencb.cellbase.core.common;

import java.util.List;

public class ConservedRegionChunk extends ConservedRegionFeature {

    private int chunkId;

    public ConservedRegionChunk(String chromosome, int start, int end, String type, int chunkId, List<Float> values) {
        super(chromosome, start, end, type, values);
        this.chunkId = chunkId;
    }

    public int getChunkId() {
        return chunkId;
    }

    public void setChunkId(int chunkId) {
        this.chunkId = chunkId;
    }
}
