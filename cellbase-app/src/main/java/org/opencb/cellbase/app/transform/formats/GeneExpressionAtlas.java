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

package org.opencb.cellbase.app.transform.formats;

import java.util.List;

/**
 * Created by antonior on 10/16/14.
 */
public class GeneExpressionAtlas {

    private String geneId;
    private String geneName;
    private List<Tissue> tissues;

    public GeneExpressionAtlas(String geneId, String geneName, List<Tissue> tissues) {
        this.geneId = geneId;
        this.geneName = geneName;
        this.tissues = tissues;
    }

    public List<Tissue> getTissues() {
        return tissues;
    }

    public void setTissues(List<Tissue> tissues) {
        this.tissues = tissues;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getGeneId() {
        return geneId;
    }

    public void setGeneId(String geneId) {
        this.geneId = geneId;
    }

    public static class Tissue {

        private String tissueName;
        private String experiment;
        private float expressionValue;

        public Tissue(String tissueName, String experiment, Float expressionValue) {
            this.tissueName = tissueName;
            this.experiment = experiment;
            this.expressionValue = expressionValue;
        }

        public String getTissueName() {
            return tissueName;
        }

        public void setTissueName(String tissueName) {
            this.tissueName = tissueName;
        }

        public String getExperiment() {
            return experiment;
        }

        public void setExperiment(String experiment) {
            this.experiment = experiment;
        }

        public float getExpressionValue() {
            return expressionValue;
        }

        public void setExpressionValue(float expressionValue) {
            this.expressionValue = expressionValue;
        }
    }

}
