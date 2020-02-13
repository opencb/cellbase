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

import com.google.common.base.Splitter;
import io.swagger.annotations.*;
import org.forester.protein.Protein;
import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.Entry;
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.core.ProteinDBAdaptor;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.ProteinManager;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.rest.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
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
                           @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException, CellbaseException {
        super(apiVersion, species, uriInfo, hsr);
        proteinManager = cellBaseManagers.getProteinManager();
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
            @ApiImplicitParam(name = "keyword", value = ParamConstants.PROTEIN_KEYWORD,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getInfoByEnsemblId(@PathParam("proteins")
                                       @ApiParam(name = "proteins", value = ParamConstants.PROTEIN_XREF_IDS,
                                               required = true) String id,
                                       @QueryParam("exclude")
                                       @ApiParam(value = ParamConstants.EXCLUDE_DESCRIPTION) String exclude,
                                       @QueryParam("include")
                                       @ApiParam(value = ParamConstants.INCLUDE_DESCRIPTION) String include,
                                       @QueryParam("sort")
                                       @ApiParam(value = ParamConstants.SORT_DESCRIPTION) String sort) {
        try {
            parseIncludesAndExcludes(exclude, include, sort);
            parseQueryParams();
            List<CellBaseDataResult> queryResults = proteinManager.info(query, queryOptions, species, assembly, id);
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
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getAll(@QueryParam("exclude")
                           @ApiParam(value = ParamConstants.EXCLUDE_DESCRIPTION) String exclude,
                           @QueryParam("include")
                           @ApiParam(value = ParamConstants.INCLUDE_DESCRIPTION) String include,
                           @QueryParam("sort")
                           @ApiParam(value = ParamConstants.SORT_DESCRIPTION) String sort,
                           @QueryParam("limit") @DefaultValue("10")
                           @ApiParam(value = ParamConstants.LIMIT_DESCRIPTION) Integer limit,
                           @QueryParam("skip") @DefaultValue("0")
                           @ApiParam(value = ParamConstants.SKIP_DESCRIPTION)  Integer skip) {
        try {
            parseIncludesAndExcludes(exclude, include, sort);
            parseLimitAndSkip(limit, skip);
            parseQueryParams();
            CellBaseDataResult<Protein> queryResults = proteinManager.search(query, queryOptions, species, assembly);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{proteins}/substitutionScores")
    @ApiOperation(httpMethod = "GET", value = "Get the gene corresponding substitution scores for the input protein",
        notes = "Schema of returned objects will vary depending on provided query parameters. If the amino acid "
                + " position is provided, all scores will be returned for every possible amino acid"
                + " change occurring at that position. If the alternate aminoacid is provided as well, Score objects as"
                + " specified at "
                + " https://github.com/opencb/biodata/blob/develop/biodata-models/src/main/resources/avro/variantAnnotation.avdl"
                + " shall be returned. If none of these parameters are provided, the whole list of scores for every"
                + " possible amino acid change in the protein shall be returned.",
            response = List.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "position",
                    value = "Integer indicating the aminoacid position to check",
                    required = false, dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "aa",
                    value = "Alternate aminoacid to check. Please, use upper-case letters and three letter encoding"
                            + " of aminoacid names, e.g.: CYS",
                    required = false, dataType = "String", paramType = "query")
    })
    public Response getSubstitutionScores(@PathParam("proteins") @ApiParam(name = "proteins", value = ParamConstants.PROTEIN_XREF_ID,
                                                  required = true) String id,
                                          @QueryParam("exclude") @ApiParam(value = ParamConstants.EXCLUDE_DESCRIPTION) String exclude,
                                          @QueryParam("include") @ApiParam(value = ParamConstants.INCLUDE_DESCRIPTION) String include,
                                          @QueryParam("sort") @ApiParam(value = ParamConstants.SORT_DESCRIPTION) String sort,
                                          @QueryParam("limit") @DefaultValue("10")
                                              @ApiParam(value = ParamConstants.LIMIT_DESCRIPTION) Integer limit,
                                          @QueryParam("skip") @DefaultValue("0")
                                              @ApiParam(value = ParamConstants.SKIP_DESCRIPTION)  Integer skip) {
        try {
            parseIncludesAndExcludes(exclude, include, sort);
            parseLimitAndSkip(limit, skip);
            parseQueryParams();
            CellBaseDataResult queryResult = proteinManager.getSubstitutionScores(query, queryOptions, species, assembly, id);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{proteins}/name")
    @ApiOperation(httpMethod = "GET", value = "Deprecated", hidden = true)
    @Deprecated
    public Response getproteinByName(@PathParam("proteins") String id) {
        try {
            parseQueryParams();
            ProteinDBAdaptor geneDBAdaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.assembly);
            return createOkResponse(geneDBAdaptor.get(Splitter.on(",").splitToList(id), queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{proteins}/gene")
    @ApiOperation(httpMethod = "GET", value = "Get the gene corresponding to the input protein", hidden = true)
    public Response getGene(@PathParam("proteins") String query) {
        return null;
    }

    @GET
    @Path("/{proteins}/transcript")
    @ApiOperation(httpMethod = "GET", value = "To be implemented", hidden = true)
    public Response getTranscript(@PathParam("proteins") String query) {
        return null;
    }

    @GET
    @Path("/{proteins}/sequence")
    @ApiOperation(httpMethod = "GET", value = "Get the aa sequence for the given protein", response = String.class,
        responseContainer = "QueryResponse")
    public Response getSequence(@PathParam("proteins") @ApiParam (name = "proteins", value = "UniProt accession id, e.g: Q9UL59",
                                        required = true) String proteins) {
        CellBaseDataResult<String> queryResult = proteinManager.getSequence(query, queryOptions, species, assembly, proteins);
        return createOkResponse(queryResult);
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
