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

package org.opencb.cellbase.client.rest;

import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.client.config.ClientConfiguration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Created by imedina on 12/05/16.
 */
public class CellBaseClient {

    private final String species;
    private final String assembly;
    private final String dataRelease;
    private final String token;
    private ClientConfiguration clientConfiguration;

    private final Map<String, ParentRestClient> clients;


    public CellBaseClient(ClientConfiguration clientConfiguration) {
        this(clientConfiguration.getDefaultSpecies(), clientConfiguration);
    }

    public CellBaseClient(String species, ClientConfiguration clientConfiguration) {
        this(species, null, clientConfiguration);
    }

    public CellBaseClient(String species, String assembly, ClientConfiguration clientConfiguration) {
        this(species, assembly, null, null, clientConfiguration);
    }

    public CellBaseClient(String species, String assembly, String dataRelease, String token, ClientConfiguration clientConfiguration) {
        if (StringUtils.isBlank(species)) {
            throw new IllegalArgumentException("Species parameter cannot be empty when building a CellBaseClient");
        }
        this.species = species;
        this.assembly = StringUtils.isEmpty(assembly) ? null : assembly;
        this.dataRelease = StringUtils.isEmpty(dataRelease) ? null : dataRelease;
        this.token = token;
        if (clientConfiguration != null && clientConfiguration.getRest() != null
                && clientConfiguration.getRest().getHosts() != null
                && !clientConfiguration.getRest().getHosts().isEmpty()
                && StringUtils.isNotBlank(clientConfiguration.getVersion())) {
            this.clientConfiguration = clientConfiguration;
        } else {
            throw new IllegalArgumentException("version and host must be provided in a ClienConfiguration object"
                    + " when building a CellBase client and cannot be empty");
        }
        clients = new ConcurrentHashMap<>();

    }

    public GeneClient getGeneClient() {
        return getClient("GENE", () -> new GeneClient(species, assembly, dataRelease, token, clientConfiguration));
    }

    public TranscriptClient getTranscriptClient() {
        return getClient("TRANSCRIPT", () -> new TranscriptClient(species, assembly, dataRelease, token, clientConfiguration));
    }

//    public VariationClient getVariationClient() {
//        return getClient("VARIATION", () -> new VariationClient(species, assembly, clientConfiguration));
//    }

    public VariantClient getVariantClient() {
        return getClient("VARIANT", () -> new VariantClient(species, assembly, dataRelease, token, clientConfiguration));
    }

    public ProteinClient getProteinClient() {
        return getClient("PROTEIN", () -> new ProteinClient(species, assembly, dataRelease, token, clientConfiguration));
    }

    public GenomicRegionClient getGenomicRegionClient() {
        return getClient("GENOME_REGION", () -> new GenomicRegionClient(species, assembly, dataRelease, token, clientConfiguration));
    }

    public MetaClient getMetaClient() {
        return getClient("META", () -> new MetaClient(species, assembly, dataRelease, token, clientConfiguration));
    }

    public ClinicalVariantClient getClinicalClient() {
        return getClient("CLINICAL", () -> new ClinicalVariantClient(species, assembly, dataRelease, token, clientConfiguration));
    }

    public GenericClient getGenericClient() {
        return getClient("GENERIC", () -> new GenericClient(species, assembly, dataRelease, token, clientConfiguration));
    }

    @SuppressWarnings("unchecked")
    private <T extends ParentRestClient> T getClient(String key, Supplier<T> constructorIfAbsent) {
        // Avoid concurrent modifications
        return (T) clients.computeIfAbsent(key, s -> constructorIfAbsent.get());
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

//    public CellBaseClient setSpecies(String species) {
//        this.species = species;
//        return this;
//    }

    public String getAssembly() {
        return assembly;
    }

    public String getDataRelease() {
        return dataRelease;
    }

    public String getToken() {
        return token;
    }

    public ClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }

//    public CellBaseClient setClientConfiguration(ClientConfiguration clientConfiguration) {
//        this.clientConfiguration = clientConfiguration;
//        return this;
//    }

}
