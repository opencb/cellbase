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

package org.opencb.cellbase.lib.variant;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.TestInstance;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.ClinicalVariantQuery;
import org.opencb.cellbase.core.api.key.ApiKeyManager;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.cellbase.lib.managers.ClinicalManager;

import java.util.ArrayList;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClinicalManagerTest extends GenericMongoDBAdaptorTest {
    private ObjectMapper jsonObjectMapper;
    private ClinicalManager clinicalManager;

    public ClinicalManagerTest() throws CellBaseException {
        super();

        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        clinicalManager = cellBaseManagerFactory.getClinicalManager(SPECIES, ASSEMBLY);
    }

    //-------------------------------------------------------------------------
    // S E A R C H
    //-------------------------------------------------------------------------

    @Test
    public void testLicensedHGMD() throws CellBaseException, QueryException, IllegalAccessException {
        // API key with licensed HGMD, so only CLINVAR and HGMD are allowed
        ClinicalVariantQuery query = new ClinicalVariantQuery();

        List<Region> regions = new ArrayList<>();
        regions.add(Region.parseRegion("10:113588287-113588287"));
        query.setRegions(regions);
        query.setDataRelease(dataRelease);
        query.setApiKey(HGMD_ACCESS_API_KEY);

        CellBaseDataResult<Variant> results = clinicalManager.search(query);
        Assert.assertEquals(1, results.getResults().size());
        Assert.assertEquals(2, results.getResults().get(0).getAnnotation().getTraitAssociation().size());
    }

    @Test
    public void testNotLicensed() throws CellBaseException, QueryException, IllegalAccessException {
        // Without API key, so only CLINVAR is allowed
        ClinicalVariantQuery query = new ClinicalVariantQuery();

        List<Region> regions = new ArrayList<>();
        regions.add(Region.parseRegion("10:113588287-113588287"));
        query.setRegions(regions);
        query.setDataRelease(dataRelease);

        CellBaseDataResult<Variant> results = clinicalManager.search(query);
        Assert.assertEquals(1, results.getResults().size());
        Assert.assertEquals(1, results.getResults().get(0).getAnnotation().getTraitAssociation().size());
        Assert.assertEquals("clinvar", results.getResults().get(0).getAnnotation().getTraitAssociation().get(0).getSource().getName());
    }

    //-------------------------------------------------------------------------
    // I T E R A T O R
    //-------------------------------------------------------------------------

    @Test
    public void testIteratorOnlyClinvar() throws CellBaseException, QueryException, IllegalAccessException {
        // API key with licensed HGMD, so only CLINVAR and HGMD are allowed
        ClinicalVariantQuery query = new ClinicalVariantQuery();

        List<Region> regions = new ArrayList<>();
        regions.add(Region.parseRegion("10:113588287-113588287"));
        query.setRegions(regions);
        query.setDataRelease(dataRelease);

        CellBaseIterator<Variant> iterator = clinicalManager.iterator(query);
        int count = 0;
        while (iterator.hasNext()) {
            Variant variant = iterator.next();
            Assert.assertEquals(1, variant.getAnnotation().getTraitAssociation().size());
            count++;
        }
        Assert.assertEquals(1, count);
    }

    @Test
    public void testIteratorLicensedHGMD() throws CellBaseException {
        // API key with licensed HGMD, so only CLINVAR and HGMD are allowed
        ClinicalVariantQuery query = new ClinicalVariantQuery();

        List<Region> regions = new ArrayList<>();
        regions.add(Region.parseRegion("10:113588287-113588287"));
        query.setRegions(regions);
        query.setDataRelease(dataRelease);
        query.setApiKey(HGMD_ACCESS_API_KEY);

        int count = 0;
        CellBaseIterator<Variant> iterator = clinicalManager.iterator(query);
        while (iterator.hasNext()) {
            Variant variant = iterator.next();
            Assert.assertEquals(2, variant.getAnnotation().getTraitAssociation().size());
            count++;
        }
        Assert.assertEquals(1, count);
    }
}
