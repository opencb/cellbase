package org.opencb.cellbase.lib.managers;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.opencb.cellbase.core.models.DataAccessTokenSources;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opencb.cellbase.lib.managers.DataAccessTokenManager.SECRET_KEY_MIN_LENGTH;

public class DataAccessTokenSourcesManagerTest {

    DataAccessTokenManager datManager;

    @Before
    public void before() {
        String randomStr = RandomStringUtils.randomAlphanumeric(SECRET_KEY_MIN_LENGTH);
        System.out.println("Secret key = " + randomStr);
        Key key = new SecretKeySpec(TextCodec.BASE64.decode(randomStr), SignatureAlgorithm.HS256.getJcaName());
        datManager = new DataAccessTokenManager(SignatureAlgorithm.HS256.getValue(), key);
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

        Set<String> validSources = datManager.getValidSources(token);
        assertEquals(2, validSources.size());
        assertThat(validSources, hasItem("cosmic"));
        assertThat(validSources, hasItem("cadd"));
    }
}