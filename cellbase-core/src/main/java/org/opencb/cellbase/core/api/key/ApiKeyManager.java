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

package org.opencb.cellbase.core.api.key;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

import static org.opencb.cellbase.core.api.key.ApiKeyJwtPayload.DATE_FORMATTER;

public class ApiKeyManager {
    private SignatureAlgorithm algorithm;
    private Key privateKey;
    private Key publicKey;
    private JwtParser jwtParser;

    private String defaultApiKey;

    private final Logger logger = LoggerFactory.getLogger(ApiKeyManager.class);

    public static final int SECRET_KEY_MIN_LENGTH = 50;

    public ApiKeyManager(String key) {
        this(SignatureAlgorithm.HS256.getValue(), new SecretKeySpec(Base64.getEncoder().encode(key.getBytes(StandardCharsets.UTF_8)),
                SignatureAlgorithm.HS256.getJcaName()));
    }

    public ApiKeyManager(String algorithm, Key secretKey) {
        this.algorithm = SignatureAlgorithm.forName(algorithm);
        this.privateKey = secretKey;
        this.publicKey = secretKey;
        jwtParser = Jwts.parserBuilder().setSigningKey(publicKey).build();

        // Create the default API key
        ApiKeyJwtPayload payload = new ApiKeyJwtPayload();
        payload.setSubject("ANONYMOUS");
        payload.setVersion(ApiKeyJwtPayload.CURRENT_VERSION);
        payload.setQuota(new ApiKeyQuota(ApiKeyQuota.MAX_NUM_ANOYMOUS_QUERIES));
        defaultApiKey = encode(payload);
    }

    public String encode(ApiKeyJwtPayload payload) {
        JwtBuilder jwtBuilder = Jwts.builder();

        return jwtBuilder.setClaims(payload)
                .signWith(privateKey, algorithm)
                .compact();
    }

    public ApiKeyJwtPayload decode(String apiKey) {
        if (publicKey == null) {
            // Remove signature to parse JWT
            apiKey = apiKey.substring(0, apiKey.lastIndexOf(".") + 1);
            return new ApiKeyJwtPayload(jwtParser.parseClaimsJwt(apiKey).getBody());
        } else {
            // Parse signed JWT (aka a 'JWS')
            return new ApiKeyJwtPayload(jwtParser.parseClaimsJws(apiKey).getBody());
        }
    }

    public String recode(String apiKey) {
        ApiKeyJwtPayload payload = decode(apiKey);
        if (MapUtils.isNotEmpty(payload.getSources())) {
            Map<String, Date> sources = new HashMap<>();
            for (Map.Entry<String, Date> entry : payload.getSources().entrySet()) {
                if (new Date().getTime() <= entry.getValue().getTime()) {
                    sources.put(entry.getKey(), entry.getValue());
                }
            }
            payload.setSources(sources);
        }

        return encode(payload);
    }

    public void validate(String apiKey) {
        decode(apiKey);
    }

    public boolean hasExpiredSource(String source, String apiKey) throws IllegalArgumentException {
        ApiKeyJwtPayload payload = decode(apiKey);
        if (MapUtils.isNotEmpty(payload.getSources()) && payload.getSources().containsKey(source)) {
            return (new Date().getTime() > payload.getSources().get(source).getTime());
        }
        throw new IllegalArgumentException("Data source '" + source + "' is not enabled for API key '" + apiKey + "'");
    }

    public Set<String> getValidSources(String apiKey) throws IllegalArgumentException {
        return getValidSources(apiKey, new HashSet<>());
    }

    public Set<String> getValidSources(String apiKey, Set<String> init) throws IllegalArgumentException {
        Set<String> validSources = new HashSet<>();
        if (CollectionUtils.isNotEmpty(init)) {
            validSources.addAll(init);
        }

        if (StringUtils.isNotEmpty(apiKey)) {
            ApiKeyJwtPayload payload = decode(apiKey);
            if (MapUtils.isNotEmpty(payload.getSources())) {
                for (Map.Entry<String, Date> entry : payload.getSources().entrySet()) {
                    if (new Date().getTime() <= entry.getValue().getTime()) {
                        validSources.add(entry.getKey());
                    } else {
                        String msg = "CellBase API key expired at " + DATE_FORMATTER.format(entry.getValue())
                                + " for data source '" + entry.getKey() + "'";
                        logger.error(msg);
                        throw new IllegalArgumentException(msg);
                    }
                }
            }
        }
        return validSources;
    }

    public String getDefaultApiKey() {
        return defaultApiKey;
    }

    public void display(String apiKey) {
        ApiKeyJwtPayload payload = decode(apiKey);

        final StringBuilder sb = new StringBuilder();
        sb.append("API key: ").append(apiKey).append("\n");
        sb.append("Organization: ").append(payload.getSubject()).append("\n");
        if (payload.getIssuedAt() != null) {
            sb.append("Issued at: ").append(DATE_FORMATTER.format(payload.getIssuedAt())).append("\n");
        } else {
            sb.append("Issued at: unknown\n");
        }
        if (payload.getExpiration() != null) {
            sb.append("Expiration at: ").append(DATE_FORMATTER.format(payload.getExpiration())).append("\n");
        } else {
            sb.append("Expiration at: unknown\n");
        }
        sb.append("Version: ").append(payload.getVersion()).append("\n");
        sb.append("Sources:\n");
        Map<String, Date> sources = payload.getSources();
        for (Map.Entry<String, Date> entry : sources.entrySet()) {
            sb.append("\t- '").append(entry.getKey()).append("' until ").append(DATE_FORMATTER.format(entry.getValue())).append("\n");
        }
        sb.append("Quota:\n");
        sb.append("\tMax. num. queries: ").append(payload.getQuota().getMaxNumQueries()).append("\n");

        System.out.println(sb);
    }
}
