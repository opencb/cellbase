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

import java.security.InvalidParameterException;
import java.util.Locale;


public final class DatabaseNameUtils {

    public static final String DBNAME_PREFIX = "cellbase";
    public static final String DBNAME_SEPARATOR = "_";

    private DatabaseNameUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String getDatabaseName(String species, String assembly, String version) {
        if (StringUtils.isEmpty(species) || StringUtils.isEmpty(assembly)) {
            throw new InvalidParameterException("Both species and assembly are required");
        }

        // Remove special characters
        String dbnameAssembly = cleanAssembly(assembly);

        // Process version from the configuration file, in order to suffix the database name
        //  - Production environment, e.g.: if version is "v5", the suffix added wil be "_v5"
        //  - Test environment, e.g.: if version is "v5.6" or "v5.6.0-SNAPSHOT", the suffix added will be "_v5_6"
        String auxVersion = version.replace(".", DBNAME_SEPARATOR).replace("-", DBNAME_SEPARATOR);
        String[] split = auxVersion.split(DBNAME_SEPARATOR);
        String dbName = DBNAME_PREFIX + DBNAME_SEPARATOR + species.toLowerCase() + DBNAME_SEPARATOR + dbnameAssembly.toLowerCase()
                + DBNAME_SEPARATOR + split[0];
        if (split.length > 1) {
            dbName += (DBNAME_SEPARATOR + split[1]);
        }
        return dbName;
    }

    public static String cleanAssembly(String assembly) {
        if (StringUtils.isEmpty(assembly)) {
            throw new InvalidParameterException("Assembly is empty");
        }

        return assembly.replace(".", "")
                .replace("-", "")
                .replace("_", "").toLowerCase(Locale.ROOT);
    }

    public static String getSpeciesFromDatabaseName(String databaseName) {
        if (StringUtils.isEmpty(databaseName)) {
            throw new InvalidParameterException("Database name is empty");
        }

        return databaseName.split(DBNAME_SEPARATOR)[1].toLowerCase(Locale.ROOT);
    }
}
