/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
