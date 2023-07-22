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

package org.opencb.cellbase.core.token;

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

import static org.opencb.cellbase.core.token.TokenJwtPayload.DATE_FORMATTER;

public class DataAccessTokenManager {
    private SignatureAlgorithm algorithm;
    private Key privateKey;
    private Key publicKey;
    private JwtParser jwtParser;

    private String defaultToken;

    private final Logger logger = LoggerFactory.getLogger(DataAccessTokenManager.class);

    public static final int SECRET_KEY_MIN_LENGTH = 50;

    public DataAccessTokenManager(String key) {
        this(SignatureAlgorithm.HS256.getValue(), new SecretKeySpec(Base64.getEncoder().encode(key.getBytes(StandardCharsets.UTF_8)),
                SignatureAlgorithm.HS256.getJcaName()));
    }

    public DataAccessTokenManager(String algorithm, Key secretKey) {
        this.algorithm = SignatureAlgorithm.forName(algorithm);
        this.privateKey = secretKey;
        this.publicKey = secretKey;
        jwtParser = Jwts.parserBuilder().setSigningKey(publicKey).build();

        // Create the default token
        TokenJwtPayload payload = new TokenJwtPayload();
        payload.setSubject("ANONYMOUS");
        payload.setVersion(TokenJwtPayload.CURRENT_VERSION);
        payload.setQuota(new TokenQuota(TokenQuota.MAX_NUM_ANOYMOUS_QUERIES));
        defaultToken = encode(payload);
    }

    public String encode(TokenJwtPayload payload) {
        JwtBuilder jwtBuilder = Jwts.builder();

        return jwtBuilder.setClaims(payload)
                .signWith(privateKey, algorithm)
                .compact();
    }

    public TokenJwtPayload decode(String token) {
        if (publicKey == null) {
            // Remove signature to parse JWT
            token = token.substring(0, token.lastIndexOf(".") + 1);
            return new TokenJwtPayload(jwtParser.parseClaimsJwt(token).getBody());
        } else {
            // Parse signed JWT (aka a 'JWS')
            return new TokenJwtPayload(jwtParser.parseClaimsJws(token).getBody());
        }
    }

    public String recode(String token) {
        TokenJwtPayload payload = decode(token);
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

    public void validate(String token) {
        decode(token);
    }

    public boolean hasExpiredSource(String source, String token) throws IllegalArgumentException {
        TokenJwtPayload payload = decode(token);
        if (MapUtils.isNotEmpty(payload.getSources()) && payload.getSources().containsKey(source)) {
            return (new Date().getTime() > payload.getSources().get(source).getTime());
        }
        throw new IllegalArgumentException("Data source '" + source + "' is not enabled for token '" + token + "'");
    }

    public Set<String> getValidSources(String token) throws IllegalArgumentException {
        return getValidSources(token, new HashSet<>());
    }

    public Set<String> getValidSources(String token, Set<String> init) throws IllegalArgumentException {
        Set<String> validSources = new HashSet<>();
        if (CollectionUtils.isNotEmpty(init)) {
            validSources.addAll(init);
        }

        if (StringUtils.isNotEmpty(token)) {
            TokenJwtPayload payload = decode(token);
            if (MapUtils.isNotEmpty(payload.getSources())) {
                for (Map.Entry<String, Date> entry : payload.getSources().entrySet()) {
                    if (new Date().getTime() <= entry.getValue().getTime()) {
                        validSources.add(entry.getKey());
                    } else {
                        String msg = "CellBase token expired at " + DATE_FORMATTER.format(entry.getValue())
                                + " for data source '" + entry.getKey() + "'";
                        logger.error(msg);
                        throw new IllegalArgumentException(msg);
                    }
                }
            }
        }
        return validSources;
    }

    public String getDefaultToken() {
        return defaultToken;
    }

    public void display(String token) {
        TokenJwtPayload payload = decode(token);

        final StringBuilder sb = new StringBuilder();
        sb.append("Token: ").append(token).append("\n");
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
