package org.opencb.cellbase.lib.managers;

import org.apache.commons.collections4.MapUtils;
import org.junit.Before;
import org.junit.Test;
import org.opencb.cellbase.core.api.key.ApiKeyManager;
import org.opencb.cellbase.core.api.key.ApiKeyJwtPayload;
import org.opencb.cellbase.core.api.key.ApiKeyQuota;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.opencb.cellbase.core.api.key.ApiKeyJwtPayload.DATE_FORMATTER;

public class ApiKeyManagerTest {

    ApiKeyManager datManager;

    @Before
    public void before() {
        String randomStr = "xPacig89igHSieEnveJEi4KCfdEslhmssC3vui1JJQGgDQ0y8v";
        System.out.println("Secret key = " + randomStr);
        datManager = new ApiKeyManager(randomStr);
    }

    @Test
    public void test1() throws ParseException {
        ApiKeyJwtPayload payload = new ApiKeyJwtPayload();
        payload.setSubject("ucam");
        payload.setVersion(ApiKeyJwtPayload.CURRENT_VERSION);
        Map<String, Date> sources = new HashMap<>();
        sources.put("cosmic", DATE_FORMATTER.parse("25/09/2025"));
        sources.put("hgmd", DATE_FORMATTER.parse("01/01/2024"));
        payload.setSources(sources);

        String apiKey = datManager.encode(payload);
        System.out.println(apiKey);
        datManager.display(apiKey);

        ApiKeyJwtPayload payload1 = datManager.decode(apiKey);
        System.out.println(payload1);

        datManager.validate(apiKey);
        System.out.println("valid API key: pass");

        try {
            apiKey += "tototo";
            datManager.validate(apiKey);
        } catch (Exception e) {
            System.out.println("invalid API key: pass");
            return;
        }
        fail();
    }

    @Test
    public void testNotExpired() throws ParseException {
        ApiKeyJwtPayload payload = new ApiKeyJwtPayload();
        payload.setSubject("ucam");
        payload.setVersion(ApiKeyJwtPayload.CURRENT_VERSION);
        Map<String, Date> sources = new HashMap<>();
        sources.put("cosmic", DATE_FORMATTER.parse("25/09/2025"));
        payload.setSources(sources);

        String apiKey = datManager.encode(payload);
        System.out.println(apiKey);

        assertEquals(false, datManager.hasExpiredSource("cosmic", apiKey));
    }

    @Test
    public void testExpired() throws ParseException {
        ApiKeyJwtPayload payload = new ApiKeyJwtPayload();
        payload.setSubject("ucam");
        payload.setVersion(ApiKeyJwtPayload.CURRENT_VERSION);
        Map<String, Date> sources = new HashMap<>();
        sources.put("cosmic", DATE_FORMATTER.parse("25/09/2021"));
        payload.setSources(sources);

        String apiKey = datManager.encode(payload);
        System.out.println(apiKey);

        assertEquals(true, datManager.hasExpiredSource("cosmic", apiKey));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSource() throws ParseException {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        ApiKeyJwtPayload payload = new ApiKeyJwtPayload();
        payload.setSubject("ucam");
        payload.setVersion(ApiKeyJwtPayload.CURRENT_VERSION);
        Map<String, Date> sources = new HashMap<>();
        sources.put("cosmic", formatter.parse("25/09/2021"));
        payload.setSources(sources);

        String apiKey = datManager.encode(payload);

        datManager.hasExpiredSource("hgmd", apiKey);
    }

    @Test
    public void testRecodeApiKey() throws ParseException {
        ApiKeyJwtPayload payload = new ApiKeyJwtPayload();
        payload.setSubject("ucam");
        payload.setVersion(ApiKeyJwtPayload.CURRENT_VERSION);
        Map<String, Date> sources = new HashMap<>();
        sources.put("cadd", DATE_FORMATTER.parse("25/09/2030"));
        sources.put("cosmic", DATE_FORMATTER.parse("25/09/2021"));
        payload.setSources(sources);

        String apiKey = datManager.encode(payload);
        String newApiKey = datManager.recode(apiKey);

        ApiKeyJwtPayload payload1 = datManager.decode(newApiKey);
        assertEquals(1, payload1.getSources().size());
        assertTrue(payload1.getSources().containsKey("cadd"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidSources() throws ParseException {
        ApiKeyJwtPayload payload = new ApiKeyJwtPayload();
        payload.setSubject("ucam");
        payload.setVersion(ApiKeyJwtPayload.CURRENT_VERSION);
        Map<String, Date> sources = new HashMap<>();
        sources.put("cosmic", DATE_FORMATTER.parse("25/09/2024"));
        sources.put("hgmd", DATE_FORMATTER.parse("25/09/2020"));
        sources.put("cadd", DATE_FORMATTER.parse("25/09/2025"));
        payload.setSources(sources);

        String apiKey = datManager.encode(payload);

        datManager.getValidSources(apiKey);
    }

    @Test
    public void defaultApiKeyTest() {
        String defaultApiKey = datManager.getDefaultApiKey();
        ApiKeyJwtPayload payload = datManager.decode(defaultApiKey);
        assertTrue(MapUtils.isEmpty(payload.getSources()));
        assertEquals((long) ApiKeyQuota.MAX_NUM_ANOYMOUS_QUERIES, payload.getQuota().getMaxNumQueries());
    }
}