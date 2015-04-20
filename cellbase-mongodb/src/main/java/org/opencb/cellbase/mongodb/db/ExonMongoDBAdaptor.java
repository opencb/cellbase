/*
 * Copyright 2015 OpenCB
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
import org.opencb.cellbase.core.lib.api.core.ExonDBAdaptor;
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
		mongoDBCollection = db.getCollection("gene");
        mongoDBCollection2 = mongoDataStore.getCollection("gene");

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
    public QueryResult getAllByXref(String name, QueryOptions queryOptions) {
        BasicDBObject query = new BasicDBObject("transcripts.xrefs.id", name.toUpperCase());
        QueryResult result = new QueryResult();
        QueryResult genes = executeQuery(name, query, queryOptions);
        for (Object gene : genes.getResult()) {
            List<Exon> exons = new ArrayList();
//            for(Transcript transcript : gene.getTranscripts()){
//                exons.addAll(transcript.getExons());
//            }
//            result.getResult().add(exons);
        }
        return result;
    }

    @Override
    public List<QueryResult> getAllByXrefList(List<String> nameList, QueryOptions exclude) {
        List<QueryResult> queryResultList = new ArrayList<>();
        List<List<List<Exon>>> exons = new ArrayList<>(nameList.size());
        for (String name : nameList) {
//            exons.add(getAllByName(name, exclude));
        }
        return queryResultList;
    }

    public QueryResult getAll() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public QueryResult getAll(QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public QueryResult next(String chromosome, int position, QueryOptions options) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public QueryResult getAllByPosition(String chromosome, int position, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult getAllByPosition(Position position, QueryOptions options) {
        return null;
    }

    @Override
    public List<QueryResult> getAllByPositionList(List<Position> positionList, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult getAllByRegion(String chromosome, int start, int end, QueryOptions options) {
        return null;
    }

    @Override
    public QueryResult getAllByRegion(Region region, QueryOptions options) {
        return null;
    }

    @Override
    public List<QueryResult> getAllByRegionList(List<Region> regions, QueryOptions options) {
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
