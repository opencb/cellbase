/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.lib.variant.annotation;

import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.exception.CellBaseException;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by fjlopez on 28/04/15.
 */
public interface VariantAnnotator {

    String IGNORE_PHASE = "ignorePhase";

    boolean open();

    void run(List<Variant> variantList) throws InterruptedException, ExecutionException, QueryException, IllegalAccessException,
            CellBaseException;

    boolean close();

}
