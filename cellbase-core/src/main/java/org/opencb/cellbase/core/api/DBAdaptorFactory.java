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

import org.opencb.cellbase.core.CellBaseConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class DBAdaptorFactory {

    protected CellBaseConfiguration cellBaseConfiguration;
    protected Logger logger;


    public DBAdaptorFactory() {
        this(null);
    }

    public DBAdaptorFactory(CellBaseConfiguration cellBaseConfiguration) {
        this.cellBaseConfiguration = cellBaseConfiguration;

        logger = LoggerFactory.getLogger(this.getClass());
    }

    protected CellBaseConfiguration.SpeciesProperties.Species getSpecies(String speciesName) {
        CellBaseConfiguration.SpeciesProperties.Species species = null;
        for (CellBaseConfiguration.SpeciesProperties.Species sp : cellBaseConfiguration.getAllSpecies()) {
            if (speciesName.equalsIgnoreCase(sp.getId()) || speciesName.equalsIgnoreCase(sp.getScientificName())) {
                species = sp;
                break;
            }
        }
        return species;
    }

    protected String getAssembly(CellBaseConfiguration.SpeciesProperties.Species species, String assemblyName) {
        String assembly = null;
        if (assemblyName == null || assemblyName.isEmpty()) {
            assembly = species.getAssemblies().get(0).getName();
        } else {
            for (CellBaseConfiguration.SpeciesProperties.Species.Assembly assembly1 : species.getAssemblies()) {
                if (assemblyName.equalsIgnoreCase(assembly1.getName())) {
                    assembly = assembly1.getName();
                }
            }
        }
        return assembly;
    }


    public abstract void setConfiguration(CellBaseConfiguration cellBaseConfiguration);

    public abstract void open(String species, String version);

    public abstract void close();



    public abstract GenomeDBAdaptor getGenomeDBAdaptor(String species);

    public abstract GenomeDBAdaptor getGenomeDBAdaptor(String species, String assembly);


    public abstract GeneDBAdaptor getGeneDBAdaptor(String species);

    public abstract GeneDBAdaptor getGeneDBAdaptor(String species, String assembly);


    public abstract TranscriptDBAdaptor getTranscriptDBAdaptor(String species);

    public abstract TranscriptDBAdaptor getTranscriptDBAdaptor(String species, String assembly);


    public abstract VariantDBAdaptor getVariationDBAdaptor(String species);

    public abstract VariantDBAdaptor getVariationDBAdaptor(String species, String assembly);


    public abstract XRefDBAdaptor getXRefDBAdaptor(String species);

    public abstract XRefDBAdaptor getXRefDBAdaptor(String species, String assembly);


//    public abstract VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species);
//
//    public abstract VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species, String assembly);


    public abstract ProteinDBAdaptor getProteinDBAdaptor(String species);

    public abstract ProteinDBAdaptor getProteinDBAdaptor(String species, String assembly);


    public abstract RegulationDBAdaptor getRegulationDBAdaptor(String species);

    public abstract RegulationDBAdaptor getRegulationDBAdaptor(String species, String assembly);


    public abstract ClinicalDBAdaptor getClinicalDBAdaptor(String species);

    public abstract ClinicalDBAdaptor getClinicalDBAdaptor(String species, String assembly);

//
//    public abstract PathwayDBAdaptor getPathwayDBAdaptor(String species);
//
//    public abstract PathwayDBAdaptor getPathwayDBAdaptor(String species, String assembly);
//

    public abstract ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species);

    public abstract ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species, String assembly);


    public abstract ConservationDBAdaptor getConservationDBAdaptor(String species);

    public abstract ConservationDBAdaptor getConservationDBAdaptor(String species, String assembly);

}
