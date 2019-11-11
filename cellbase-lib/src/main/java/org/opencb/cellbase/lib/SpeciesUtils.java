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

package org.opencb.cellbase.lib;

import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.common.Species;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;

import java.util.List;


public class SpeciesUtils {

    /**
     * Return species object based on species name and assembly. If assembly is not provided, default assembly is used
     * (the first one listed in the config file)
     *
     * @param configuration configuration file listing the valid species and assemblies
     * @param userProvidedSpecies  name of species, e.g. Homo sapiens or hsapiens
     * @param userProvidedAssembly assembly version
     * @throws CellbaseException if the species or assembly is invalid
     * @return species object
     */
    public static Species getSpecies(CellBaseConfiguration configuration, String userProvidedSpecies,
                                     String userProvidedAssembly) throws CellbaseException {
        Species species = null;
        for (SpeciesConfiguration sp : configuration.getAllSpecies()) {
            if (userProvidedSpecies.equalsIgnoreCase(sp.getScientificName())
                    || userProvidedSpecies.equalsIgnoreCase(sp.getCommonName())
                    || userProvidedSpecies.equalsIgnoreCase(sp.getId())) {
                String assembly;
                if (StringUtils.isNotEmpty(userProvidedAssembly)) {
                    assembly = checkAssembly(sp, userProvidedAssembly);
                    if (assembly == null) {
                        throw new CellbaseException("Assembly '" + userProvidedAssembly + "' not found for species "
                                + userProvidedSpecies);
                    }
                } else {
                    assembly = getDefaultAssembly(sp);
                }
                species = new Species(sp.getId(), sp.getCommonName(), sp.getScientificName(), assembly);
            }
        }
        if (species == null) {
            throw new CellbaseException("species '" + userProvidedSpecies + "' not found");
        }
        return species;
    }

    private static String checkAssembly(SpeciesConfiguration speciesConfiguration, String assemblyString) {
        for (SpeciesConfiguration.Assembly assembly : speciesConfiguration.getAssemblies()) {
            if (assembly.getName().equalsIgnoreCase(assemblyString)) {
                return assemblyString;
            }
        }
        return null;
    }

    private static String getDefaultAssembly(SpeciesConfiguration speciesConfiguration) throws CellbaseException {
        List<SpeciesConfiguration.Assembly> assemblies = speciesConfiguration.getAssemblies();
        if (assemblies == null || assemblies.isEmpty()) {
            throw new CellbaseException("Species has no associated assembly " + speciesConfiguration.getScientificName());
        }
        return assemblies.get(0).getName();
    }
}
