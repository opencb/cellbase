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

package org.opencb.cellbase.core.db;

import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.db.api.CpGIslandDBAdaptor;
import org.opencb.cellbase.core.db.api.CytobandDBAdaptor;
import org.opencb.cellbase.core.db.api.core.*;
import org.opencb.cellbase.core.db.api.regulatory.MirnaDBAdaptor;
import org.opencb.cellbase.core.db.api.regulatory.RegulatoryRegionDBAdaptor;
import org.opencb.cellbase.core.db.api.regulatory.TfbsDBAdaptor;
import org.opencb.cellbase.core.db.api.systems.PathwayDBAdaptor;
import org.opencb.cellbase.core.db.api.systems.ProteinProteinInteractionDBAdaptor;
import org.opencb.cellbase.core.db.api.variation.*;
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

// protected String getSpeciesVersionPrefix(String species, String version) {
// String speciesPrefix = null;
// if(species != null && !species.equals("")) {
// // coding an alias to application code species
// species = speciesAlias.get(species);
// // if 'version' parameter has not been provided the default version is selected
// if(version == null || version.trim().equals("")) {
// version = applicationProperties.getProperty(species+".DEFAULT.VERSION").toUpperCase();
// logger.debug("HibernateDBAdaptorFactory: 'version' parameter is null or empty, it's been set to: '"+version+"'");
// }
// // setting database configuration for the 'species.version'
// speciesPrefix = species.toUpperCase() + "." + version.toUpperCase();
// }
// return speciesPrefix;
// }

    public abstract void setConfiguration(CellBaseConfiguration cellBaseConfiguration);

    public abstract void open(String species, String version);

    public abstract void close();


    public abstract GenomeDBAdaptor getGenomeDBAdaptor(String species);

    public abstract GenomeDBAdaptor getGenomeDBAdaptor(String species, String assembly);


    public abstract GeneDBAdaptor getGeneDBAdaptor(String species);

    public abstract GeneDBAdaptor getGeneDBAdaptor(String species, String assembly);


    public abstract TranscriptDBAdaptor getTranscriptDBAdaptor(String species);

    public abstract TranscriptDBAdaptor getTranscriptDBAdaptor(String species, String assembly);


    @Deprecated
    public abstract ExonDBAdaptor getExonDBAdaptor(String species);

    @Deprecated
    public abstract ExonDBAdaptor getExonDBAdaptor(String species, String assembly);


    public abstract VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species);

    public abstract VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species, String assembly);


    public abstract ProteinDBAdaptor getProteinDBAdaptor(String species);

    public abstract ProteinDBAdaptor getProteinDBAdaptor(String species, String assembly);


    public abstract CytobandDBAdaptor getCytobandDBAdaptor(String species);

    public abstract CytobandDBAdaptor getCytobandDBAdaptor(String species, String assembly);


    public abstract XRefsDBAdaptor getXRefDBAdaptor(String species);

    public abstract XRefsDBAdaptor getXRefDBAdaptor(String species, String assembly);


    public abstract TfbsDBAdaptor getTfbsDBAdaptor(String species);

    public abstract TfbsDBAdaptor getTfbsDBAdaptor(String species, String assembly);


    public abstract RegulatoryRegionDBAdaptor getRegulatoryRegionDBAdaptor(String species);

    public abstract RegulatoryRegionDBAdaptor getRegulatoryRegionDBAdaptor(String species, String assembly);


    public abstract MirnaDBAdaptor getMirnaDBAdaptor(String species);

    public abstract MirnaDBAdaptor getMirnaDBAdaptor(String species, String assembly);


    public abstract MutationDBAdaptor getMutationDBAdaptor(String species);

    public abstract MutationDBAdaptor getMutationDBAdaptor(String species, String assembly);


    public abstract ClinVarDBAdaptor getClinVarDBAdaptor(String species);

    public abstract ClinVarDBAdaptor getClinVarDBAdaptor(String species, String assembly);


    public abstract ClinicalDBAdaptor getClinicalDBAdaptor(String species);

    public abstract ClinicalDBAdaptor getClinicalDBAdaptor(String species, String assembly);


    public abstract CpGIslandDBAdaptor getCpGIslandDBAdaptor(String species);

    public abstract CpGIslandDBAdaptor getCpGIslandDBAdaptor(String species, String assembly);


    public abstract StructuralVariationDBAdaptor getStructuralVariationDBAdaptor(String species);

    public abstract StructuralVariationDBAdaptor getStructuralVariationDBAdaptor(String species, String assembly);


    public abstract PathwayDBAdaptor getPathwayDBAdaptor(String species);

    public abstract PathwayDBAdaptor getPathwayDBAdaptor(String species, String assembly);


    public abstract ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species);

    public abstract ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species, String assembly);


//    public abstract RegulatoryRegionDBAdaptor getRegulationDBAdaptor(String species);
//
//    public abstract RegulatoryRegionDBAdaptor getRegulationDBAdaptor(String species, String assembly);


    public abstract VariationDBAdaptor getVariationDBAdaptor(String species);

    public abstract VariationDBAdaptor getVariationDBAdaptor(String species, String assembly);


    public abstract VariantFunctionalScoreDBAdaptor getVariantFunctionalScoreDBAdaptor(String species);

    public abstract VariantFunctionalScoreDBAdaptor getVariantFunctionalScoreDBAdaptor(String species, String assembly);


    public abstract ConservedRegionDBAdaptor getConservedRegionDBAdaptor(String species);

    public abstract ConservedRegionDBAdaptor getConservedRegionDBAdaptor(String species, String assembly);


}
