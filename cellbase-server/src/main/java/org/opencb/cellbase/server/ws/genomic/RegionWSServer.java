package org.opencb.cellbase.server.ws.genomic;

import com.google.common.base.Splitter;
import org.opencb.cellbase.core.common.IntervalFeatureFrequency;
import org.opencb.cellbase.core.common.Region;
import org.opencb.cellbase.core.common.core.CpGIsland;
import org.opencb.cellbase.core.common.regulatory.MirnaTarget;
import org.opencb.cellbase.core.common.variation.StructuralVariation;
import org.opencb.cellbase.core.lib.api.*;
import org.opencb.cellbase.core.lib.api.regulatory.RegulatoryRegionDBAdaptor;
import org.opencb.cellbase.core.lib.api.regulatory.TfbsDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.MutationDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.StructuralVariationDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.VariationDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Path("/{version}/{species}/genomic/region")
@Produces(MediaType.APPLICATION_JSON)
public class RegionWSServer extends GenericRestWSServer {
    // private int histogramIntervalSize = 1000000;
    private int histogramIntervalSize = 200000;

//    private List<String> exclude = new ArrayList<>();

    public RegionWSServer(@PathParam("version") String version, @PathParam("species") String species,
                          @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
        super(version, species, uriInfo, hsr);
//        this.exclude = Arrays.asList(exclude.trim().split(","));
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
        if (getHistogramParameter().toLowerCase().equals("true")) {
            return true;
        } else {
            return false;
        }
    }

    @GET
    @Path("/{chrRegionId}/gene")
    public Response getGenesByRegion(@PathParam("chrRegionId") String chregionId,
                                     @DefaultValue("true") @QueryParam("transcript") String transcripts,
                                     @DefaultValue("") @QueryParam("biotype") String biotype) {
        try {
            checkVersionAndSpecies();
            GeneDBAdaptor geneDBAdaptor = dbAdaptorFactory.getGeneDBAdaptor(this.species, this.version);

            List<Region> regions = Region.parseRegions(chregionId);

            if (hasHistogramQueryParam()) {
//				long t1 = System.currentTimeMillis();
                // Response resp = generateResponse(chregionId,
                // getHistogramByFeatures(dbAdaptor.getAllByRegionList(regions)));
//				Response resp = generateResponse(chregionId,
//						geneDBAdaptor.getAllIntervalFrequencies(regions.get(0), getHistogramIntervalSize()));
                queryOptions.put("interval", getHistogramIntervalSize());
                List<QueryResult> res = geneDBAdaptor.getAllIntervalFrequencies(regions, queryOptions);
//				logger.info("Old histogram: " + (System.currentTimeMillis() - t1) + ",  resp: " + res.toString());
                return createOkResponse(res);
            } else {
//				QueryOptions queryOptions = new QueryOptions("biotypes", StringUtils.toList(biotype, ","));
//				queryOptions.put("biotype", biotype);
                if(biotype != null && !biotype.equals("")) {
                    queryOptions.put("biotype", Splitter.on(",").splitToList(biotype));
                }
//				queryOptions.put("transcripts", transcripts.equalsIgnoreCase("true"));
                addExcludeReturnFields("transcripts.exons.sequence", queryOptions);
//				return createOkResponse(chregionId, "GENE",	geneDBAdaptor.getAllByRegionList(regions, queryOptions));
                return createOkResponse(geneDBAdaptor.getAllByRegionList(regions, queryOptions));
//				if (transcripts != null) {
//					if (biotype != null && !biotype.equals("")) {
//					} else {
//						return generateResponse(chregionId, "GENE", geneDBAdaptor.getAllByRegionList(regions, queryOptions));
//					}

//				} else {
//					queryOptions.put("transcripts", false);
//					return createOkResponse(chregionId, "GENE",	geneDBAdaptor.getAllByRegionList(regions, queryOptions));
////					if (biotype != null && !biotype.equals("")) {
////					} else {
////						return generateResponse(chregionId, "GENE", geneDBAdaptor.getAllByRegionList(regions, queryOptions));
////					}
//				}
            }
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getGenesByRegion", e.toString());
        }
    }

