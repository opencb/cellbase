/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.server.rest.feature;

import io.swagger.annotations.*;
import org.opencb.biodata.formats.protein.uniprot.v202003jaxb.Entry;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.queries.ProteinQuery;
import org.opencb.cellbase.core.api.queries.QueryException;
import org.opencb.cellbase.core.api.queries.TranscriptQuery;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.SpeciesUtils;
import org.opencb.cellbase.lib.managers.ProteinManager;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.rest.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Path("/{apiVersion}/{species}/feature/protein")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Protein", description = "Protein RESTful Web Services API")
public class ProteinWSServer extends GenericRestWSServer {

    private ProteinManager proteinManager;

    public ProteinWSServer(@PathParam("apiVersion")
                                @ApiParam(name = "apiVersion", value = ParamConstants.VERSION_DESCRIPTION,
                                        defaultValue = ParamConstants.DEFAULT_VERSION) String apiVersion,
                           @PathParam("species")
                                @ApiParam(name = "species", value = ParamConstants.SPECIES_DESCRIPTION) String species,
                           @ApiParam(name = "assembly", value = ParamConstants.ASSEMBLY_DESCRIPTION)
                           @DefaultValue("")
                           @QueryParam("assembly") String assembly,
                           @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, IOException, CellbaseException {
        super(apiVersion, species, uriInfo, hsr);
        if (assembly == null) {
            assembly = SpeciesUtils.getDefaultAssembly(cellBaseConfiguration, species).getName();
        }
        proteinManager = cellBaseManagerFactory.getProteinManager(species, assembly);
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = ParamConstants.DATA_MODEL_DESCRIPTION, response = Map.class,
            responseContainer = "QueryResponse")
    public Response getModel() {
        return createModelResponse(Entry.class);
    }

    @GET
    @Path("/{proteins}/info")
    @ApiOperation(httpMethod = "GET", value = "Get the protein info", response = Entry.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getInfo(@PathParam("proteins") @ApiParam(name = "proteins", value = ParamConstants.PROTEIN_XREF_IDS,
                                               required = true) String id) {
        try {
            List<ProteinQuery> proteinQueries = new ArrayList<>();
            String[] identifiers = id.split(",");
            for (String identifier : identifiers) {
                ProteinQuery query = new ProteinQuery(uriParams);
                query.setXrefs(Arrays.asList(identifier));
                proteinQueries.add(query);
                logger.info("REST proteinQuery: {}", query.toString());
            }
            List<CellBaseDataResult<Entry>> queryResults = proteinManager.info(proteinQueries);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/search")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time.",
            value = "Get all proteins", response = Entry.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "count", value = ParamConstants.COUNT_DESCRIPTION,
                    required = false, dataType = "boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "accession", value = ParamConstants.PROTEIN_ACCESSIONS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "name", value = ParamConstants.PROTEIN_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "gene", value = ParamConstants.GENE_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "xrefs", value = ParamConstants.PROTEIN_XREF_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = ParamConstants.PROTEIN_KEYWORD,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = ParamConstants.SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "order", value = ParamConstants.ORDER_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query",
                    defaultValue = "", allowableValues="ASCENDING,DESCENDING"),
            @ApiImplicitParam(name = "limit", value = ParamConstants.LIMIT_DESCRIPTION,
                    required = false, defaultValue = "10", dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "skip", value = ParamConstants.SKIP_DESCRIPTION,
                    required = false, defaultValue = "0", dataType = "java.util.List", paramType = "query")
    })
    public Response getAll() {
        try {
            ProteinQuery query = new ProteinQuery(uriParams);
            CellBaseDataResult<Entry> queryResults = proteinManager.search(query);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{proteins}/substitutionScores")
    @ApiOperation(httpMethod = "GET", value = "Get the gene corresponding substitution scores for the input protein",
        notes = ParamConstants.SUBSTITUTION_SCORE_NOTE, response = List.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({

            @ApiImplicitParam(name = "exclude", value = ParamConstants.EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = ParamConstants.INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = ParamConstants.SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "order", value = ParamConstants.ORDER_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query",
                    defaultValue = "", allowableValues="ASCENDING,DESCENDING"),
            @ApiImplicitParam(name = "limit", value = ParamConstants.LIMIT_DESCRIPTION,
                    required = false, defaultValue = "10", dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "skip", value = ParamConstants.SKIP_DESCRIPTION,
                    required = false, defaultValue = "0", dataType = "java.util.List", paramType = "query")
    })
    public Response getSubstitutionScores(@PathParam("proteins") @ApiParam(name = "proteins", value = ParamConstants.PROTEIN_XREF_ID,
                                                  required = true) String id,
                                          @QueryParam("position") @ApiParam(name = "position", value = ParamConstants.POSITION_DESCRIPTION,
                                                  required = false) Integer position,
                                          @QueryParam("aa") @ApiParam(name = "aa", value = ParamConstants.AA_DESCRIPTION,
                                                  required = false) String aa) {
        try {
            TranscriptQuery query = new TranscriptQuery(uriParams);
            query.setTranscriptsXrefs(Arrays.asList(id.split(",")));
            CellBaseDataResult queryResult = proteinManager.getSubstitutionScores(query, position, aa);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

//    @GET
//    @Path("/{proteins}/name")
//    @ApiOperation(httpMethod = "GET", value = "Deprecated", hidden = true)
//    @Deprecated
//    public Response getproteinByName(@PathParam("proteins") String id) {
//        try {
//            parseQueryParams();
//            ProteinDBAdaptor geneDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.assembly);
//            return createOkResponse(geneDBAdaptor.get(Splitter.on(",").splitToList(id), queryOptions));
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

//    @GET
//    @Path("/{proteins}/gene")
//    @ApiOperation(httpMethod = "GET", value = "Get the gene corresponding to the input protein", hidden = true)
//    public Response getGene(@PathParam("proteins") String query) {
//        return null;
//    }
//
//    @GET
//    @Path("/{proteins}/transcript")
//    @ApiOperation(httpMethod = "GET", value = "To be implemented", hidden = true)
//    public Response getTranscript(@PathParam("proteins") String query) {
//        return null;
//    }

    @GET
    @Path("/{proteins}/sequence")
    @ApiOperation(httpMethod = "GET", value = "Get the aa sequence for the given protein", response = String.class,
        responseContainer = "QueryResponse")
    public Response getSequence(@PathParam("proteins") @ApiParam (name = "proteins", value = ParamConstants.PROTEIN_ACCESSION,
                                        required = true) String proteins) throws QueryException {
        try {
            ProteinQuery query = new ProteinQuery(uriParams);
            query.setAccessions(Arrays.asList(proteins.split(",")));
            CellBaseDataResult<String> queryResult = proteinManager.getSequence(query);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/help")
    public Response help() {
        StringBuilder sb = new StringBuilder();
        sb.append("Input:\n");
        sb.append("all id formats are accepted.\n\n\n");
        sb.append("Resources:\n");
        sb.append("- info: Get protein information: name, UniProt ID and description.\n");
        sb.append(" Output columns: UniProt accession, protein name, full name, gene name, organism.\n\n");
        sb.append("- feature: Get particular features for the protein sequence: natural variants in the aminoacid sequence, "
                + "mutagenesis sites, etc.\n");
        sb.append(" Output columns: feature type, aa start, aa end, original, variation, identifier, description.\n\n\n");
        sb.append("Documentation:\n");
        sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Feature_rest_ws_api#Protein");

        return createOkResponse(sb.toString());
    }

}
