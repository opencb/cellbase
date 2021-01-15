package org.opencb.cellbase.core.variant.annotation.hgvs;

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
