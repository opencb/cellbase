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
public class Xref {

    private String id;            //1
    private String source;        //2
    private String version;    //3

    public Xref(String id, String source) {
        this(id, source, "");
    }

    public Xref(String id, String source, String version) {
        this.id = id;
        this.source = source;
        this.version = version;
    }


    public String getDataBase() {
        return source;
    }

    public void setDataBase(String dataBase) {
        this.source = dataBase;
    }


    public String getCrossReference() {
        return id;
    }

    public void setCrossReference(String crossReference) {
        this.id = crossReference;
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


}
