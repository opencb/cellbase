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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.opencb.biodata.models.variation.Variation;
import org.opencb.cellbase.core.api.VariantDBAdaptor;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryResult;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author imedina
 */
@Path("/{version}/{species}/feature/snp")
@Produces("application/json")
@Api(value = "SNP", description = "SNP RESTful Web Services API")
public class SnpWSServer extends GenericRestWSServer {


    public SnpWSServer(@PathParam("version") String version, @PathParam("species") String species,
                       @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = "Get the object data model")
    public Response getModel() {
        return createModelResponse(Variation.class);
    }

    @GET
    @Path("/first")
    @Override
    @ApiOperation(httpMethod = "GET", value = "Get the first object in the database")
    public Response first() {
        VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
        return createOkResponse(variationDBAdaptor.first());
    }

    @GET
    @Path("/count")
    public Response count(@DefaultValue("") @QueryParam("region") String region) {
        VariantDBAdaptor variationDBAdaptor1 = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
        query.append(VariantDBAdaptor.QueryParams.REGION.key(), region);
        return createOkResponse(variationDBAdaptor1.count(query));
    }

    @GET
    @Path("/stats")
    @Override
    public Response stats() {
        return super.stats();
    }


    @GET
    @Path("/{snpId}/info")
    @ApiOperation(httpMethod = "GET", value = "Resource to get information about a (list of) SNPs")
    public Response getByEnsemblId(@PathParam("snpId") String id) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            String[] ids = id.split(",");
            List<Query> queries = new ArrayList<>(ids.length);
            for (String s : ids) {
                queries.add(new Query(VariantDBAdaptor.QueryParams.ID.key(), s));
            }
            return createOkResponse(variationDBAdaptor.nativeGet(queries, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{snpId}/next")
    @ApiOperation(httpMethod = "GET", value = "Get information about the next SNP")
    public Response getNextById(@PathParam("snpId") String id) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            query.put(VariantDBAdaptor.QueryParams.ID.key(), id.split(",")[0]);
            return createOkResponse(variationDBAdaptor.next(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/consequence_types")
    public Response getAllConsequenceTypes() {
        try {
            parseQueryParams();
//            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
//            query.put(VariantDBAdaptor.QueryParams.REGION.key(), "22:1-50000000");
//            return createOkResponse(variationDBAdaptor.distinct(query, "displayConsequenceType"));

            List<String> consequenceTypes = VariantAnnotationUtils.SO_SEVERITY.keySet().stream()
                    .sorted()
                    .collect(Collectors.toList());
            QueryResult<String> queryResult = new QueryResult<>("consequence_types");
            queryResult.setNumResults(consequenceTypes.size());
            queryResult.setResult(consequenceTypes);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    // TODO: phenotype queries to be solved by the clinical WS
//    @GET
//    @Path("/phenotypes")
//    @Deprecated
//    public Response getAllPhenotypes(@QueryParam("phenotype") String phenotype) {
//        try {
//            parseQueryParams();
//            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
//
//            queryOptions.put("phenotype", phenotype);
//
//            return createOkResponse(variationDBAdaptor.getAllPhenotypes(queryOptions));
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

    @GET
    @Path("/{snpId}/consequence_type")
    @ApiOperation(httpMethod = "GET", value = "Get the biological impact of the SNP(s)")
    public Response getConsequenceTypeByGetMethod(@PathParam("snpId") String snpId) {
        return getConsequenceType(snpId);
    }

    @POST
    @Path("/consequence_type")
    @ApiOperation(httpMethod = "POST", value = "Get the biological impact of the SNP(s)")
    public Response getConsequenceTypeByPostMethod(@QueryParam("id") String snpId) {
        return getConsequenceType(snpId);
    }

    private Response getConsequenceType(String snpId) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            return generateResponse(snpId, "SNP_CONSEQUENCE_TYPE", Arrays.asList(""));
        } catch (Exception e) {
            return createErrorResponse("getConsequenceTypeByPostMethod", e.toString());
        }
    }


    @GET
    @Path("/{snpId}/regulatory")
    @ApiOperation(httpMethod = "GET", value = "Get the regulatory impact of the SNP(s)")
    public Response getRegulatoryByGetMethod(@PathParam("snpId") String snpId) {
        return getRegulatoryType(snpId);
    }

    @POST
    @Path("/regulatory")
    @ApiOperation(httpMethod = "POST", value = "Get the regulatory impact of the SNP(s)")
    public Response getRegulatoryTypeByPostMethod(@QueryParam("id") String snpId) {
        return getRegulatoryType(snpId);
    }

    private Response getRegulatoryType(String snpId) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            return null;
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


    @GET
    @Path("/{snpId}/phenotype")
    @ApiOperation(httpMethod = "GET", value = "Retrieve known phenotypes associated with the SNP(s)")
    public Response getSnpPhenotypesByNameByGet(@PathParam("snpId") String snps) {
        return getSnpPhenotypesByName(snps, outputFormat);
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("/phenotype")
    public Response getSnpPhenotypesByNameByPost(@FormParam("of") String outputFormat, @FormParam("snps") String snps) {
        return getSnpPhenotypesByName(snps, outputFormat);
    }

    public Response getSnpPhenotypesByName(String snps, String outputFormat) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            return createOkResponse("Mongo TODO");
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{snpId}/sequence")
    @ApiOperation(httpMethod = "GET", value = "Get the adjacent sequence to the SNP(s)")
    public Response getSequence(@PathParam("snpId") String query) {
        try {
            return null;
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


    @GET
    @Path("/{snpId}/population_frequency")
    @ApiOperation(httpMethod = "GET", value = "Get the frequencies in the population for the SNP(s)")
    public Response getPopulationFrequency(@PathParam("snpId") String snpId) {
        try {
            parseQueryParams();
//            SnpDBAdaptor snpDBAdaptor = dbAdaptorFactory.getSnpDBAdaptor(species, version);
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            return generateResponse(snpId, "SNP_POPULATION_FREQUENCY", Arrays.asList(""));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{snpId}/xref")
    @ApiOperation(httpMethod = "GET", value = "Retrieve all external references for the SNP(s)")
    public Response getXrefs(@PathParam("snpId") String query) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            return null;
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{snpId}/starts_with")
    @ApiOperation(httpMethod = "GET", value = "Get the genes that match the beginning of the given string")
    public Response getByLikeQuery(@PathParam("snpId") String id) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
            return createOkResponse(variationDBAdaptor.startsWith(id, queryOptions));
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
        sb.append("SNP format: rsID.\n\n\n");
        sb.append("Resources:\n");
        sb.append("- info: Get SNP information: name, position, consequence type, adjacent nucleotides, ...\n");
        sb.append(" Output columns: rsID, chromosome, position, Ensembl consequence type, SO consequence type, sequence.\n\n");
        sb.append("- consequence_type: Get SNP effect on the transcript\n");
        sb.append(" Output columns: chromosome, start, end, feature ID, feature name, consequence type, biotype, feature chromosome, "
                + "feature start, feature end, feature strand, snp ID, ancestral allele, alternative allele, gene Ensembl ID, "
                + "Ensembl transcript ID, gene name, SO consequence type ID, SO consequence type name, consequence type description, "
                + "consequence type category, aminoacid change, codon change.\n\n");
        sb.append("- population_frequency: Get the allelic and genotypic frequencies for this SNP acroos populations.\n\n");
        sb.append("- phenotype: Get the phenotypes that have been previously associated to this SNP.\n\n");
        sb.append("- xref: Get the external references for this SNP.\n\n\n");
        sb.append("Documentation:\n");
        sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Feature_rest_ws_api#SNP");

        return createOkResponse(sb.toString());
    }

}
