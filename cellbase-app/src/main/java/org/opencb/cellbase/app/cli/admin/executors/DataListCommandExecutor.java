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

package org.opencb.cellbase.app.cli.admin.executors;

import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.utils.SpeciesUtils;

import java.util.List;

public class DataListCommandExecutor extends CommandExecutor {

    private AdminCliOptionsParser.DataListCommandOptions dataListCommandOptions;

    public DataListCommandExecutor(AdminCliOptionsParser.DataListCommandOptions dataListCommandOptions) {
        super(dataListCommandOptions.commonOptions.logLevel, dataListCommandOptions.commonOptions.conf);

        this.dataListCommandOptions = dataListCommandOptions;
    }


    /**
     * Execute one of the selected actions according to the input parameters.
     */
    public void execute() {
        SpeciesConfiguration speciesConfiguration = SpeciesUtils.getSpeciesConfiguration(configuration, dataListCommandOptions.species);
        if (speciesConfiguration == null) {
            System.out.println("Unknown species: " + dataListCommandOptions.species);
            System.out.println("Available species:");
            List<SpeciesConfiguration> allSpecies = SpeciesUtils.getAllSpecies(configuration);
            for (SpeciesConfiguration species : allSpecies) {
                System.out.println("\t- " + species.getScientificName() + " (" + species.getId() + ")");
            }
            return;
        }

        System.out.println("Species: " + dataListCommandOptions.species);
        System.out.println("Available data: " + StringUtils.join(speciesConfiguration.getData(), ","));
    }
}
