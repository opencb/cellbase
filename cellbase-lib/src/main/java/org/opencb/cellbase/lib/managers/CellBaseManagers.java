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

import org.apache.commons.collections.map.MultiKeyMap;
import org.opencb.cellbase.core.config.CellBaseConfiguration;

import java.util.Map;

public class CellBaseManagers {

    private CellBaseConfiguration configuration;

    private Map<String, GeneManager> geneManager;
    private TranscriptManager transcriptManager;
    private VariantManager variantManager;
    private ProteinManager proteinManager;
    private GenomeManager genomeManager;
    private ClinicalManager clinicalManager;
    private RegulatoryManager regulatoryManager;
    private XrefManager xrefManager;

    public CellBaseManagers(CellBaseConfiguration configuration) {
        this.configuration = configuration;
    }

    private String getMultiKey(String species, String assembly) {
        return species + "_" + assembly;
    }

    public GeneManager getGeneManager(String species) {
        // get the unique assembly

        return getGeneManager(species, null);
    }

    public GeneManager getGeneManager(String species, String assembly) {
        if (!geneManager.containsKey(getMultiKey(species, assembly))) {
            // check

            geneManager.put(species + "_" + assembly, new GeneManager(species, assembly, configuration));
        }
        return geneManager.get(getMultiKey(species, assembly));
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

    public GenomeManager getGenomeManager() {
        if (genomeManager == null) {
            genomeManager = new GenomeManager(configuration);
        }
        return genomeManager;
    }

    public ClinicalManager getClinicalManager() {
        if (clinicalManager == null) {
            clinicalManager = new ClinicalManager(configuration);
        }
        return clinicalManager;
    }

    public RegulatoryManager getRegulatoryManager() {
        if (regulatoryManager == null) {
            regulatoryManager = new RegulatoryManager(configuration);
        }
        return regulatoryManager;
    }

    public XrefManager getXrefManager() {
        if (xrefManager == null) {
            xrefManager = new XrefManager(configuration);
        }
        return xrefManager;
    }
}
