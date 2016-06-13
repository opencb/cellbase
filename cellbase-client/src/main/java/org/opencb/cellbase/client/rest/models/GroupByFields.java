package org.opencb.cellbase.client.rest.models;

import java.util.List;

/**
 * Created by swaathi on 20/05/16.
 */

public class GroupByFields {
    private String _id;
    private List<String> features;

    public GroupByFields() {
    }

    public GroupByFields(String id, List<String> features) {
        this._id = id;
        this.features = features;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String id) {
        this._id = id;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }
}
