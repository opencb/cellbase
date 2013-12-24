package org.opencb.cellbase.server.ws.network;

import org.opencb.cellbase.core.lib.api.network.PathwayDBAdaptor;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.cellbase.server.exception.VersionException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

@Path("/{version}/{species}/network/reactome-pathway")
@Produces("text/plain")
public class PathwayWSServerMongo extends GenericRestWSServer {

	public PathwayWSServerMongo(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
		super(version, species, uriInfo, hsr);
	}

	@GET
	@Path("/list")
	public Response getAllPathways() {
		try {
//			checkVersionAndSpecies();
			
			PathwayDBAdaptor pathwayDBAdaptor = dbAdaptorFactory.getPathwayDBAdaptor(this.species, this.version);
	        String pathways = pathwayDBAdaptor.getPathways();
	        return createOkResponse(pathways);
	        
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getAllPathways", e.toString());
		}
	}
	
	@GET
	@Path("/tree")
	public Response getTree() {
		try {
			PathwayDBAdaptor pathwayDBAdaptor = dbAdaptorFactory.getPathwayDBAdaptor(this.species, this.version);
			String result = pathwayDBAdaptor.getTree();
			return createOkResponse(result);
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getAllPathways", e.toString());
		}
	}

	@GET
	@Path("/{pathwayId}/info")
	public Response getPathwayInfo(@PathParam("pathwayId") String pathwayId) {
		try {
			PathwayDBAdaptor pathwayDBAdaptor = dbAdaptorFactory.getPathwayDBAdaptor(this.species, this.version);
			String result = pathwayDBAdaptor.getPathway(pathwayId);
			return createOkResponse(result);
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getPathwayInfo", e.toString());
		}
	}
	
	@GET
	@Path("/search")
	public Response search(@QueryParam("by") String searchBy, @QueryParam("text") String searchText , @QueryParam("onlyIds") boolean returnOnlyIds) {
		try {
			PathwayDBAdaptor pathwayDBAdaptor = dbAdaptorFactory.getPathwayDBAdaptor(this.species, this.version);
			String result = pathwayDBAdaptor.search(searchBy, searchText, returnOnlyIds);
			return createOkResponse(result);
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("search", e.toString());
		}
	}
	
	
	@GET
	@Path("/help")
	public Response help() {
		StringBuilder sb = new StringBuilder();
		sb.append("Input:\n");
		sb.append("all id formats are accepted.\n\n\n");
		sb.append("Resources:\n");
		sb.append("- list: This subcategory is an informative WS that show the complete list of available pathways. This is an special resource which does not need a pathway name as input.\n");
		sb.append(" Output columns: internal ID, pathway name, description.\n\n");
		sb.append("- info: Prints descriptive information about a pathway.\n");
		sb.append(" Output columns: internal ID, pathway name, description.\n\n");
		sb.append("- image: Download an image of the selected pathway.\n\n\n");
		sb.append("Documentation:\n");
		sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Network_rest_ws_api#Pathway");
		
		return createOkResponse(sb.toString());
	}
}
