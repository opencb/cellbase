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

import org.bson.Document;
import org.opencb.cellbase.core.db.api.systems.PathwayDBAdaptor;
import org.opencb.cellbase.mongodb.MongoDBCollectionConfiguration;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptor;
import org.opencb.commons.datastore.mongodb.MongoDataStore;

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
        Document query = new Document();

        Document returnFields = new Document();
        returnFields.put("_id", 0);
        returnFields.put("name", 1);
        returnFields.put("displayName", 1);
        returnFields.put("subPathways", 1);
        returnFields.put("parentPathway", 1);

        Document orderBy = new Document();
        orderBy.put("name", 1);

        return null;
    }

    @Override
    public String getTree() {
        Document query = new Document();
        query.put("parentPathway", "none");

        Document returnFields = new Document();
        returnFields.put("_id", 0);
        returnFields.put("name", 1);
        returnFields.put("displayName", 1);
        returnFields.put("subPathways", 1);

        Document orderBy = new Document();
        orderBy.put("displayName", 1);

        return null;
    }

    @Override
    public String getPathway(String pathwayId) {
        Document query = new Document();
        query.put("name", pathwayId);

        Document returnFields = new Document();
        returnFields.put("_id", 0);

        return null;
    }

    @Override
    public String search(String searchBy, String searchText, boolean returnOnlyIds) {
        Pattern regex = Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);

        Document query = new Document();
        if (searchBy.equalsIgnoreCase("pathway")) {
            query.put("displayName", regex);
        } else {
            Document query1 = new Document("physicalEntities.params.displayName", regex);
            Document query2 = new Document("interactions.params.displayName", regex);
            ArrayList<Document> queryList = new ArrayList<Document>();
            queryList.add(query1);
            queryList.add(query2);
            query.put("$or", queryList);
        }

        System.out.println("Query: " + query);

        Document returnFields = new Document();
        returnFields.put("_id", 0);
        if (returnOnlyIds) {
            returnFields.put("name", 1);
        }

        return null;
    }

}
