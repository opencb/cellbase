package org.opencb.cellbase.server.ws.feature;

import com.google.common.base.Splitter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.opencb.biodata.models.feature.Region;
import org.opencb.cellbase.core.lib.api.core.GeneDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.ClinVarDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.ClinicalDBAdaptor;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.datastore.core.QueryResponse;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author imedina
 */
@Path("/{version}/{species}/feature/clinvar")
@Produces("application/json")
@Api(value = "ClinVar", description = "ClinVar RESTful Web Services API")
public class ClinicalWSServer extends GenericRestWSServer {

    public ClinicalWSServer(@PathParam("version") String version, @PathParam("species") String species,
                            @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/all")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the clinvar objects", response = QueryResponse.class)
    public Response getAll(@DefaultValue("") @QueryParam("gene") String gene,
                           @DefaultValue("") @QueryParam("region") String region,
                           @DefaultValue("") @QueryParam("phenotype") String phenotype) {
        try {
            checkParams();
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(this.species, this.assembly);
            if(queryOptions.get("limit") == null || queryOptions.getInt("limit") > 1000) {
                queryOptions.put("limit", 1000);
            }
            if(gene != null && !gene.equals("")) {
                queryOptions.add("gene", Arrays.asList(gene.split(",")));
            }
            if(region != null && !region.equals("")) {
                queryOptions.add("region", Region.parseRegions(region));
            }
            if(phenotype != null && !phenotype.equals("")) {
                queryOptions.add("phenotype", Arrays.asList(phenotype.split(",")));
            }

            return createOkResponse(clinicalDBAdaptor.getAll(queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAll", e.toString());
        }
    }

    @GET
    @Path("/{acc}/info")
    @ApiOperation(httpMethod = "GET", value = "Resource to get ClinVar info from a list of accession IDs")
    public Response getAllByAccessions(@PathParam("acc") String query,
                                       @DefaultValue("") @QueryParam("gene") String gene,
                                       @DefaultValue("") @QueryParam("region") String region,
                                       @DefaultValue("") @QueryParam("phenotype") String phenotype) {
        try {
            checkParams();
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(this.species, this.assembly);
            if(gene != null && !gene.equals("")) {
                queryOptions.add("gene", Arrays.asList(gene.split(",")));
            }
            if(phenotype != null && !phenotype.equals("")) {
                queryOptions.add("phenotype", Arrays.asList(phenotype.split(",")));
            }
            if(region != null && !region.equals("")) {
                queryOptions.add("region", Region.parseRegions(region));
            }
            return createOkResponse(clinicalDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
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
