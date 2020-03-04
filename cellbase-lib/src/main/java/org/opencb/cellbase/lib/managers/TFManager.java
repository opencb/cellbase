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

package org.opencb.cellbase.lib.managers;

import org.opencb.cellbase.core.api.queries.GeneQuery;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.GeneCoreDBAdaptor;

import java.util.ArrayList;
import java.util.List;

public class TFManager extends AbstractManager {

    private GeneCoreDBAdaptor geneDBAdaptor;

    public TFManager(String species, String assembly, CellBaseConfiguration configuration) {
        super(species, assembly, configuration);
        this.init();
    }

    private void init() {
        geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(species, assembly);
    }

    public CellBaseDataResult getByGene(GeneQuery geneQuery) {
        List<String> includes = new ArrayList<>();
        includes.add("id");
        includes.add("transcripts.id");
        includes.add("transcripts.tfbs.tfName");
        includes.add("transcripts.tfbs.pwm");
        includes.add("transcripts.tfbs.chromosome");
        includes.add("transcripts.tfbs.start");
        includes.add("transcripts.tfbs.end");
        includes.add("transcripts.tfbs.strand");
        includes.add("transcripts.tfbs.relativeStart");
        includes.add("transcripts.tfbs.relativeEnd");
        includes.add("transcripts.tfbs.score");
        CellBaseDataResult queryResults = geneDBAdaptor.query(geneQuery);
        return queryResults;
    }
}
