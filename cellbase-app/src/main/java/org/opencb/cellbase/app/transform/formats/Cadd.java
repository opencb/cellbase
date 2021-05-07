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

import java.util.ArrayList;
import java.util.List;

/**
 * @author by antonior on 5/22/14.
 * @author Luis Miguel Cruz.
 * @since October 08, 2014
 */
public class Cadd {

    /***
     * Alternate Allele.
     ***/
    private String alternate;

    /***
     * Reference Allele.
     ***/
    private String reference;

    /***
     * Chromosome.
     ***/
    private String chromosome;

    /***
     * Variant position start.
     ***/
    private Integer start;

    /***
     * Variant position end.
     ***/
    private Integer end;

    /***
     * Maximum ENCODE expression value.
     ***/
    private Float EncExp;

    /***
     * Maximum ENCODE H3K27 acetylation level.
     ***/
    private Float EncH3K27Ac;

    /***
     * Maximum ENCODE H3K4 methylation level.
     ***/
    private Float EncH3K4Me1;

    /***
     * Maximum ENCODE H3K4 trimethylation level.
     ***/
    private Float EncH3K4Me3;


    /***
     * Maximum of ENCODE Nucleosome position track score.
     ***/
    private Float EncNucleo;

    /***
     * ENCODE open chromatin code.
     ***/
    private Integer EncOCC;

    /***
     * ENCODE combined p-Value (PHRED-scale) of Faire, Dnase,polII, CTCF, Myc evidence for open chromatin.
     ***/
    private Float EncOCCombPVal;

    /***
     * p-Value (PHRED-scale) of Dnase evidence for open chromatin.
     ***/
    private Float EncOCDNasePVal;

    /***
     * p-Value (PHRED-scale) of Faire evidence for open chromatin.
     ***/
    private Float EncOCFairePVal;

    /***
     * p-Value (PHRED-scale) of polII evidence for open chromatin.
     ***/
    private Float EncOCpolIIPVal;


    /***
     * p-Value (PHRED-scale) of CTCF evidence for open chromatin.
     ***/
    private Float EncOCctcfPVal;


    /***
     * p-Value (PHRED-scale) of Myc evidence for open chromatin.
     ***/
    private Float EncOCmycPVal;


    /***
     * Peak signal for Dnase evidence of open chromatin.
     ***/
    private Float EncOCDNaseSig;


    /***
     * Peak signal for Faire evidence of open chromatin.
     ***/
    private Float EncOCFaireSig;

    /***
     * Peak signal for polII evidence of open chromatin.
     ***/
    private Float EncOCpolIISig;


    /***
     * Peak signal for CTCF evidence of open chromatin.
     ***/
    private Float EncOCctcfSig;

    /***
     * Peak signal for Myc evidence of open chromatin.
     ***/
    private Float EncOCmycSig;


    /***
     * List of pvalues, phred and genomicFeature.
     ***/
    private List<CaddValues> caddValues;

    public Cadd(String alternate, String reference, String chromosome, Integer start, Integer end, Float encExp,
                Float encH3K27Ac, Float encH3K4Me1, Float encH3K4Me3, Float encNucleo, Integer encOCC, Float encOCCombPVal,
                Float encOCDNasePVal, Float encOCFairePVal, Float encOCpolIIPVal, Float encOCctcfPVal, Float encOCmycPVal,
                Float encOCDNaseSig, Float encOCFaireSig, Float encOCpolIISig, Float encOCctcfSig, Float encOCmycSig) {
        this.alternate = alternate;
        this.reference = reference;
        this.chromosome = chromosome;
        this.start = start;
        this.end = end;
        this.EncExp = encExp;
        this.EncH3K27Ac = encH3K27Ac;
        this.EncH3K4Me1 = encH3K4Me1;
        this.EncH3K4Me3 = encH3K4Me3;
        this.EncNucleo = encNucleo;
        this.EncOCC = encOCC;
        this.EncOCCombPVal = encOCCombPVal;
        this.EncOCDNasePVal = encOCDNasePVal;
        this.EncOCFairePVal = encOCFairePVal;
        this.EncOCpolIIPVal = encOCpolIIPVal;
        this.EncOCctcfPVal = encOCctcfPVal;
        this.EncOCmycPVal = encOCmycPVal;
        this.EncOCDNaseSig = encOCDNaseSig;
        this.EncOCFaireSig = encOCFaireSig;
        this.EncOCpolIISig = encOCpolIISig;
        this.EncOCctcfSig = encOCctcfSig;
        this.EncOCmycSig = encOCmycSig;
        this.caddValues = new ArrayList<>();
    }

    public Cadd() {
    }


    public String getAlternate() {
        return alternate;
    }

