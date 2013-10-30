package org.opencb.cellbase.server;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Created with IntelliJ IDEA.
 * User: fsalavert
 * Date: 10/30/13
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class CellbaseServer extends ResourceConfig {
    public CellbaseServer() {
        packages("org.opencb.cellbase.server.ws");
    }
}
