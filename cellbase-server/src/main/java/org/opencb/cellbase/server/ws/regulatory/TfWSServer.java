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

import com.google.common.base.Splitter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.opencb.cellbase.core.db.api.regulatory.TfbsDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.datastore.core.QueryResponse;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Path("/{version}/{species}/regulatory/tf")
@Produces("text/plain")
@Api(value = "TFBS", description = "Gene RESTful Web Services API")
public class TfWSServer extends RegulatoryWSServer {

    public TfWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo,
                      @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }


//    @GET
//    @Path("/{tfId}/info")
//    public Response getTfInfo(@PathParam("tfId") String query) {
//        try {
//            parseQueryParams();
//            ProteinDBAdaptor adaptor = dbAdaptorFactory.getProteinDBAdaptor(this.species, this.assembly);
//            return generateResponse(query, adaptor.getAllByGeneNameList(Splitter.on(",").splitToList(query)));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return createErrorResponse("getTfInfo", e.toString());
//        }
//    }

    @GET
    @Path("/{tfId}/tfbs")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the gene objects", response = QueryResponse.class, notes = "This is a note")
    public Response getAllByTfbs(
            @ApiParam(name = "pepe", required = true, allowableValues = "a,b,c", value = "aaaaa") @PathParam("tfId") String query,
            @DefaultValue("") @QueryParam("celltype") String celltype, @DefaultValue("-2500") @QueryParam("start") String start,
            @DefaultValue("500") @QueryParam("end") String end) {
        try {
            parseQueryParams();
            TfbsDBAdaptor adaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species, this.assembly);
            if (celltype == null || celltype.equals("")) {
                celltype = null;
            }
            int iStart = -2500;
            int iEnd = 500;
            try {
                if (start != null && end != null) {
                    iStart = Integer.parseInt(start);
                    iEnd = Integer.parseInt(end);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return createErrorResponse("getAllByTfbs", e.toString());
            }
            return createOkResponse(adaptor.getAllByIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAllByTfbs", e.toString());
        }
    }


//    @GET
//    @Path("/{tfId}/gene")
//    public Response getEnsemblGenes(@PathParam("tfId") String query) {
//        try {
//            parseQueryParams();
//            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.assembly);
//            return  generateResponse(query, "GENE", geneDBAdaptor.getAllByTfList(Splitter.on(",").splitToList(query)));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return createErrorResponse("getEnsemblGenes", e.toString());
//        }
//    }
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

//    @GET
//    @Path("/{tfId}/pwm")
//    public Response getAllPwms(@PathParam("tfId") String query) {
//        try {
//            parseQueryParams();
//            TfbsDBAdaptor tfbsDBAdaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species, this.assembly);
//            return generateResponse(query, tfbsDBAdaptor.getAllPwmByTfGeneNameList(Splitter.on(",").splitToList(query))));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return createErrorResponse("getAllPwms", e.toString());
//        }
//    }


    @GET
    @Path("/annotation")
    public Response getAnnotation(@DefaultValue("") @QueryParam("celltype") String celltype) {
        try {
            parseQueryParams();
            TfbsDBAdaptor tfbsDBAdaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species, this.assembly);
            List<Object> results;
            if (celltype.equals("")) {
                results = tfbsDBAdaptor.getAllAnnotation();
            } else {
                results = tfbsDBAdaptor.getAllAnnotationByCellTypeList(Splitter.on(",").splitToList(celltype));
            }
            List<String> lista = new ArrayList<String>();

            for (Object result : results) {
                lista.add(((Object[]) result)[0].toString() + "\t" + ((Object[]) result)[1].toString());
            }
            return generateResponse(new String(), lista);

        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAnnotation", e.toString());
        }
    }

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
