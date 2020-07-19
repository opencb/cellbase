package org.opencb.cellbase.client.rest.models;

/**
 * Created by swaathi on 20/05/16.
 */

public class GroupCount {
    private String _id;
    private int count;

    public GroupCount() {
    }

    public GroupCount(String id, int count) {
        this._id = id;
        this.count = count;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String id) {
        this._id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}


