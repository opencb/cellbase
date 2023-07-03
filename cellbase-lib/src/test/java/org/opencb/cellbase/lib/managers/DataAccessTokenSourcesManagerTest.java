package org.opencb.cellbase.lib.managers;

import org.junit.Before;
import org.junit.Test;
import org.opencb.cellbase.core.token.DataAccessTokenSources;
import org.opencb.cellbase.core.token.DataAccessTokenManager;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DataAccessTokenSourcesManagerTest {

    DataAccessTokenManager datManager;

    @Before
    public void before() {
        String randomStr = "xPacig89igHSieEnveJEi4KCfdEslhmssC3vui1JJQGgDQ0y8v";
        System.out.println("Secret key = " + randomStr);
        datManager = new DataAccessTokenManager(randomStr);
    }

    @Test
    public void test1() throws ParseException {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        DataAccessTokenSources dat = new DataAccessTokenSources();
        dat.setVersion("1.0");
        Map<String, Long> sources = new HashMap<>();

        sources.put("cosmic", formatter.parse("25/09/2025").getTime());
        sources.put("hgmd", formatter.parse("01/01/2024").getTime());
        dat.setSources(sources);
        System.out.println(dat);

        String token = datManager.encode("ucam", dat);
        System.out.println("token = " + token);
        datManager.display(token);

        DataAccessTokenSources dat1 = datManager.decode(token);
        System.out.println(dat1);

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
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        DataAccessTokenSources dat = new DataAccessTokenSources();
        dat.setVersion("1.0");
        Map<String, Long> sources = new HashMap<>();
        sources.put("cosmic", formatter.parse("25/09/2025").getTime());
        dat.setSources(sources);

        String token = datManager.encode("ucam", dat);
        System.out.println(token);

        assertEquals(false, datManager.hasExpiredSource("cosmic", token));
    }

    @Test
    public void testExpired() throws ParseException {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        DataAccessTokenSources dat = new DataAccessTokenSources();
        dat.setVersion("1.0");
        Map<String, Long> sources = new HashMap<>();
        sources.put("cosmic", formatter.parse("25/09/2021").getTime());
        dat.setSources(sources);

        String token = datManager.encode("ucam", dat);
        System.out.println(token);

        assertEquals(true, datManager.hasExpiredSource("cosmic", token));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSource() throws ParseException {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        DataAccessTokenSources dat = new DataAccessTokenSources();
        dat.setVersion("1.0");
        Map<String, Long> sources = new HashMap<>();
        sources.put("cosmic", formatter.parse("25/09/2021").getTime());
        dat.setSources(sources);

        String token = datManager.encode("ucam", dat);

        datManager.hasExpiredSource("hgmd", token);
    }

    @Test
    public void testRecodeToken() throws ParseException {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        DataAccessTokenSources dat = new DataAccessTokenSources();
        dat.setVersion("1.0");
        Map<String, Long> sources = new HashMap<>();
        sources.put("cadd", formatter.parse("25/09/2030").getTime());
        sources.put("cosmic", formatter.parse("25/09/2021").getTime());
        dat.setSources(sources);

        String token = datManager.encode("ucam", dat);
        String newToken = datManager.recode(token);

        DataAccessTokenSources newDat = datManager.decode(newToken);
        assertEquals(1, newDat.getSources().size());
        assertTrue(newDat.getSources().containsKey("cadd"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidSources() throws ParseException {
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        DataAccessTokenSources dat = new DataAccessTokenSources();
        dat.setVersion("1.0");
        Map<String, Long> sources = new HashMap<>();
        sources.put("cosmic", formatter.parse("25/09/2024").getTime());
        sources.put("hgmd", formatter.parse("25/09/2020").getTime());
        sources.put("cadd", formatter.parse("25/09/2025").getTime());
        dat.setSources(sources);

        String token = datManager.encode("ucam", dat);

        datManager.getValidSources(token);
    }
}