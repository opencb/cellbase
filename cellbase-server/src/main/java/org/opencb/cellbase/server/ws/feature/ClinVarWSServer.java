package org.opencb.cellbase.server.ws.feature;

import com.google.common.base.Splitter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import org.opencb.biodata.models.feature.Region;
import org.opencb.cellbase.core.lib.api.variation.ClinVarDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.ClinicalDBAdaptor;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Arrays;

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
    public Response getAllByAccessions(@PathParam("clinVarAcc") String query,
                                       @DefaultValue("") @QueryParam("gene") String gene,
                                       @DefaultValue("") @QueryParam("region") String region,
                                       @DefaultValue("") @QueryParam("rs") String rs) {
        try {
            checkParams();
            ClinicalDBAdaptor clinVarDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(this.species, this.assembly);
            //ClinVarDBAdaptor clinVarDBAdaptor = dbAdaptorFactory.getClinVarDBAdaptor(this.species, this.assembly);
            if(gene != null && !gene.equals("")) {
                queryOptions.add("gene", Arrays.asList(gene.split(",")));
            }
            if(region != null && !region.equals("")) {
                queryOptions.add("region", Region.parseRegions(query));
            }
            if(rs != null && !rs.equals("")) {
                queryOptions.add("rs", Arrays.asList(rs.split(",")));
            }
            return createOkResponse(clinVarDBAdaptor.getAllClinvarByIdList(Splitter.on(",").splitToList(query), queryOptions));
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
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(this.species, this.assembly);
            return createOkResponse(clinicalDBAdaptor.getListClinvarAccessions(queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAllListAccessions", e.toString());
        }
    }

}
