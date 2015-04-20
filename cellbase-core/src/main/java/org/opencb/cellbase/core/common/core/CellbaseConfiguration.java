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

package org.opencb.cellbase.core.common.core;

import org.opencb.cellbase.core.common.Species;
import org.opencb.cellbase.core.common.variation.StructuralVariation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by javi on 16/09/14.
 */
@Deprecated
public class CellbaseConfiguration {


    private String version;
    private int coreChunkSize = 5000;
    private int variationChunkSize = 1000;
    private int genomeSequenceChunkSize = 2000;

    private int conservedRegionChunkSize = 2000;
    private Map<String, String> speciesAlias = new HashMap<>();
    private Map<String, Map<String,Species>> availableSpeciesInfo = new HashMap<>();
    private Map<String, Map<String,ConnectionParameters>> availableSpeciesConnection = new HashMap<>();

    class ConnectionParameters {
        private String host;
        private String database;
        private int port;
        private String driverClass;
        private String username;
        private String pass;
        private int maxPoolSize;
        private int timeout;

        public ConnectionParameters(String host, String database, int port, String driverClass, String username, String pass,
                                    int maxPoolSize, int timeout) {
            this.host = host;
            this.database = database;
            this.port = port;
            this.driverClass = driverClass;
            this.username = username;
            this.pass = pass;
            this.maxPoolSize = maxPoolSize;
            this.timeout = timeout;
        }

        public String getHost() { return this.host; }

        public String getDatabase() { return this.database; }

        public int getPort() { return this.port; }

        public String getDriverClass() { return this.driverClass; }

        public String getUsername() { return this.username; }

        public String getPass() { return this.pass; }

        public int getMaxPoolSize() { return this.maxPoolSize; }

        public int getTimeout() { return this.timeout; }

    }

    public CellbaseConfiguration() { super(); }

    public void setVersion(String version) { this.version = version; }

    public void setCoreChunkSize(int coreChunkSize){ this.coreChunkSize = coreChunkSize; }

    public void setVariationChunkSize(int variationChunkSize){ this.variationChunkSize = variationChunkSize; }

    public void setGenomeSequenceChunkSize(int genomeSequenceChunkSize){ this.genomeSequenceChunkSize = genomeSequenceChunkSize; }

    public void setConservedRegionChunkSize(int conservedRegionChunkSize) { this.conservedRegionChunkSize = conservedRegionChunkSize; }

    public void addSpeciesInfo(String speciesId, String taxonomy) {
        addSpeciesInfo(speciesId, "default", taxonomy);
    }

    public void addSpeciesInfo(String speciesId, String assembly, String taxonomy) {
        if(!availableSpeciesInfo.containsKey(speciesId)){
            availableSpeciesInfo.put(speciesId, new HashMap<String, Species>());
        }
        availableSpeciesInfo.get(speciesId).put(assembly, new Species(speciesId, assembly, taxonomy));
    }

    public void addSpeciesConnection(String speciesId, String host, String database, int port, String driverClass, String username, String pass,
                                     int maxPoolSize, int timeout) {
        addSpeciesConnection(speciesId, "default", host, database, port, driverClass, username, pass, maxPoolSize, timeout);
    }

    public void addSpeciesConnection(String speciesId, String assembly, String host, String database, int port, String driverClass, String username,
                                     String pass,int maxPoolSize, int timeout) {
        if(!availableSpeciesConnection.containsKey(speciesId)) {
            availableSpeciesConnection.put(speciesId, new HashMap<String, ConnectionParameters>());
        }
        availableSpeciesConnection.get(speciesId).put(assembly, new ConnectionParameters(host, database, port, driverClass, username, pass,
                maxPoolSize, timeout));
    }

    public void addSpeciesAlias(String al, String species){
        speciesAlias.put(al, species);
    }

    public String getVersion() { return version; }

    public int getCoreChunkSize() { return coreChunkSize; }

    public int getVariationChunkSize() { return variationChunkSize; }

    public int getGenomeSequenceChunkSize() { return genomeSequenceChunkSize; }

    public int getConservedRegionChunkSize() { return conservedRegionChunkSize; }

    public String getAlias(String species) { return this.speciesAlias.get(species); }

    public String getHost(String species, String assembly) {
        if(assembly==null || assembly.trim().equals("")){
            return this.availableSpeciesConnection.get(species).values().iterator().next().getHost();
        }
        return this.availableSpeciesConnection.get(species).get(assembly).getHost();
    }

    public String getDatabase(String species, String assembly) {

        Logger logger = LoggerFactory.getLogger(this.getClass());


        if(assembly==null || assembly.trim().equals("")){

            if(this.availableSpeciesConnection.get(species).values().iterator().next()==null){
                logger.info("Esto es null, vaya polla");
            } else {
                logger.info(this.availableSpeciesConnection.get(species).values().iterator().next().getDatabase());
            }
            return this.availableSpeciesConnection.get(species).values().iterator().next().getDatabase();
        }
        return this.availableSpeciesConnection.get(species).get(assembly).getDatabase();
    }

    public int getPort(String species, String assembly) {
        if(assembly==null || assembly.trim().equals("")){
            return this.availableSpeciesConnection.get(species).values().iterator().next().getPort();
        }
        return this.availableSpeciesConnection.get(species).get(assembly).getPort();
    }

    public String getDriverClass(String species, String assembly) {
        if(assembly==null || assembly.trim().equals("")){
            return this.availableSpeciesConnection.get(species).values().iterator().next().getDriverClass();
        }
        return this.availableSpeciesConnection.get(species).get(assembly).getDriverClass();
    }

    public String getUsername(String species, String assembly) {
        if(assembly==null || assembly.trim().equals("")){
            return this.availableSpeciesConnection.get(species).values().iterator().next().getUsername();
        }
        return this.availableSpeciesConnection.get(species).get(assembly).getUsername();
    }

    public String getPass(String species, String assembly) {
        if(assembly==null || assembly.trim().equals("")){
            return this.availableSpeciesConnection.get(species).values().iterator().next().getPass();
        }
        return this.availableSpeciesConnection.get(species).get(assembly).getPass();
    }

    public int getMaxPoolSize(String species, String assembly) {
        if(assembly==null || assembly.trim().equals("")){
            return this.availableSpeciesConnection.get(species).values().iterator().next().getMaxPoolSize();
        }
        return this.availableSpeciesConnection.get(species).get(assembly).getMaxPoolSize();
    }

    public int getTimeout(String species, String assembly) {
        if(assembly==null || assembly.trim().equals("")){
            return this.availableSpeciesConnection.get(species).values().iterator().next().getTimeout();
        }
        return this.availableSpeciesConnection.get(species).get(assembly).getTimeout();
    }


}
