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

package org.opencb.cellbase.lib.managers;

import io.jsonwebtoken.*;
import org.apache.commons.collections4.MapUtils;
import org.opencb.cellbase.core.models.DataAccessTokenSources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataAccessTokenManager {
    private SignatureAlgorithm algorithm;
    private Key privateKey;
    private Key publicKey;

    private final DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    private final Logger logger;

    public static final int SECRET_KEY_MIN_LENGTH = 32;

    public DataAccessTokenManager(String algorithm, Key secretKey) {
        this.algorithm = SignatureAlgorithm.forName(algorithm);
        this.privateKey = secretKey;
        this.publicKey = secretKey;

        this.logger = LoggerFactory.getLogger(DataAccessTokenManager.class);
    }

    public String encode(String client, DataAccessTokenSources dat) {
        JwtBuilder jwtBuilder = Jwts.builder();

        Map<String, Object> claims = new HashMap<>();
        claims.put("version", dat.getVersion());
        if (MapUtils.isNotEmpty(dat.getSources())) {
            claims.put("sources", dat.getSources());
        }

        jwtBuilder.setClaims(claims)
                .setSubject(client)
                .setIssuedAt(new Date())
                .signWith(privateKey, algorithm);

        return jwtBuilder.compact();
    }

    public DataAccessTokenSources decode(String token) {
        DataAccessTokenSources dat = new DataAccessTokenSources();

        Jws<Claims> claimsJws = parse(token);
        Claims body = claimsJws.getBody();
        for (String key : body.keySet()) {
            switch (key) {
                case "version":
                    dat.setVersion((String) body.get(key));
                    break;
                case "sources":
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
            if (new Date().getTime() > dat.getSources().get(source)) {
                return true;
            }
            return false;
        }
        throw new IllegalArgumentException("Data source '" + source + "' is not enabled for token '" + token + "'");
    }

    public Set<String> getValidSources(String token) throws IllegalArgumentException {
        Set<String> validSources = new HashSet<>();
        DataAccessTokenSources dat = decode(token);
        if (MapUtils.isNotEmpty(dat.getSources())) {
            for (Map.Entry<String, Long> entry : dat.getSources().entrySet()) {
                if (new Date().getTime() <= entry.getValue()) {
                    validSources.add(entry.getKey());
                }
            }
        }
        return validSources;
    }

    public String getClient(String token) {
        Jws<Claims> parse = parse(token);
        return parse.getBody().getSubject();
    }

    public String getCreationDate(String token) {
        Jws<Claims> parse = parse(token);
        return formatter.format(parse.getBody().getIssuedAt());
    }

    public void display(String token) {
        Claims body = parse(token).getBody();

        final StringBuilder sb = new StringBuilder();
        sb.append("Token: ").append(token).append("\n");
        sb.append("Client: ").append(body.getSubject()).append("\n");
        sb.append("Issued at: ").append(formatter.format(body.getIssuedAt())).append("\n");
        sb.append("Version: ").append(body.get("version")).append("\n");
        sb.append("Sources:\n");
        Map<String, Long> sources = (Map<String, Long>) body.get("sources");
        for (Map.Entry<String, Long> entry : sources.entrySet()) {
            sb.append("\t- '").append(entry.getKey()).append("' until ").append(formatter.format(entry.getValue())).append("\n");
        }

        System.out.println(sb);
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token);
    }

    //    constructor("x.y.z")
//    - encode/decode
//    - hasExpired()
//    - isCosmicValid()
}