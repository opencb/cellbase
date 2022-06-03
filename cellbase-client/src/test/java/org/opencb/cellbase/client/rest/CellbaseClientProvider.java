package org.opencb.cellbase.client.rest;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.opencb.cellbase.client.config.ClientConfiguration;
import org.opencb.cellbase.client.config.RestConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class CellbaseClientProvider implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
        List<ClientConfiguration> configurations = new ArrayList<>();
        configurations.add(getClientConfiguration("http://bioinfo.hpc.cam.ac.uk/cellbase-4.7.3", "v4"));
        configurations.add(getClientConfiguration("https://ws.opencb.org/cellbase-5.0.0", "v5"));
        configurations.add(getClientConfiguration("https://ws.zettagenomics.com/cellbase", "v5.0"));
        configurations.add(getClientConfiguration("https://ws.zettagenomics.com/cellbase", "v5.1"));

        return configurations.stream().map(CellBaseClient::new).map(Arguments::of);
    }

    private ClientConfiguration getClientConfiguration(String host, String version) {
        return new ClientConfiguration()
                .setDefaultSpecies("hsapiens").setVersion(version).setRest(new RestConfig(Collections.singletonList(host), 2000));
    }
}
