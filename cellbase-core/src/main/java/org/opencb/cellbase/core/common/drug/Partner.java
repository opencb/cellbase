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

package org.opencb.cellbase.core.common.drug;

import java.util.List;

/**
 * Created by mbleda on 12/12/13.
 */

public class Partner {
    private String id;
    private String name;
    private List<XRef> xrefs;
    private String sequence;
    private String generalFunction;
    private String specificFunction;
    private String essentiality;
    private String cellularLocation;
    private String references;

    public Partner() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<XRef> getXrefs() {
        return xrefs;
    }

    public void setXrefs(List<XRef> xrefs) {
        this.xrefs = xrefs;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getGeneralFunction() {
        return generalFunction;
    }

    public void setGeneralFunction(String generalFunction) {
        this.generalFunction = generalFunction;
    }

    public String getSpecificFunction() {
        return specificFunction;
    }

    public void setSpecificFunction(String specificFunction) {
        this.specificFunction = specificFunction;
    }

    public String getEssentiality() {
        return essentiality;
    }

    public void setEssentiality(String essentiality) {
        this.essentiality = essentiality;
    }

    public String getCellularLocation() {
        return cellularLocation;
    }

    public void setCellularLocation(String cellularLocation) {
        this.cellularLocation = cellularLocation;
    }

    public String getReferences() {
        return references;
    }

    public void setReferences(String references) {
        this.references = references;
    }
}
