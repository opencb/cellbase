package org.opencb.cellbase.mongodb.db;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.opencb.biodata.models.core.Exon;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.feature.Region;
import org.opencb.cellbase.core.common.Position;
import org.opencb.cellbase.core.lib.api.ExonDBAdaptor;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.opencb.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExonMongoDBAdaptor extends MongoDBAdaptor implements ExonDBAdaptor {

	public ExonMongoDBAdaptor(DB db) {
		super(db);
	}

	public ExonMongoDBAdaptor(DB db, String species, String version) {
		super(db, species, version);
		mongoDBCollection = db.getCollection("gene");
	}

    public ExonMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection2 = mongoDataStore.getCollection("clinical");

        logger.info("ExonMongoDBAdaptor: in 'constructor'");
    }

    private List<Gene> executeQuery(DBObject query, List<String> excludeFields) {
        List<Gene> result = null;

        DBCursor cursor = null;
        if (excludeFields != null && excludeFields.size() > 0) {
            BasicDBObject returnFields = new BasicDBObject("_id", 0);
            for (String field : excludeFields) {
                returnFields.put(field, 0);
            }
            cursor = mongoDBCollection.find(query, returnFields);
        } else {
            cursor = mongoDBCollection.find(query);
        }

        try {
            if (cursor != null) {
                result = new ArrayList<Gene>(cursor.size());
//                Gson jsonObjectMapper = new Gson();
                Gene gene = null;
                while (cursor.hasNext()) {
//                    gene = (Gene) jsonObjectMapper.fromJson(cursor.next().toString(), Gene.class);
                    result.add(gene);
                }
            }
        } finally {
            cursor.close();
        }
        return result;
    }


    @Override
    public List<List<Exon>> getAllByName(String name, List<String> exclude) {
        BasicDBObject query = new BasicDBObject("transcripts.xrefs.id", name.toUpperCase());
        List<List<Exon>> result = new ArrayList<List<Exon>>();
        List<Gene> genes = executeQuery(query, exclude);
        for (Gene gene : genes) {
            List<Exon> exons = new ArrayList<Exon>();
            for(Transcript transcript : gene.getTranscripts()){
                exons.addAll(transcript.getExons());
            }
            result.add(exons);
        }
        return result;
    }

    @Override
    public List<List<List<Exon>>> getAllByNameList(List<String> nameList, List<String> exclude) {
        List<List<List<Exon>>> exons = new ArrayList<List<List<Exon>>>(nameList.size());
        for (String name : nameList) {
            exons.add(getAllByName(name, exclude));
        }
        return exons;
    }

    public QueryResult getAll() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public QueryResult getAll(QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public QueryResult next(String id, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public QueryResult next(String chromosome, int position, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<String> getAllIds() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, Object> getInfo(String id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Map<String, Object>> getInfoByIdList(List<String> idList) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, Object> getFullInfo(String id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Map<String, Object>> getFullInfoByIdList(List<String> idList) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Region getRegionById(String id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Region> getAllRegionsByIdList(List<String> idList) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getSequenceById(String id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<String> getAllSequencesByIdList(List<String> idList) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<String> getAllEnsemblIds() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Exon getByEnsemblId(String ensemblId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Exon> getAllByEnsemblIdList(List<String> ensemblIdList) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Exon> getByEnsemblTranscriptId(String transcriptId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<List<Exon>> getByEnsemblTranscriptIdList(List<String> transcriptIdList) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Exon> getByEnsemblGeneId(String geneId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<List<Exon>> getByEnsemblGeneIdList(List<String> geneIdList) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Exon> getAllByPosition(String chromosome, int position) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Exon> getAllByPosition(Position position) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<List<Exon>> getAllByPositionList(List<Position> positionList) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Exon> getAllByRegion(String chromosome) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Exon> getAllByRegion(String chromosome, int start) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Exon> getAllByRegion(String chromosome, int start, int end) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Exon> getAllByRegion(Region region) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<List<Exon>> getAllByRegionList(List<Region> regionList) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Exon> getAllByCytoband(String chromosome, String cytoband) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Exon> getAllBySnpId(String snpId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<List<Exon>> getAllBySnpIdList(List<String> snpIdList) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<String> getAllSequencesByIdList(List<String> ensemblIdList, int strand) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
