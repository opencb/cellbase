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

import com.google.common.base.Splitter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.opencb.biodata.models.core.CpGIsland;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variation.StructuralVariation;
import org.opencb.cellbase.core.api.ConservationDBAdaptor;
import org.opencb.cellbase.core.common.IntervalFeatureFrequency;
import org.opencb.cellbase.core.db.api.CpGIslandDBAdaptor;
import org.opencb.cellbase.core.db.api.CytobandDBAdaptor;
import org.opencb.cellbase.core.db.api.core.*;
import org.opencb.cellbase.core.db.api.regulatory.RegulatoryRegionDBAdaptor;
import org.opencb.cellbase.core.db.api.regulatory.TfbsDBAdaptor;
import org.opencb.cellbase.core.db.api.variation.ClinicalDBAdaptor;
import org.opencb.cellbase.core.db.api.variation.MutationDBAdaptor;
import org.opencb.cellbase.core.db.api.variation.StructuralVariationDBAdaptor;
import org.opencb.cellbase.core.db.api.variation.VariationDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryResult;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Path("/{version}/{species}/genomic/region")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Region", description = "Region RESTful Web Services API")
public class RegionWSServer extends GenericRestWSServer {
    // private int histogramIntervalSize = 1000000;
    private int histogramIntervalSize = 200000;

//    private List<String> exclude = new ArrayList<>();

    public RegionWSServer(@PathParam("version") String version, @PathParam("species") String species,
                          @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
//        this.exclude = Arrays.asList(exclude.trim().split(","));
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = "Get the object data model")
    public Response getModel() {
        return createModelResponse(Region.class);
    }

