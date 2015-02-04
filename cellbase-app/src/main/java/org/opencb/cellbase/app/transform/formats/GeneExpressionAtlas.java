package org.opencb.cellbase.app.transform.formats;

import java.util.List;

/**
 * Created by antonior on 10/16/14.
 */
public class GeneExpressionAtlas {

    private String geneId;
    private String geneName;
    private List <Tissue> tissues;

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
        private Float expressionValue;

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

        public Float getExpressionValue() {
            return expressionValue;
        }

        public void setExpressionValue(Float expressionValue) {
            this.expressionValue = expressionValue;
        }
    }

}
