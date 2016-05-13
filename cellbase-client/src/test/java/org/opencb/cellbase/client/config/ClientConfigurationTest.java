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

package org.opencb.cellbase.client.config;

import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Created by imedina on 13/05/16.
 */
public class ClientConfigurationTest {

    @Test
    public void testDefault() {
        ClientConfiguration clientConfiguration = new ClientConfiguration();

        clientConfiguration.setVersion("v4");
        clientConfiguration.setLogFile("");

        RestConfig restConfig = new RestConfig(Arrays.asList("bioinfodev.hpc.cam.ac.uk/cellbase-dev-v4.0", "bioinfodev.hpc.cam.ac.uk/cellbase"), 2000);
        GrpcConfig grpcConfig = new GrpcConfig("localhost:9091");

        clientConfiguration.setRest(restConfig);
        clientConfiguration.setGrpc(grpcConfig);

        try {
            clientConfiguration.serialize(new FileOutputStream("/tmp/client-configuration-test.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLoad() throws Exception {
        ClientConfiguration storageConfiguration = ClientConfiguration.load(getClass().getResource("/client-configuration-test.yml").openStream());
        System.out.println("clientConfiguration = " + storageConfiguration);
    }

}