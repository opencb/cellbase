package org.opencb.cellbase.lib.mongodb;

import com.mongodb.DB;
import org.opencb.cellbase.core.lib.api.GeneDBAdaptor;
import org.opencb.cellbase.core.lib.api.ProteinDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.List;

/**
 * Created by imedina on 06/03/14.
 */
public class ProteinMongoDBAdaptor extends MongoDBAdaptor implements ProteinDBAdaptor {

    public ProteinMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
    }

    @Override
    public QueryResult getAll(QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult next(String chromosome, int position, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult getAllById(String id, QueryOptions options) {
        return null;
    }

    @Override
    public List<QueryResult> getAllByIdList(List<String> idList, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult getAllByAccession(String id, QueryOptions options) {
        return null;
    }

    @Override
    public List<QueryResult> getAllByAccessionList(List<String> idList, QueryOptions options) {
        return null;
    }
}
