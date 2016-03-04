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

package org.opencb.cellbase.server.ws.genomic;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.opencb.biodata.models.core.Region;
import org.opencb.cellbase.core.api.*;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryResult;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Path("/{version}/{species}/genomic/region")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Region", description = "Region RESTful Web Services API")
public class RegionWSServer extends GenericRestWSServer {

    private int histogramIntervalSize = 200000;

    public RegionWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo,
                          @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = "Get the object data model")
    public Response getModel() {
        return createModelResponse(Region.class);
    }


    private String getHistogramParameter() {
        MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
        return (parameters.get("histogram") != null) ? parameters.get("histogram").get(0) : "false";
    }

    private int getHistogramIntervalSize() {
        MultivaluedMap<String, String> parameters = uriInfo.getQueryParameters();
        if (parameters.containsKey("interval")) {
            int value = this.histogramIntervalSize;
            try {
                value = Integer.parseInt(parameters.get("interval").get(0));
                logger.debug("Interval: " + value);
                return value;
            } catch (Exception exp) {
                exp.printStackTrace();
                /** malformed string y no se puede castear a int **/
                return value;
            }
        } else {
            return this.histogramIntervalSize;
        }
    }

    private boolean hasHistogramQueryParam() {
        return Boolean.parseBoolean(getHistogramParameter());
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("/gene")
    @ApiOperation(httpMethod = "POST", value = "Retrieves all the gene objects for the regions")
    public Response getGenesByRegionPost(@FormParam("region") String region) {
//                                     @DefaultValue("true") @QueryParam("transcript") String transcripts,
//                                     @DefaultValue("") @QueryParam("biotype") String biotype) {
        return getGenesByRegion(region, "true", "");
    }

    @POST
    @Path("/{chrRegionId}/gene")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the gene objects")
    public Response getGenesByRegionPost(@PathParam("chrRegionId") String chregionId,
                                         @DefaultValue("true") @QueryParam("transcript") String transcripts,
                                         @DefaultValue("") @QueryParam("biotype") String biotype) {
        return getGenesByRegion(chregionId, transcripts, biotype);
    }

    @GET
    @Path("/{chrRegionId}/gene")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the gene objects for the regions")
    public Response getGenesByRegion(@PathParam("chrRegionId") String region,
                                     @DefaultValue("true") @QueryParam("transcript") String transcripts,
                                     @DefaultValue("") @QueryParam("biotype") String biotype) {
        try {
            parseQueryParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory2.getGeneDBAdaptor(this.species, this.assembly);
            query.put(GeneDBAdaptor.QueryParams.REGION.key(), region);

            if (hasHistogramQueryParam()) {
                queryOptions.put("interval", getHistogramIntervalSize());
                QueryResult res = geneDBAdaptor.getIntervalFrequencies(query, histogramIntervalSize, queryOptions);
                return createOkResponse(res);
            } else {
                return createOkResponse(geneDBAdaptor.nativeGet(query, queryOptions));
            }
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


    @GET
    @Path("/{chrRegionId}/transcript")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the transcripts objects")
    public Response getTranscriptByRegion(@PathParam("chrRegionId") String region, @QueryParam("biotype") String biotype) {
        try {
            parseQueryParams();
            TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory2.getTranscriptDBAdaptor(this.species, this.assembly);
            query.put(TranscriptDBAdaptor.QueryParams.REGION.key(), region);
            return createOkResponse(transcriptDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


    @GET
    @Path("/{chrRegionId}/variation")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all SNP objects")
    public Response getVariationByRegion(@PathParam("chrRegionId") String region,
                                         @DefaultValue("") @QueryParam("consequence_type") String consequenceTypes) {
        try {
            parseQueryParams();
            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);

            List<Region> regions = Region.parseRegions(region);
            // remove regions bigger than 10Mb
            if (regions != null) {
                for (Region r : regions) {
                    if ((r.getEnd() - r.getStart()) > 10000000) {
                        return createErrorResponse("getSNpByRegion", "Regions must be smaller than 10Mb");
                    }
                }
            }

            query.put(VariantDBAdaptor.QueryParams.REGION.key(), region);

            if (hasHistogramQueryParam()) {
                queryOptions.put("interval", getHistogramIntervalSize());
                return createOkResponse(variationDBAdaptor.getIntervalFrequencies(query, histogramIntervalSize, queryOptions));
            } else {
                System.out.println("query = " + query.toJson());
                System.out.println("queryOptions = " + queryOptions.toJson());
                return createOkResponse(variationDBAdaptor.nativeGet(query, queryOptions));
            }
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Deprecated
    @Path("/{chrRegionId}/snp")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all SNP objects")
    public Response getSnpByRegion(@PathParam("chrRegionId") String region,
                                   @DefaultValue("") @QueryParam("consequence_type") String consequenceTypes,
                                   @DefaultValue("") @QueryParam("phenotype") String phenotype) {
        return getVariationByRegion(region, consequenceTypes);
//        try {
//            parseQueryParams();
//            VariantDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
//
//            List<Region> regions = Region.parseRegions(region);
//            // remove regions bigger than 10Mb
//            if (regions != null) {
//                for (Region r : regions) {
//                    if ((r.getEnd() - r.getStart()) > 10000000) {
//                        return createErrorResponse("getSNpByRegion", "Regions must be smaller than 10Mb");
//                    }
//                }
//            }
//
//            query.put(VariantDBAdaptor.QueryParams.REGION.key(), region);
//
//            if (hasHistogramQueryParam()) {
//                queryOptions.put("interval", getHistogramIntervalSize());
//                return createOkResponse(variationDBAdaptor.getIntervalFrequencies(query, histogramIntervalSize, queryOptions));
//            } else {
//                System.out.println("query = " + query.toJson());
//                System.out.println("queryOptions = " + queryOptions.toJson());
//                return createOkResponse(variationDBAdaptor.nativeGet(query, queryOptions));
//            }
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
    }


    @GET
    @Path("/{chrRegionId}/sequence")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the clinical variants")
    public Response getSequenceByRegion(@PathParam("chrRegionId") String region, @DefaultValue("1") @QueryParam("strand") String strand) {
        try {
            parseQueryParams();
            GenomeDBAdaptor genomeDBAdaptor = dbAdaptorFactory2.getGenomeDBAdaptor(this.species, this.assembly);

            if (region.contains(",")) {
                String[] regions = region.split(",");
                List<Query> queries = new ArrayList<>(regions.length);
                for (String s : regions) {
                    Query q = new Query("region", s);
                    q.put("strand", strand);
                    queries.add(q);
                }
                return createOkResponse(genomeDBAdaptor.getGenomicSequence(queries, queryOptions));
            } else {
                query.put(GenomeDBAdaptor.QueryParams.REGION.key(), region);
                query.put("strand", strand);
                return createOkResponse(genomeDBAdaptor.getGenomicSequence(query, queryOptions));
            }
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


    @GET
    @Path("/{chrRegionId}/clinical")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the clinical variants")
    public Response getClinicalByRegion(@PathParam("chrRegionId") String region,
                                        @DefaultValue("") @QueryParam("gene") String gene,
                                        @DefaultValue("") @QueryParam("id") String id,
                                        @DefaultValue("") @QueryParam("phenotype") String phenotype) {
        try {
            parseQueryParams();
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory2.getClinicalDBAdaptor(this.species, this.assembly);
            query.put(ClinicalDBAdaptor.QueryParams.REGION.key(), region);
//            List<Region> regions = Region.parseRegions(query);
            if (hasHistogramQueryParam()) {
                return null;
            } else {
//                if (gene != null && !gene.equals("")) {
//                    queryOptions.add("gene", Arrays.asList(gene.split(",")));
//                }
//                if (id != null && !id.equals("")) {
//                    queryOptions.add("id", Arrays.asList(id.split(",")));
//                }
//                if (phenotype != null && !phenotype.equals("")) {
//                    queryOptions.add("phenotype", Arrays.asList(phenotype.split(",")));
//                }


//                List<QueryResult> clinicalQueryResultList = clinicalDBAdaptor.getAllClinvarByRegionList(regions, queryOptions);
//                List<QueryResult> queryResultList = new ArrayList<>();
//                for(QueryResult clinicalQueryResult: clinicalQueryResultList) {
//                    QueryResult queryResult = new QueryResult();
//                    queryResult.setId(clinicalQueryResult.getId());
//                    queryResult.setDbTime(clinicalQueryResult.getDbTime());
//                    BasicDBList basicDBList = new BasicDBList();
//                    int numResults = 0;
//                    for (Document clinicalRecord : (List<Document>) clinicalQueryResult.getResult()) {
//                        if(clinicalRecord.containsKey("clinvarList")) {
//                            basicDBList.add(clinicalRecord);
//                            numResults += 1;
//                        }
//                    }
//                    queryResult.setResult(basicDBList);
//                    queryResult.setNumResults(numResults);
//                    queryResultList.add(queryResult);
//                }
//                return createOkResponse(queryResultList);
                return createOkResponse(clinicalDBAdaptor.nativeGet(query, queryOptions));
            }

        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    // TODO: modify the code below to use clinicalDBAdaptor rather than variationDBAdaptor
//    @GET
//    @Path("/{chrRegionId}/phenotype")
//    public Response getPhenotypeByRegion(@PathParam("chrRegionId") String query, @DefaultValue("") @QueryParam("source") String source) {
//        try {
//            parseQueryParams();
//            VariationDBAdaptor variationDBAdaptor = dbAdaptorFactory2.getVariationDBAdaptor(this.species, this.assembly);
//            List<Region> regions = Region.parseRegions(query);
//
//            if (hasHistogramQueryParam()) {
//                QueryResult queryResult = variationDBAdaptor.getAllIntervalFrequencies(regions.get(0), queryOptions);
//                return createOkResponse(queryResult);
//            } else {
//                if (source != null && !source.equals("")) {
//                    queryOptions.put("source", Splitter.on(",").splitToList(source));
//                }
//                List<QueryResult> queryResults = variationDBAdaptor.getAllPhenotypeByRegion(regions, queryOptions);
//                return createOkResponse(queryResults);
//            }
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

//    @GET
//    @Path("/{chrRegionId}/structural_variation")
//    public Response getStructuralVariationByRegion(@PathParam("chrRegionId") String query,
//                                                   @QueryParam("min_length") Integer minLength,
//                                                   @QueryParam("max_length") Integer maxLength) {
//        try {
//            parseQueryParams();
//            StructuralVariationDBAdaptor structuralVariationDBAdaptor = dbAdaptorFactory2
//                    .getStructuralVariationDBAdaptor(this.species, this.assembly);
//            List<Region> regions = Region.parseRegions(query);
//
//            if (hasHistogramQueryParam()) {
//                List<IntervalFeatureFrequency> intervalList = structuralVariationDBAdaptor.getAllIntervalFrequencies(
//                        regions.get(0), getHistogramIntervalSize());
//                return generateResponse(query, intervalList);
//            } else {
//                List<List<StructuralVariation>> structuralVariationList = null;
//                if (minLength == null && maxLength == null) {
//                    structuralVariationList = structuralVariationDBAdaptor.getAllByRegionList(regions);
//                } else {
//                    if (minLength == null) {
//                        minLength = 1;
//                    }
//                    if (maxLength == null) {
//                        maxLength = Integer.MAX_VALUE;
//                    }
//                    structuralVariationList = structuralVariationDBAdaptor.getAllByRegionList(regions, minLength,
//                            maxLength);
//                }
//                return this.generateResponse(query, "STRUCTURAL_VARIATION", structuralVariationList);
//            }
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

//    @GET
//    @Path("/{chrRegionId}/cytoband")
//    public Response getCytobandByRegion(@PathParam("chrRegionId") String chregionId) {
//        try {
//            parseQueryParams();
//            CytobandDBAdaptor cytobandDBAdaptor = dbAdaptorFactory2.getCytobandDBAdaptor(this.species, this.assembly);
//            List<Region> regions = Region.parseRegions(chregionId);
//            return generateResponse(chregionId, cytobandDBAdaptor.getAllByRegionList(regions));
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }


    @GET
    @Path("/{chrRegionId}/regulatory")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the regulatory elements")
    public Response getFeatureMap(@PathParam("chrRegionId") String region,
                                  @DefaultValue("") @QueryParam("type") String featureType,
                                  @DefaultValue("") @QueryParam("class") String featureClass,
                                  @DefaultValue("") @QueryParam("name") String name) {
        try {
            parseQueryParams();
            RegulationDBAdaptor regRegionDBAdaptor = dbAdaptorFactory2.getRegulationDBAdaptor(this.species, this.assembly);
            List<Query> queries = createQueries(region, RegulationDBAdaptor.QueryParams.REGION.key(),
                    RegulationDBAdaptor.QueryParams.FEATURE_TYPE.key(), featureType,
                    RegulationDBAdaptor.QueryParams.FEATURE_CLASS.key(), featureClass,
                    RegulationDBAdaptor.QueryParams.NAME.key(), name);
            List<QueryResult> queryResults = regRegionDBAdaptor.nativeGet(queries, queryOptions);
            return createOkResponse(queryResults);
//            query.put(RegulationDBAdaptor.QueryParams.REGION.key(), region);
//            query.put(RegulationDBAdaptor.QueryParams.FEATURE_TYPE.key(), featureType);
//            query.put(RegulationDBAdaptor.QueryParams.FEATURE_CLASS.key(), featureClass);
//            query.put(RegulationDBAdaptor.QueryParams.NAME.key(), name);
//            return createOkResponse(regRegionDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chrRegionId}/tfbs")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the TFBS")
    public Response getTfByRegion(@PathParam("chrRegionId") String regionId,
                                  @DefaultValue("") @QueryParam("name") String name) {
        try {
            parseQueryParams();
            RegulationDBAdaptor regulationDBAdaptor = dbAdaptorFactory2.getRegulationDBAdaptor(this.species, this.assembly);

            if (hasHistogramQueryParam()) {
                Query query = new Query();
                QueryResult intervalFrequencies =
                        regulationDBAdaptor.getIntervalFrequencies(query, getHistogramIntervalSize(), queryOptions);
                return createOkResponse(intervalFrequencies);
            } else {
                List<Query> queries = createQueries(regionId, RegulationDBAdaptor.QueryParams.REGION.key(),
                        RegulationDBAdaptor.QueryParams.FEATURE_TYPE.key(), "TF_binding_site_motif",
                        RegulationDBAdaptor.QueryParams.NAME.key(), name);
                List<QueryResult> queryResults = regulationDBAdaptor.nativeGet(queries, queryOptions);
                return createOkResponse(queryResults);
//                query.put(RegulationDBAdaptor.QueryParams.REGION.key(), regionId);
//                query.put("featureType", "TF_binding_site_motif");
//                query.put(RegulationDBAdaptor.QueryParams.NAME.key(), name);
//                return createOkResponse(regulationDBAdaptor.nativeGet(query, queryOptions));
            }
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

//    @GET
//    @Path("/{chrRegionId}/mirna_target")
//    public Response getMirnaTargetByRegion(@PathParam("chrRegionId") String query,
//                                           @DefaultValue("") @QueryParam("source") String source) {
//        try {
//            parseQueryParams();
//            MirnaDBAdaptor mirnaDBAdaptor = dbAdaptorFactory2.getMirnaDBAdaptor(this.species, this.assembly);
//            List<Region> regions = Region.parseRegions(query);
//
//            if (hasHistogramQueryParam()) {
//                System.out.println("PAKO:" + "si");
//                List<IntervalFeatureFrequency> intervalList = mirnaDBAdaptor.getAllMirnaTargetsIntervalFrequencies(
//                        regions.get(0), getHistogramIntervalSize());
//                return generateResponse(query, intervalList);
//            } else {
//                System.out.println("PAKO:" + "NO");
//                List<List<MirnaTarget>> mirnaTargetList = mirnaDBAdaptor.getAllMiRnaTargetsByRegionList(regions);
//                return this.generateResponse(query, "MIRNA_TARGET", mirnaTargetList);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return createErrorResponse("getMirnaTargetByRegion", e.toString());
//        }
//    }

//    @GET
//    @Path("/{chrRegionId}/cpg_island")
//    public Response getCpgIslandByRegion(@PathParam("chrRegionId") String query) {
//        try {
//            parseQueryParams();
//            CpGIslandDBAdaptor cpGIslandDBAdaptor = dbAdaptorFactory2.getCpGIslandDBAdaptor(this.species, this.assembly);
//            List<Region> regions = Region.parseRegions(query);
//
//            if (hasHistogramQueryParam()) {
//                List<IntervalFeatureFrequency> intervalList = cpGIslandDBAdaptor.getAllIntervalFrequencies(
//                        regions.get(0), getHistogramIntervalSize());
//                return generateResponse(query, intervalList);
//            } else {
//                List<List<CpGIsland>> cpGIslandList = cpGIslandDBAdaptor.getAllByRegionList(regions);
//                return this.generateResponse(query, cpGIslandList);
//            }
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

//    @GET
//    @Path("/{chrRegionId}/conserved_region")
//    @Deprecated
//    public Response getConservedRegionByRegion(@PathParam("chrRegionId") String query) {
//        try {
//            parseQueryParams();
//            List<Region> regions = Region.parseRegions(query);
//            ConservedRegionDBAdaptor conservedRegionDBAdaptor = dbAdaptorFactory2
// .getConservedRegionDBAdaptor(this.species, this.assembly);
//            return createOkResponse(conservedRegionDBAdaptor.getAllByRegionList(regions, queryOptions));
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }

    @GET
    @Path("/{chrRegionId}/conservation")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the conservation scores")
    public Response conservation(@PathParam("chrRegionId") String region) {
        try {
            parseQueryParams();
            GenomeDBAdaptor conservationDBAdaptor = dbAdaptorFactory2.getGenomeDBAdaptor(this.species, this.assembly);
            return createOkResponse(conservationDBAdaptor.getConservation(Region.parseRegions(region), queryOptions));
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
        sb.append("Chr. region format: chr:start-end (i.e.: 7:245000-501560)\n\n\n");
        sb.append("Resources:\n");
        sb.append("- gene: This resource obtain the genes belonging to one of the regions specified.\n");
        sb.append(" Output columns: Ensembl ID, external name, external name source, biotype, status, chromosome, start, end, strand, "
                + "source, description.\n\n");
        sb.append("- transcript: This resource obtain the transcripts belonging to one of the regions specified.\n");
        sb.append(" Output columns: Ensembl ID, external name, external name source, biotype, status, chromosome, start, end, strand, "
                + "coding region start, coding region end, cdna coding start, cdna coding end, description.\n\n");
        sb.append("- snp: To obtain the SNPs belonging to one of the regions specified write snp\n");
        sb.append(" Output columns: rsID, chromosome, position, Ensembl consequence type, SO consequence type, sequence.\n\n");
        sb.append("- sequence: To obtain the genomic sequence of one region write sequence as resource\n\n");
        sb.append("- tfbs: To obtain the TFBS of one region write sequence as resource\n");
        sb.append(" Output columns: TF name, target gene name, chromosome, start, end, cell type, sequence, score.\n\n");
        sb.append("- regulatory: To obtain the regulatory elements of one region write sequence as resource\n");
        sb.append(" Output columns: name, type, chromosome, start, end, cell type, source.\n\n\n");
        sb.append("Documentation:\n");
        sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Genomic_rest_ws_api#Region");

        return createOkResponse(sb.toString());
    }

}
