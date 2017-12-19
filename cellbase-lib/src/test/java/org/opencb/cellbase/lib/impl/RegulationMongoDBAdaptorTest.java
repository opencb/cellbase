package org.opencb.cellbase.lib.impl;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.core.api.RegulationDBAdaptor;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import static org.junit.Assert.*;

/**
 * Created by fjlopez on 12/12/17.
 */
public class RegulationMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {

    public RegulationMongoDBAdaptorTest() throws IOException { super(); }

    @Before
    public void setUp() throws Exception {
        clearDB(GRCH37_DBNAME);
        Path path = Paths.get(getClass()
                .getResource("/regulatory-region/regulatory_region.test.json.gz").toURI());
        loadRunner.load(path, "regulatory_region");
    }

    @Test
    public void getByRegionTest() throws Exception {
        RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor("hsapiens", "GRCh37");
        List<Region> regionList = Arrays.asList(new Region("22:16049980-16050240"),
                new Region("22:16050740-16054000"), new Region("1:100-100000"));

        // Just return required fields
        // MERGE = true essential so that just one query will be raised with all regions
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.add(QueryOptions.INCLUDE, "chromosome,start,end,featureType");

        List<QueryResult> queryResultListNoMerge = regulationDBAdaptor.getByRegion(regionList, queryOptions);
        int numTotalResultsNoMerge = 0;
        for (QueryResult queryResult : queryResultListNoMerge) {
            numTotalResultsNoMerge += queryResult.getNumTotalResults();
        }

        queryOptions.put("merge", true);
        QueryResult queryResultMerge = (QueryResult) regulationDBAdaptor.getByRegion(regionList, queryOptions).get(0);

        assertEquals(numTotalResultsNoMerge, queryResultMerge.getNumTotalResults());

        // First region (22:16049980-16050240) overlaps two regions. Second region (22:16050740-16054000) overlaps one
        // regulatory feature. Last region (1:100-100000) does not overlap any regulatory feature.
        for (QueryResult queryResultNoMerge : queryResultListNoMerge) {
            for (RegulatoryFeature regulatoryFeature : (List<RegulatoryFeature>) queryResultNoMerge.getResult()) {
                assertThatContainsRegulatoryFeature((List<RegulatoryFeature>) queryResultMerge.getResult(),
                        regulatoryFeature);
            }
        }

    }

    private void assertThatContainsRegulatoryFeature(List<RegulatoryFeature> regulatoryFeatureList,
                                                     RegulatoryFeature regulatoryFeature) {
        boolean found = false;
        for (RegulatoryFeature regulatoryFeature1 : regulatoryFeatureList) {
            if (regulatoryFeature1.getChromosome() != null
                    && regulatoryFeature1.getChromosome().equals(regulatoryFeature.getChromosome())
                && regulatoryFeature1.getStart() == regulatoryFeature.getStart()
                && regulatoryFeature1.getEnd() == regulatoryFeature.getEnd()
                && regulatoryFeature1.getFeatureType() != null
                    && regulatoryFeature1.getFeatureType().equals(regulatoryFeature.getFeatureType())) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }
}