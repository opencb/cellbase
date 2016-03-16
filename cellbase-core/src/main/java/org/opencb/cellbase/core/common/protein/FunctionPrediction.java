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

package org.opencb.cellbase.core.common.protein;

import java.util.Map;

/**
 * Created by imedina on 10/12/13.
 */
public class FunctionPrediction {

    private String checksum;
    private String uniprotId;
    private String transcriptId;
    private int size;
    private Map<Integer, Map<String, Map<String, Float>>> aaPosition;

    public FunctionPrediction() {

    }


    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getUniprotId() {
        return uniprotId;
    }

    public void setUniprotId(String uniprotId) {
        this.uniprotId = uniprotId;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Map<Integer, Map<String, Map<String, Float>>> getAaPosition() {
        return aaPosition;
    }

    public void setAaPosition(Map<Integer, Map<String, Map<String, Float>>> aaPosition) {
        this.aaPosition = aaPosition;
    }
}
