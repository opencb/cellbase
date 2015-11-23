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

package org.opencb.cellbase.server;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Created with IntelliJ IDEA.
 * User: fsalavert
 * Date: 10/30/13
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class CellBaseServer extends ResourceConfig {

    public CellBaseServer() {
        packages("org.opencb.cellbase.server.ws");

//        BeanConfig beanConfig = new BeanConfig();
//        beanConfig.setVersion("1.0.2");
//        beanConfig.setSchemes(new String[]{"http"});
//        beanConfig.setHost("localhost:8080");
//        beanConfig.setBasePath("/api");
//        beanConfig.setResourcePackage("io.swagger.resources");
//        beanConfig.setScan(true);

    }
}
