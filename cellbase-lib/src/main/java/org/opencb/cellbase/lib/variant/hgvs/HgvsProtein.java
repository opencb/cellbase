/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.lib.variant.hgvs;

import java.util.List;

public class HgvsProtein {

    private List<String> ids;
    private String hgvs;
    private String alternateProteinSequence;

    public HgvsProtein(List<String> ids, String hgvs, String alternateProteinSequence) {
        this.ids = ids;
        this.hgvs = hgvs;
        this.alternateProteinSequence = alternateProteinSequence;
    }

    public List<String> getIds() {
        return ids;
    }

    public HgvsProtein setIds(List<String> ids) {
        this.ids = ids;
        return this;
    }

    public String getHgvs() {
        return hgvs;
    }

    public HgvsProtein setHgvs(String hgvs) {
        this.hgvs = hgvs;
        return this;
    }

    public String getAlternateProteinSequence() {
        return alternateProteinSequence;
    }

    public HgvsProtein setAlternateProteinSequence(String alternateProteinSequence) {
        this.alternateProteinSequence = alternateProteinSequence;
        return this;
    }
}
