/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.server.ws.feature;

import io.swagger.annotations.*;
import org.bson.Document;
import org.opencb.cellbase.core.api.ClinicalDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
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
@Path("/{version}/{species}/feature/clinical")
@Produces("application/json")
@Api(value = "Clinical", description = "Clinical RESTful Web Services API")
public class ClinicalWSServer extends GenericRestWSServer {


    public ClinicalWSServer(@PathParam("version")
                            @ApiParam(name = "version", value = "Use 'latest' for last stable version",
                                    defaultValue = "latest") String version,
                            @PathParam("species")
                            @ApiParam(name = "species", value = "Name of the species, e.g.: hsapiens. For a full list "
                                    + "of potentially available species ids, please refer to: "
                                    + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/latest/meta/species") String species,
                            @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/all")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time. "
            + "Please note that ClinVar, COSMIC or GWAS objects may be returned as stored in the database. Please have "
            + "a look at "
            + "https://github.com/opencb/cellbase/wiki/MongoDB-implementation#clinical for further details.",
            value = "Retrieves all the clinical objects", response = Document.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
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
            @ApiImplicitParam(name = "rcv",
                    value = "Comma separated list of rcv ids, e.g.: RCV000033215",
                            required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "rs",
                    value = "Comma separated list of rs ids, e.g.: rs6025",
                            required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "type",
                    value = "Comma separated list of variant types as stored in ClinVar (only enabled for ClinVar "
                        + "variants, e.g. \"single nucleotide variant\" ",
                            required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "review",
                    value = "Comma separated list of review lables (only enabled for ClinVar variants), "
                        + " e.g.: CRITERIA_PROVIDED_SINGLE_SUBMITTER",
                            required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "significance",
                    value = "Comma separated list of clinical significance labels as stored in ClinVar (only enabled "
                        + "for ClinVar variants), e.g.: Benign",
                            required = false, dataType = "list of strings", paramType = "query")
    })
    public Response getAll() {
        try {
            logger.info("VERSION: {}", this.version);
            parseQueryParams();
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory2.getClinicalDBAdaptor(this.species, this.assembly);
            if (!queryOptions.containsKey("limit") || ((int) queryOptions.get("limit")) > 1000) {
                queryOptions.put("limit", 1000);
            }

            return createOkResponse(clinicalDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/group")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the clinical objects", response = Document.class,
            responseContainer = "QueryResponse", hidden = true)
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
            @ApiImplicitParam(name = "rcv",
                    value = "Comma separated list of rcv ids, e.g.: RCV000033215",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "rs",
                    value = "Comma separated list of rs ids, e.g.: rs6025",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "type",
                    value = "Comma separated list of variant types as stored in ClinVar (only enabled for ClinVar "
                            + "variants, e.g. \"single nucleotide variant\" ",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "review",
                    value = "Comma separated list of review lables (only enabled for ClinVar variants), "
                            + " e.g.: CRITERIA_PROVIDED_SINGLE_SUBMITTER",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "significance",
                    value = "Comma separated list of clinical significance labels as stored in ClinVar (only enabled "
                            + "for ClinVar variants), e.g.: Benign",
                    required = false, dataType = "list of strings", paramType = "query"),
    })
    public Response groupBy(@DefaultValue("")
                            @QueryParam("fields")
                            @ApiParam(name = "fields",
                                      value = "Comma separated list of fields to group by. For example: alternate,"
                                              + "clinvarSet.referenceClinVarAssertion.clinicalSignificance.reviewStatus",
                                      required = true) String fields) {
        try {
            parseQueryParams();
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory2.getClinicalDBAdaptor(this.species, this.assembly);
            return createOkResponse(clinicalDBAdaptor.groupBy(query, Arrays.asList(fields.split(",")), queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/phenotype-gene")
    @ApiOperation(httpMethod = "GET", value = "To be reimplemented soon. Resource to get all phenotype-gene relations",
            hidden = true)
    public Response getPhenotypeGeneRelations() {

        try {
            parseQueryParams();
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory2.getClinicalDBAdaptor(this.species, this.assembly);
            return createOkResponse(clinicalDBAdaptor.getPhenotypeGeneRelations(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

//    @GET
//    @Path("/listAcc")
//    @ApiOperation(httpMethod = "GET", value = "Resource to list all accession IDs")
//    public Response getAllListAccessions() {
//        try {
//            parseQueryParams();
//            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(this.species, this.assembly);
//            return createOkResponse(clinicalDBAdaptor.getListClinvarAccessions(queryOptions));
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

}
