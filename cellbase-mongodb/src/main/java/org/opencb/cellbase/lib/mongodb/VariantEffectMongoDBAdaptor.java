package org.opencb.cellbase.lib.mongodb;

import com.mongodb.DB;
import org.opencb.cellbase.core.common.GenomicVariant;
import org.opencb.cellbase.core.lib.api.variation.VariantEffectDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 8/28/13
 * Time: 4:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariantEffectMongoDBAdaptor extends MongoDBAdaptor implements VariantEffectDBAdaptor {


    public VariantEffectMongoDBAdaptor(DB db) {
        super(db);
    }

    public VariantEffectMongoDBAdaptor(DB db, String species, String version) {
        super(db, species, version);
        mongoDBCollection = db.getCollection("core");
    }


    @Override
    public QueryResult getAllConsequenceTypesByVariant(GenomicVariant variant, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<QueryResult> getAllConsequenceTypesByVariantList(List<GenomicVariant> variants, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public QueryResult getAllEffectsByVariant(GenomicVariant variant, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<QueryResult> getAllEffectsByVariantList(List<GenomicVariant> variants, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
