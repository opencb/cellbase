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

package org.opencb.cellbase.core.common.core;

public class Xref {

    private String id;
    private String dbName;
    private String dbDisplayName;
    private String description;

    public Xref() {

    }

    public Xref(String id, String dbName, String dbDisplayName) {
        this(id, dbName, dbDisplayName, "");
    }

    public Xref(String id, String dbName, String dbDisplayName, String description) {
        this.id = id;
        this.dbName = dbName;
        this.dbDisplayName = dbDisplayName;
        this.description = description;
    }

    @Override
    public boolean equals(Object obj) {
        Xref xrefObj = (Xref) obj;
        return id.equals(xrefObj.id) && dbName.equals(xrefObj.dbName);
    }

    @Override
    public int hashCode() {
        return (id + dbName).hashCode();
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


    public String getDbDisplayName() {
        return dbDisplayName;
    }

    public void setDbDisplayName(String dbDisplayName) {
        this.dbDisplayName = dbDisplayName;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
