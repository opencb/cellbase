package org.opencb.cellbase.server.ws.feature;

import com.google.common.base.Splitter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.opencb.cellbase.core.lib.api.variation.ClinVarDBAdaptor;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;

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
@Path("/{version}/{species}/feature/clinvar")
@Produces("application/json")
@Api(value = "ClinVar", description = "ClinVar RESTful Web Services API")
public class ClinVarWSServer extends GenericRestWSServer {

    public ClinVarWSServer(@PathParam("version") String version, @PathParam("species") String species,
                        @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/{clinVarAcc}/info")
    @ApiOperation(httpMethod = "GET", value = "Resource to get ClinVar info from a list of accession IDs")
    public Response getAllByAccessions(@PathParam("clinVarAcc") String query) {
        try {
            checkParams();
            ClinVarDBAdaptor clinVarDBAdaptor = dbAdaptorFactory.getClinVarDBAdaptor(this.species, this.assembly);
            return createOkResponse(clinVarDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAllByAccessions", e.toString());
        }
    }

    @GET
    @Path("/listAcc")
    @ApiOperation(httpMethod = "GET", value = "Resource to list all accession IDs")
    public Response getAllListAccessions() {
        try {
            checkParams();
            ClinVarDBAdaptor clinVarDBAdaptor = dbAdaptorFactory.getClinVarDBAdaptor(this.species, this.assembly);
            return createOkResponse(clinVarDBAdaptor.getListAccessions(queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAllListAccessions", e.toString());
        }
    }

}