    // private RegulatoryRegionDBAdaptor regulatoryRegionDBAdaptor =
    // dbAdaptorFactory.getRegulatoryRegionDBAdaptor(this.species);
    // private MutationDBAdaptor mutationDBAdaptor =
    // dbAdaptorFactory.getMutationDBAdaptor(this.species);
    // private CpGIslandDBAdaptor cpGIslandDBAdaptor =
    // dbAdaptorFactory.getCpGIslandDBAdaptor(this.species);
    // private StructuralVariationDBAdaptor structuralVariationDBAdaptor =
    // dbAdaptorFactory.getStructuralVariationDBAdaptor(this.species);
    // private MirnaDBAdaptor mirnaDBAdaptor =
    // dbAdaptorFactory.getMirnaDBAdaptor(this.species);
    // private TfbsDBAdaptor tfbsDBAdaptor =
    // dbAdaptorFactory.getTfbsDBAdaptor(this.species);

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
    public Response getGenesByRegion(@PathParam("chrRegionId") String chregionId,
                                     @DefaultValue("true") @QueryParam("transcript") String transcripts,
                                     @DefaultValue("") @QueryParam("biotype") String biotype) {
        try {
            parseQueryParams();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.assembly);

            List<Region> regions = Region.parseRegions(chregionId);
            if (hasHistogramQueryParam()) {
                queryOptions.put("interval", getHistogramIntervalSize());
                QueryResult res = geneDBAdaptor.getIntervalFrequencies(regions.get(0), queryOptions);
                return createOkResponse(res);
            } else {
                if (biotype != null && !biotype.equals("")) {
                    queryOptions.put("biotype", Splitter.on(",").splitToList(biotype));
                }
//                System.out.println("queryOptions = " + queryOptions.get("exclude"));
//                logger.debug("queryOptions: " + queryOptions);
                return createOkResponse(geneDBAdaptor.getAllByRegionList(regions, queryOptions));
            }
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


    @GET
    @Path("/{chrRegionId}/transcript")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the transcripts objects")
    public Response getTranscriptByRegion(@PathParam("chrRegionId") String chregionId,
                                          @DefaultValue("") @QueryParam("biotype") String biotype) {
        try {
            parseQueryParams();
            TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species, this.assembly);
            List<Region> regions = Region.parseRegions(chregionId);
            if (biotype != null && !biotype.isEmpty()) {
                queryOptions.put("biotype", Splitter.on(",").splitToList(biotype));
            }
            return createOkResponse(transcriptDBAdaptor.getAllByRegionList(regions, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }

    }

    @GET
    @Path("/{chrRegionId}/exon")
    @Deprecated
    public Response getExonByRegion(@PathParam("chrRegionId") String chregionId) {
        try {
            parseQueryParams();
            ExonDBAdaptor exonDBAdaptor = dbAdaptorFactory.getExonDBAdaptor(this.species, this.assembly);
            List<Region> regions = Region.parseRegions(chregionId);
//            return createOkResponse(exonDBAdaptor.getAllSequencesByRegionList(regions));
            return createOkResponse("not implemented");
        } catch (Exception e) {
            return createErrorResponse("getExonByRegion", e.toString());
        }
    }

    @GET
    @Path("/{chrRegionId}/snp")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all SNP objects")
    public Response getSnpByRegion(@PathParam("chrRegionId") String chregionId,
                                   @DefaultValue("") @QueryParam("consequence_type") String consequenceTypes,
                                   @DefaultValue("") @QueryParam("phenotype") String phenotype) {
        try {
            parseQueryParams();
            VariationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);
            List<Region> regions = Region.parseRegions(chregionId);
            // remove regions bigger than 10Mb
//            if (regions != null) {
//                for (Region region : regions) {
//                    if ((region.getEnd() - region.getStart()) > 10000000) {
//                        return createErrorResponse("getSNpByRegion", "Regions must be smaller than 10Mb");
//                    }
//                }
//            }

            if (hasHistogramQueryParam()) {
                queryOptions.put("interval", getHistogramIntervalSize());
                return createOkResponse(variationDBAdaptor.getAllIntervalFrequencies(regions, queryOptions));
            } else {
                if (!consequenceTypes.equals("")) {
                    queryOptions.put("consequence_type", consequenceTypes);
                }
                if (!phenotype.equals("")) {
                    queryOptions.put("phenotype", phenotype);
                }
                return createOkResponse(variationDBAdaptor.getAllByRegionList(regions, queryOptions));
            }
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chrRegionId}/mutation")
    @Deprecated
    public Response getMutationByRegion(@PathParam("chrRegionId") String query) {
        try {
            parseQueryParams();
            MutationDBAdaptor mutationDBAdaptor = dbAdaptorFactory.getMutationDBAdaptor(this.species, this.assembly);
            List<Region> regions = Region.parseRegions(query);
            if (hasHistogramQueryParam()) {
                QueryResult queryResult = mutationDBAdaptor.getIntervalFrequencies(regions.get(0), queryOptions);
                return createOkResponse(queryResult);
            } else {
                List<QueryResult> queryResults = mutationDBAdaptor.getAllByRegionList(regions, queryOptions);
                return createOkResponse(queryResults);
            }
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chrRegionId}/sequence")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the clinical variants")
    public Response getSequenceByRegion(@PathParam("chrRegionId") String chregionId,
                                        @DefaultValue("1") @QueryParam("strand") String strandParam,
                                        @DefaultValue("") @QueryParam("format") String format) {
        try {
            parseQueryParams();
            GenomeDBAdaptor genomeDBAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(this.species, this.assembly);
            List<Region> regions = Region.parseRegions(chregionId);
            queryOptions.put("strand", strandParam);
            return createOkResponse(genomeDBAdaptor.getAllSequencesByRegionList(regions, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chrRegionId}/clinical")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the clinical variants")
    public Response getClinicalByRegion(@PathParam("chrRegionId") String query,
                                        @DefaultValue("") @QueryParam("gene") String gene,
                                        @DefaultValue("") @QueryParam("id") String id,
                                        @DefaultValue("") @QueryParam("phenotype") String phenotype) {
        try {
            parseQueryParams();
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(this.species, this.assembly);
            List<Region> regions = Region.parseRegions(query);
            if (hasHistogramQueryParam()) {
                return null;
            } else {
                if (gene != null && !gene.equals("")) {
                    queryOptions.add("gene", Arrays.asList(gene.split(",")));
                }
                if (id != null && !id.equals("")) {
                    queryOptions.add("id", Arrays.asList(id.split(",")));
                }
                if (phenotype != null && !phenotype.equals("")) {
                    queryOptions.add("phenotype", Arrays.asList(phenotype.split(",")));
                }
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
                return createOkResponse(clinicalDBAdaptor.getAllByRegionList(regions, queryOptions));
            }

        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }


    @GET
    @Path("/{chrRegionId}/phenotype")
    public Response getPhenotypeByRegion(@PathParam("chrRegionId") String query, @DefaultValue("") @QueryParam("source") String source) {
        try {
            parseQueryParams();
            VariationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.assembly);
            List<Region> regions = Region.parseRegions(query);

            if (hasHistogramQueryParam()) {
                QueryResult queryResult = variationDBAdaptor.getAllIntervalFrequencies(regions.get(0), queryOptions);
                return createOkResponse(queryResult);
            } else {
                if (source != null && !source.equals("")) {
                    queryOptions.put("source", Splitter.on(",").splitToList(source));
                }
                List<QueryResult> queryResults = variationDBAdaptor.getAllPhenotypeByRegion(regions, queryOptions);
                return createOkResponse(queryResults);
            }
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chrRegionId}/structural_variation")
    public Response getStructuralVariationByRegion(@PathParam("chrRegionId") String query,
                                                   @QueryParam("min_length") Integer minLength,
                                                   @QueryParam("max_length") Integer maxLength) {
        try {
            parseQueryParams();
            StructuralVariationDBAdaptor structuralVariationDBAdaptor = dbAdaptorFactory
                    .getStructuralVariationDBAdaptor(this.species, this.assembly);
            List<Region> regions = Region.parseRegions(query);

            if (hasHistogramQueryParam()) {
                List<IntervalFeatureFrequency> intervalList = structuralVariationDBAdaptor.getAllIntervalFrequencies(
                        regions.get(0), getHistogramIntervalSize());
                return generateResponse(query, intervalList);
            } else {
                List<List<StructuralVariation>> structuralVariationList = null;
                if (minLength == null && maxLength == null) {
                    structuralVariationList = structuralVariationDBAdaptor.getAllByRegionList(regions);
                } else {
                    if (minLength == null) {
                        minLength = 1;
                    }
                    if (maxLength == null) {
                        maxLength = Integer.MAX_VALUE;
                    }
                    structuralVariationList = structuralVariationDBAdaptor.getAllByRegionList(regions, minLength,
                            maxLength);
                }
                return this.generateResponse(query, "STRUCTURAL_VARIATION", structuralVariationList);
            }
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chrRegionId}/cytoband")
    public Response getCytobandByRegion(@PathParam("chrRegionId") String chregionId) {
        try {
            parseQueryParams();
            CytobandDBAdaptor cytobandDBAdaptor = dbAdaptorFactory.getCytobandDBAdaptor(this.species, this.assembly);
            List<Region> regions = Region.parseRegions(chregionId);
            return generateResponse(chregionId, cytobandDBAdaptor.getAllByRegionList(regions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chrRegionId}/tfbs")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the TFBS")
    public Response getTfByRegion(@PathParam("chrRegionId") String query) {
        try {
            parseQueryParams();
            TfbsDBAdaptor tfbsDBAdaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species, this.assembly);
            List<Region> regions = Region.parseRegions(query);

            if (hasHistogramQueryParam()) {
                List<IntervalFeatureFrequency> intervalList = tfbsDBAdaptor.getAllTfIntervalFrequencies(regions.get(0),
                        getHistogramIntervalSize());
                return generateResponse(query, intervalList);
            } else {
                return createOkResponse(tfbsDBAdaptor.getAllByRegionList(regions, queryOptions));
            }
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chrRegionId}/regulatory")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the regulatory elements")
    public Response getFeatureMap(@PathParam("chrRegionId") String chregionId,
                                  @DefaultValue("") @QueryParam("type") String featureType,
                                  @DefaultValue("") @QueryParam("class") String featureClass) {
        try {
            parseQueryParams();
            RegulatoryRegionDBAdaptor regRegionDBAdaptor = dbAdaptorFactory.getRegulatoryRegionDBAdaptor(this.species, this.assembly);
            List<Region> regions = Region.parseRegions(chregionId);
            queryOptions.put("featureType", (!featureType.equals("")) ? Splitter.on(",").splitToList(featureType) : null);
            queryOptions.put("featureClass", (!featureClass.equals("")) ? Splitter.on(",").splitToList(featureClass) : null);
//            logger.info(regions.get(0).toString());
            return createOkResponse(regRegionDBAdaptor.getAllByRegionList(regions, queryOptions));
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
//            MirnaDBAdaptor mirnaDBAdaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species, this.assembly);
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

    @GET
    @Path("/{chrRegionId}/cpg_island")
    public Response getCpgIslandByRegion(@PathParam("chrRegionId") String query) {
        try {
            parseQueryParams();
            CpGIslandDBAdaptor cpGIslandDBAdaptor = dbAdaptorFactory.getCpGIslandDBAdaptor(this.species, this.assembly);
            List<Region> regions = Region.parseRegions(query);

            if (hasHistogramQueryParam()) {
                List<IntervalFeatureFrequency> intervalList = cpGIslandDBAdaptor.getAllIntervalFrequencies(
                        regions.get(0), getHistogramIntervalSize());
                return generateResponse(query, intervalList);
            } else {
                List<List<CpGIsland>> cpGIslandList = cpGIslandDBAdaptor.getAllByRegionList(regions);
                return this.generateResponse(query, cpGIslandList);
            }
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chrRegionId}/conserved_region")
    @Deprecated
    public Response getConservedRegionByRegion(@PathParam("chrRegionId") String query) {
        try {
            parseQueryParams();
            List<Region> regions = Region.parseRegions(query);
            ConservedRegionDBAdaptor conservedRegionDBAdaptor = dbAdaptorFactory.getConservedRegionDBAdaptor(this.species, this.assembly);
            return createOkResponse(conservedRegionDBAdaptor.getAllByRegionList(regions, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chrRegionId}/conservation")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the conservation scores")
    public Response getConservedRegionByRegion2(@PathParam("chrRegionId") String query) {
        try {
            parseQueryParams();
            ConservedRegionDBAdaptor conservedRegionDBAdaptor = dbAdaptorFactory.getConservedRegionDBAdaptor(this.species, this.assembly);
            List<Region> regions = Region.parseRegions(query);
//            if (hasHistogramQueryParam()) {
//                List<IntervalFeatureFrequency> intervalList = regulatoryRegionDBAdaptor
//                        .getAllConservedRegionIntervalFrequencies(regions.get(0), getHistogramIntervalSize());
//                return generateResponse(query, intervalList);
//            } else {
//                return this.generateResponse(query,
//                        regulatoryRegionDBAdaptor.getAllConservedRegionByRegionList(regions));
//            }
            return createOkResponse(conservedRegionDBAdaptor.getAllByRegionList(regions, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chrRegionId}/conservation2")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the conservation scores")
    public Response conservation2(@PathParam("chrRegionId") String region) {
        ConservationDBAdaptor conservationDBAdaptor =
                dbAdaptorFactory2.getConservedRegionDBAdaptor(this.species, this.assembly);

        Query query = new Query();
        query.append(ConservationDBAdaptor.QueryParams.REGION.key(), region);
        return createOkResponse(conservationDBAdaptor.nativeGet(query, queryOptions));
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
