package org.opencb.cellbase.core.common.protein;

/**
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 12/4/13
 * Time: 2:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class XRef {

    private String id;
    private String dbName;
    private String description;


    public XRef() {
    }

    public XRef(String id, String dbName) {
        this.id = id;
        this.dbName = dbName;
    }

    public XRef(String id, String dbName, String description) {
        this.id = id;
        this.dbName = dbName;
        this.description = description;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
