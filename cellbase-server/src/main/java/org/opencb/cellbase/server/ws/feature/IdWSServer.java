package org.opencb.cellbase.server.ws.feature;

import com.google.common.base.Splitter;
import org.opencb.cellbase.core.common.core.Xref;
import org.opencb.cellbase.core.lib.api.GeneDBAdaptor;
import org.opencb.cellbase.core.lib.api.XRefsDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.VariationDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.cellbase.server.exception.VersionException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/{version}/{species}/feature/id")
@Produces("text/plain")
public class IdWSServer extends GenericRestWSServer {

    private List<String> exclude = new ArrayList<>();

    public IdWSServer(@PathParam("version") String version, @PathParam("species") String species,
                      @DefaultValue("") @QueryParam("exclude") String exclude,
                      @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
        super(version, species, uriInfo, hsr);
        this.exclude = Arrays.asList(exclude.trim().split(","));
    }

    @GET
    @Path("/{id}/xref")
    public Response getByFeatureId(@PathParam("id") String query, @DefaultValue("") @QueryParam("dbname") String dbname) {
        try {
            checkVersionAndSpecies();
            XRefsDBAdaptor x = dbAdaptorFactory.getXRefDBAdaptor(this.species, this.version);
                if(!dbname.equals("")) {
                    queryOptions.put("dbname", Splitter.on(",").splitToList(dbname));
                }
                return createOkResponse(x.getAllByDBNameList(Splitter.on(",").splitToList(query), queryOptions));
//            if (dbName.equals("")) {
//                return generateResponse(query, "XREF", x.getAllByDBNameList(Splitter.on(",").splitToList(query), null));
//            }
//            else {
//                return generateResponse(query, "XREF", x.getAllByDBNameList(Splitter.on(",").splitToList(query), Splitter.on(",").splitToList(dbName)));
//            }
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getByEnsemblId", e.toString());
        }
    }

    @GET
    @Path("/{id}/gene")
    public Response getGeneByEnsemblId(@PathParam("id") String query) {
        try {
            checkVersionAndSpecies();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.version);

            QueryOptions queryOptions = new QueryOptions("exclude", exclude);

            return createJsonResponse(geneDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));

//			return generateResponse(query, "GENE",  x.getAllByNameList(Splitter.on(",").splitToList(query),exclude));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getByEnsemblId", e.toString());
        }
    }

    @GET
    @Path("/{id}/snp")
    public Response getSnpByFeatureId(@PathParam("id") String query) {
        try {
            checkVersionAndSpecies();
            VariationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.version);
            return createOkResponse(variationDBAdaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getByEnsemblId", e.toString());
        }
    }

    @GET
    @Path("/{id}/starts_with")
    public Response getByLikeQuery(@PathParam("id") String query) {
        try {
            checkVersionAndSpecies();
            XRefsDBAdaptor x = dbAdaptorFactory.getXRefDBAdaptor(this.species, this.version);
            List<List<Xref>> xrefs = x.getByStartsWithQueryList(Splitter.on(",").splitToList(query));
            if (query.startsWith("rs") || query.startsWith("AFFY_") || query.startsWith("SNP_") || query.startsWith("VAR_") || query.startsWith("CRTAP_") || query.startsWith("FKBP10_") || query.startsWith("LEPRE1_") || query.startsWith("PPIB_")) {
                List<List<Xref>> snpXrefs = x.getByStartsWithSnpQueryList(Splitter.on(",").splitToList(query));
                for (List<Xref> xrefList : snpXrefs) {
                    xrefs.get(0).addAll(xrefList);
                }
            }
            return generateResponse(query, "XREF", xrefs);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getByEnsemblId", e.toString());
        }
    }

    @GET
    @Path("/{id}/contains")
    public Response getByContainsQuery(@PathParam("id") String query) {
        try {
            checkVersionAndSpecies();
            XRefsDBAdaptor x = dbAdaptorFactory.getXRefDBAdaptor(this.species, this.version);
            List<List<Xref>> xrefs = x.getByContainsQueryList(Splitter.on(",").splitToList(query));
            if (query.startsWith("rs") || query.startsWith("AFFY_") || query.startsWith("SNP_") || query.startsWith("VAR_") || query.startsWith("CRTAP_") || query.startsWith("FKBP10_") || query.startsWith("LEPRE1_") || query.startsWith("PPIB_")) {
                List<List<Xref>> snpXrefs = x.getByStartsWithSnpQueryList(Splitter.on(",").splitToList(query));
                for (List<Xref> xrefList : snpXrefs) {
                    xrefs.get(0).addAll(xrefList);
                }
            }
            return generateResponse(query, xrefs);
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getByEnsemblId", e.toString());
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
