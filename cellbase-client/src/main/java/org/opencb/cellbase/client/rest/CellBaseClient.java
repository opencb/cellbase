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
import java.util.function.Supplier;

/**
 * Created by imedina on 12/05/16.
 */
public class CellBaseClient {

    private String species;
    private ClientConfiguration clientConfiguration;

    private final Map<String, ParentRestClient> clients;


    public CellBaseClient(ClientConfiguration clientConfiguration) {
        this(clientConfiguration.getDefaultSpecies(), clientConfiguration);
    }

    public CellBaseClient(String species, ClientConfiguration clientConfiguration) {
        this.species = species;
        this.clientConfiguration = clientConfiguration;

        clients = new HashMap<>();
    }


    public GeneClient getGeneClient() {
        return getClient("GENE", () -> new GeneClient(species, clientConfiguration));
    }

    public TranscriptClient getTranscriptClient() {
        return getClient("TRANSCRIPT", () -> new TranscriptClient(species, clientConfiguration));
    }

    public VariationClient getVariationClient() {
        return getClient("VARIATION", () -> new VariationClient(species, clientConfiguration));
    }

    public VariantClient getVariantClient() {
        return getClient("VARIANT", () -> new VariantClient(species, clientConfiguration));
    }

    public ProteinClient getProteinClient() {
        return getClient("PROTEIN", () -> new ProteinClient(species, clientConfiguration));
    }

    public GenomicRegionClient getGenomicRegionClient() {
        return getClient("GENOME_REGION", () -> new GenomicRegionClient(species, clientConfiguration));
    }

    @SuppressWarnings("unchecked")
    private <T extends ParentRestClient> T getClient(String key, Supplier<T> constructorIfAbsent) {
        // Avoid concurrent modifications
        if (!clients.containsKey(key)) {
            synchronized (clients) {
                if (!clients.containsKey(key)) {
                    clients.put(key, constructorIfAbsent.get());
                }
            }
        }
        return (T) clients.get(key);
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
