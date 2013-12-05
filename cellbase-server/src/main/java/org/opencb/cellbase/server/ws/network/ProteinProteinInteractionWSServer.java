package org.opencb.cellbase.server.ws.network;

import com.google.common.base.Splitter;
import org.opencb.cellbase.core.lib.api.network.ProteinProteinInteractionDBAdaptor;
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

/**
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 12/5/13
 * Time: 5:08 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/{version}/{species}/network/ppi")
@Produces(MediaType.APPLICATION_JSON)
public class ProteinProteinInteractionWSServer extends GenericRestWSServer {

    public ProteinProteinInteractionWSServer(@PathParam("version") String version, @PathParam("species") String species,
                                             @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
        super(version, species, uriInfo, hsr);
    }


    @GET
    @Path("/all")
    public Response getAllPPI(@DefaultValue("") @QueryParam("interactor") String interactor,
                              @DefaultValue("") @QueryParam("type") String type) {
        try {
            checkVersionAndSpecies();
            ProteinProteinInteractionDBAdaptor ppiDBAdaptor = dbAdaptorFactory.getProteinProteinInteractionDBAdaptor(this.species, this.version);

            if(interactor != null && !interactor.equals("")) {
                queryOptions.put("interactor", Splitter.on(",").splitToList(interactor));
            }

            if(type != null && !type.equals("")) {
                queryOptions.put("type", type);
            }


//            addExcludeReturnFields("transcripts.exons.sequence", queryOptions);
            return createOkResponse(ppiDBAdaptor.getAll(queryOptions));
        } catch (VersionException | SpeciesException e) {
            e.printStackTrace();
            return createErrorResponse("getTranscriptByRegion", e.toString());
        }

    }

}
