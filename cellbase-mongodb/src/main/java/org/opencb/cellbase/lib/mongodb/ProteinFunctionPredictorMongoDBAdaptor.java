package org.opencb.cellbase.lib.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import org.opencb.cellbase.core.lib.api.ProteinFunctionPredictorDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by imedina on 10/12/13.
 */
public class ProteinFunctionPredictorMongoDBAdaptor  extends MongoDBAdaptor implements ProteinFunctionPredictorDBAdaptor {

    public ProteinFunctionPredictorMongoDBAdaptor(DB db) {
        super(db);
    }

    public ProteinFunctionPredictorMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
        mongoDBCollection = db.getCollection("protein_functional_prediction");

        logger.info("ProteinFunctionPredictorMongoDBAdaptor: in 'constructor'");
    }
    @Override
    public QueryResult getAllByEnsemblTranscriptId(String transcriptId, QueryOptions options) {
        return getAllByEnsemblTranscriptIdList(Arrays.asList(transcriptId), options).get(0);
    }

    @Override
    public List<QueryResult> getAllByEnsemblTranscriptIdList(List<String> transcriptIdList, QueryOptions options) {
        List<DBObject> queries = new ArrayList<>(transcriptIdList.size());

        if(options.containsKey("aaPosition")) {
            if(options.containsKey("aaChange")) {
                addIncludeReturnFields("aaPositions."+options.getString("aaPosition")+"."+options.getString("aaChange"), options);
            }else {
                addIncludeReturnFields("aaPositions."+options.getString("aaPosition"), options);
            }
        }


        for (String id : transcriptIdList) {
            QueryBuilder builder = QueryBuilder.start("transcriptId").is(id);
            queries.add(builder.get());
        }

//        options = addExcludeReturnFields("transcripts", options);
        return executeQueryList(transcriptIdList, queries, options);
    }
}
