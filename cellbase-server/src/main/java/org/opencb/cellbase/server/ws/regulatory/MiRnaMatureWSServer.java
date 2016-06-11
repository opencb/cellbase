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
import org.opencb.cellbase.core.db.api.regulatory.MirnaDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Path("/{version}/{species}/regulatory/mirna_mature")
@Produces("text/plain")
@Api(value = "Mature miRNA", description = "miRNA RESTful Web Services API", hidden = true)
public class MiRnaMatureWSServer extends RegulatoryWSServer {

    public MiRnaMatureWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo,
                               @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/{mirnaId}/info")
    public Response getMiRnaMatureInfo(@PathParam("mirnaId") String query) {
        try {
            parseQueryParams();
            // miRnaGene y Ensembl Genes + Transcripts
            // mirnaDiseases
            // mirnaTargets
            MirnaDBAdaptor mirnaDBAdaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species, this.assembly);
            logger.debug("En getMiRnaMatureInfo: " + query);
            return generateResponse(query, mirnaDBAdaptor.getAllMiRnaMaturesByNameList(Splitter.on(",").splitToList(query)));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getMiRnaMatureInfo", e.toString());
        }
    }

//    @GET
//    @Path("/{mirnaId}/gene")
//    public Response getEnsemblGene(@PathParam("mirnaId") String query) {
//        try {
//            parseQueryParams();
//            GeneDBAdaptor adaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.assembly);
//            return  generateResponse(query, adaptor.getAllByMiRnaMatureList(Splitter.on(",").splitToList(query)));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return createErrorResponse("getEnsemblGene", e.toString());
//        }
//    }

    @GET
    @Path("/{mirnaId}/mirna_gene")
    public Response getMiRnaGene(@PathParam("mirnaId") String query) {
        try {
            parseQueryParams();
            MirnaDBAdaptor mirnaDBAdaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species, this.assembly);
            return generateResponse(query, mirnaDBAdaptor.getAllMiRnaGenesByMiRnaMatureList(Splitter.on(",").splitToList(query)));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getMiRnaGene", e.toString());
        }
    }

    @GET
    @Path("/{mirnaId}/target")
    public Response getMirnaTargets(@PathParam("mirnaId") String query, @DefaultValue("") @QueryParam("source") String source) {
        try {
            parseQueryParams();
            MirnaDBAdaptor adaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species, this.assembly);
            return generateResponse(query, adaptor.getAllMiRnaTargetsByMiRnaMatureList(Splitter.on(",").splitToList(query),
                    Splitter.on(",").splitToList(source)));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getMirnaTargets", e.toString());
        }
    }

    @GET
    @Path("/{mirnaId}/disease")
    public Response getMinaDisease(@PathParam("mirnaId") String query) {
        try {
            parseQueryParams();
            MirnaDBAdaptor mirnaDBAdaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species, this.assembly);
            return generateResponse(query, mirnaDBAdaptor.getAllMiRnaDiseasesByMiRnaMatureList(Splitter.on(",").splitToList(query)));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getMinaDisease", e.toString());
        }
    }

    @GET
    @Path("/annotation")
    public Response getAnnotation(@DefaultValue("") @QueryParam("source") String source) {
        try {
            parseQueryParams();
            MirnaDBAdaptor adaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species, this.assembly);

            List<?> results;
            if (source.equals("")) {
                results = adaptor.getAllAnnotation();
            } else {
                results = adaptor.getAllAnnotationBySourceList(Splitter.on(",").splitToList(source));
            }

            List<String> lista = new ArrayList<String>();
            if (results != null && results.size() > 0) {
                for (Object result : results) {
                    lista.add(((Object[]) result)[0].toString() + "\t" + ((Object[]) result)[1].toString());
                }
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
        sb.append("all id formats are accepted.\n\n\n");
        sb.append("Resources:\n");
        sb.append("- info: Get information about a miRNA mature: name, accession and sequence.\n");
        sb.append(" Output columns: miRBase accession, miRBase ID, sequence.\n\n");
        sb.append("- gene: Get the gene associated to this miRNA mature.\n");
        sb.append(" Output columns: Ensembl gene, external name, external name source, biotype, status, chromosome, start, end, strand, "
                + "source, description.\n\n");
        sb.append("- mirna_gene: Get the miRNA gene information associated to this miRNA mature.\n");
        sb.append(" Output columns: miRBase accession, miRBase ID, status, sequence, source.\n\n");
        sb.append("- target_gene: Get all genes that are regulated by this miRNA mature.\n");
        sb.append(" Output columns: Ensembl gene, external name, external name source, biotype, status, chromosome, start, end, strand, "
                + "source, description.\n\n");
        sb.append("- target: Get all binding sites associated to this miRNA.\n");
        sb.append(" Output columns: miRBase ID, gene target name, chromosome, start, end, strand, pubmed ID, source.\n\n");
        sb.append("- disease: Get all diseases related with this miRNA.\n");
        sb.append(" Output columns: miRBase ID, disease name, pubmed ID, description.\n\n\n");
        sb.append("Documentation:\n");
        sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Regulatory_rest_ws_api#MicroRNA-mature");

        return createOkResponse(sb.toString());
    }
}
