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

package org.opencb.cellbase.core.common.clinical.gwas;

import java.util.ArrayList;
import java.util.List;

/**
* Created by parce on 5/12/14.
*/
public class GwasTrait {

    private String diseaseTrait;
    private String dateAddedToCatalog;
    private List<GwasTest> tests;

    public GwasTrait() {}

    public GwasTrait(String diseaseTrait, String dateAddedToCatalog) {
        this.diseaseTrait = diseaseTrait;
        this.dateAddedToCatalog = dateAddedToCatalog;
        this.tests = new ArrayList<>();
    }

    public String getDiseaseTrait() {
        return diseaseTrait;
    }

    public void setDiseaseTrait(String diseaseTrait) {
        this.diseaseTrait = diseaseTrait;
    }


    public String getDateAddedToCatalog() {
        return dateAddedToCatalog;
    }

    public void setDateAddedToCatalog(String dateAddedToCatalog) {
        this.dateAddedToCatalog = dateAddedToCatalog;
    }

    public List<GwasTest> getTests() {
        return this.tests;
    }

    public void setTests(List<GwasTest> tests) {
        this.tests = tests;
    }

    public void addTests(List<GwasTest> tests) {
        this.tests.addAll(tests);
    }

    public void addTest(GwasTest test) {
        this.tests.add(test);
    }

    @Override
    public boolean equals(Object o) {
        boolean equals = false;
        if (o instanceof GwasTrait) {
            equals = this.diseaseTrait.equals(((GwasTrait)o).getDiseaseTrait());
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return this.diseaseTrait.hashCode();
    }
}
