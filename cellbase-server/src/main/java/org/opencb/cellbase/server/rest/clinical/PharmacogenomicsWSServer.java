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

package org.opencb.cellbase.server.rest.clinical;

import io.swagger.annotations.*;
import org.opencb.biodata.models.pharma.PharmaChemical;
import org.opencb.cellbase.core.api.PharmaChemicalQuery;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.core.utils.SpeciesUtils;
import org.opencb.cellbase.lib.managers.ClinicalManager;
import org.opencb.cellbase.lib.managers.PharmacogenomicsManager;
import org.opencb.cellbase.server.rest.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.opencb.cellbase.core.ParamConstants.*;


@Path("/{apiVersion}/{species}/clinical/pharmacogenomics")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Clinical Pharmacogenomics", description = "Clinical RESTful Web Services API")
public class PharmacogenomicsWSServer extends GenericRestWSServer {

    private ClinicalManager clinicalManager;
    private PharmacogenomicsManager pharmacogenomicsManager;

    public PharmacogenomicsWSServer(@PathParam("apiVersion") @ApiParam(name = "apiVersion", value = VERSION_DESCRIPTION,
            defaultValue = DEFAULT_VERSION) String apiVersion,
                                    @PathParam("species") @ApiParam(name = "species", value = SPECIES_DESCRIPTION) String species,
                                    @ApiParam(name = "assembly", value = ASSEMBLY_DESCRIPTION) @DefaultValue("") @QueryParam("assembly")
                                            String assembly,
                                    @ApiParam(name = "dataRelease", value = DATA_RELEASE_DESCRIPTION) @DefaultValue("0")
                                    @QueryParam("dataRelease") int dataRelease,
                                    @ApiParam(name = "apiKey", value = API_KEY_DESCRIPTION) @DefaultValue("") @QueryParam("apiKey")
                                            String apiKey,
                                    @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws QueryException, IOException, CellBaseException {
        super(apiVersion, species, uriInfo, hsr);
        if (assembly == null) {
            assembly = SpeciesUtils.getDefaultAssembly(cellBaseConfiguration, species).getName();
        }

        clinicalManager = cellBaseManagerFactory.getClinicalManager(species, assembly);
        pharmacogenomicsManager = cellBaseManagerFactory.getPharmacogenomicsManager(species, assembly);
    }

    @GET
    @Path("/search")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time. "
            + DOT_NOTATION_NOTE,
            value = "Retrieves all chemicals/drugs", response = PharmaChemical.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "count", value = COUNT_DESCRIPTION,
                    required = false, dataType = "boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true"),
//            @ApiImplicitParam(name = SOURCE_PARAM, value = SOURCE_DESCRIPTION,
//                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "name", value = "List of chemical/drug names, e.g.: warfarin. In order to get the list of chemical or"
                    + " drug names, please, call the endpoint pharmacogenomics/distinct?field=names", dataType = "java.util.List",
                    paramType = "query"),
            @ApiImplicitParam(name = "type", value = "List of chemical/drug types, e.g.: Drug,Metabolite. In order to get the list of"
                    + " chemical or drug types, please, call the endpoint pharmacogenomics/distinct?field=types",
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "variant", value = "List of variants (dbSNP IDs), e.g.: rs1429376,rs11191561. In order to get the list"
                    + " of variant IDs, please, call the endpoint endpoint pharmacogenomics/distinct?field=variants.variantId",
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "haplotype", value = "List of haplotypes, e.g.: CYP2A6*1. In order to get the list of gene names,"
                    + "please, call the endpoint endpoint pharmacogenomics/distinct?field=variants.haplotypes", dataType = "java.util.List",
                    paramType = "query"),
            @ApiImplicitParam(name = "geneName", value = "List of gene names, e.g.: NT5C2,VKORC1. In order to get the list of gene names,"
                    + "please, call the endpoint endpoint pharmacogenomics/distinct?field=variants.geneNames", dataType = "java.util.List",
                    paramType = "query"),
            @ApiImplicitParam(name = "location", value = "List of chromosomic coordinates in the format: chromosome:position, e.g.:"
                    + " 10:103109774", dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "phenotype", value = "List of phenotypes, e.g.: Hemorrhage,Thrombosis. In order to get the list of"
                    + "phenotype values, please, call the endpoint pharmacogenomics/distinct?field=variants.phenotypes",
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "phenotypeType", value = "List of phenotype categories (i.e., association phenotype), e.g.: Dosage,"
                    + "Toxicity. In order to get the list of phenotype category values, please, call the endpoint pharmacogenomics/distinct"
                    + "?field=variants.phenotypeTypes", dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "confidence", value = "List of confidence values. Valid values: 1A, 1B, 2A, 2B, 3, 4",
                    dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "pubmedId", value = "List of evidence PubMed IDs, e.g.: 14765194", dataType = "java.util.List",
                    paramType = "query"),
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
    public Response getAll() {
        try {
            PharmaChemicalQuery query = new PharmaChemicalQuery(uriParams);
            CellBaseDataResult<PharmaChemical> queryResults = pharmacogenomicsManager.search(query);

            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chemicals}/info")
    @ApiOperation(httpMethod = "GET", value = "Get information about the specified chemical(s) or drug(s)", response = PharmaChemical.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "exclude", value = EXCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "include", value = INCLUDE_DESCRIPTION,
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getInfo(@PathParam("chemicals") @ApiParam(name = "chemicals", value = "Chemical/drug names", required = true)
                                    String chemicals) {
        try {
            PharmaChemicalQuery pharmaQuery = new PharmaChemicalQuery(uriParams);
            List<CellBaseDataResult<PharmaChemical>> queryResults = pharmacogenomicsManager.info(Arrays.asList(chemicals.split(",")),
                    pharmaQuery, getDataRelease(), getApiKey());
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/distinct")
    @ApiOperation(httpMethod = "GET", notes = "Gets a unique list of values, e.g. variants.location",
            value = "Get a unique list of values for a given field.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type", value = "List of types",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "gene", value = "List of gene names",
                    required = false, dataType = "java.util.List", paramType = "query"),
    })
    public Response getUniqueValues(@QueryParam("field") @ApiParam(name = "field", required = true,
            value = "Name of column to return, e.g. variants.location") String field) {
        try {
            copyToFacet("field", field);
            PharmaChemicalQuery query = new PharmaChemicalQuery(uriParams);
            CellBaseDataResult<String> queryResults = pharmacogenomicsManager.distinct(query);
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

}
