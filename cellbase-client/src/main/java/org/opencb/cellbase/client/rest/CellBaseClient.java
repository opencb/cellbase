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

package org.opencb.cellbase.client.rest;

import org.opencb.cellbase.client.config.ClientConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by imedina on 12/05/16.
 */
public class CellBaseClient {

    private String species;
    private ClientConfiguration clientConfiguration;

    private Map<String, ParentRestClient> clients;


    public CellBaseClient(ClientConfiguration clientConfiguration) {
        this(clientConfiguration.getDefaultSpecies(), clientConfiguration);
    }

    public CellBaseClient(String species, ClientConfiguration clientConfiguration) {
        this.species = species;
        this.clientConfiguration = clientConfiguration;

        clients = new HashMap<>();
    }


    public GeneClient getGeneClient() {
        clients.putIfAbsent("GENE", new GeneClient(species, clientConfiguration));
        return (GeneClient) clients.get("GENE");
    }

    public TranscriptClient getTranscriptClient() {
        clients.putIfAbsent("TRANSCRIPT", new TranscriptClient(species, clientConfiguration));
        return (TranscriptClient) clients.get("TRANSCRIPT");
    }

    public VariationClient getVariationClient() {
        clients.putIfAbsent("VARIATION", new VariationClient(species, clientConfiguration));
        return (VariationClient) clients.get("VARIATION");
    }

    public ProteinClient getProteinClient() {
        clients.putIfAbsent("PROTEIN", new ProteinClient(species, clientConfiguration));
        return (ProteinClient) clients.get("PROTEIN");
    }

    public GenomicRegionClient getGenomicRegionClient() {
        clients.putIfAbsent("GENOME_REGION", new GenomicRegionClient(species, clientConfiguration));
        return (GenomicRegionClient) clients.get("GENOME_REGION");
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CellBaseClient{");
        sb.append("species='").append(species).append('\'');
        sb.append(", clientConfiguration=").append(clientConfiguration);
        sb.append(", clients=").append(clients);
        sb.append('}');
        return sb.toString();
    }


    public String getSpecies() {
        return species;
    }

    public CellBaseClient setSpecies(String species) {
        this.species = species;
        return this;
    }

    public ClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }

    public CellBaseClient setClientConfiguration(ClientConfiguration clientConfiguration) {
        this.clientConfiguration = clientConfiguration;
        return this;
    }
}
