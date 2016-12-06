package org.opencb.cellbase.server.ws.clinical;

import io.swagger.annotations.*;
import org.bson.Document;
import org.opencb.cellbase.core.api.ClinicalDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
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
 * Created by fjlopez on 06/12/16.
 */
@Path("/{version}/{species}/feature/clinical")
@Produces("application/json")
@Api(value = "Clinical (Deprecated)", description = "Clinical RESTful Web Services API")
@Deprecated
public class ClinicalWSServer extends GenericRestWSServer {


    public ClinicalWSServer(@PathParam("version")
                                  @ApiParam(name = "version", value = "Possible values: v3, v4",
                                          defaultValue = "v4") String version,
                                  @PathParam("species")
                                  @ApiParam(name = "species", value = "Name of the species, e.g.: hsapiens. For a full list "
                                          + "of potentially available species ids, please refer to: "
                                          + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/meta/species") String species,
                                  @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/search")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time. "
            + "Please note that ClinVar, COSMIC or GWAS objects may be returned as stored in the database. Please have "
            + "a look at "
            + "https://github.com/opencb/cellbase/wiki/MongoDB-implementation#clinical for further details.",
            value = "Retrieves all the clinical objects", response = Document.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "source",
                    value = "Comma separated list of database sources of the documents to be returned. Possible values "
                            + " are clinvar,cosmic or gwas. E.g.: clinvar,cosmic",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "so",
                    value = "Comma separated list of sequence ontology term names, e.g.: missense_variant. Exact text "
                            + "matches will be returned.",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "gene",
                    value = "Comma separated list gene ids, e.g.: BRCA2. Gene ids can be either HGNC symbols or "
                            + " ENSEMBL gene ids. Exact text matches will be returned.",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "phenotype",
                    value = "String to indicate the phenotypes to query. A text search will be run.",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "clinvarId",
                    value = "Comma separated list of rcv ids, e.g.: RCV000033215",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "rs",
                    value = "Comma separated list of rs ids, e.g.: rs6025",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "cosmicId",
                    value = "Comma separated list of cosmic ids, e.g.: COSM306824",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "type",
                    value = "Comma separated list of variant types as stored in ClinVar (only enabled for ClinVar "
                            + "variants, e.g. \"single nucleotide variant\" ",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "review",
                    value = "Comma separated list of review lables (only enabled for ClinVar variants), "
                            + " e.g.: CRITERIA_PROVIDED_SINGLE_SUBMITTER",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "clinvar-significance",
                    value = "Comma separated list of clinical significance labels as stored in ClinVar (only enabled "
                            + "for ClinVar variants), e.g.: Benign",
                    required = false, dataType = "list of strings", paramType = "query")
    })
    public Response getAll() {
        try {
            parseQueryParams();
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory2.getClinicalDBAdaptor(this.species, this.assembly);

            return createOkResponse(clinicalDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

}
