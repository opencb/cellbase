package org.opencb.cellbase.core.common.protein;

import java.util.Map;

/**
 * Created by imedina on 10/12/13.
 */
public class FunctionPrediction {

    private String checksum;
    private String uniprotId;
    private String transcriptId;
    private int size;
    private Map<Integer, Map<String, Map<String, Float>>> aaPosition;

    public FunctionPrediction() {

    }


    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getUniprotId() {
        return uniprotId;
    }

    public void setUniprotId(String uniprotId) {
        this.uniprotId = uniprotId;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Map<Integer, Map<String, Map<String, Float>>> getAaPosition() {
        return aaPosition;
    }

    public void setAaPosition(Map<Integer, Map<String, Map<String, Float>>> aaPosition) {
        this.aaPosition = aaPosition;
    }
}
