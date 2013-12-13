package org.opencb.cellbase.core.common;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

/**
 * User: fsalavert
 * Date: 4/10/13
 * Time: 12:09 PM
 */
public class GenericFeature {
    private String id;
    private String chromosome;
    private List<String> chunkIds;
    private String source;
    private String featureType;
    private int start;
    private int end;
    private String score;
    private String strand;
    private String frame;
    private String itemRGB;
    private String name;
    private String featureClass;
    private String alias;
    private List<String> cellTypes = new ArrayList<>();
    private String matrix;

    public GenericFeature() {

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

    public String getStrand() {
        return strand;
    }

    public void setStrand(String strand) {
        this.strand = strand;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getItemRGB() {
        return itemRGB;
    }

    public void setItemRGB(String itemRGB) {
        this.itemRGB = itemRGB;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFeatureType() {
        return featureType;
    }

    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFeatureClass() {
        return featureClass;
    }

    public void setFeatureClass(String featureClass) {
        this.featureClass = featureClass;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getFrame() {
        return frame;
    }

    public void setFrame(String frame) {
        this.frame = frame;
    }

    public List<String> getCellTypes() {
        return cellTypes;
    }

    public void setCellTypes(List<String> cellTypes) {
        this.cellTypes = cellTypes;
    }

    public String getMatrix() {
        return matrix;
    }

    public void setMatrix(String matrix) {
        this.matrix = matrix;
    }

    public List<String> getChunkIds() {
        return chunkIds;
    }

    public void setChunkIds(List<String> chunkIds) {
        this.chunkIds = chunkIds;
    }
}
