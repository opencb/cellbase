/*
 * Copyright 2015-2019 OpenCB
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

package org.opencb.cellbase.core.common.genedisease;

import org.opencb.biodata.models.core.Disease;

import java.util.List;

/**
 * Created by fjlopez on 20/05/15.
 */
@Deprecated
public class Disgenet {

    // gene HUGO symbol
    private String geneName;

    // gene name
    private String geneSymbol;

    private List<Disease> diseases;

    public Disgenet(String geneName, String geneSymbol, List<Disease> diseases) {
        this.geneName = geneName;
        this.geneSymbol = geneSymbol;
        this.diseases = diseases;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public void setGeneSymbol(String geneSymbol) {
        this.geneSymbol = geneSymbol;
    }

    public List<Disease> getDiseases() {
        return diseases;
    }

    public void setDiseases(List<Disease> diseases) {
        this.diseases = diseases;
    }

}
