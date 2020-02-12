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

import org.opencb.cellbase.core.config.CellBaseConfiguration;

public class CellBaseManagers {

    private CellBaseConfiguration configuration;
    private GeneManager geneManager;
    private TranscriptManager transcriptManager;
    private VariantManager variantManager;
    private ProteinManager proteinManager;

    public CellBaseManagers(CellBaseConfiguration configuration) {
        this.configuration = configuration;
    }

    public GeneManager getGeneManager() {
        if (geneManager == null) {
            geneManager = new GeneManager(configuration);
        }
        return geneManager;
    }

    public TranscriptManager getTranscriptManager() {
        if (transcriptManager == null) {
            transcriptManager = new TranscriptManager(configuration);
        }
        return transcriptManager;
    }

    public VariantManager getVariantManager() {
        if (variantManager == null) {
            variantManager = new VariantManager(configuration);
        }
        return variantManager;
    }

    public ProteinManager getProteinManager() {
        if (proteinManager == null) {
            proteinManager = new ProteinManager(configuration);
        }
        return proteinManager;
    }
}