    @GET
    @Path("/{chrRegionId}/transcript")
    public Response getTranscriptByRegion(@PathParam("chrRegionId") String chregionId, @DefaultValue("") @QueryParam("biotype") String biotype) {
        try {
            checkVersionAndSpecies();
            TranscriptDBAdaptor transcriptDBAdaptor = dbAdaptorFactory.getTranscriptDBAdaptor(this.species, this.version);
            List<Region> regions = Region.parseRegions(chregionId);
            if (biotype != null && !biotype.equals("")) {
                queryOptions.put("biotype", Splitter.on(",").splitToList(biotype));
            }
            return createOkResponse(transcriptDBAdaptor.getAllByRegionList(regions, queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getTranscriptByRegion", e.toString());
        }

    }

    @GET
    @Path("/{chrRegionId}/exon")
    public Response getExonByRegion(@PathParam("chrRegionId") String chregionId) {
        try {
            checkVersionAndSpecies();
            ExonDBAdaptor exonDBAdaptor = dbAdaptorFactory.getExonDBAdaptor(this.species, this.version);
            List<Region> regions = Region.parseRegions(chregionId);
            return createOkResponse(exonDBAdaptor.getAllByRegionList(regions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getExonByRegion", e.toString());
        }
    }

    @GET
    @Path("/{chrRegionId}/snp")
    public Response getSnpByRegion(@PathParam("chrRegionId") String chregionId,
                                   @DefaultValue("") @QueryParam("consequence_type") String consequenceTypes,
                                   @DefaultValue("") @QueryParam("phenotype") String phenotype) {
        try {
            checkVersionAndSpecies();
            VariationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.version);
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
                if(!consequenceTypes.equals("")) {
                    queryOptions.put("consequence_type", consequenceTypes);
                }
                if(!phenotype.equals("")) {
                    queryOptions.put("phenotype", phenotype);
                }
                return createOkResponse(variationDBAdaptor.getAllByRegionList(regions, queryOptions));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getSnpByRegion", e.toString());
        }
    }

    @GET
    @Path("/{chrRegionId}/mutation")
    public Response getMutationByRegion(@PathParam("chrRegionId") String query) {
        try {
            checkVersionAndSpecies();
            MutationDBAdaptor mutationDBAdaptor = dbAdaptorFactory.getMutationDBAdaptor(this.species, this.version);
            List<Region> regions = Region.parseRegions(query);

            if (hasHistogramQueryParam()) {
//				List<IntervalFeatureFrequency> intervalList = mutationDBAdaptor.getAllIntervalFrequencies(
//						regions.get(0), getHistogramIntervalSize());
                QueryResult queryResult = mutationDBAdaptor.getAllIntervalFrequencies(regions.get(0), queryOptions);
//				return generateResponse(query, intervalList);
                return createOkResponse(queryResult);
            } else {
//				List<List<MutationPhenotypeAnnotation>> mutationList = mutationDBAdaptor.getAllByRegionList(regions);
                List<QueryResult> queryResults = mutationDBAdaptor.getAllByRegionList(regions, queryOptions);
//				return this.generateResponse(query, "MUTATION", mutationList);
                return createOkResponse(queryResults);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getMutationByRegion", e.toString());
        }
    }

    @GET
    @Path("/{chrRegionId}/phenotype")
    public Response getPhenotypeByRegion(@PathParam("chrRegionId") String query) {
        try {
            checkVersionAndSpecies();
            VariationDBAdaptor variationDBAdaptor = dbAdaptorFactory.getVariationDBAdaptor(this.species, this.version);
            List<Region> regions = Region.parseRegions(query);

            if (hasHistogramQueryParam()) {
//				List<IntervalFeatureFrequency> intervalList = mutationDBAdaptor.getAllIntervalFrequencies(
//						regions.get(0), getHistogramIntervalSize());
                QueryResult queryResult = variationDBAdaptor.getAllIntervalFrequencies(regions.get(0), queryOptions);
//				return generateResponse(query, intervalList);
                return createOkResponse(queryResult);
            } else {
//				List<List<MutationPhenotypeAnnotation>> mutationList = mutationDBAdaptor.getAllByRegionList(regions);
                List<QueryResult> queryResults = variationDBAdaptor.getAllPhenotypeByRegion(regions, queryOptions);
//				return this.generateResponse(query, "MUTATION", mutationList);
                return createOkResponse(queryResults);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getMutationByRegion", e.toString());
        }
    }


    @GET
    @Path("/{chrRegionId}/structural_variation")
    public Response getStructuralVariationByRegion(@PathParam("chrRegionId") String query,
                                                   @QueryParam("min_length") Integer minLength, @QueryParam("max_length") Integer maxLength) {
        try {
            checkVersionAndSpecies();
            StructuralVariationDBAdaptor structuralVariationDBAdaptor = dbAdaptorFactory
                    .getStructuralVariationDBAdaptor(this.species, this.version);
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
            e.printStackTrace();
            return createErrorResponse("getStructuralVariationByRegion", e.toString());
        }
    }

    @GET
    @Path("/{chrRegionId}/cytoband")
    public Response getCytobandByRegion(@PathParam("chrRegionId") String chregionId) {
        try {
            checkVersionAndSpecies();
            CytobandDBAdaptor cytobandDBAdaptor = dbAdaptorFactory.getCytobandDBAdaptor(this.species, this.version);
            List<Region> regions = Region.parseRegions(chregionId);
            return generateResponse(chregionId, cytobandDBAdaptor.getAllByRegionList(regions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getCytobandByRegion", e.toString());
        }
    }

    @GET
    @Path("/{chrRegionId}/sequence")
    public Response getSequenceByRegion(@PathParam("chrRegionId") String chregionId,
                                        @DefaultValue("1") @QueryParam("strand") String strandParam,
                                        @DefaultValue("") @QueryParam("format") String format) {
        try {
            checkVersionAndSpecies();
            List<Region> regions = Region.parseRegions(chregionId);
            GenomeSequenceDBAdaptor genomeSequenceDBAdaptor = dbAdaptorFactory.getGenomeSequenceDBAdaptor(this.species,	this.version);
            queryOptions.put("strand", strandParam);
            return createOkResponse(genomeSequenceDBAdaptor.getAllByRegionList(regions, queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getSequenceByRegion", e.toString());
        }
    }

    @GET
    @Path("/{chrRegionId}/reverse")
    @Deprecated
    public Response getReverseSequenceByRegion(@PathParam("chrRegionId") String chregionId) {
        try {
            checkVersionAndSpecies();
            List<Region> regions = Region.parseRegions(chregionId);
            GenomeSequenceDBAdaptor dbAdaptor = dbAdaptorFactory.getGenomeSequenceDBAdaptor(this.species, this.version);
            queryOptions.put("strand", -1);
            return createOkResponse(dbAdaptor.getAllByRegionList(regions, queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getReverseSequenceByRegion", e.toString());
        }
    }

    @GET
    @Path("/{chrRegionId}/tfbs")
    public Response getTfByRegion(@PathParam("chrRegionId") String query) {
        try {
            checkVersionAndSpecies();
            TfbsDBAdaptor tfbsDBAdaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species, this.version);
            List<Region> regions = Region.parseRegions(query);

            if (hasHistogramQueryParam()) {
                List<IntervalFeatureFrequency> intervalList = tfbsDBAdaptor.getAllTfIntervalFrequencies(regions.get(0),	getHistogramIntervalSize());
                return generateResponse(query, intervalList);
            } else {
                return createOkResponse(tfbsDBAdaptor.getAllByRegionList(regions, queryOptions));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getTfByRegion", e.toString());
        }
    }

    @GET
    @Path("/{chrRegionId}/regulatory")
    public Response getFeatureMap(@PathParam("chrRegionId") String chregionId,
                                  @DefaultValue("") @QueryParam("type") String featureType,
                                  @DefaultValue("") @QueryParam("class") String featureClass) {
        try {
            checkVersionAndSpecies();
            List<Region> regions = Region.parseRegions(chregionId);

            queryOptions.put("featureType", (!featureType.equals("")) ? Splitter.on(",").splitToList(featureType) : null);
            queryOptions.put("featureClass",(!featureClass.equals("")) ? Splitter.on(",").splitToList(featureClass) : null);
            RegulatoryRegionDBAdaptor regulatoryRegionDBAdaptor = dbAdaptorFactory.getRegulatoryRegionDBAdaptor(this.species, this.version);

            return createOkResponse(regulatoryRegionDBAdaptor.getAllByRegionList(regions, queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getFeatureMap", e.toString());
        }
    }

//    @GET
//    @Path("/{chrRegionId}/regulatory")
//    public Response getRegulatoryByRegion(@PathParam("chrRegionId") String chregionId,
//                                          @DefaultValue("") @QueryParam("type") String type) {
//        try {
//            checkVersionAndSpecies();
//            RegulatoryRegionDBAdaptor regulatoryRegionDBAdaptor = dbAdaptorFactory.getRegulatoryRegionDBAdaptor(this.species, this.version);
//            /**
//             * type ["open chromatin", "Polymerase", "HISTONE",
//             * "Transcription Factor"]
//             **/
//            List<Region> regions = Region.parseRegions(chregionId);
//
////			if (hasHistogramQueryParam()) {
////				// return generateResponse(chregionId,
////				// getHistogramByFeatures(results));
////				return generateResponse(chregionId,
////						regulatoryRegionDBAdaptor.getAllRegulatoryRegionIntervalFrequencies(regions.get(0),
////								getHistogramIntervalSize(), type));
////			} else {
////				List<List<RegulatoryRegion>> results;
////				if (type.equals("")) {
////					results = regulatoryRegionDBAdaptor.getAllByRegionList(regions);
////				} else {
////					results = regulatoryRegionDBAdaptor.getAllByRegionList(regions, Arrays.asList(type.split(",")));
////				}
////				return generateResponse(chregionId, "REGULATORY_REGION", results);
////			}
////            return generateResponse(chregionId, regulatoryRegionDBAdaptor.getByRegionList(regions, Arrays.asList(type.split(","))));
//            return Response.ok().build();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return createErrorResponse("getRegulatoryByRegion", e.toString());
//        }
//    }

    @GET
    @Path("/{chrRegionId}/mirna_target")
    public Response getMirnaTargetByRegion(@PathParam("chrRegionId") String query,
                                           @DefaultValue("") @QueryParam("source") String source) {
        try {
            checkVersionAndSpecies();
            MirnaDBAdaptor mirnaDBAdaptor = dbAdaptorFactory.getMirnaDBAdaptor(this.species, this.version);
            List<Region> regions = Region.parseRegions(query);

            if (hasHistogramQueryParam()) {
                System.out.println("PAKO:" + "si");
                List<IntervalFeatureFrequency> intervalList = mirnaDBAdaptor.getAllMirnaTargetsIntervalFrequencies(
                        regions.get(0), getHistogramIntervalSize());
                return generateResponse(query, intervalList);
            } else {
                System.out.println("PAKO:" + "NO");
                List<List<MirnaTarget>> mirnaTargetList = mirnaDBAdaptor.getAllMiRnaTargetsByRegionList(regions);
                return this.generateResponse(query, "MIRNA_TARGET", mirnaTargetList);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getMirnaTargetByRegion", e.toString());
        }
    }

    @GET
    @Path("/{chrRegionId}/cpg_island")
    public Response getCpgIslandByRegion(@PathParam("chrRegionId") String query) {
        try {
            checkVersionAndSpecies();
            CpGIslandDBAdaptor cpGIslandDBAdaptor = dbAdaptorFactory.getCpGIslandDBAdaptor(this.species, this.version);
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
            e.printStackTrace();
            return createErrorResponse("getCpgIslandByRegion", e.toString());
        }
    }

//	@GET
//	@Path("/{chrRegionId}/conserved_region")
//	public Response getConservedRegionByRegion(@PathParam("chrRegionId") String query) {
//		try {
//			checkVersionAndSpecies();
//			RegulatoryRegionDBAdaptor regulatoryRegionDBAdaptor = dbAdaptorFactory.getRegulatoryRegionDBAdaptor(
//					this.species, this.version);
//			List<Region> regions = Region.parseRegions(query);
//
//			if (hasHistogramQueryParam()) {
//				List<IntervalFeatureFrequency> intervalList = regulatoryRegionDBAdaptor.getAllConservedRegionIntervalFrequencies(regions.get(0), getHistogramIntervalSize());
//				return generateResponse(query, intervalList);
//			} else {
//				return this.generateResponse(query,
//						regulatoryRegionDBAdaptor.getAllConservedRegionByRegionList(regions));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return createErrorResponse("getConservedRegionByRegion", e.toString());
//		}
//	}

    @GET
    @Path("/{chrRegionId}/conserved_region2")
    public Response getConservedRegionByRegion2(@PathParam("chrRegionId") String query) {
        try {
            checkVersionAndSpecies();

            ConservedRegionDBAdaptor conservedRegionDBAdaptor = dbAdaptorFactory.getConservedRegionDBAdaptor(this.species, this.version);
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
            e.printStackTrace();
            return createErrorResponse("getConservedRegionByRegion2", e.toString());
        }
    }

    @GET
    @Path("/{chrRegionId}/peptide")
    public Response getPeptideByRegion(@PathParam("chrRegionId") String region) {
        try {
            checkVersionAndSpecies();
            List<Region> regions = Region.parseRegions(region);
            boolean isUTR = false;
            List<String> peptide = new ArrayList<String>(0);
            // GenomicRegionFeatureDBAdaptor genomicRegionFeatureDBAdaptor =
            // dbAdaptorFactory.getFeatureMapDBAdaptor(this.species);
            // if (regions != null && !regions.get(0).equals("")){
            // for (Region reg: regions){
            // List<FeatureMap> featureMapList =
            // genomicRegionFeatureDBAdaptor.getFeatureMapsByRegion(reg);
            // if(featureMapList != null){
            // for(FeatureMap featureMap: featureMapList) {
            // String line = "";
            // if(featureMap.getFeatureType().equalsIgnoreCase("5_prime_utr") ||
            // featureMap.getFeatureType().equalsIgnoreCase("3_prime_utr")) {
            // isUTR = true;
            // line = featureMap.getTranscriptStableId()+"\tNo-coding\t\t";
            // peptide.add(line);
            // }else{
            // isUTR = false;
            // if(featureMap.getFeatureType().equalsIgnoreCase("exon")) {
            // if (!isUTR &&
            // featureMap.getBiotype().equalsIgnoreCase("protein_coding")) {
            // System.out.println("Exon: "+featureMap.getFeatureId());
            // System.out.println("Phase: "+featureMap.getExonPhase());
            // if(!featureMap.getExonPhase().equals("") &&
            // !featureMap.getExonPhase().equals("-1")) {
            // System.out.println("with phase");
            // int aaPositionStart = -1;
            // int aaPositionEnd = -1;
            // if(featureMap.getStrand().equals("1")) {
            // aaPositionStart =
            // ((reg.getStart()-featureMap.getStart()+1+featureMap.getExonCdnaCodingStart()-featureMap.getTranscriptCdnaCodingStart())/3)+1;
            // aaPositionEnd =
            // ((reg.getEnd()-featureMap.getStart()+1+featureMap.getExonCdnaCodingStart()-featureMap.getTranscriptCdnaCodingStart())/3)+1;
            // }else {
            // aaPositionStart =
            // ((featureMap.getEnd()-reg.getStart()+1+featureMap.getExonCdnaCodingStart()-featureMap.getTranscriptCdnaCodingStart())/3)+1;
            // aaPositionEnd =
            // ((featureMap.getEnd()-reg.getEnd()+1+featureMap.getExonCdnaCodingStart()-featureMap.getTranscriptCdnaCodingStart())/3)+1;
            // }
            // line =
            // featureMap.getTranscriptStableId()+"\t"+"Protein"+"\t"+aaPositionStart+"\t"+aaPositionEnd;
            // peptide.add(line);
            // }else{
            // if(!featureMap.getExonPhase().equals("") &&
            // !featureMap.getExonPhase().equals("-1")) {
            //
            // }
            // }
            // }
            // }
            // }
            // }
            // }
            // }
            //
            // }
            return createOkResponse("");
            // return generateResponse(region, exonIds);

        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getPeptideByRegion", e.toString());
        }
    }

    @Deprecated
    private List<?> getHistogramByFeatures(List<?> list) {
//		Histogram histogram = new Histogram(list, this.getHistogramIntervalSize());
//		return histogram.getIntervals();
        return null;
    }

    @GET
    public Response getHelp() {
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
        sb.append(" Output columns: Ensembl ID, external name, external name source, biotype, status, chromosome, start, end, strand, source, description.\n\n");
        sb.append("- transcript: This resource obtain the transcripts belonging to one of the regions specified.\n");
        sb.append(" Output columns: Ensembl ID, external name, external name source, biotype, status, chromosome, start, end, strand, coding region start, coding region end, cdna coding start, cdna coding end, description.\n\n");
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

    // private class GeneListDeserializer implements JsonSerializer<List> {
    //
    // @Override
    // public JsonElement serialize(List geneListList, Type typeOfSrc,
    // JsonSerializationContext context) {
    // System.out.println("GeneListDeserializer - gene JSON elem: ");
    // Gson gsonLocal = gson = new
    // GsonBuilder().serializeNulls().setExclusionStrategies(new
    // FeatureExclusionStrategy()).create();
    // //logger.debug("SnpWSCLient - FeatureListDeserializer - json FeatureList<SNP> size: "+json.getAsJsonArray().size());
    // List<List<Gene>> snps = new
    // ArrayList<List<Gene>>(json.getAsJsonArray().size());
    // List<Gene> geneList;
    // JsonArray ja = new JsonArray();
    // for(JsonElement geneArray: json.getAsJsonArray()) {
    // System.out.println("GeneListDeserializer - gene JSON elem: ");
    // geneList = new ArrayList<Gene>(geneArray.getAsJsonArray().size());
    // for(JsonElement gene: geneArray.getAsJsonArray()) {
    // geneList.add(gsonLocal.fromJson(gene, Gene.class));
    // }
    // snps.add(geneList);
    // }
    // return null;
    // }
    //
    // @Override
    // public List<List<Gene>> deserialize(JsonElement json, Type typeOfT,
    // JsonDeserializationContext context) throws JsonParseException {
    // System.out.println("GeneListDeserializer - gene JSON elem: ");
    // Gson gsonLocal = gson = new
    // GsonBuilder().serializeNulls().setExclusionStrategies(new
    // FeatureExclusionStrategy()).create();
    // //logger.debug("SnpWSCLient - FeatureListDeserializer - json FeatureList<SNP> size: "+json.getAsJsonArray().size());
    // List<List<Gene>> snps = new
    // ArrayList<List<Gene>>(json.getAsJsonArray().size());
    // List<Gene> geneList;
    // for(JsonElement geneArray: json.getAsJsonArray()) {
    // System.out.println("GeneListDeserializer - gene JSON elem: ");
    // geneList = new ArrayList<Gene>(geneArray.getAsJsonArray().size());
    // for(JsonElement gene: geneArray.getAsJsonArray()) {
    // geneList.add(gsonLocal.fromJson(gene, Gene.class));
    // }
    // snps.add(geneList);
    // }
    // return snps;
    // }
    // }

}
