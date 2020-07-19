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

import java.util.List;

@Deprecated
public class TranscriptVariation {

    private String transcriptId;
    private String alleleString;
    private String somatic;
    private List<String> consequenceTypes;
    private int cdsStart;
    private int cdsEnd;
    private int cdnaStart;
    private int cdnaEnd;
    private int translationStart;
    private int translationEnd;
    private int distanceToTranscript;
    private String codonAlleleString;
    private String peptideAlleleString;
    private String hgvsGenomic;
    private String hgvsTranscript;
    private String hgvsProtein;
    private String polyphenPrediction;
    private float polyphenScore;
    private String siftPrediction;
    private float siftScore;

    public TranscriptVariation(String transcriptId, String alleleString,
                               String somatic, List<String> consequenceTypes, int cdsStart,
                               int cdsEnd, int cdnaStart, int cdnEnd, int translationStart,
                               int translationEnd, int distanceToTranscript,
                               String codonAlleleString, String peptideAlleleString,
                               String hgvsGenomic, String hgvsTranscript, String hgvsProtein,
                               String polyphenPrediction, float polyphenScore,
                               String siftPrediction, float siftScore) {
        this.transcriptId = transcriptId;
        this.alleleString = alleleString;
        this.somatic = somatic;
        this.consequenceTypes = consequenceTypes;
        this.cdsStart = cdsStart;
        this.cdsEnd = cdsEnd;
        this.cdnaStart = cdnaStart;
        this.cdnaEnd = cdnEnd;
        this.translationStart = translationStart;
        this.translationEnd = translationEnd;
        this.distanceToTranscript = distanceToTranscript;
        this.codonAlleleString = codonAlleleString;
        this.peptideAlleleString = peptideAlleleString;
        this.hgvsGenomic = hgvsGenomic;
        this.hgvsTranscript = hgvsTranscript;
        this.hgvsProtein = hgvsProtein;
        this.polyphenPrediction = polyphenPrediction;
        this.polyphenScore = polyphenScore;
        this.siftPrediction = siftPrediction;
        this.siftScore = siftScore;
    }


    public String getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
    }

    public String getAlleleString() {
        return alleleString;
    }

    public void setAlleleString(String alleleString) {
        this.alleleString = alleleString;
    }

    public String getSomatic() {
        return somatic;
    }

    public void setSomatic(String somatic) {
        this.somatic = somatic;
    }

    public List<String> getConsequenceTypes() {
        return consequenceTypes;
    }

    public void setConsequenceTypes(List<String> consequenceTypes) {
        this.consequenceTypes = consequenceTypes;
    }

    public int getCdsStart() {
        return cdsStart;
    }

    public void setCdsStart(int cdsStart) {
        this.cdsStart = cdsStart;
    }

    public int getCdsEnd() {
        return cdsEnd;
    }

    public void setCdsEnd(int cdsEnd) {
        this.cdsEnd = cdsEnd;
    }

    public int getCdnaStart() {
        return cdnaStart;
    }

    public void setCdnaStart(int cdnaStart) {
        this.cdnaStart = cdnaStart;
    }

    public int getCdnaEnd() {
        return cdnaEnd;
    }

    public void setCdnaEnd(int cdnaEnd) {
        this.cdnaEnd = cdnaEnd;
    }

    public int getTranslationStart() {
        return translationStart;
    }

    public void setTranslationStart(int translationStart) {
        this.translationStart = translationStart;
    }

    public int getTranslationEnd() {
        return translationEnd;
    }

    public void setTranslationEnd(int translationEnd) {
        this.translationEnd = translationEnd;
    }

    public int getDistanceToTranscript() {
        return distanceToTranscript;
    }

    public void setDistanceToTranscript(int distanceToTranscript) {
        this.distanceToTranscript = distanceToTranscript;
    }

    public String getCodonAlleleString() {
        return codonAlleleString;
    }

    public void setCodonAlleleString(String codonAlleleString) {
        this.codonAlleleString = codonAlleleString;
    }

    public String getPeptideAlleleString() {
        return peptideAlleleString;
    }

    public void setPeptideAlleleString(String peptideAlleleString) {
        this.peptideAlleleString = peptideAlleleString;
    }

    public String getHgvsGenomic() {
        return hgvsGenomic;
    }

    public void setHgvsGenomic(String hgvsGenomic) {
        this.hgvsGenomic = hgvsGenomic;
    }

    public String getHgvsTranscript() {
        return hgvsTranscript;
    }

    public void setHgvsTranscript(String hgvsTranscript) {
        this.hgvsTranscript = hgvsTranscript;
    }

    public String getHgvsProtein() {
        return hgvsProtein;
    }

    public void setHgvsProtein(String hgvsProtein) {
        this.hgvsProtein = hgvsProtein;
    }

    public String getPolyphenPrediction() {
        return polyphenPrediction;
    }

    public void setPolyphenPrediction(String polyphenPrediction) {
        this.polyphenPrediction = polyphenPrediction;
    }

    public float getPolyphenScore() {
        return polyphenScore;
    }

    public void setPolyphenScore(float polyphenScore) {
        this.polyphenScore = polyphenScore;
    }

    public String getSiftPrediction() {
        return siftPrediction;
    }

    public void setSiftPrediction(String siftPrediction) {
        this.siftPrediction = siftPrediction;
    }

    public float getSiftScore() {
        return siftScore;
    }

    public void setSiftScore(float siftScore) {
        this.siftScore = siftScore;
    }


}
