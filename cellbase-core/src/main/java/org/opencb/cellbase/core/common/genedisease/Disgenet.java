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
