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

package org.opencb.cellbase.server.rest.genomic;

import io.swagger.annotations.*;
import org.bson.Document;
import org.opencb.biodata.models.core.*;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.Repeat;
import org.opencb.cellbase.core.api.*;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.core.utils.SpeciesUtils;
import org.opencb.cellbase.lib.managers.*;
import org.opencb.cellbase.server.exception.CellBaseServerException;
import org.opencb.cellbase.server.rest.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.opencb.cellbase.core.ParamConstants.*;

@Path("/{apiVersion}/{species}/genomic/region")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Region", description = "Region RESTful Web Services API")
public class RegionWSServer extends GenericRestWSServer {

    private GeneManager geneManager;
    private VariantManager variantManager;
    private GenomeManager genomeManager;
    private TranscriptManager transcriptManager;
    private ClinicalManager clinicalManager;
    private RegulatoryManager regulatoryManager;
    private RepeatsManager repeatsManager;

    public RegionWSServer(@PathParam("apiVersion") @ApiParam(name = "apiVersion", value = VERSION_DESCRIPTION,
            defaultValue = DEFAULT_VERSION) String apiVersion,
                          @PathParam("species") @ApiParam(name = "species", value = SPECIES_DESCRIPTION) String species,
                          @ApiParam(name = "assembly", value = ASSEMBLY_DESCRIPTION) @DefaultValue("") @QueryParam("assembly")
                                  String assembly,
                          @ApiParam(name = "dataRelease", value = DATA_RELEASE_DESCRIPTION) @DefaultValue("0") @QueryParam("dataRelease")
                                  int dataRelease,
                          @ApiParam(name = "apiKey", value = API_KEY_DESCRIPTION) @DefaultValue("") @QueryParam("apiKey") String apiKey,
                          @Context UriInfo uriInfo,
                          @Context HttpServletRequest hsr) throws CellBaseServerException {
        super(apiVersion, species, uriInfo, hsr);
        try {
            if (assembly == null) {
                assembly = SpeciesUtils.getDefaultAssembly(cellBaseConfiguration, species).getName();
            }

            geneManager = cellBaseManagerFactory.getGeneManager(species, assembly);
            variantManager = cellBaseManagerFactory.getVariantManager(species, assembly);
            genomeManager = cellBaseManagerFactory.getGenomeManager(species, assembly);
            transcriptManager = cellBaseManagerFactory.getTranscriptManager(species, assembly);
            clinicalManager = cellBaseManagerFactory.getClinicalManager(species, assembly);
            regulatoryManager = cellBaseManagerFactory.getRegulatoryManager(species, assembly);
            repeatsManager = cellBaseManagerFactory.getRepeatsManager(species, assembly);
        } catch (Exception e) {
            throw new CellBaseServerException(e.getMessage());
        }
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = DATA_MODEL_DESCRIPTION,
            response = Map.class, responseContainer = "QueryResponse")
    public Response getModel() {
        return createModelResponse(Region.class);
    }

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Path("/gene")
    @ApiOperation(httpMethod = "POST", value = "Retrieves all the gene objects for the regions. If query param "
            + "histogram=true, frequency values per genomic interval will be returned instead.", notes = "If "
            + "histogram=false Gene objects will be returned "
            + "(see https://github.com/opencb/biodata/tree/develop/biodata-models/src/main/java/org/opencb/biodata/models/core). "
            + "If histogram=true Document objects with keys start,end,chromosome & feature_count will be returned.",
            responseContainer = "QueryResponse", hidden = true)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "biotype",  value = GENE_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype", value = TRANSCRIPT_BIOTYPES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.xrefs", value = TRANSCRIPT_XREFS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.id", value = TRANSCRIPT_TFBS_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.id", value = ANNOTATION_DISEASES_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.name", value = ANNOTATION_DISEASES_NAMES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.gene", value = ANNOTATION_EXPRESSION_GENE,
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
    public Response getGenesByRegionPost(@FormParam("region") @ApiParam(name = "region",
            value = REGION_DESCRIPTION, required = true) String region) {
        return getGenesByRegion(region);
    }

