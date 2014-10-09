package org.opencb.cellbase.server.ws.feature;

import com.google.common.base.Splitter;
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
 * Created by imedina on 26/09/14.
 */
@Path("/{version}/{species}/feature/clinvar")
@Produces("text/plain")
public class ClinVarWSServer extends GenericRestWSServer {

    public ClinVarWSServer(@PathParam("version") String version, @PathParam("species") String species,
                        @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/{clinVarAcc}/info")
    public Response getByEnsemblId(@PathParam("clinVarAcc") String query) {
        try {
            checkVersionAndSpecies();
            ClinVarDBAdaptor clinVarDBAdaptor = dbAdaptorFactory.getClinVarDBAdaptor(this.species, this.version);
            return createOkResponse(clinVarDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getByEnsemblId", e.toString());
        }
    }

}
