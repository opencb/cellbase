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

package org.opencb.cellbase.server.rest.regulatory;

import io.swagger.annotations.*;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.cellbase.core.api.GeneQuery;
import org.opencb.cellbase.core.api.RegulationQuery;
import org.opencb.cellbase.core.api.query.LogicalList;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.managers.GeneManager;
import org.opencb.cellbase.lib.managers.RegulatoryManager;
import org.opencb.cellbase.lib.managers.TfbsManager;
import org.opencb.cellbase.server.exception.CellBaseServerException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.opencb.cellbase.core.ParamConstants.*;


@Path("/{apiVersion}/{species}/regulation/tf")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "TFBS", description = "Gene RESTful Web Services API")
public class TfWSServer extends RegulatoryWSServer {

    private RegulatoryManager regulatoryManager;
    private TfbsManager tfbsManager;
    private GeneManager geneManager;

    public TfWSServer(@PathParam("apiVersion") @ApiParam(name = "apiVersion", value = VERSION_DESCRIPTION, defaultValue = DEFAULT_VERSION)
                              String apiVersion,
                      @PathParam("species") @ApiParam(name = "species", value = SPECIES_DESCRIPTION) String species,
                      @ApiParam(name = "assembly", value = ASSEMBLY_DESCRIPTION) @DefaultValue("") @QueryParam("assembly") String assembly,
                      @ApiParam(name = "dataRelease", value = DATA_RELEASE_DESCRIPTION) @DefaultValue("0") @QueryParam("dataRelease")
                              int dataRelease,
                      @ApiParam(name = "token", value = DATA_ACCESS_TOKEN_DESCRIPTION) @DefaultValue("") @QueryParam("token")
                              String token,
                      @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws CellBaseServerException {
        super(apiVersion, species, assembly, dataRelease, token, uriInfo, hsr);
        try {
            regulatoryManager = cellBaseManagerFactory.getRegulatoryManager(species, assembly);
            tfbsManager = cellBaseManagerFactory.getTFManager(species, assembly);
            geneManager = cellBaseManagerFactory.getGeneManager(species, assembly);
        } catch (Exception e) {
            throw new CellBaseServerException(e.getMessage());
        }
    }


    @GET
    @Path("/{tf}/tfbs")
    @ApiOperation(httpMethod = "GET", value = "Retrieves the corresponding TFBS objects",
            response = RegulatoryFeature.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "cellType", value = CELLTYPE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "count", value = COUNT_DESCRIPTION,
                    required = false, dataType = "boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "region", value = REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "order", value = ORDER_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "limit", value = LIMIT_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_LIMIT, dataType = "java.util.List",
                    paramType = "query"),
            @ApiImplicitParam(name = "skip", value = SKIP_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_SKIP, dataType = "java.util.List",
                    paramType = "query")
    })
    public Response getAllByTfbs(@PathParam("tf") @ApiParam(name = "tf", value = TFBS_IDS, required = true) String tf) {
        try {
            List<RegulationQuery> queries = new ArrayList<>();
            String[] identifiers = tf.split(",");
            for (String identifier : identifiers) {
                RegulationQuery query = new RegulationQuery(uriParams);
                query.setNames(Arrays.asList(identifier));
                query.setFeatureTypes(Arrays.asList("TF_binding_site"));
                queries.add(query);
                logger.info("REST RegulationQuery: {}", query.toString());
            }
            List<CellBaseDataResult<RegulatoryFeature>> queryResults = regulatoryManager.search(queries);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{tf}/gene")
    @ApiOperation(httpMethod = "GET", value = "Retrieves gene info for a (list of) TF(s)", response = Gene.class,
        responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "count", value = COUNT_DESCRIPTION,
                    required = false, dataType = "java.lang.Boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "transcripts.id", value = TRANSCRIPT_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.name", value = TRANSCRIPT_NAMES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.id", value = TRANSCRIPT_TFBS_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.id", value = ANNOTATION_DISEASES_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.name", value = ANNOTATION_DISEASES_NAMES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.tissue", value = ANNOTATION_EXPRESSION_TISSUE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.name", value = ANNOTATION_DRUGS_NAME_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "exclude", value = EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "limit", value = LIMIT_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_LIMIT, dataType = "java.util.List",
                    paramType = "query"),
            @ApiImplicitParam(name = "skip", value = SKIP_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_SKIP, dataType = "java.util.List",
                    paramType = "query")
    })
    public Response getEnsemblGenes(@PathParam("tf") @ApiParam(name = "tf", value = TFBS_IDS, required = true) String tf) {
        try {
            GeneQuery geneQuery = new GeneQuery(uriParams);
            LogicalList<String> logicalList = new LogicalList(Arrays.asList(tf.split(",")));
            geneQuery.setTranscriptsTfbsId(logicalList);
            CellBaseDataResult<Gene> queryResults = geneManager.search(geneQuery);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/help")
    public Response help() {
        StringBuilder sb = new StringBuilder();
        sb.append("Input:\n");
        sb.append("All id formats are accepted.\n\n\n");
        sb.append("Resources:\n");
        sb.append("- info: Get information about this transcription factor (TF).\n\n");
        sb.append("- tfbs: Get all transcription factor binding sites (TFBSs) for this TF.\n");
        sb.append(" Output columns: TF name, target gene name, chromosome, start, end, cell type, sequence, score.\n\n");
        sb.append("- gene: Get all genes regulated by this TF.\n");
        sb.append(" Output columns: Ensembl gene, external name, external name source, biotype, status, chromosome, start, end, strand, "
                + "source, description.\n\n");
        sb.append("- pwm: Get all position weight matrices associated to this TF.\n");
        sb.append(" Output columns: TF Name, type, frequency_matrix, description, source, length, jaspar_accession.\n\n\n");
        sb.append("Documentation:\n");
        sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Regulatory_rest_ws_api#Transcription-Factor");

        return createOkResponse(sb.toString());
    }
}
