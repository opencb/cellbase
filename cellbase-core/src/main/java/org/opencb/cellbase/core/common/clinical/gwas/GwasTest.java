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

package org.opencb.cellbase.core.common.clinical.gwas;

/**
* Created by parce on 5/12/14.
*/
public class GwasTest {

    private Float pValue;
    private Float pValueMlog;
    private String pValueText;
    private String orBeta;
    private String percentCI;

    public GwasTest() {}

    public GwasTest(Float pValue, Float pValueMlog, String pValueText, String orBeta, String percentCI) {
        this.pValue = pValue;
        this.pValueMlog = pValueMlog;
        this.pValueText = pValueText;
        this.orBeta = orBeta;
        this.percentCI = percentCI;
    }

    public Float getpValue() {
        return pValue;
    }

    public void setpValue(Float pValue) {
        this.pValue = pValue;
    }

    public Float getpValueMlog() {
        return pValueMlog;
    }

    public void setpValueMlog(Float pValueMlog) {
        this.pValueMlog = pValueMlog;
    }

    public String getpValueText() {
        return pValueText;
    }

    public void setpValueText(String pValueText) {
        this.pValueText = pValueText;
    }

    public String getOrBeta() {
        return orBeta;
    }

    public void setOrBeta(String orBeta) {
        this.orBeta = orBeta;
    }

    public String getPercentCI() {
        return percentCI;
    }

    public void setPercentCI(String percentCI) {
        this.percentCI = percentCI;
    }

}
