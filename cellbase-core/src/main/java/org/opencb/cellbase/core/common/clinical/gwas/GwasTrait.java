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
