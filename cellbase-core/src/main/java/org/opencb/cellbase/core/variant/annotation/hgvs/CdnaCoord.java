package org.opencb.cellbase.core.variant.annotation.hgvs;

/**
 * Created by fjlopez on 27/01/17.
 */

public class CdnaCoord {

    public enum Landmark {
        CDNA_START_CODON, CDNA_STOP_CODON
    }

    private int cdsPosition = -1;
    private int startStopCodonOffset = -1;
    private Landmark landmark;

    public CdnaCoord() {
    }

    public int getCdsPosition() {
        return cdsPosition;
    }

    public void setCdsPosition(int cdsPosition) {
        this.cdsPosition = cdsPosition;
    }

    public int getStartStopCodonOffset() {
        return startStopCodonOffset;
    }

    public void setStartStopCodonOffset(int startStopCodonOffset) {
        this.startStopCodonOffset = startStopCodonOffset;
    }

    public Landmark getLandmark() {
        return landmark;
    }

    public void setLandmark(Landmark landmark) {
        this.landmark = landmark;
    }


    @Override
    public String toString() {
        return "CdnaCoord{" +
                "cdsPosition=" + cdsPosition +
                ", startStopCodonOffset=" + startStopCodonOffset +
                ", landmark=" + landmark +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CdnaCoord cdnaCoord = (CdnaCoord) o;

        if (cdsPosition != cdnaCoord.cdsPosition) return false;
        if (startStopCodonOffset != cdnaCoord.startStopCodonOffset) return false;
        return landmark == cdnaCoord.landmark;

    }

    @Override
    public int hashCode() {
        int result = cdsPosition;
        result = 31 * result + startStopCodonOffset;
        result = 31 * result + (landmark != null ? landmark.hashCode() : 0);
        return result;
    }
}
