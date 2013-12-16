package org.opencb.cellbase.server.ws.feature;

import com.google.common.base.Splitter;
import org.opencb.cellbase.core.lib.api.variation.MutationDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 25/11/13
 * Time: 14:09
 * To change this template use File | Settings | File Templates.
 */
@Path("/{version}/{species}/feature/mutation")
@Produces("application/json")
public class MutationWSServer  extends GenericRestWSServer {

    public MutationWSServer(@PathParam("version") String version, @PathParam("species") String species,
                            @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/list")
    public Response getMutations(@DefaultValue("") @QueryParam("disease") String disease) {
        try {
            checkVersionAndSpecies();
            MutationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getMutationDBAdaptor(this.species, this.version);
            queryOptions.put("disease", Splitter.on(",").splitToList(disease));
            return createOkResponse(variationDBAdaptor.getAll(queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getByEnsemblId", e.toString());
        }
    }

    @GET
    @Path("/diseases")
    public Response getMutationDiseases(@PathParam("mutationId") String query) {
        try {
            checkVersionAndSpecies();
            MutationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getMutationDBAdaptor(this.species, this.version);
            return createOkResponse(variationDBAdaptor.getAllDiseases(queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getByEnsemblId", e.toString());
        }
    }

    @GET
    @Path("/{mutationId}/info")
    public Response getByEnsemblId(@PathParam("mutationId") String query) {
        try {
            checkVersionAndSpecies();
            MutationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getMutationDBAdaptor(this.species, this.version);
            return createOkResponse(variationDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getByEnsemblId", e.toString());
        }
    }

}
