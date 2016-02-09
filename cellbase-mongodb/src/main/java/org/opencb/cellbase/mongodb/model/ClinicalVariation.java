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

package org.opencb.cellbase.mongodb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.opencb.cellbase.core.common.clinical.ClinvarPublicSet;
import org.opencb.cellbase.core.common.clinical.Cosmic;
import org.opencb.cellbase.core.common.clinical.gwas.Gwas;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by parce on 10/31/14.
 */
@Deprecated
public class ClinicalVariation {

    private String chromosome;
    private int start;
    private int end;
    private String reference;
    private String alternate;
    @JsonIgnoreProperties({"chromosome", "start", "end", "reference", "alternate"})
    private List<ClinvarPublicSet> clinvarList;
    @JsonIgnoreProperties({"chromosome", "start", "end", "reference", "alternate"})
    private List<Cosmic> cosmicList;
    @JsonIgnoreProperties({"chromosome", "start", "end", "reference", "alternate"})
    private List<Gwas> gwasList;

    public ClinicalVariation(String chromosome, int start, int end, String reference, String alternate) {
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.reference = reference;
        this.alternate = alternate;
    }

    public ClinicalVariation(ClinvarPublicSet clinvarSet) {
        this(clinvarSet.getChromosome(), clinvarSet.getStart(), clinvarSet.getEnd(), clinvarSet.getReference(), clinvarSet.getAlternate());
        this.addClinvar(clinvarSet);
    }

    public ClinicalVariation(Cosmic cosmic) {
        this(cosmic.getChromosome(), cosmic.getStart(), cosmic.getEnd(), cosmic.getReference(), cosmic.getAlternate());
        this.addCosmic(cosmic);
    }

    public ClinicalVariation(Gwas gwas) {
        this(gwas.getChromosome(), gwas.getStart(), gwas.getEnd(), gwas.getReference(), gwas.getAlternate());
        this.addGwas(gwas);
    }


    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getAlternate() {
        return alternate;
    }

    public void setAlternate(String alternate) {
        this.alternate = alternate;
    }

    public void addClinvar(ClinvarPublicSet clinvar) {
        if (clinvarList == null) {
            clinvarList = new ArrayList<>();
        }
        clinvarList.add(clinvar);
    }

    public void addCosmic(Cosmic cosmic) {
        if (cosmicList == null) {
            cosmicList = new ArrayList<>();
        }
        cosmicList.add(cosmic);
    }

    public void addGwas(Gwas gwas) {
        if (gwasList == null) {
            gwasList = new ArrayList<>();
        }
        gwasList.add(gwas);
    }

    public List<ClinvarPublicSet> getClinvarList() {
        return clinvarList;
    }

    public void setClinvarList(List<ClinvarPublicSet> clinvarList) {
        this.clinvarList = clinvarList;
    }

    public List<Cosmic> getCosmicList() {
        return cosmicList;
    }

    public void setCosmicList(List<Cosmic> cosmicList) {
        this.cosmicList = cosmicList;
    }

    public List<Gwas> getGwasList() {
        return gwasList;
    }

    public void setGwasList(List<Gwas> gwasList) {
        this.gwasList = gwasList;
    }

    public String getVariantString() {
        return chromosome + ":" + start + "-" + end + ":" + reference + "->" + alternate;
    }
}
