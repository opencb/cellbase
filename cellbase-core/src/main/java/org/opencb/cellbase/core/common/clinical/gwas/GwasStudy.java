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
public class GwasStudy {

    private String pubmedId;
    private String firstAuthor;
    private String date;
    private String journal;
    private String link;
    private String study;
    private String initialSampleSize;
    private String replicationSampleSize;
    private String platform;
    private List<GwasTrait> traits;

    public GwasStudy() {
    }

    public GwasStudy(String pubmedId, String firstAuthor, String date, String journal, String link, String study,
                     String initialSampleSize, String replicationSampleSize, String platform) {
        this.pubmedId = pubmedId;
        this.firstAuthor = firstAuthor;
        this.date = date;
        this.journal = journal;
        this.link = link;
        this.study = study;
        this.initialSampleSize = initialSampleSize;
        this.replicationSampleSize = replicationSampleSize;
        this.platform = platform;
        this.traits = new ArrayList<>();
    }

    public String getPubmedId() {
        return pubmedId;
    }

    public void setPubmedId(String pubmedId) {
        this.pubmedId = pubmedId;
    }

    public String getFirstAuthor() {
        return firstAuthor;
    }

    public void setFirstAuthor(String firstAuthor) {
        this.firstAuthor = firstAuthor;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getStudy() {
        return study;
    }

    public void setStudy(String study) {
        this.study = study;
    }

    public String getInitialSampleSize() {
        return initialSampleSize;
    }

    public void setInitialSampleSize(String initialSampleSize) {
        this.initialSampleSize = initialSampleSize;
    }

    public String getReplicationSampleSize() {
        return replicationSampleSize;
    }

    public void setReplicationSampleSize(String replicationSampleSize) {
        this.replicationSampleSize = replicationSampleSize;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public List<GwasTrait> getTraits() {
        return traits;
    }

    public void setTraits(List<GwasTrait> traits) {
        this.traits = traits;
    }

    public void addTraits(List<GwasTrait> traits) {
        for (GwasTrait trait : traits) {
            addTrait(trait);
        }
    }

    public void addTrait(GwasTrait trait) {
        if (this.traits.contains(trait)) {
            int traitIndex = this.traits.indexOf(trait);
            this.traits.get(traitIndex).addTests(trait.getTests());
        } else {
            this.traits.add(trait);
        }
    }

    @Override
    public boolean equals(Object study) {
        boolean equals = false;
        if (study instanceof GwasStudy) {
            equals = this.pubmedId.equals(((GwasStudy) study).getPubmedId());
        }
        return equals;
    }

    @Override
    public int hashCode() {
        return this.pubmedId.hashCode();
    }

}
