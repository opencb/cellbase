package org.opencb.cellbase.server.ws.regulatory;

import com.wordnik.swagger.annotations.Api;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.cellbase.server.exception.VersionException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

@Path("/{version}/{species}/regulatory")
@Produces("text/plain")
public class RegulatoryWSServer extends GenericRestWSServer {

	public RegulatoryWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
		super(version, species, uriInfo, hsr);
	}
	
	
	
 
	
}
