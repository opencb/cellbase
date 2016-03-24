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

package org.opencb.cellbase.core.common.variation;

@Deprecated
public class PopulationFrequency {

    private String pop;
    private String refAllele;
    private String altAllele;
    private double refAlleleFreq;
    private double altAlleleFreq;

    public PopulationFrequency() {
    }

    public PopulationFrequency(String pop, String refAllele, String altAllele, double refAlleleFreq, double altAlleleFreq) {
        this.pop = pop;
        this.refAllele = refAllele;
        this.altAllele = altAllele;
        this.refAlleleFreq = refAlleleFreq;
        this.altAlleleFreq = altAlleleFreq;
    }

    public String getPop() {
        return pop;
    }

    public void setPop(String pop) {
        this.pop = pop;
    }

    public String getRefAllele() {
        return refAllele;
    }

    public void setRefAllele(String refAllele) {
        this.refAllele = refAllele;
    }

    public String getAltAllele() {
        return altAllele;
    }

    public void setAltAllele(String altAllele) {
        this.altAllele = altAllele;
    }

    public double getRefAlleleFreq() {
        return refAlleleFreq;
    }

    public void setRefAlleleFreq(double refAlleleFreq) {
        this.refAlleleFreq = refAlleleFreq;
    }

    public double getAltAlleleFreq() {
        return altAlleleFreq;
    }

    public void setAltAlleleFreq(double altAlleleFreq) {
        this.altAlleleFreq = altAlleleFreq;
    }

    // private double homRefAlleleFreq;
    // private double hetAlleleFreq;
    // private double HomAltAlleleFreq;

}
