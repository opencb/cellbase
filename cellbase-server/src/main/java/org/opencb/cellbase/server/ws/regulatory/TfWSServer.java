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

package org.opencb.cellbase.server.ws.regulatory;

import io.swagger.annotations.*;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.cellbase.core.api.GeneDBAdaptor;
import org.opencb.cellbase.core.api.RegulationDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;


@Path("/{version}/{species}/regulation/tf")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "TFBS", description = "Gene RESTful Web Services API")
public class TfWSServer extends RegulatoryWSServer {

    public TfWSServer(@PathParam("version")
                      @ApiParam(name = "version", value = "Use 'latest' for last stable version",
                              defaultValue = "latest") String version,
                      @PathParam("species")
                      @ApiParam(name = "species", value = "Name of the species, e.g.: hsapiens. For a full list "
                              + "of potentially available species ids, please refer to: "
                              + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/latest/meta/species") String species,
                      @Context UriInfo uriInfo,
                      @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }


    @GET
    @Path("/{tfId}/tfbs")
    @ApiOperation(httpMethod = "GET", value = "Retrieves the corresponding TFBS objects",
            response = RegulatoryFeature.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "list of strings", paramType = "query")
    })
    public Response getAllByTfbs(@PathParam("tfId")
                                             @ApiParam(name = "tfId", value = "String containing a comma separated list "
                                                     + " of TF names to search, e.g.: CTCF", required = true) String tfId,
                                 @DefaultValue("") @QueryParam("celltype") String celltype) {
        try {
            parseQueryParams();
            RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory2.getRegulationDBAdaptor(this.species, this.assembly);
            query.put(RegulationDBAdaptor.QueryParams.NAME.key(), tfId);
            query.put(RegulationDBAdaptor.QueryParams.FEATURE_TYPE.key(), RegulationDBAdaptor.FeatureType.TF_binding_site
                            + "," + RegulationDBAdaptor.FeatureType.TF_binding_site_motif);
            return createOkResponse(regulationDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{tfId}/gene")
    @ApiOperation(httpMethod = "GET", value = "Retrieves gene info for a (list of) TF(s)", response = Gene.class,
        responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "transcripts.id",
                    value = "Comma separated list of ENSEMBL transcript ids, e.g.: ENST00000342992,ENST00000380152,"
                            + "ENST00000544455. Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.name",
                    value = "Comma separated list of transcript names, e.g.: BRCA2-201,TTN-003."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.name",
                    value = "Comma separated list of TFBS names, e.g.: CTCF,Gabp."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.id",
                    value = "Comma separated list of phenotype ids (OMIM, UMLS), e.g.: umls:C0030297,OMIM:613390,"
                            + "OMIM:613390. Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.name",
                    value = "Comma separated list of phenotypes, e.g.: Cryptorchidism,Absent thumb,Stage 5 chronic "
                            + "kidney disease. Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.tissue",
                    value = "Comma separated list of tissues for which expression values are available, "
                            + "e.g.: adipose tissue,heart atrium,tongue."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.name",
                    value = "Comma separated list of drug names, "
                            + "e.g.: BMN673,OLAPARIB,VELIPARIB."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query")
    })
    public Response getEnsemblGenes(@PathParam("tfId")
                                                @ApiParam(name = "tfId", value = "String containing a comma separated "
                                                        + " list of HGNC symbols, e.g.: CTCF", required = true) String tfId) {
        try {
            parseQueryParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory2.getGeneDBAdaptor(this.species, this.assembly);
            query.put(GeneDBAdaptor.QueryParams.NAME.key(), tfId);
            return createOkResponse(geneDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
//
//    @GET
//    @Path("/{tfId}/target_gene")
//    public Response getTargetGenes(@PathParam("tfId") String query) {
//        try {
//            parseQueryParams();
//            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.assembly);
//            return  generateResponse(query, "GENE", geneDBAdaptor.getAllTargetsByTfList(Splitter.on(",").splitToList(query)));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return createErrorResponse("getEnsemblGenes", e.toString());
//        }
//    }


    @GET
    public Response defaultMethod() {
        return help();
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
