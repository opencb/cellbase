package org.opencb.cellbase.server.ws.phenotype;

import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

/**
 * Created by imedina on 20/01/14.
 */
public class PhenotypeWSServer extends GenericRestWSServer {


    public PhenotypeWSServer(@PathParam("version") String version, @PathParam("species") String species,
                          @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
        super(version, species, uriInfo, hsr);
//        this.exclude = Arrays.asList(exclude.trim().split(","));
    }




}
