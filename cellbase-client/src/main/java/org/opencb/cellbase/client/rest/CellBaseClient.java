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

import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.client.config.ClientConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by imedina on 12/05/16.
 */
public class CellBaseClient {

    private String species;
    private String assembly;
    private ClientConfiguration clientConfiguration;

    private final Map<String, ParentRestClient> clients;


    public CellBaseClient(ClientConfiguration clientConfiguration) {
        this(clientConfiguration.getDefaultSpecies(), clientConfiguration);
    }

    public CellBaseClient(String species, ClientConfiguration clientConfiguration) {
        this(species, null, clientConfiguration);
    }

    public CellBaseClient(String species, String assembly, ClientConfiguration clientConfiguration) {

        if (StringUtils.isBlank(species)) {
            throw new IllegalArgumentException("Species parameter cannot be empty when building a CellBaseClient");
        }
        this.species = species;
        this.assembly = StringUtils.isEmpty(assembly) ? null : assembly;
        if (clientConfiguration != null && clientConfiguration.getRest() != null
                && clientConfiguration.getRest().getHosts() != null
                && !clientConfiguration.getRest().getHosts().isEmpty()
                && StringUtils.isNotBlank(clientConfiguration.getVersion())) {
            this.clientConfiguration = clientConfiguration;
        } else {
            throw new IllegalArgumentException("version and host must be provided in a ClienConfiguration object"
                    + " when building a CellBase client and cannot be empty");
        }
        clients = new HashMap<>();

    }


    public GeneClient getGeneClient() {
        return getClient("GENE", () -> new GeneClient(species, assembly, clientConfiguration));
    }

    public TranscriptClient getTranscriptClient() {
        return getClient("TRANSCRIPT", () -> new TranscriptClient(species, assembly, clientConfiguration));
    }

    public VariationClient getVariationClient() {
        return getClient("VARIATION", () -> new VariationClient(species, assembly, clientConfiguration));
    }

    public VariantClient getVariantClient() {
        return getClient("VARIANT", () -> new VariantClient(species, assembly, clientConfiguration));
    }

    public ProteinClient getProteinClient() {
        return getClient("PROTEIN", () -> new ProteinClient(species, assembly, clientConfiguration));
    }

    public GenomicRegionClient getGenomicRegionClient() {
        return getClient("GENOME_REGION", () -> new GenomicRegionClient(species, assembly, clientConfiguration));
    }

    public MetaClient getMetaClient() {
        return getClient("GENOME_REGION", () -> new MetaClient(species, assembly, clientConfiguration));
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
