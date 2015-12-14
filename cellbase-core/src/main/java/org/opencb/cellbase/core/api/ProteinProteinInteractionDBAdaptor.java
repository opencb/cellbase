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

package org.opencb.cellbase.core.api;

import org.opencb.commons.datastore.core.QueryParam;

import static org.opencb.commons.datastore.core.QueryParam.Type.TEXT_ARRAY;

/**
 * Created by imedina on 13/12/15.
 */
public interface ProteinProteinInteractionDBAdaptor<PPI> extends CellBaseDBAdaptor<PPI> {

    enum QueryParams implements QueryParam {
        INTERACTOR_A_ID("interactorA.id", TEXT_ARRAY, ""),
        INTERACTOR_B_ID("interactorB.id", TEXT_ARRAY, ""),
        INTERACTOR_A_XREFS("interactorA.xrefs", TEXT_ARRAY, ""),
        INTERACTOR_B_XREFS("interactorB.xrefs", TEXT_ARRAY, ""),
        XREFs("xrefs", TEXT_ARRAY, ""),
        TYPE_PSIMI("type.psimi", TEXT_ARRAY, ""),
        TYPE_NAME("type.name", TEXT_ARRAY, ""),
        DETECTION_METHOD_NAME("detectionMethod.name", TEXT_ARRAY, "");

        QueryParams(String key, Type type, String description) {
            this.key = key;
            this.type = type;
            this.description = description;
        }

        private final String key;
        private Type type;
        private String description;

        @Override
        public String key() {
            return key;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public Type type() {
            return type;
        }
    }

}
