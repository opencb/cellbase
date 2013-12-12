package org.opencb.cellbase.core.common.protein;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 12/5/13
 * Time: 12:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class Interactor {
    private String id;
    private String dbName;
    private List<Psimi> biologicalRole;
    private Psimi moleculeType;
    private List<XRef> xrefs;

    public Interactor() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public List<Psimi> getBiologicalRole() {
        return biologicalRole;
    }

    public void setBiologicalRole(List<Psimi> biologicalRole) {
        this.biologicalRole = biologicalRole;
    }

    public Psimi getMoleculeType() {
        return moleculeType;
    }

    public void setMoleculeType(Psimi moleculeType) {
        this.moleculeType = moleculeType;
    }

    public List<XRef> getXrefs() {
        return xrefs;
    }

    public void setXrefs(List<XRef> xrefs) {
        this.xrefs = xrefs;
    }
}
