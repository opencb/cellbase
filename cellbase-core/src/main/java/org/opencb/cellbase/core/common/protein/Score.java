package org.opencb.cellbase.core.common.protein;

/**
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 12/4/13
 * Time: 5:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class Score {
    private String type;
    private float score;

    public Score() {
    }

    public Score(String type, float score) {
        this.type = type;
        this.score = score;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
