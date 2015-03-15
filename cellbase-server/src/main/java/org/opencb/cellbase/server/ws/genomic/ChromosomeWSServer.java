package org.opencb.cellbase.server.ws.genomic;

import com.google.common.base.Splitter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.opencb.cellbase.core.lib.api.ChromosomeDBAdaptor;
import org.opencb.cellbase.server.QueryResponse;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.datastore.core.QueryOptions;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

/**
 * @author imedina
 */
@Path("/{version}/{species}/genomic/chromosome")
@Produces("application/json")
@Api(value = "Genome Sequence", description = "Genome Sequence RESTful Web Services API")
public class ChromosomeWSServer extends GenericRestWSServer {

	public ChromosomeWSServer(@PathParam("version") String version, @PathParam("species") String species,
                              @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
		super(version, species, uriInfo, hsr);
	}
	@GET
	@Path("/all")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the chromosome objects", response = QueryResponse.class)
	public Response getChromosomesAll() {
		try {
			checkParams();
			ChromosomeDBAdaptor dbAdaptor = dbAdaptorFactory.getChromosomeDBAdaptor(this.species, this.assembly);
			return createOkResponse(dbAdaptor.getAll(queryOptions));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("all", e.toString());
		}
	}

	@GET
	@Path("/list")
    @ApiOperation(httpMethod = "GET", value = "Retrieves the chromosomes names", response = QueryResponse.class)
	public Response getChromosomes() {
		try {
			checkParams();
			ChromosomeDBAdaptor dbAdaptor = dbAdaptorFactory.getChromosomeDBAdaptor(this.species, this.assembly);
			QueryOptions options = new QueryOptions();
			options.put("include", "chromosomes.name");
			return createOkResponse(dbAdaptor.getAll(options));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("list", e.toString());
		}
	}

	@GET
	@Path("/{chromosomeName}/info")
	public Response getChromosomes(@PathParam("chromosomeName") String query) {
		try {
			checkParams();
			ChromosomeDBAdaptor dbAdaptor = dbAdaptorFactory.getChromosomeDBAdaptor(this.species, this.assembly);
			return createOkResponse(dbAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("/{chromosomeName}/info", e.toString());
		}
	}
	
	@GET
	@Path("/{chromosomeName}/size")
	public Response getChromosomeSize(@PathParam("chromosomeName") String query) {
		try {
			checkParams();
			ChromosomeDBAdaptor dbAdaptor = dbAdaptorFactory.getChromosomeDBAdaptor(this.species, this.assembly);
			QueryOptions options = new QueryOptions();
			options.put("include", "size");
			return createOkResponse(dbAdaptor.getById(query, options));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("/{chromosomeName}/cytoband", e.toString());
		}
	}

	@GET
	@Path("/{chromosomeName}/cytoband")
	public Response getByChromosomeName(@PathParam("chromosomeName") String query) {
		try {
			checkParams();
			ChromosomeDBAdaptor dbAdaptor = dbAdaptorFactory.getChromosomeDBAdaptor(this.species, this.assembly);
			return createOkResponse(dbAdaptor.getAllCytobandsByIdList(Splitter.on(",").splitToList(query), queryOptions));
		} catch (Exception e) {
			e.printStackTrace();
			return createErrorResponse("/{chromosomeName}/cytoband", e.toString());
		}
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
