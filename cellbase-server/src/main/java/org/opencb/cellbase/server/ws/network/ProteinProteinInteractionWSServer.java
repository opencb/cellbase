package org.opencb.cellbase.server.ws.network;

import com.google.common.base.Splitter;
import org.opencb.cellbase.core.lib.api.network.ProteinProteinInteractionDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 12/5/13
 * Time: 5:08 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/{version}/{species}/network/protein")
@Produces(MediaType.APPLICATION_JSON)
public class ProteinProteinInteractionWSServer extends GenericRestWSServer {

    public ProteinProteinInteractionWSServer(@PathParam("version") String version, @PathParam("species") String species,
                                             @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
        super(version, species, uriInfo, hsr);
    }


    @GET
    @Path("/all")
    public Response getAllPPI(@DefaultValue("") @QueryParam("interactor") String interactor,
                              @DefaultValue("") @QueryParam("type") String type,
                              @DefaultValue("") @QueryParam("database") String database,
                              @DefaultValue("") @QueryParam("status") String status,
                              @DefaultValue("") @QueryParam("detectionMethod") String detectionMethod) {
        try {
            checkVersionAndSpecies();
            ProteinProteinInteractionDBAdaptor ppiDBAdaptor = dbAdaptorFactory.getProteinProteinInteractionDBAdaptor(this.species, this.version);

            if(interactor != null && !interactor.equals("")) {
                queryOptions.put("interactor", Splitter.on(",").splitToList(interactor));
            }

            if(type != null && !type.equals("")) {
                queryOptions.put("type", Splitter.on(",").splitToList(type));
            }

            if(database != null && !database.equals("")) {
                queryOptions.put("database", Splitter.on(",").splitToList(database));
            }

            if(detectionMethod != null && !detectionMethod.equals("")) {
                queryOptions.put("detectionMethod", Splitter.on(",").splitToList(detectionMethod));
            }

            if(status != null && !status.equals("")) {
                queryOptions.put("status", Splitter.on(",").splitToList(status));
            }

//            if(type != null && !type.equals("")) {
//                queryOptions.put("type", type);
//            }

            return createOkResponse(ppiDBAdaptor.getAll(queryOptions));
        } catch (VersionException | SpeciesException e) {
            e.printStackTrace();
            return createErrorResponse("getAllPPI", e.toString());
        }

    }

    @GET
    @Path("/{interaction}/info")
    public Response getPPIByInteractionId(@PathParam("interaction") String interaction) {
        try {
            checkVersionAndSpecies();
            ProteinProteinInteractionDBAdaptor ppiDBAdaptor = dbAdaptorFactory.getProteinProteinInteractionDBAdaptor(this.species, this.version);

            List<QueryResult> queryResults = ppiDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(interaction), queryOptions);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getPPIByInteractionId", e.toString());
        }
    }

    // TODO Correct method
    @GET
    @Path("/{interaction}/interactors")
    public Response getInteractorsByInteractionId(@PathParam("interaction") String interaction,
                                                  @DefaultValue("interactorA,interactorB") @QueryParam("include") String include) {
        try {
            checkVersionAndSpecies();
            ProteinProteinInteractionDBAdaptor ppiDBAdaptor = dbAdaptorFactory.getProteinProteinInteractionDBAdaptor(this.species, this.version);

//            queryOptions.put("include", Splitter.on(",").splitToList(include));
            queryOptions.put("include", include);

            List<QueryResult> queryResults = ppiDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(interaction), queryOptions);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getPPIByInteractionId", e.toString());
        }
    }
}
