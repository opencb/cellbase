package org.opencb.cellbase.lib.managers;

import io.jsonwebtoken.Jwts;
import org.apache.commons.collections4.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.opencb.cellbase.core.token.DataAccessTokenManager;
import org.opencb.cellbase.core.token.TokenJwtPayload;
import org.opencb.cellbase.core.token.TokenQuota;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.opencb.cellbase.core.token.TokenJwtPayload.DATE_FORMATTER;

public class DataAccessTokenManagerTest {

    DataAccessTokenManager datManager;

    @Before
    public void before() {
        String randomStr = "xPacig89igHSieEnveJEi4KCfdEslhmssC3vui1JJQGgDQ0y8v";
        System.out.println("Secret key = " + randomStr);
        datManager = new DataAccessTokenManager(randomStr);
    }

    @Test
    public void test1() throws ParseException {
        TokenJwtPayload payload = new TokenJwtPayload();
        payload.setSubject("ucam");
        payload.setVersion(TokenJwtPayload.CURRENT_VERSION);
        Map<String, Date> sources = new HashMap<>();
        sources.put("cosmic", DATE_FORMATTER.parse("25/09/2025"));
        sources.put("hgmd", DATE_FORMATTER.parse("01/01/2024"));
        payload.setSources(sources);

        String token = datManager.encode(payload);
        System.out.println(token);
        datManager.display(token);

        TokenJwtPayload payload1 = datManager.decode(token);
        System.out.println(payload1);

        datManager.validate(token);
        System.out.println("valid token: pass");

        try {
            token += "tototo";
            datManager.validate(token);
        } catch (Exception e) {
            System.out.println("invalid token: pass");
            return;
        }
        fail();
    }

    @Test
    public void testNotExpired() throws ParseException {
        TokenJwtPayload payload = new TokenJwtPayload();
        payload.setSubject("ucam");
        payload.setVersion(TokenJwtPayload.CURRENT_VERSION);
        Map<String, Date> sources = new HashMap<>();
        sources.put("cosmic", DATE_FORMATTER.parse("25/09/2025"));
        payload.setSources(sources);

        String token = datManager.encode(payload);
        System.out.println(token);

        assertEquals(false, datManager.hasExpiredSource("cosmic", token));
    }

    @Test
    public void testExpired() throws ParseException {
        TokenJwtPayload payload = new TokenJwtPayload();
        payload.setSubject("ucam");
        payload.setVersion(TokenJwtPayload.CURRENT_VERSION);
        Map<String, Date> sources = new HashMap<>();
        sources.put("cosmic", DATE_FORMATTER.parse("25/09/2021"));
        payload.setSources(sources);

        String token = datManager.encode(payload);
        System.out.println(token);

        assertEquals(true, datManager.hasExpiredSource("cosmic", token));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSource() throws ParseException {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        TokenJwtPayload payload = new TokenJwtPayload();
        payload.setSubject("ucam");
        payload.setVersion(TokenJwtPayload.CURRENT_VERSION);
        Map<String, Date> sources = new HashMap<>();
        sources.put("cosmic", formatter.parse("25/09/2021"));
        payload.setSources(sources);

        String token = datManager.encode(payload);

        datManager.hasExpiredSource("hgmd", token);
    }

    @Test
    public void testRecodeToken() throws ParseException {
        TokenJwtPayload payload = new TokenJwtPayload();
        payload.setSubject("ucam");
        payload.setVersion(TokenJwtPayload.CURRENT_VERSION);
        Map<String, Date> sources = new HashMap<>();
        sources.put("cadd", DATE_FORMATTER.parse("25/09/2030"));
        sources.put("cosmic", DATE_FORMATTER.parse("25/09/2021"));
        payload.setSources(sources);

        String token = datManager.encode(payload);
        String newToken = datManager.recode(token);

        TokenJwtPayload payload1 = datManager.decode(newToken);
        assertEquals(1, payload1.getSources().size());
        assertTrue(payload1.getSources().containsKey("cadd"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidSources() throws ParseException {
        TokenJwtPayload payload = new TokenJwtPayload();
        payload.setSubject("ucam");
        payload.setVersion(TokenJwtPayload.CURRENT_VERSION);
        Map<String, Date> sources = new HashMap<>();
        sources.put("cosmic", DATE_FORMATTER.parse("25/09/2024"));
        sources.put("hgmd", DATE_FORMATTER.parse("25/09/2020"));
        sources.put("cadd", DATE_FORMATTER.parse("25/09/2025"));
        payload.setSources(sources);

        String token = datManager.encode(payload);

        datManager.getValidSources(token);
    }

    @Test
    public void defaultTokenTest() {
        String defaultToken = datManager.getDefaultToken();
        TokenJwtPayload payload = datManager.decode(defaultToken);
        assertTrue(MapUtils.isEmpty(payload.getSources()));
        assertEquals((long) TokenQuota.MAX_NUM_ANOYMOUS_QUERIES, payload.getQuota().getMaxNumQueries());
    }
}