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

import com.google.common.base.Splitter;
import io.swagger.annotations.*;
import org.bson.Document;
import org.opencb.biodata.formats.protein.uniprot.v201504jaxb.Entry;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.RegulatoryFeature;
import org.opencb.biodata.models.core.Transcript;
import org.opencb.biodata.models.core.TranscriptTfbs;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.*;
import org.opencb.cellbase.core.db.api.regulatory.MirnaDBAdaptor;
import org.opencb.cellbase.core.db.api.systems.ProteinProteinInteractionDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryResult;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.*;

/**
 * @author imedina
 */
@Path("/{version}/{species}/feature/gene")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Gene", description = "Gene RESTful Web Services API")
public class GeneWSServer extends GenericRestWSServer {


    public GeneWSServer(@PathParam("version")
                        @ApiParam(name = "version", value = "Use 'latest' for last stable version",
                                defaultValue = "latest") String version,
                        @PathParam("species")
                        @ApiParam(name = "species", value = "Name of the species, e.g.: hsapiens. For a full list "
                                + "of potentially available species ids, please refer to: "
                                + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/latest/meta/species") String species,
                        @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = "Get JSON specification of gene data model", response = Map.class,
        responseContainer = "QueryResponse")
    public Response getModel() {
        return createModelResponse(Gene.class);
    }

    @GET
    @Path("/first")
    @Override
    @ApiOperation(httpMethod = "GET", value = "Get the first object in the database", response = Gene.class,
            responseContainer = "QueryResponse")
    public Response first() {
        GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory2.getGeneDBAdaptor(this.species, this.assembly);
        return createOkResponse(geneDBAdaptor.first(queryOptions));
    }

    @GET
    @Path("/count")
    @ApiOperation(httpMethod = "GET", value = "Get the number of genes in the database", response = Integer.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "id",
                    value = "Comma separated list of ENSEMBL gene ids, e.g.: ENST00000380152,ENSG00000155657."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "name",
                    value = "Comma separated list of gene HGNC names, e.g.: BRCA2,TTN,MUC4."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "biotype",
                    value = "Comma separated list of gene gencode biotypes, e.g.: protein_coding,miRNA,lincRNA."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype",
                    value = "Comma separated list of transcript gencode biotypes, e.g.: protein_coding,miRNA,lincRNA."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.xrefs",
                    value = "Comma separated list transcript xrefs ids, e.g.: ENSG00000145113,35912_at,GO:0002020."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
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
            @ApiImplicitParam(name = "annotation.expression.gene",
                    value = "Comma separated list of ENSEMBL gene ids for which expression values are available, "
                            + "e.g.: ENSG00000139618,ENSG00000155657. Exact text matches will be returned",
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
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.gene",
                    value = "Comma separated list of gene names for which drug data is available, "
                            + "e.g.: BRCA2,TTN."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
    })
    public Response count() {
//    public Response count(@DefaultValue("") @QueryParam("region") String region,
//                          @DefaultValue("") @QueryParam("biotype") String biotype,
//                          @DefaultValue("") @QueryParam("xrefs") String xrefs) {
        try {
            parseQueryParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory2.getGeneDBAdaptor(this.species, this.assembly);
//            query.put(GeneDBAdaptor.QueryParams.REGION.key(), region);
//            query.put(GeneDBAdaptor.QueryParams.BIOTYPE.key(), biotype);
//            query.put(GeneDBAdaptor.QueryParams.XREFS.key(), xrefs);
            return createOkResponse(geneDBAdaptor.count(query));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


    @GET
    @Path("/stats")
    @Override
    @ApiOperation(httpMethod = "GET", value = "Not yet implemented ", response = Integer.class,
            responseContainer = "QueryResponse", hidden = true)
    public Response stats() {
        return super.stats();
    }

    @GET
    @Path("/group")
    @ApiOperation(httpMethod = "GET", value = "Groups gene HGNC symbols by a field(s). ", response = Integer.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "id",
                    value = "Comma separated list of ENSEMBL gene ids, e.g.: ENST00000380152,ENSG00000155657."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "name",
                    value = "Comma separated list of gene HGNC names, e.g.: BRCA2,TTN,MUC4."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "biotype",
                    value = "Comma separated list of gene gencode biotypes, e.g.: protein_coding,miRNA,lincRNA."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype",
                    value = "Comma separated list of transcript gencode biotypes, e.g.: protein_coding,miRNA,lincRNA."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.xrefs",
                    value = "Comma separated list transcript xrefs ids, e.g.: ENSG00000145113,35912_at,GO:0002020."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
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
            @ApiImplicitParam(name = "annotation.expression.gene",
                    value = "Comma separated list of ENSEMBL gene ids for which expression values are available, "
                            + "e.g.: ENSG00000139618,ENSG00000155657. Exact text matches will be returned",
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
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.gene",
                    value = "Comma separated list of gene names for which drug data is available, "
                            + "e.g.: BRCA2,TTN."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
    })
    public Response groupBy(@DefaultValue("")
                            @QueryParam("fields")
                            @ApiParam(name = "fields",
                                    value = "Comma separated list of field(s) to group by, e.g.: biotype.",
                                    required = true) String fields) {
        try {
            parseQueryParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory2.getGeneDBAdaptor(this.species, this.assembly);
            return createOkResponse(geneDBAdaptor.groupBy(query, Arrays.asList(fields.split(",")), queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/all")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time.",
        value = "Retrieves all gene objects", response = Gene.class,
        responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "id",
                    value = "Comma separated list of ENSEMBL gene ids, e.g.: ENST00000380152,ENSG00000155657."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "name",
                    value = "Comma separated list of gene HGNC names, e.g.: BRCA2,TTN,MUC4."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "biotype",
                    value = "Comma separated list of gene gencode biotypes, e.g.: protein_coding,miRNA,lincRNA."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype",
                    value = "Comma separated list of transcript gencode biotypes, e.g.: protein_coding,miRNA,lincRNA."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.xrefs",
                    value = "Comma separated list transcript xrefs ids, e.g.: ENSG00000145113,35912_at,GO:0002020."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
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
            @ApiImplicitParam(name = "annotation.expression.gene",
                    value = "Comma separated list of ENSEMBL gene ids for which expression values are available, "
                            + "e.g.: ENSG00000139618,ENSG00000155657. Exact text matches will be returned",
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
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.gene",
                    value = "Comma separated list of gene names for which drug data is available, "
                            + "e.g.: BRCA2,TTN."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query")
    })
    public Response getAll() {
//    public Response getAll(@ApiParam(value = "String with the list of biotypes to return")
//                           @DefaultValue("") @QueryParam("biotype") String biotype) {
        try {
            parseQueryParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory2.getGeneDBAdaptor(this.species, this.assembly);
            return createOkResponse(geneDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/list")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the gene Ensembl IDs", response = List.class,
        responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "name",
                    value = "Comma separated list of gene HGNC names, e.g.: BRCA2,TTN,MUC4."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "biotype",
                    value = "Comma separated list of gene gencode biotypes, e.g.: protein_coding,miRNA,lincRNA."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype",
                    value = "Comma separated list of transcript gencode biotypes, e.g.: protein_coding,miRNA,lincRNA."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.xrefs",
                    value = "Comma separated list transcript xrefs ids, e.g.: ENSG00000145113,35912_at,GO:0002020."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
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
            @ApiImplicitParam(name = "annotation.expression.gene",
                    value = "Comma separated list of ENSEMBL gene ids for which expression values are available, "
                            + "e.g.: ENSG00000139618,ENSG00000155657. Exact text matches will be returned",
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
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.gene",
                    value = "Comma separated list of gene names for which drug data is available, "
                            + "e.g.: BRCA2,TTN."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
    })
    public Response getAllIDs() {
//    public Response getAllIDs(@ApiParam(value = "String with the list of biotypes to return. Not currently used.")
//                              @DefaultValue("") @QueryParam("biotype") String biotype) {
        try {
            parseQueryParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory2.getGeneDBAdaptor(this.species, this.assembly);
            queryOptions.put("include", Collections.singletonList("id"));
            return createOkResponse(geneDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{geneId}/info")
    @ApiOperation(httpMethod = "GET", value = "Get information about the specified gene(s)", response = Gene.class,
        responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "biotype",
                    value = "Comma separated list of gene gencode biotypes, e.g.: protein_coding,miRNA,lincRNA."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype",
                    value = "Comma separated list of transcript gencode biotypes, e.g.: protein_coding,miRNA,lincRNA."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
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
            @ApiImplicitParam(name = "annotation.expression.gene",
                    value = "Comma separated list of ENSEMBL gene ids for which expression values are available, "
                            + "e.g.: ENSG00000139618,ENSG00000155657. Exact text matches will be returned",
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
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.gene",
                    value = "Comma separated list of gene names for which drug data is available, "
                            + "e.g.: BRCA2,TTN."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
    })
    public Response getByEnsemblId(@PathParam("geneId")
                                   @ApiParam(name = "geneId",
                                           value = "Comma separated list gene/transcript xrefs ids, e.g.: "
                                                   + " ENSG00000145113,35912_at,GO:0002020."
                                                   + " Exact text matches will be returned",
                                           required = true)
                                   String geneId) {
        try {
            parseQueryParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory2.getGeneDBAdaptor(this.species, this.assembly);
//            query.put(GeneDBAdaptor.QueryParams.XREFS.key(), geneId);
            List<Query> queries = createQueries(geneId, GeneDBAdaptor.QueryParams.XREFS.key());
            List<QueryResult> queryResults = geneDBAdaptor.nativeGet(queries, queryOptions);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{geneId}/next")
    @ApiOperation(httpMethod = "GET", value = "Get information about the specified gene(s) - Not yet implemented",
        hidden = true)
    public Response getNextByEnsemblId(@PathParam("geneId") String geneId) {
        try {
            parseQueryParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory2.getGeneDBAdaptor(this.species, this.assembly);
            QueryResult genes = geneDBAdaptor.next(query, queryOptions);
            return createOkResponse(genes);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{geneId}/transcript")
    @ApiOperation(httpMethod = "GET", value = "Get the transcripts of a list of gene IDs", response = Transcript.class,
        responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "transcripts.region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype",
                    value = "Comma separated list of gene gencode biotypes, e.g.: protein_coding,miRNA,lincRNA."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.name",
                    value = "Comma separated list of TFBS names, e.g.: CTCF,Gabp."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.xrefs",
                    value = "Comma separated list transcript xrefs ids, e.g.: ENSG00000145113,35912_at,GO:0002020."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query")
    })
    public Response getTranscriptsByGeneId(@PathParam("geneId")
                                           @ApiParam(name = "geneId",
                                                   value = "Comma separated list gene xrefs ids, e.g.: "
                                                           + " ENSG00000145113,35912_at,GO:0002020."
                                                           + " Exact text matches will be returned",
                                                   required = true) String geneId) {
        try {
            parseQueryParams();
            TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory2.getTranscriptDBAdaptor(this.species, this.assembly);
            query.put(TranscriptDBAdaptor.QueryParams.XREFS.key(), geneId);
            return createOkResponse(transcriptDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/biotype")
    @ApiOperation(httpMethod = "GET", value = "Get the list of existing biotypes")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "id",
                    value = "Comma separated list of ENSEMBL gene ids, e.g.: ENST00000380152,ENSG00000155657."
                            + "Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "name",
                    value = "Comma separated list of gene HGNC names, e.g.: BRCA2,TTN,MUC4"
                            + "Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.xrefs",
                    value = "Comma separated list transcript xrefs ids, e.g.: ENSG00000145113,35912_at,GO:0002020."
                            + "Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.id",
                    value = "Comma separated list of ENSEMBL transcript ids, e.g.: ENST00000342992,ENST00000380152,"
                            + "ENST00000544455. Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.name",
                    value = "Comma separated list of transcript names, e.g.: BRCA2-201,TTN-003"
                            + "Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.name",
                    value = "Comma separated list of TFBS names, e.g.: CTCF,Gabp"
                            + "Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.id",
                    value = "Comma separated list of phenotype ids (OMIM, UMLS), e.g.: umls:C0030297,OMIM:613390,OMIM:613390"
                            + "Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.name",
                    value = "Comma separated list of phenotypes, e.g.: Cryptorchidism,Absent thumb,Stage 5 chronic kidney disease"
                            + "Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.gene",
                    value = "Comma separated list of ENSEMBL gene ids for which expression values are available, "
                            + "e.g.: ENSG00000139618,ENSG00000155657"
                            + "Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.tissue",
                    value = "Comma separated list of tissues for which expression values are available, "
                            + "e.g.: adipose tissue,heart atrium,tongue"
                            + "Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.name",
                    value = "Comma separated list of drug names, "
                            + "e.g.: BMN673,OLAPARIB,VELIPARIB"
                            + "Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.gene",
                    value = "Comma separated list of gene names for which drug data is available, "
                            + "e.g.: BRCA2,TTN"
                            + "Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
    })
    public Response getAllBiotypes() {
        try {
            parseQueryParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory2.getGeneDBAdaptor(this.species, this.assembly);
            return createOkResponse(geneDBAdaptor.distinct(query, "biotype"));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{geneId}/snp")
    @ApiOperation(httpMethod = "GET", value = "Get all SNPs within the specified genes", response = Variant.class,
        responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "reference",
                    value = "Comma separated list of possible reference to be queried, e.g.: A,T",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "alternate",
                    value = "Comma separated list of possible alternate to be queried, e.g.: A,T",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "consequenceType",
                    value = "Comma separated list of possible SO names describing consequence types to be queried, "
                            + " e.g.: missense_variant,downstream_variant. Exact text matches will be retrieved.",
                    required = false, dataType = "list of strings", paramType = "query"),
    })
//    @ApiOperation(httpMethod = "GET", value = "Get all SNPs within the specified genes and offset")
    public Response getSNPByGeneId(@PathParam("geneId") String geneId) {
//    public Response getSNPByGeneId(@PathParam("geneId") String geneId, @DefaultValue("5000") @QueryParam("offset") int offset) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            query.put("gene", geneId);
//            queryOptions.put("offset", offset);
            QueryResult queryResult = variationDBAdaptor.nativeGet(query, queryOptions);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


//    @GET
//    @Path("/{geneId}/mutation")
////    @ApiOperation(httpMethod = "GET", value = "[DEPRECATED] Get all variants within the specified gene(s)")
//    @Deprecated
//    public Response getMutationByGene(@PathParam("geneId") String query) {
//        try {
//            parseQueryParams();
//            MutationDBAdaptor mutationAdaptor = dbAdaptorFactory.getMutationDBAdaptor(this.species, this.assembly);
//            List<QueryResult> queryResults = mutationAdaptor.getAllByGeneNameList(Splitter.on(",").splitToList(query), queryOptions);
////            return generateResponse(query, "MUTATION", queryResults);
//            return createOkResponse(queryResults);
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

    @GET
    @Path("/{geneId}/regulation")
    @ApiOperation(httpMethod = "GET", value = "Get all transcription factor binding sites for this gene(s) - Not yet implemented",
            response = RegulatoryFeature.class, responseContainer = "QueryResponse", hidden = true)
    public Response getAllRegulatoryElements(@PathParam("geneId")
                                             @ApiParam(name = "geneId",
                                                     value = "Comma separated list of ENSEMBL gene ids, e.g.: "
                                                             + "ENSG00000237683,ENSG00000243485,ENSG00000269981."
                                                             + " Exact text matches will be returned",
                                                     required = true) String geneId,
                                             @DefaultValue("false")
                                             @QueryParam("merge")
                                             @ApiParam(name = "merge",
                                                     value = "Return one TFBs per QueryResult or all of them merged"
                                                             + " into the same QueryResult object.",
                                                     defaultValue = "false", required = true) boolean merge) {
        try {
            parseQueryParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory2.getGeneDBAdaptor(this.species, this.assembly);
            if (merge) {
                query.put(GeneDBAdaptor.QueryParams.ID.key(), geneId);
                QueryResult queryResult = geneDBAdaptor.getRegulatoryElements(query, queryOptions);
                return createOkResponse(queryResult);
            } else {
                String[] genes = geneId.split(",");
                List<QueryResult> queryResults = new ArrayList<>(genes.length);
                for (String gene : genes) {
                    query.put(GeneDBAdaptor.QueryParams.ID.key(), gene);
                    QueryResult queryResult = geneDBAdaptor.getRegulatoryElements(query, queryOptions);
                    queryResults.add(queryResult);
                }
                return createOkResponse(queryResults);
            }

        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{geneId}/tfbs")
    @ApiOperation(httpMethod = "GET", value = "Get all transcription factor binding sites for this gene(s)",
        response = TranscriptTfbs.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "biotype",
                    value = "Comma separated list of gene gencode biotypes, e.g.: protein_coding,miRNA,lincRNA."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype",
                    value = "Comma separated list of transcript gencode biotypes, e.g.: protein_coding,miRNA,lincRNA."
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
            @ApiImplicitParam(name = "annotation.expression.gene",
                    value = "Comma separated list of ENSEMBL gene ids for which expression values are available, "
                            + "e.g.: ENSG00000139618,ENSG00000155657. Exact text matches will be returned",
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
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "annotation.drugs.gene",
                    value = "Comma separated list of gene names for which drug data is available, "
                            + "e.g.: BRCA2,TTN."
                            + " Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
    })
    public Response getAllTfbs(@PathParam("geneId")
                               @ApiParam(name = "geneId",
                                       value = "Comma separated list of ENSEMBL gene ids, e.g.: ENST00000380152,"
                                               + "ENSG00000155657."
                                               + " Exact text matches will be returned",
                                       required = true) String geneId,
                               @DefaultValue("false")
                               @QueryParam("merge")
                               @ApiParam(name = "merge",
                                       value = "Return one TFBs per QueryResult or all of them merged"
                                               + " into the same QueryResult object.",
                                       defaultValue = "false", required = true) boolean merge) {
        try {
            parseQueryParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory2.getGeneDBAdaptor(this.species, this.assembly);
            if (merge) {
                query.put(GeneDBAdaptor.QueryParams.XREFS.key(), geneId);
                QueryResult queryResult = geneDBAdaptor.getTfbs(query, queryOptions);
                return createOkResponse(queryResult);
            } else {
                String[] genes = geneId.split(",");
                List<QueryResult> queryResults = new ArrayList<>(genes.length);
                for (String gene : genes) {
                    query.put(GeneDBAdaptor.QueryParams.XREFS.key(), gene);
                    QueryResult queryResult = geneDBAdaptor.getTfbs(query, queryOptions);
                    queryResults.add(queryResult);
                }
                return createOkResponse(queryResults);
            }
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


    @GET
    @Path("/{geneId}/mirna_target")
    @ApiOperation(httpMethod = "GET", value = "Get all microRNAs binding sites for this gene(s). Not yet implemented",
            hidden = true)
    public Response getAllMirna(@PathParam("geneId") String geneId) {
        try {
            parseQueryParams();
            MirnaDBAdaptor mirnaDBAdaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species, this.assembly);
            return createOkResponse(mirnaDBAdaptor.getAllMiRnaTargetsByGeneNameList(Splitter.on(",").splitToList(geneId)));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


    @GET
    @Path("/{geneId}/protein")
    @ApiOperation(httpMethod = "GET", value = "Return info of the corresponding proteins", response = Entry.class,
        responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "keyword",
                    value = "Comma separated list of keywords that may be associated with the protein(s), e.g.: "
                            + "Transcription,Zinc. Exact text matches will be returned",
                    required = false, dataType = "list of strings", paramType = "query"),
    })
    public Response getProteinById(@PathParam("geneId")
                                   @ApiParam(name = "xrefs",
                                           value = "Comma separated list of gene ids, e.g.: ENSG00000268020,BRCA2"
                                                   + "Exact text matches will be returned",
                                           required = true) String geneId) {
        try {
            parseQueryParams();
            ProteinDBAdaptor proteinDBAdaptor = dbAdaptorFactory2.getProteinDBAdaptor(this.species, this.assembly);
            List<Query> queries = createQueries(geneId, ProteinDBAdaptor.QueryParams.XREFS.key());
            return createOkResponse(proteinDBAdaptor.nativeGet(queries, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{geneId}/ppi")
    @ApiOperation(httpMethod = "GET", value = "Get the protein-protein interactions in which this gene is involved -"
            + " - Not yet implemented", hidden = true)
    public Response getPPIByEnsemblId(@PathParam("geneId") String query) {
        try {
            parseQueryParams();
            ProteinProteinInteractionDBAdaptor ppiDBAdaptor =
                    dbAdaptorFactory.getProteinProteinInteractionDBAdaptor(this.species, this.assembly);
            return createOkResponse(ppiDBAdaptor.getAllByInteractorIdList(Splitter.on(",").splitToList(query), queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


    @GET
    @Path("/{geneId}/clinical")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time. "
            + "Please note that ClinVar, COSMIC or GWAS objects may be returned as stored in the database. Please have "
            + "a look at "
            + "https://github.com/opencb/cellbase/wiki/MongoDB-implementation#clinical for further details.",
            value = "Resource to get clinical variants from a list of gene HGNC symbols", response = Document.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "so",
                    value = "Comma separated list of sequence ontology term names, e.g.: missense_variant. Exact text "
                            + "matches will be returned.",
                    required = false, dataType = "list of strings", paramType = "query"),
            @ApiImplicitParam(name = "phenotype",
                    value = "String to indicate the phenotypes to query. A text search will be run.",
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
    public Response getAllClinvarByGene(@PathParam("geneId") String geneId) {
        try {
            parseQueryParams();
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory2.getClinicalDBAdaptor(this.species, this.assembly);
            query.put("gene", geneId);
            return createOkResponse(clinicalDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
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
        sb.append("- info: Get gene information: name, position, biotype.\n");
        sb.append(" Output columns: Ensembl gene, external name, external name source, biotype, status, chromosome, start, end, strand, "
                + "source, description.\n\n");
        sb.append("- transcript: Get all transcripts for this gene.\n");
        sb.append(" Output columns: Ensembl ID, external name, external name source, biotype, status, chromosome, start, end, strand, "
                + "coding region start, coding region end, cdna coding start, cdna coding end, description.\n\n");
        sb.append("- tfbs: Get transcription factor binding sites (TFBSs) that map to the promoter region of this gene.\n");
        sb.append(" Output columns: TF name, target gene name, chromosome, start, end, cell type, sequence, score.\n\n");
        sb.append("- mirna_target: Get all microRNA target sites for this gene.\n");
        sb.append(" Output columns: miRBase ID, gene target name, chromosome, start, end, strand, pubmed ID, source.\n\n");
        sb.append("- protein_feature: Get protein information related to this gene.\n");
        sb.append(" Output columns: feature type, aa start, aa end, original, variation, identifier, description.\n\n\n");
        sb.append("Documentation:\n");
        sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Feature_rest_ws_api#Gene");

        return createOkResponse(sb.toString());
    }

}
