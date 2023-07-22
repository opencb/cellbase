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
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.core.token.DataAccessTokenManager;
import org.opencb.cellbase.core.token.TokenJwtPayload;
import org.opencb.cellbase.core.token.TokenQuota;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DataTokenCommandExecutor extends CommandExecutor {

    private AdminCliOptionsParser.DataTokenCommandOptions dataTokenCommandOptions;

    private DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");


    public DataTokenCommandExecutor(AdminCliOptionsParser.DataTokenCommandOptions dataTokenCommandOptions) {
        super(dataTokenCommandOptions.commonOptions.logLevel, dataTokenCommandOptions.commonOptions.conf);

        this.dataTokenCommandOptions = dataTokenCommandOptions;
    }


    /**
     * Execute one of the selected actions according to the input parameters.
     */
    public void execute() {
        checkParameters();

        Key key = new SecretKeySpec(Base64.getEncoder().encode(configuration.getSecretKey().getBytes(StandardCharsets.UTF_8)),
                SignatureAlgorithm.HS256.getJcaName());
        DataAccessTokenManager datManager = new DataAccessTokenManager(SignatureAlgorithm.HS256.getValue(), key);

        try {
            if (StringUtils.isNotEmpty(dataTokenCommandOptions.createWithDataSources)) {
                // Create the token payload
                TokenJwtPayload payload = new TokenJwtPayload();
                payload.setSubject(dataTokenCommandOptions.organization);
                payload.setVersion(TokenJwtPayload.CURRENT_VERSION);
                payload.setIssuedAt(new Date());
                if (dataTokenCommandOptions.expiration != null) {
                    payload.setExpiration(parseDate(dataTokenCommandOptions.expiration));
                }
                payload.setSources(parseSources(dataTokenCommandOptions.createWithDataSources));
                payload.setQuota(new TokenQuota(dataTokenCommandOptions.maxNumQueries));

                // Create token
                String token = datManager.encode(payload);
                System.out.println("Data access token generated:\n" + token);
            } else if (StringUtils.isNotEmpty(dataTokenCommandOptions.tokenToView)) {
                // View data token
                datManager.display(dataTokenCommandOptions.tokenToView);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void checkParameters() {
        if (StringUtils.isNotEmpty(dataTokenCommandOptions.createWithDataSources)
                && StringUtils.isNotEmpty(dataTokenCommandOptions.tokenToView)) {
            throw new IllegalArgumentException("Please, select only one of these input parameters: create or view");
        }
        if (StringUtils.isEmpty(dataTokenCommandOptions.createWithDataSources)
                && StringUtils.isEmpty(dataTokenCommandOptions.tokenToView)) {
            throw new IllegalArgumentException("Please, it is mandatory to select one of these input parameters: create or view");
        }

        // Check create parameters
        if (StringUtils.isNotEmpty(dataTokenCommandOptions.createWithDataSources)) {
            if (StringUtils.isEmpty(dataTokenCommandOptions.organization)) {
                throw new IllegalArgumentException("Missing organization");
            }
        }

        if (StringUtils.isEmpty(configuration.getSecretKey())) {
            throw new IllegalArgumentException("Missing secret key in the CellBase configuration file.");
        }
    }

    private Map<String, Date> parseSources(String sources) throws ParseException {
        Map<String, Date> sourcesMap = new HashMap<>();
        if (StringUtils.isNotEmpty(sources)) {
            String[] split = sources.split(",");
            for (String source : split) {
                String[] splits = source.split(":");
                if (splits.length == 1) {
                    sourcesMap.put(splits[0], parseDate("31/12/999999"));
                } else {
                    sourcesMap.put(splits[0], parseDate(splits[1]));
                }
            }
        }
        return sourcesMap;
    }

    private Date parseDate(String date) throws ParseException {
        return dateFormatter.parse(date);
    }
}
