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

package org.opencb.cellbase.mongodb.db.systems;

import com.mongodb.BasicDBObject;
import org.opencb.cellbase.core.db.api.systems.PathwayDBAdaptor;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptor;
import org.opencb.datastore.mongodb.MongoDataStore;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class PathwayMongoDBAdaptor extends MongoDBAdaptor implements PathwayDBAdaptor {


    private int genomeSequenceChunkSize = MongoDBCollectionConfiguration.GENOME_SEQUENCE_CHUNK_SIZE;


    public PathwayMongoDBAdaptor(String species, String assembly, MongoDataStore mongoDataStore) {
        super(species, assembly, mongoDataStore);
        mongoDBCollection = mongoDataStore.getCollection("pathway");

        logger.info("PathwayMongoDBAdaptor: in 'constructor'");
    }

    private int getChunk(int position) {
        return (position / genomeSequenceChunkSize);
    }

    private int getOffset(int position) {
        return ((position) % genomeSequenceChunkSize);
    }

    @Override
    public String getPathways() {
        BasicDBObject query = new BasicDBObject();

        BasicDBObject returnFields = new BasicDBObject();
        returnFields.put("_id", 0);
        returnFields.put("name", 1);
        returnFields.put("displayName", 1);
        returnFields.put("subPathways", 1);
        returnFields.put("parentPathway", 1);

        BasicDBObject orderBy = new BasicDBObject();
        orderBy.put("name", 1);

        return null;
    }

    @Override
    public String getTree() {
        BasicDBObject query = new BasicDBObject();
        query.put("parentPathway", "none");

        BasicDBObject returnFields = new BasicDBObject();
        returnFields.put("_id", 0);
        returnFields.put("name", 1);
        returnFields.put("displayName", 1);
        returnFields.put("subPathways", 1);

        BasicDBObject orderBy = new BasicDBObject();
        orderBy.put("displayName", 1);

        return null;
    }

    @Override
    public String getPathway(String pathwayId) {
        BasicDBObject query = new BasicDBObject();
        query.put("name", pathwayId);

        BasicDBObject returnFields = new BasicDBObject();
        returnFields.put("_id", 0);

        return null;
    }

    @Override
    public String search(String searchBy, String searchText, boolean returnOnlyIds) {
        Pattern regex = Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);

        BasicDBObject query = new BasicDBObject();
        if (searchBy.equalsIgnoreCase("pathway")) {
            query.put("displayName", regex);
        } else {
            BasicDBObject query1 = new BasicDBObject("physicalEntities.params.displayName", regex);
            BasicDBObject query2 = new BasicDBObject("interactions.params.displayName", regex);
            ArrayList<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
            queryList.add(query1);
            queryList.add(query2);
            query.put("$or", queryList);
        }

        System.out.println("Query: " + query);

        BasicDBObject returnFields = new BasicDBObject();
        returnFields.put("_id", 0);
        if (returnOnlyIds) {
            returnFields.put("name", 1);
        }

        return null;
    }

}
