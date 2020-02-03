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
import org.opencb.cellbase.core.ParamConstants;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.core.api.RegulationDBAdaptor;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.commons.datastore.core.Query;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.List;


@Path("/{version}/{species}/regulation/tf")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "TFBS", description = "Gene RESTful Web Services API")
public class TfWSServer extends RegulatoryWSServer {

    public TfWSServer(@PathParam("version")
                      @ApiParam(name = "version", value = ParamConstants.VERSION_DESCRIPTION,
                              defaultValue = ParamConstants.DEFAULT_VERSION) String version,
                      @PathParam("species")
                      @ApiParam(name = "species", value = ParamConstants.SPECIES_DESCRIPTION) String species,
                      @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException, CellbaseException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/{tfId}/tfbs")
    @ApiOperation(httpMethod = "GET", value = "Retrieves the corresponding TFBS objects",
            response = RegulatoryFeature.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region", value = ParamConstants.REGION_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getAllByTfbs(@PathParam("tfId")
                                             @ApiParam(name = "tfId", value = "String containing a comma separated list "
                                                     + " of TF names to search, e.g.: CTCF", required = true) String tfId,
                                 @DefaultValue("") @QueryParam("celltype") String celltype) {
        try {
            parseQueryParams();
            RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory.getRegulationDBAdaptor(this.species, this.assembly);
            List<Query> queries = createQueries(tfId, RegulationDBAdaptor.QueryParams.NAME.key(),
                    RegulationDBAdaptor.QueryParams.FEATURE_TYPE.key(), RegulationDBAdaptor.FeatureType.TF_binding_site
                            + "," + RegulationDBAdaptor.FeatureType.TF_binding_site_motif);
            List<CellBaseDataResult> queryResults = regulationDBAdaptor.nativeGet(queries, queryOptions);
            for (int i = 0; i < queries.size(); i++) {
                queryResults.get(i).setId((String) queries.get(i).get(RegulationDBAdaptor.QueryParams.NAME.key()));
            }
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{tfId}/gene")
    @ApiOperation(httpMethod = "GET", value = "Retrieves gene info for a (list of) TF(s)", response = Gene.class,
        responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "transcripts.id", value = ParamConstants.TRANSCRIPT_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.name", value = ParamConstants.TRANSCRIPT_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.name", value = ParamConstants.TRANSCRIPT_TFBS_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.id", value = ParamConstants.ANNOTATION_DISEASES_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.name", value = ParamConstants.ANNOTATION_DISEASES_NAMES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.tissue", value = ParamConstants.ANNOTATION_EXPRESSION_TISSUE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.name", value = ParamConstants.ANNOTATION_DRUGS_NAME,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getEnsemblGenes(@PathParam("tfId")
                                                @ApiParam(name = "tfId", value = "String containing a comma separated "
                                                        + " list of HGNC symbols, e.g.: CTCF", required = true) String tfId) {
        try {
            parseQueryParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.assembly);
            List<Query> queries = createQueries(tfId, GeneDBAdaptor.QueryParams.NAME.key());
            List<CellBaseDataResult> queryResults = geneDBAdaptor.nativeGet(queries, queryOptions);
            for (int i = 0; i < queries.size(); i++) {
                queryResults.get(i).setId((String) queries.get(i).get(GeneDBAdaptor.QueryParams.NAME.key()));
            }
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