    public void setAlternate(String alternate) {
        this.alternate = alternate;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Float getEncExp() {
        return EncExp;
    }

    public void setEncExp(Float encExp) {
        EncExp = encExp;
    }

    public Float getEncH3K27Ac() {
        return EncH3K27Ac;
    }

    public void setEncH3K27Ac(Float encH3K27Ac) {
        EncH3K27Ac = encH3K27Ac;
    }

    public Float getEncH3K4Me1() {
        return EncH3K4Me1;
    }

    public void setEncH3K4Me1(Float encH3K4Me1) {
        EncH3K4Me1 = encH3K4Me1;
    }

    public Float getEncH3K4Me3() {
        return EncH3K4Me3;
    }

    public void setEncH3K4Me3(Float encH3K4Me3) {
        EncH3K4Me3 = encH3K4Me3;
    }

    public Float getEncNucleo() {
        return EncNucleo;
    }

    public void setEncNucleo(Float encNucleo) {
        EncNucleo = encNucleo;
    }

    public Integer getEncOCC() {
        return EncOCC;
    }

    public void setEncOCC(Integer encOCC) {
        EncOCC = encOCC;
    }

    public Float getEncOCCombPVal() {
        return EncOCCombPVal;
    }

    public void setEncOCCombPVal(Float encOCCombPVal) {
        EncOCCombPVal = encOCCombPVal;
    }

    public Float getEncOCDNasePVal() {
        return EncOCDNasePVal;
    }

    public void setEncOCDNasePVal(Float encOCDNasePVal) {
        EncOCDNasePVal = encOCDNasePVal;
    }

    public Float getEncOCFairePVal() {
        return EncOCFairePVal;
    }

    public void setEncOCFairePVal(Float encOCFairePVal) {
        EncOCFairePVal = encOCFairePVal;
    }

    public Float getEncOCpolIIPVal() {
        return EncOCpolIIPVal;
    }

    public void setEncOCpolIIPVal(Float encOCpolIIPVal) {
        EncOCpolIIPVal = encOCpolIIPVal;
    }

    public Float getEncOCctcfPVal() {
        return EncOCctcfPVal;
    }

    public void setEncOCctcfPVal(Float encOCctcfPVal) {
        EncOCctcfPVal = encOCctcfPVal;
    }

    public Float getEncOCmycPVal() {
        return EncOCmycPVal;
    }

    public void setEncOCmycPVal(Float encOCmycPVal) {
        EncOCmycPVal = encOCmycPVal;
    }

    public Float getEncOCDNaseSig() {
        return EncOCDNaseSig;
    }

    public void setEncOCDNaseSig(Float encOCDNaseSig) {
        EncOCDNaseSig = encOCDNaseSig;
    }

    public Float getEncOCFaireSig() {
        return EncOCFaireSig;
    }

    public void setEncOCFaireSig(Float encOCFaireSig) {
        EncOCFaireSig = encOCFaireSig;
    }

    public Float getEncOCpolIISig() {
        return EncOCpolIISig;
    }

    public void setEncOCpolIISig(Float encOCpolIISig) {
        EncOCpolIISig = encOCpolIISig;
    }

    public Float getEncOCctcfSig() {
        return EncOCctcfSig;
    }

    public void setEncOCctcfSig(Float encOCctcfSig) {
        EncOCctcfSig = encOCctcfSig;
    }

    public Float getEncOCmycSig() {
        return EncOCmycSig;
    }

    public void setEncOCmycSig(Float encOCmycSig) {
        EncOCmycSig = encOCmycSig;
    }

    public List<CaddValues> getCaddValues() {
        return caddValues;
    }

    public void setCaddValues(List<CaddValues> values) {
        this.caddValues = values;
    }

    public void addCaddValues(float cscore, float phred, String genomicFeature) {
        this.caddValues.add(new CaddValues(cscore, phred, genomicFeature));
    }

    /**
     * @author antonior on 5/22/14.
     * @author Luis Miguel Cruz.
     * @since October 08, 2014
     */
    public class CaddValues {
        /***
         * Cadd score.
         */
        private float cscore;

        /***
         * Cadd score PHRED scale.
         */
        private float phred;

        /***
         * genomicFeature.
         */
        private String genomicFeature;


        public CaddValues(float cscore, float phred, String genomicFeature) {
            this.cscore = cscore;
            this.phred = phred;
            this.genomicFeature = genomicFeature;
        }

        public float getCscore() {
            return cscore;
        }

        public void setCscore(float cscore) {
            this.cscore = cscore;
        }

        public float getPhred() {
            return phred;
        }

        public void setPhred(float phred) {
            this.phred = phred;
        }


        public String getGenomicFeature() {
            return genomicFeature;
        }

        public void setGenomicFeature(String genomicFeature) {
            this.genomicFeature = genomicFeature;
        }
    }
}
