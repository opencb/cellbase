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

package org.opencb.cellbase.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.common.Species;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;

import java.util.List;


public class SpeciesUtils {

    /**
     * Return species object based on species name and assembly. If assembly is not provided, default assembly is used
     * (the first one listed in the config file)
     *
     * @param configuration configuration file listing the valid species and assemblies
     * @param speciesStr  name of species, e.g. Homo sapiens or hsapiens
     * @param assemblyStr assembly version
     * @throws CellBaseException if the species or assembly is invalid
     * @return species object
     */
    public static Species getSpecies(CellBaseConfiguration configuration, String speciesStr, String assemblyStr) throws CellBaseException {
        Species species = null;
        for (SpeciesConfiguration sp : configuration.getAllSpecies()) {
            if (speciesStr.equalsIgnoreCase(sp.getScientificName()) || speciesStr.equalsIgnoreCase(sp.getCommonName())
                    || speciesStr.equalsIgnoreCase(sp.getId())) {
                SpeciesConfiguration.Assembly assembly;
                if (StringUtils.isNotEmpty(assemblyStr)) {
                    assembly = getAssembly(sp, assemblyStr);
                    if (assembly == null) {
                        throw new CellBaseException("Assembly '" + assemblyStr + "' not found for species '" + speciesStr + "'");
                    }
                } else {
                    assembly = getDefaultAssembly(sp);
                }
                species = new Species(sp.getId(), sp.getCommonName(), sp.getScientificName(), assembly.getName());
                break;
            }
        }
        if (species == null) {
            throw new CellBaseException("Species '" + speciesStr + "' not found");
        }
        return species;
    }

    /**
     * Check given assembly is valid, case insensitve. NULL if invalid assembly.
     *
     * @param speciesConfiguration configuration entry for this species
     * @param assemblyString name of assembly
     * @return the assembly name OR NULL of no assembly found.
     */
    public static SpeciesConfiguration.Assembly getAssembly(SpeciesConfiguration speciesConfiguration, String assemblyString) {
        for (SpeciesConfiguration.Assembly assembly : speciesConfiguration.getAssemblies()) {
            if (assembly.getName().equalsIgnoreCase(assemblyString)) {
                return assembly;
            }
        }
        return null;
    }

    public static boolean validateSpeciesAndAssembly(CellBaseConfiguration configuration, String species, String assembly) {
        if (StringUtils.isEmpty(species) || StringUtils.isEmpty(assembly)) {
            return false;
        }

        for (SpeciesConfiguration sp : configuration.getAllSpecies()) {
            if (species.equalsIgnoreCase(sp.getScientificName()) || species.equalsIgnoreCase(sp.getCommonName())
                    || species.equalsIgnoreCase(sp.getId())) {
                return getAssembly(sp, assembly) != null;
            }
        }
        return false;
    }

    public static boolean validateSpecies(CellBaseConfiguration configuration, String species) {
        if (StringUtils.isEmpty(species)) {
            return false;
        }

        for (SpeciesConfiguration sp : configuration.getAllSpecies()) {
            if (species.equalsIgnoreCase(sp.getScientificName()) || species.equalsIgnoreCase(sp.getCommonName())
                    || species.equalsIgnoreCase(sp.getId())) {
                return true;
            }
        }
        // species not found
        return false;
    }

    public static SpeciesConfiguration getSpeciesConfiguration(CellBaseConfiguration configuration, String species) {
        SpeciesConfiguration speciesConfiguration = null;
        for (SpeciesConfiguration sp : configuration.getAllSpecies()) {
            if (species.equalsIgnoreCase(sp.getScientificName())
                    || species.equalsIgnoreCase(sp.getCommonName())
                    || species.equalsIgnoreCase(sp.getId())) {
                speciesConfiguration = sp;
                break;
            }
        }
        return speciesConfiguration;
    }

    /**
     * Get the default assembly for species. Is naive and just gets the first one. Order not guaranteed, don't rely on this at all.
     *
     * @param speciesConfiguration configuration entry for this species
     * @return the default assembly
     * @throws CellBaseException if the species has no associated assembly
     */
    public static SpeciesConfiguration.Assembly getDefaultAssembly(SpeciesConfiguration speciesConfiguration) throws CellBaseException {
        List<SpeciesConfiguration.Assembly> assemblies = speciesConfiguration.getAssemblies();
        if (assemblies == null || assemblies.isEmpty()) {
            throw new CellBaseException("Species has no associated assembly: " + speciesConfiguration.getScientificName());
        }
        return assemblies.get(0);
    }

    /**
     * Get the default assembly for species. Is naive and just gets the first one. Order not guaranteed, don't rely on this at all.
     *
     * @param species name of species
     * @param configuration for this database
     * @return the default assembly
     * @throws CellBaseException if the species has no associated assembly
     */
    public static SpeciesConfiguration.Assembly getDefaultAssembly(CellBaseConfiguration configuration, String species)
        throws CellBaseException {
        SpeciesConfiguration speciesConfiguration = getSpeciesConfiguration(configuration, species);
        List<SpeciesConfiguration.Assembly> assemblies = speciesConfiguration.getAssemblies();
        if (assemblies == null || assemblies.isEmpty()) {
            throw new CellBaseException("Species has no associated assembly " + speciesConfiguration.getScientificName());
        }
        return assemblies.get(0);
    }

    /**
     * Formats the species name, e.g. Homo sapiens to hsapiens. Used in the download for the species directory.
     *
     * @param speciesConfiguration configuration entry for this species
     * @return formatted species name
     */
    public static String getSpeciesShortname(SpeciesConfiguration speciesConfiguration) {
        return speciesConfiguration.getScientificName().toLowerCase()
                .replaceAll("\\.", "")
                .replaceAll("\\)", "")
                .replaceAll("\\(", "")
                .replaceAll("[-/]", " ")
                .replaceAll("\\s+", "_");
    }
}