    @GET
    @Path("/{regions}/gene")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the gene objects for the regions.", responseContainer = "QueryResponse")
    @ApiImplicitParams({
//            @ApiImplicitParam(name = "histogram",
//                    value = "Boolean to indicate whether gene counts per interval shall be returned", defaultValue = "false",
//                    required = false, allowableValues = "true,false", dataType = "boolean", paramType = "query"),
//            @ApiImplicitParam(name = "interval",
//                    value = "Use only if histogram=true. Boolean indicating the size of the histogram interval",
//                    defaultValue = "200000", required = false, dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name = "count", value = COUNT_DESCRIPTION,
                    required = false, dataType = "boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
            @ApiImplicitParam(name = "biotype",  value = GENE_BIOTYPES,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.biotype", value = TRANSCRIPT_BIOTYPES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.xrefs", value = TRANSCRIPT_XREFS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.id", value = TRANSCRIPT_TFBS_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.id", value = ANNOTATION_DISEASES_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.diseases.name", value = ANNOTATION_DISEASES_NAMES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "annotation.expression.gene", value = ANNOTATION_EXPRESSION_GENE,
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
    public Response getGenesByRegion(@PathParam("regions") @ApiParam(name = "regions", value = REGION_DESCRIPTION,
            required = true) String regions) {
        try {
            List<GeneQuery> queries = new ArrayList<>();
            String[] coordinates = regions.split(",");
            for (String coordinate : coordinates) {
                GeneQuery query = new GeneQuery(uriParams);
                query.setDataRelease(getDataRelease());
                query.setRegions(Collections.singletonList(Region.parseRegion(coordinate)));
                queries.add(query);
                logger.info("REST GeneQuery: {}", query.toString());
            }
            List<CellBaseDataResult<Gene>> queryResults = geneManager.search(queries);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{regions}/transcript")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all transcript objects for the regions", response = Transcript.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "transcripts.biotype", value = TRANSCRIPT_BIOTYPES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.xrefs", value = TRANSCRIPT_XREFS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.id", value = TRANSCRIPT_IDS_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.name", value = TRANSCRIPT_NAMES_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "transcripts.tfbs.id", value = TRANSCRIPT_TFBS_IDS_DESCRIPTION,
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
    public Response getTranscriptByRegion(@PathParam("regions") @ApiParam(name = "regions",
            value = REGION_DESCRIPTION, required = true) String regions) {
        try {
            List<TranscriptQuery> queries = new ArrayList<>();
            String[] coordinates = regions.split(",");
            for (String coordinate : coordinates) {
                TranscriptQuery query = new TranscriptQuery(uriParams);
                query.setDataRelease(getDataRelease());
                query.setRegions(Region.parseRegions(coordinate));
                queries.add(query);
                logger.info("REST TranscriptQuery: {}", query.toString());
            }
            List<CellBaseDataResult<Transcript>> queryResults = transcriptManager.search(queries);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{regions}/repeat")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all repeats for the regions", response = Transcript.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exclude", value = EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "sort", value = SORT_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "order", value = ORDER_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query",
                    defaultValue = "", allowableValues="ASCENDING,DESCENDING"),
            @ApiImplicitParam(name = "limit", value = LIMIT_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_LIMIT, dataType = "java.util.List",
                    paramType = "query"),
            @ApiImplicitParam(name = "skip", value = SKIP_DESCRIPTION,
                    required = false, defaultValue = DEFAULT_SKIP, dataType = "java.util.List",
                    paramType = "query")
    })
    public Response getRepeatByRegion(@PathParam("regions") @ApiParam(name = "regions",
            value = REGION_DESCRIPTION, required = true) String region) {
        try {
            List<RepeatsQuery> queries = new ArrayList<>();
            String[] coordinates = region.split(",");
            for (String coordinate : coordinates) {
                RepeatsQuery query = new RepeatsQuery(uriParams);
                query.setDataRelease(getDataRelease());
                query.setRegions(Region.parseRegions(coordinate));
                queries.add(query);
                logger.info("REST RepeatsQuery: {}", query.toString());
            }
            List<CellBaseDataResult<Repeat>> queryResults = repeatsManager.search(queries);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{regions}/variant")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the variant objects for the regions.", notes = "Please NOTE that regions "
            + "must be smaller than 10Mb", responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "consequenceType", value = CONSEQUENCE_TYPE,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "gene", value = GENE_IDS,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "id", value = "Comma separated list of ids, e.g.: 22:35490160:G:A",
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
    public Response getVariantByRegion(@PathParam("regions") @ApiParam(name = "regions", value = REGION_DESCRIPTION,
            required = true) String regions) {
        try {
            List<VariantQuery> queries = new ArrayList<>();
            if (!variantManager.validateRegionInput(regions)) {
                return createErrorResponse("getVariantByRegion", "Regions must be smaller than 10Mb");
            }
            List<Region> regionList = Region.parseRegions(regions);
            for (Region region : regionList) {
                VariantQuery query = new VariantQuery(uriParams);
                query.setDataRelease(getDataRelease());
                query.setRegions(Collections.singletonList(region));
                queries.add(query);
                logger.info("REST variantQuery: {}", query.toString());
            }
            List<CellBaseDataResult<Variant>> queryResults = variantManager.search(queries);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

//    @GET
//    @Deprecated
//    @Path("/{regions}/snp")
//    @ApiOperation(httpMethod = "GET", value = "Retrieves all SNP objects", hidden = true)
//    public Response getSnpByRegion(@PathParam("regions") String region,
//                                   @DefaultValue("") @QueryParam("consequence_type") String consequenceTypes,
//                                   @DefaultValue("") @QueryParam("phenotype") String phenotype,
//                                   @QueryParam("exclude") @ApiParam(value = ParamConstants.EXCLUDE_DESCRIPTION) String exclude,
//                                   @QueryParam("include") @ApiParam(value = ParamConstants.INCLUDE_DESCRIPTION) String include,
//                                   @QueryParam("sort") @ApiParam(value = ParamConstants.SORT_DESCRIPTION) String sort,
//                                   @QueryParam("limit") @DefaultValue("10")
//                                       @ApiParam(value = ParamConstants.LIMIT_DESCRIPTION) Integer limit,
//                                   @QueryParam("skip") @DefaultValue("0")
//                                       @ApiParam(value = ParamConstants.SKIP_DESCRIPTION)  Integer skip) {
//        return getVariationByRegion(region, exclude, include, sort, limit, skip);
//    }


    @GET
    @Path("/{regions}/sequence")
    @ApiOperation(httpMethod = "GET", value = "Retrieves genomic sequence", response = String.class,
            responseContainer = "QueryResponse")
    public Response getSequenceByRegion(@PathParam("regions")
                                        @ApiParam(name = "regions", value = REGION_DESCRIPTION,
                                                required = true) String regions,
                                        @DefaultValue("1") @QueryParam("strand")
                                        @ApiParam(name = "strand", value = STRAND,
                                                allowableValues = "1,-1", defaultValue = "1", required = true) String strand) {
        try {
            GenomeQuery query = new GenomeQuery(uriParams);
            query.setDataRelease(getDataRelease());
            query.setRegions(Region.parseRegions(regions));
            List<CellBaseDataResult<GenomeSequenceFeature>> queryResults = genomeManager.getByRegions(query);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{regions}/clinical")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the clinical variants",
            notes = "No more than 1000 objects are allowed to be returned at a time. "
                    + "Please note that ClinVar, COSMIC or GWAS objects may be returned as stored in the database. Please have "
                    + "a look at "
                    + "https://github.com/opencb/cellbase/wiki/MongoDB-implementation#clinical for further details.",
            response = Document.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "so", value = SEQUENCE_ONTOLOGY_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "gene",
                    value = "Comma separated list gene ids, e.g.: BRCA2. Gene ids can be either HGNC symbols or "
                            + " ENSEMBL gene ids. Exact text matches will be returned.",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "phenotype",
                    value = "String to indicate the phenotypes to query. A text search will be run.",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "clinvarId",
                    value = "Comma separated list of rcv ids, e.g.: RCV000033215",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "rs",
                    value = "Comma separated list of rs ids, e.g.: rs6025",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "type",
                    value = "Comma separated list of variant types as stored in ClinVar (only enabled for ClinVar "
                            + "variants, e.g. \"single nucleotide variant\" ",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "review",
                    value = "Comma separated list of review lables (only enabled for ClinVar variants), "
                            + " e.g.: CRITERIA_PROVIDED_SINGLE_SUBMITTER",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "clinvar-significance",
                    value = "Comma separated list of clinical significance labels as stored in ClinVar (only enabled "
                            + "for ClinVar variants), e.g.: Benign",
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
    public Response getClinicalByRegion(@PathParam("regions") @ApiParam(name = "regions", value = REGION_DESCRIPTION,
            required = true) String regions) {
        try {
            // FIXME
//            parseQueryParams();
//            CellBaseDataResult queryResult = clinicalManager.getByRegion(query, queryOptions, regions);
//            return createOkResponse(queryResult);
            return createErrorResponse(new CellBaseException("Not refactored, please update"));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{regions}/regulatory")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all regulatory elements in a region", notes = "An independent"
            + " database query will be issued for each region in regionStr, meaning that results for each region will be"
            + " returned in independent QueryResult objects within the QueryResponse object.",
            response = RegulatoryFeature.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "featureType", value = REGULATION_FEATURE_TYPES,
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
    public Response getRegulatoryRegions(@PathParam("regions") @ApiParam(name = "regions", value = REGION_DESCRIPTION,
            required = true) String regions) {
        try {
            List<RegulationQuery> queries = new ArrayList<>();
            String[] regionArray = regions.split(",");
            for (String regionString : regionArray) {
                RegulationQuery query = new RegulationQuery(uriParams);
                query.setDataRelease(getDataRelease());
                query.setRegions(Region.parseRegions(regionString));
                logger.info("REST RegulationQuery: {}", query.toString());
                queries.add(query);
            }
            List<CellBaseDataResult<RegulatoryFeature>> queryResults = regulatoryManager.search(queries);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{regions}/tfbs")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all transcription factor binding site objects for the regions. ",
            response = RegulatoryFeature.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
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
    public Response getTfByRegion(@PathParam("regions") @ApiParam(name = "regions", value = REGION_DESCRIPTION,
            required = false) String regions) {
        try {
            List<RegulationQuery> queries = new ArrayList<>();
            String[] regionArray = regions.split(",");
            for (String regionString : regionArray) {
                RegulationQuery query = new RegulationQuery(uriParams);
                query.setDataRelease(getDataRelease());
                query.setRegions(Collections.singletonList(Region.parseRegion(regionString)));
                query.setFeatureTypes(Collections.singletonList("TF_binding_site"));
                logger.info("REST RegulationQuery: {}", query.toString());
                queries.add(query);
            }
            List<CellBaseDataResult<RegulatoryFeature>> queryResults = regulatoryManager.search(queries);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{regions}/conservation")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the conservation scores", response = GenomicScoreRegion.class,
            responseContainer = "QueryResponse")
    public Response conservation(@PathParam("regions")
                                 @ApiParam(name = "regions", value = REGION_DESCRIPTION,
                                         required = true) String regions) {
        try {
            GenomeQuery query = new GenomeQuery(uriParams);
            List<CellBaseDataResult<GenomicScoreRegion<Float>>> queryResults
                    = genomeManager.getConservation(query.toQueryOptions(), regions, getDataRelease());
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
