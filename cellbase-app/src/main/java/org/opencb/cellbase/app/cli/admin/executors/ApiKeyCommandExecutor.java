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
import org.opencb.cellbase.core.api.key.ApiKeyJwtPayload;
import org.opencb.cellbase.core.api.key.ApiKeyManager;
import org.opencb.cellbase.core.api.key.ApiKeyQuota;

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

public class ApiKeyCommandExecutor extends CommandExecutor {

    private AdminCliOptionsParser.ApiKeyCommandOptions apiKeyCommandOptions;

    private DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");


    public ApiKeyCommandExecutor(AdminCliOptionsParser.ApiKeyCommandOptions apiKeyCommandOptions) {
        super(apiKeyCommandOptions.commonOptions.logLevel, apiKeyCommandOptions.commonOptions.conf);

        this.apiKeyCommandOptions = apiKeyCommandOptions;
    }

    /**
     * Execute one of the selected actions according to the input parameters.
     */
    public void execute() {
        checkParameters();

        Key key = new SecretKeySpec(Base64.getEncoder().encode(configuration.getSecretKey().getBytes(StandardCharsets.UTF_8)),
                SignatureAlgorithm.HS256.getJcaName());
        ApiKeyManager apiKeyManager = new ApiKeyManager(SignatureAlgorithm.HS256.getValue(), key);

        try {
            if (apiKeyCommandOptions.createApiKey) {
                // Create the API key JWT payload
                ApiKeyJwtPayload payload = new ApiKeyJwtPayload();
                payload.setSubject(apiKeyCommandOptions.organization);
                payload.setVersion(ApiKeyJwtPayload.CURRENT_VERSION);
                payload.setIssuedAt(new Date());
                if (apiKeyCommandOptions.expiration != null) {
                    payload.setExpiration(parseDate(apiKeyCommandOptions.expiration));
                }
                payload.setSources(parseSources(apiKeyCommandOptions.dataSources));
                payload.setQuota(new ApiKeyQuota(apiKeyCommandOptions.maxNumQueries));

                // Create API key
                String apiKey = apiKeyManager.encode(payload);
                System.out.println("API key generated:\n" + apiKey);
            } else if (StringUtils.isNotEmpty(apiKeyCommandOptions.apiKeyToView)) {
                // View API key
                apiKeyManager.display(apiKeyCommandOptions.apiKeyToView);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void checkParameters() {
        if (apiKeyCommandOptions.createApiKey && StringUtils.isNotEmpty(apiKeyCommandOptions.apiKeyToView)) {
            throw new IllegalArgumentException("Please, select only one of these input parameters: create-api-key or view-api-key");
        }
        if (!apiKeyCommandOptions.createApiKey && StringUtils.isEmpty(apiKeyCommandOptions.apiKeyToView)) {
            throw new IllegalArgumentException("Please, it is mandatory to select one of these input parameters: create-api-key or"
                    + " view-api-key");
        }

        // Check create parameters
        if (apiKeyCommandOptions.createApiKey) {
            if (StringUtils.isEmpty(apiKeyCommandOptions.organization)) {
                throw new IllegalArgumentException("Missing organization");
            }
            if (StringUtils.isEmpty(apiKeyCommandOptions.dataSources) && apiKeyCommandOptions.maxNumQueries <= 0) {
                throw new IllegalArgumentException("Please, it is mandatory to specify either the licensed data sources, the maximum number"
                        + " of queries, or both");
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
