package org.opencb.cellbase.core.variant.annotation.hgvs;

/**
 * Created by fjlopez on 27/01/17.
 */

public class CdnaCoord {

    public enum Landmark {
        CDNA_START_CODON, TRANSCRIPT_START, CDNA_STOP_CODON
    }

    private int referencePosition = 0;
    private int offset = 0;
    private Landmark landmark;

    public CdnaCoord() {
    }

    public int getReferencePosition() {
        return referencePosition;
    }

    public void setReferencePosition(int referencePosition) {
        this.referencePosition = referencePosition;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Landmark getLandmark() {
        return landmark;
    }

    public void setLandmark(Landmark landmark) {
        this.landmark = landmark;
    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (landmark.equals(Landmark.CDNA_STOP_CODON)) {
            stringBuilder.append("*");
            if (referencePosition != 0) {
                // Remove sign
                stringBuilder.append(Math.abs(referencePosition));
            }
        } else if (referencePosition != 0) {
            stringBuilder.append(referencePosition);
        }

//        if (referencePosition != 0) {
//            stringBuilder.append(referencePosition);
//            if (offset != 0) {
//                if (offset > 0) {
//                    stringBuilder.append("+");
//                }
//                stringBuilder.append(offset);
//            }
//        } else if (offset != 0) {
//            // Remove sign
//            stringBuilder.append(Math.abs(offset));
//        }

        if (offset < 0) {
            stringBuilder.append(offset);
        } else if (offset > 0) {
            if (referencePosition != 0) {
                stringBuilder.append("+");
            }
            stringBuilder.append(offset);
        }

        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CdnaCoord cdnaCoord = (CdnaCoord) o;

        if (referencePosition != cdnaCoord.referencePosition) {
            return false;
        }
        if (offset != cdnaCoord.offset) {
            return false;
        }
        return landmark == cdnaCoord.landmark;

    }

    @Override
    public int hashCode() {
        int result = referencePosition;
        result = 31 * result + offset;
        result = 31 * result + (landmark != null ? landmark.hashCode() : 0);
        return result;
    }
}
