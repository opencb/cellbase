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

package org.opencb.cellbase.lib.token;

import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.TextCodec;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.token.DataAccessTokenSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.*;

import static org.opencb.cellbase.core.token.DataAccessTokenSources.dateFormatter;

public class DataAccessTokenManager {
    private SignatureAlgorithm algorithm;
    private Key privateKey;
    private Key publicKey;

    private final Logger logger;

    public static final int SECRET_KEY_MIN_LENGTH = 50;
    public static final String VERSION_FIELD_NAME = "version";
    public static final String SOURCES_FIELD_NAME = "sources";

    public DataAccessTokenManager(String key) {
        this(SignatureAlgorithm.HS256.getValue(), new SecretKeySpec(TextCodec.BASE64.decode(key), SignatureAlgorithm.HS256.getJcaName()));
    }

    public DataAccessTokenManager(String algorithm, Key secretKey) {
        this.algorithm = SignatureAlgorithm.forName(algorithm);
        this.privateKey = secretKey;
        this.publicKey = secretKey;

        logger = LoggerFactory.getLogger(DataAccessTokenManager.class);
    }

    public String encode(String organization, DataAccessTokenSources dat) {
        JwtBuilder jwtBuilder = Jwts.builder();

        Map<String, Object> claims = new HashMap<>();
        claims.put(VERSION_FIELD_NAME, dat.getVersion());
        if (MapUtils.isNotEmpty(dat.getSources())) {
            claims.put(SOURCES_FIELD_NAME, dat.getSources());
        }

        jwtBuilder.setClaims(claims)
                .setSubject(organization)
                .setIssuedAt(new Date())
                .signWith(privateKey, algorithm);

        return jwtBuilder.compact();
    }

    public DataAccessTokenSources decode(String token) {
        DataAccessTokenSources dat = new DataAccessTokenSources();

        Jws<Claims> claimsJws = parse(token);
        Claims body = claimsJws.getBody();
        for (Map.Entry<String, Object> entry : body.entrySet()) {
            String key = entry.getKey();
            switch (key) {
                case VERSION_FIELD_NAME:
                    dat.setVersion((String) body.get(key));
                    break;
                case SOURCES_FIELD_NAME:
                    dat.setSources((Map<String, Long>) body.get(key));
                    break;
                default:
                    break;
            }
        }

        return dat;
    }

    public void validate(String token) {
        parse(token);
    }

    public boolean hasExpiredSource(String source, String token) throws IllegalArgumentException {
        DataAccessTokenSources dat = decode(token);
        if (MapUtils.isNotEmpty(dat.getSources()) && dat.getSources().containsKey(source)) {
            return (new Date().getTime() > dat.getSources().get(source));
        }
        throw new IllegalArgumentException("Data source '" + source + "' is not enabled for token '" + token + "'");
    }

    public Set<String> getValidSources(String token) throws IllegalArgumentException {
        Set<String> validSources = new HashSet<>();
        if (StringUtils.isNotEmpty(token)) {
            DataAccessTokenSources dat = decode(token);
            if (MapUtils.isNotEmpty(dat.getSources())) {
                for (Map.Entry<String, Long> entry : dat.getSources().entrySet()) {
                    if (new Date().getTime() <= entry.getValue()) {
                        validSources.add(entry.getKey());
                    } else {
                        String msg = "CellBase token expired at " + dateFormatter().format(entry.getValue()) + " for data source '"
                                + entry.getKey() + "'";
                        logger.error(msg);
                        throw new IllegalArgumentException(msg);
                    }
                }
            }
        }
        return validSources;
    }

    public String getOrganization(String token) {
        Jws<Claims> parse = parse(token);
        return parse.getBody().getSubject();
    }

    public String getCreationDate(String token) {
        Jws<Claims> parse = parse(token);
        return dateFormatter().format(parse.getBody().getIssuedAt());
    }

    public void display(String token) {
        Claims body = parse(token).getBody();

        final StringBuilder sb = new StringBuilder();
        sb.append("Token: ").append(token).append("\n");
        sb.append("Organization: ").append(body.getSubject()).append("\n");
        sb.append("Issued at: ").append(dateFormatter().format(body.getIssuedAt())).append("\n");
        sb.append("Version: ").append(body.get(VERSION_FIELD_NAME)).append("\n");
        sb.append("Sources:\n");
        Map<String, Long> sources = (Map<String, Long>) body.get(SOURCES_FIELD_NAME);
        for (Map.Entry<String, Long> entry : sources.entrySet()) {
            sb.append("\t- '").append(entry.getKey()).append("' until ").append(dateFormatter().format(entry.getValue())).append("\n");
        }

        System.out.println(sb);
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);
    }
}
