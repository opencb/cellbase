/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.lib.builders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.biodata.models.core.*;
import org.opencb.biodata.models.variant.avro.Expression;
import org.opencb.biodata.models.variant.avro.GeneDrugInteraction;
import org.opencb.biodata.models.variant.avro.GeneTraitAssociation;
import org.opencb.biodata.models.core.MiRnaTarget;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class RocksDbManager {

    private static ObjectMapper mapper;
    private static ObjectWriter jsonObjectWriter;

    public RocksDbManager() {
        init();
    }

    private void init() {
        mapper = new ObjectMapper();
        mapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectWriter = mapper.writer();
    }

    public RocksDB getDBConnection(String dbLocation) {
        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();
        // the Options class contains a set of configurable DB options
        // that determines the behavior of a database.
        Options options = new Options().setCreateIfMissing(true);
        RocksDB db = null;
        try {
            return RocksDB.open(options, dbLocation);
        } catch (RocksDBException e) {
            // do some error handling
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }

    public List<Xref> getXrefs(RocksDB rdb, String key) throws RocksDBException, IOException {
        byte[] dbContent = rdb.get(key.getBytes());
        if (dbContent == null) {
            return null;
        }
        Xref[] xrefs =  mapper.readValue(dbContent, Xref[].class);
        return Arrays.asList(xrefs);
    }

    public List<Expression> getExpression(RocksDB rdb, String key) throws RocksDBException, IOException {
        byte[] dbContent = rdb.get(key.getBytes());
        if (dbContent == null) {
            return null;
        }
        return Arrays.asList(mapper.readValue(dbContent, Expression[].class));
    }

    public List<GeneDrugInteraction> getDrugs(RocksDB rdb, String key) throws RocksDBException, IOException {
        byte[] dbContent = rdb.get(key.getBytes());
        if (dbContent == null) {
            return null;
        }
        return Arrays.asList(mapper.readValue(dbContent, GeneDrugInteraction[].class));
    }

    public List<GeneTraitAssociation> getDiseases(RocksDB rdb, String key) throws RocksDBException, IOException {
        byte[] dbContent = rdb.get(key.getBytes());
        if (dbContent == null) {
            return null;
        }
        return Arrays.asList(mapper.readValue(dbContent, GeneTraitAssociation[].class));
    }

    public List<Constraint> getConstraints(RocksDB rdb, String key) throws RocksDBException, IOException {
        byte[] dbContent = rdb.get(key.getBytes());
        if (dbContent == null) {
            return null;
        }
        return Arrays.asList(mapper.readValue(dbContent, Constraint[].class));
    }

    public List<FeatureOntologyTermAnnotation> getOntologyAnnotations(RocksDB rdb, String key) throws RocksDBException, IOException {
        byte[] dbContent = rdb.get(key.getBytes());
        if (dbContent == null) {
            return null;
        }
        return Arrays.asList(mapper.readValue(dbContent, FeatureOntologyTermAnnotation[].class));
    }

    public List<MiRnaTarget> getMirnaTargets(RocksDB rdb, String key) throws RocksDBException, IOException {
        byte[] dbContent = rdb.get(key.getBytes());
        if (dbContent == null) {
            return null;
        }
        return Arrays.asList(mapper.readValue(dbContent, MiRnaTarget[].class));
    }

    public MiRNAGene getMirnaGene(RocksDB rdb, String key) throws RocksDBException, IOException {
        byte[] dbContent = rdb.get(key.getBytes());
        if (dbContent == null) {
            return null;
        }
        return mapper.readValue(dbContent, MiRNAGene.class);
    }


    /**
     * Add an entry to specified rocksdb. Overwrites any existing entry.
     *
     * @param rdb rockdb to update
     * @param key key to insert
     * @param value valud to insert
     * @throws JsonProcessingException JSON is bad
     * @throws RocksDBException something went wrong with rocksdb
     */
    public void update(RocksDB rdb, String key, Object value) throws JsonProcessingException, RocksDBException {
        rdb.put(key.getBytes(), jsonObjectWriter.writeValueAsBytes(value));
    }

    /**
     * Add an entry to specified rocksdb. Overwrites any existing entry.
     *
     * @param rdb rockdb to update
     * @param key key to insert
     * @param value valud to insert
     * @throws RocksDBException something went wrong with rocksdb
     */
    public void update(RocksDB rdb, String key, String value) throws RocksDBException {
        rdb.put(key.getBytes(), value.getBytes());
    }

    public void closeIndex(RocksDB rdb, Options dbOption, String dbLocation) throws IOException {
        if (rdb != null) {
            rdb.close();
        }
        if (dbOption != null) {
            dbOption.dispose();
        }
        if (dbLocation != null && Files.exists(Paths.get(dbLocation))) {
            org.apache.commons.io.FileUtils.deleteDirectory(new File(dbLocation));
        }
    }
}
