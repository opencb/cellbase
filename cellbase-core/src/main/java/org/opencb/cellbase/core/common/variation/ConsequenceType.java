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

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 11/4/13
 * Time: 12:51 PM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class ConsequenceType {

    private String soTerm;
    private String soAccesion;
    private String soDescription;
    private String ensemblConsequenceType;

    public ConsequenceType() {

    }

    public ConsequenceType(String soTerm, String soAccesion, String soDescription, String ensemblConsequenceType) {
        this.soTerm = soTerm;
        this.soAccesion = soAccesion;
        this.soDescription = soDescription;
        this.ensemblConsequenceType = ensemblConsequenceType;
    }

    @Override
    public String toString() {
        return soTerm + "\t" + soAccesion + "\t" + soDescription + "\t" + ensemblConsequenceType;
    }


    public String getSoTerm() {
        return soTerm;
    }

    public void setSoTerm(String soTerm) {
        this.soTerm = soTerm;
    }


    public String getSoAccesion() {
        return soAccesion;
    }

    public void setSoAccesion(String soAccesion) {
        this.soAccesion = soAccesion;
    }


    public String getSoDescription() {
        return soDescription;
    }

    public void setSoDescription(String soDescription) {
        this.soDescription = soDescription;
    }


    public String getEnsemblConsequenceType() {
        return ensemblConsequenceType;
    }

    public void setEnsemblConsequenceType(String ensemblConsequenceType) {
        this.ensemblConsequenceType = ensemblConsequenceType;
    }

}
