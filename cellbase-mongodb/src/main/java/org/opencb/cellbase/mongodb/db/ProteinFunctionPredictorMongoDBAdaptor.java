package org.opencb.cellbase.mongodb.db;

import com.mongodb.*;
import org.opencb.cellbase.core.lib.api.core.ProteinFunctionPredictorDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.datastore.mongodb.MongoDataStore;

import java.util.*;

/**
 * Created by imedina on 10/12/13.
 */
public class ProteinFunctionPredictorMongoDBAdaptor  extends MongoDBAdaptor implements ProteinFunctionPredictorDBAdaptor {

    private static Map<String,String> aaShortName = new HashMap();

    static {
        aaShortName.put("ALA","A");
        aaShortName.put("ARG","R");
        aaShortName.put("ASN","N");
        aaShortName.put("ASP","D");
        aaShortName.put("CYS","C");
        aaShortName.put("GLN","Q");
        aaShortName.put("GLU","E");
        aaShortName.put("GLY","G");
        aaShortName.put("HIS","H");
        aaShortName.put("ILE","I");
        aaShortName.put("LEU","L");
        aaShortName.put("LYS","K");
        aaShortName.put("MET","M");
        aaShortName.put("PHE","F");
        aaShortName.put("PRO","P");
        aaShortName.put("SER","S");
        aaShortName.put("THR","T");
        aaShortName.put("TRP","W");
        aaShortName.put("TYR","Y");
        aaShortName.put("VAL","V");
    }

    public ProteinFunctionPredictorMongoDBAdaptor(DB db) {
        super(db);
    }

    public ProteinFunctionPredictorMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
        mongoDBCollection = db.getCollection("protein_functional_prediction");

        logger.info("ProteinFunctionPredictorMongoDBAdaptor: in 'constructor'");
    }

    public ProteinFunctionPredictorMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection2 = mongoDataStore.getCollection("protein_functional_prediction");

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

    public QueryResult getByAaChange(String transcriptId, Integer aaPosition, String newAa, QueryOptions queryOptions) {

        QueryBuilder builder = QueryBuilder.start("transcriptId").is(transcriptId);
        QueryResult allChangesQueryResult = executeQuery(transcriptId, builder.get(), queryOptions);

        QueryResult proteinSubstitionScoresQueryResult = new QueryResult();
        proteinSubstitionScoresQueryResult.setDbTime(allChangesQueryResult.getDbTime());
        proteinSubstitionScoresQueryResult.setId(transcriptId+"-"+aaPosition+"-"+newAa);

//        String currentAaShortName;
//        Map aaPositions;
//        if(allChangesQueryResult.getNumResults()>0 && (currentAaShortName = aaShortName.get(newAa))!=null &&
//                (aaPositions = ((HashMap) ((BasicDBObject) ((BasicDBList) allChangesQueryResult.getResult()).get(0)).get("aaPositions")))!=null) {
//            proteinSubstitionScoresQueryResult.setNumResults(1);
//            proteinSubstitionScoresQueryResult.setResult(((HashMap) aaPositions.get(Integer.toString(aaPosition))).get(currentAaShortName));
//        } else {
//            proteinSubstitionScoresQueryResult.setNumResults(0);
//        }

        return proteinSubstitionScoresQueryResult;
    }

}
