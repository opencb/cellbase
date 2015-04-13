package org.opencb.cellbase.mongodb.db;

import com.google.common.base.Splitter;
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
            List<QueryResult> clinicalQueryResultList = clinicalDBAdaptor.getAllClinvarByRegionList(Arrays.asList(new Region("3", 550000, 1166666)), queryOptions);
            List<QueryResult> queryResultList = new ArrayList<>();
            for (QueryResult clinvarQueryResult : clinicalQueryResultList) {
                QueryResult queryResult = new QueryResult();
                queryResult.setId(clinvarQueryResult.getId());
                queryResult.setDbTime(clinvarQueryResult.getDbTime());
                queryResult.setNumResults(clinvarQueryResult.getNumResults());
                BasicDBList basicDBList = new BasicDBList();

                for (BasicDBObject clinicalRecord : (List<BasicDBObject>) clinvarQueryResult.getResult()) {
                    if (clinicalRecord.containsKey("clinvarList")) {
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

    @Test
    public void testGetClinvarById() throws Exception {

        CellbaseConfiguration config = new CellbaseConfiguration();

        config.addSpeciesAlias("hsapiens", "hsapiens");

        DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(config);

        ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor("hsapiens", "GRCh37");

        clinicalDBAdaptor.getAllClinvarByIdList(Splitter.on(",").splitToList("RCV000091359"), new QueryOptions());
    }

    @Test
    public void testGetAll() throws  Exception {

        CellbaseConfiguration config = new CellbaseConfiguration();

        config.addSpeciesConnection("hsapiens", "GRCh37", "mongodb-hxvm-var-001", "cellbase_hsapiens_grch37_v3", 27017, "mongo", "biouser",
                "B10p@ss", 10, 10);

        config.addSpeciesAlias("hsapiens", "hsapiens");

        DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(config);

        ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor("hsapiens", "GRCh37");
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.addToListOption("include", "clinvar");
//        queryOptions.add("phenotype", "ALZHEIMER DISEASE 2, DUE TO APOE4 ISOFORM");
//        queryOptions.addToListOption("phenotype", "ALZHEIMER");
//        queryOptions.addToListOption("phenotype", "alzheimer");
//        queryOptions.addToListOption("phenotype", "diabetes");
        queryOptions.addToListOption("region", new Region("3", 550000, 1166666));
        queryOptions.addToListOption("region", new Region("13", 550000, 1166666));
//        queryOptions.addToListOption("gene", "APOE");
//        queryOptions.addToListOption("rs", "rs429358");
//        queryOptions.addToListOption("rcv", "RCV000019455");

//        ((List<String>) queryOptions.get("include")).remove(0);

        clinicalDBAdaptor.getAll(queryOptions);

    }
}