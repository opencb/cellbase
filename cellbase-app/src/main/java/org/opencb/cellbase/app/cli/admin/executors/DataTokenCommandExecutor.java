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

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.core.models.DataAccessTokenSources;
import org.opencb.cellbase.lib.managers.DataAccessTokenManager;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.text.ParseException;

public class DataTokenCommandExecutor extends CommandExecutor {

    private AdminCliOptionsParser.DataTokenCommandOptions dataTokenCommandOptions;

    public DataTokenCommandExecutor(AdminCliOptionsParser.DataTokenCommandOptions dataTokenCommandOptions) {
        super(dataTokenCommandOptions.commonOptions.logLevel, dataTokenCommandOptions.commonOptions.conf);

        this.dataTokenCommandOptions = dataTokenCommandOptions;
    }


    /**
     * Execute one of the selected actions according to the input parameters.
     */
    public void execute() {
        checkParameters();

        Key key = new SecretKeySpec(TextCodec.BASE64.decode(dataTokenCommandOptions.secretKey), SignatureAlgorithm.HS256.getJcaName());
        DataAccessTokenManager datManager = new DataAccessTokenManager(SignatureAlgorithm.HS256.getValue(), key);

        try {
            if (dataTokenCommandOptions.create) {
                // Create data token
                DataAccessTokenSources dataSources = null;
                dataSources = DataAccessTokenSources.parse(dataTokenCommandOptions.dataSources);
                String token = datManager.encode(dataTokenCommandOptions.organization, dataSources);
                System.out.println("Data access token generated:\n" + token);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (dataTokenCommandOptions.display) {
            // Display token
            datManager.display(dataTokenCommandOptions.token);
        }
    }

    private void checkParameters() {
        if (dataTokenCommandOptions.create && dataTokenCommandOptions.display) {
            throw new IllegalArgumentException("Please, select only one of these input parameters: create or view");
        }
        if (!dataTokenCommandOptions.create && !dataTokenCommandOptions.display) {
            throw new IllegalArgumentException("Please, it is mandatory to select one of these input parameters: create or view");
        }

        // Check create parameters
        if (dataTokenCommandOptions.create) {
            if (StringUtils.isEmpty(dataTokenCommandOptions.organization)) {
                throw new IllegalArgumentException("Missing organization");
            }
            if (StringUtils.isEmpty(dataTokenCommandOptions.dataSources)) {
                throw new IllegalArgumentException("Missing data sources");
            }
        }

        // Check view parameters
        if (dataTokenCommandOptions.display) {
            if (StringUtils.isEmpty(dataTokenCommandOptions.token)) {
                throw new IllegalArgumentException("Missing token");
            }
        }

        if (StringUtils.isEmpty(dataTokenCommandOptions.secretKey)) {
            throw new IllegalArgumentException("Missing secret key");
        }
    }
}
