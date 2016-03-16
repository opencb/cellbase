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

package org.opencb.cellbase.core.common;

public class IntervalFeatureFrequency {

    private int start;
    private int end;
    private int interval;
    private int absolute;
    private float value;

    public IntervalFeatureFrequency(int start, int end, int interval, int absolute, float value) {
        super();
        this.start = start;
        this.end = end;
        this.interval = interval;
        this.absolute = absolute;
        this.value = value;
    }

    @Override
    public String toString() {
        return start + "\t" + end + "\t" + interval + "\t" + absolute + "\t" + value;
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

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getAbsolute() {
        return absolute;
    }

    public void setAbsolute(int absolute) {
        this.absolute = absolute;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }


}
