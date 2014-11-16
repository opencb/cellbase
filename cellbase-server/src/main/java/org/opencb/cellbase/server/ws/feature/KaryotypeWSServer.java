package org.opencb.cellbase.server.ws.feature;

import com.google.common.base.Splitter;
import org.opencb.cellbase.core.lib.api.CytobandDBAdaptor;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.cellbase.server.exception.VersionException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

@Deprecated
@Path("/{version}/{species}/feature/karyotype")
@Produces("text/plain")
public class KaryotypeWSServer extends GenericRestWSServer {
	
	
	public KaryotypeWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
		super(version, species, uriInfo, hsr);
	}

	@GET
	@Path("/{chromosomeName}/cytoband")
	public Response getByChromosomeName(@PathParam("chromosomeName") String chromosome) {
		try {
			checkParams();
			CytobandDBAdaptor dbAdaptor = dbAdaptorFactory.getCytobandDBAdaptor(this.species, this.assembly);
			return generateResponse(chromosome, dbAdaptor.getAllByChromosomeList(Splitter.on(",").splitToList(chromosome)));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getByChromosomeName", e.toString());
		}
	}
	
	@GET
	@Path("/chromosome")
	public Response getChromosomes() {
		try {
			checkParams();
			CytobandDBAdaptor dbAdaptor = dbAdaptorFactory.getCytobandDBAdaptor(this.species, this.assembly);
			return generateResponse("", dbAdaptor.getAllChromosomeNames());
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("getChromosomes", e.toString());
		}
	}

	@GET
	@Path("/{chromosomeName}/chromosome")
	public Response getChromosomes(@PathParam("chromosomeName") String query) {
		return getChromosomes();
//		try {
//			return getChromosomes();
//		} catch (Exception e) {
//			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
//		}
	}
	
	@GET
	public Response getHelp() {
		return help();
	}
	@GET
	@Path("/help")
	public Response help() {
		return createOkResponse("Usage:");
	}
}
