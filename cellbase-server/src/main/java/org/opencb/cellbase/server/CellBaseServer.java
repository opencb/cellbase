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
    }
}
