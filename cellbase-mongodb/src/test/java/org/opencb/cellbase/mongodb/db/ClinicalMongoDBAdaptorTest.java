package org.opencb.cellbase.mongodb.db;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.junit.Test;
import org.opencb.biodata.models.feature.Region;
import org.opencb.cellbase.core.common.core.CellbaseConfiguration;
import org.opencb.cellbase.core.lib.DBAdaptorFactory;
import org.opencb.cellbase.core.lib.api.variation.ClinicalDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.VariantAnnotationDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ClinicalMongoDBAdaptorTest {

    @Test
    public void testGetAllByRegionList() throws Exception {
        try {
            CellbaseConfiguration config = new CellbaseConfiguration();


            config.addSpeciesAlias("hsapiens", "hsapiens");

            DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(config);

            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor("hsapiens", "GRCh37");
            QueryOptions queryOptions = new QueryOptions("include", "clinvarList");
            List<QueryResult> clinicalQueryResultList = clinicalDBAdaptor.getAllByRegionList(Arrays.asList(new Region("3", 550000, 1166666)), queryOptions);
            List<QueryResult> queryResultList = new ArrayList<>();
            for(QueryResult clinvarQueryResult: clinicalQueryResultList) {
                QueryResult queryResult = new QueryResult();
                queryResult.setId(clinvarQueryResult.getId());
                queryResult.setDbTime(clinvarQueryResult.getDbTime());
                queryResult.setNumResults(clinvarQueryResult.getNumResults());
                BasicDBList basicDBList = new BasicDBList();

                for (BasicDBObject clinicalRecord : (List<BasicDBObject>) clinvarQueryResult.getResult()) {
                    if(clinicalRecord.containsKey("clinvarList")) {
                        for (BasicDBObject clinvarRecord : (List<BasicDBObject>) clinicalRecord.get("clinvarList")) {
                            basicDBList.add(clinvarRecord);
                        }
                    }
                }
                queryResult.setResult(basicDBList);
                queryResultList.add(queryResult);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}