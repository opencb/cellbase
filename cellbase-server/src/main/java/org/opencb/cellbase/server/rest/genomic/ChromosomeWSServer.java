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

import com.google.common.base.Splitter;
import io.swagger.annotations.*;
import org.opencb.biodata.models.core.Chromosome;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.rest.GenericRestWSServer;
import org.opencb.commons.datastore.core.QueryOptions;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author imedina
 */
@Path("/{version}/{species}/genomic/chromosome")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Genome Sequence", description = "Genome Sequence RESTful Web Services API")
public class ChromosomeWSServer extends GenericRestWSServer {


    public ChromosomeWSServer(@PathParam("version")
                              @ApiParam(name = "version", value = "Possible values: v4, v5",
                                      defaultValue = "v5") String version,
                              @PathParam("species")
                              @ApiParam(name = "species", value = "Name of the species, e.g.: hsapiens. For a full list "
                                      + "of potentially available species ids, please refer to: "
                                      + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/meta/species") String species,
                              @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException, CellbaseException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/model")
    @ApiOperation(httpMethod = "GET", value = "Returns a JSON specification of the Chromosome data model",
            response = Chromosome.class, responseContainer = "QueryResponse")
    public Response getModel() {
        return createModelResponse(Chromosome.class);
    }

    @GET
    @Path("/search")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the chromosome objects", response = Chromosome.class,
        responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "count",
                    value = "Get a count of the number of results obtained. Deactivated by default.",
                    required = false, dataType = "boolean", paramType = "query", defaultValue = "false",
                    allowableValues = "false,true")
    })
    public Response getChromosomesAll(@QueryParam("limit") @DefaultValue("10")
                                          @ApiParam(value = "Max number of results to be returned. Cannot exceed 5,000.") Integer limit,
                                      @QueryParam("skip") @DefaultValue("0")
                                          @ApiParam(value = "Number of results to be skipped.")  Integer skip) {
        try {
            parseExtraQueryParams(limit, skip);
            parseQueryParams();
            GenomeDBAdaptor dbAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(this.species, this.assembly);
            return createOkResponse(dbAdaptor.getGenomeInfo(queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/list")
    @Deprecated
    @ApiOperation(httpMethod = "GET", value = "Retrieves the chromosomes names", response = CellBaseDataResult.class,
        hidden = true)
    public Response getChromosomes() {
        try {
            parseQueryParams();
            GenomeDBAdaptor dbAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(this.species, this.assembly);
            QueryOptions options = new QueryOptions();
            options.put("include", "chromosomes.name");
            return createOkResponse(dbAdaptor.getGenomeInfo(options));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chromosomeName}/info")
    @ApiOperation(httpMethod = "GET", value = "Retrieves chromosome data for specified chromosome names",
            response = Chromosome.class, responseContainer = "QueryResponse")
    public Response getChromosomes(@PathParam("chromosomeName")
                                   @ApiParam(name = "chromosomeName", value = "Comma separated list of chromosome ids,"
                                           + " e.g.: 1,2,X,MT. Exact text matches will be returned.",
                                                required = true) String chromosomeId) {
        try {
            parseQueryParams();
            GenomeDBAdaptor dbAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(this.species, this.assembly);
            List<String> chromosomeList = Splitter.on(",").splitToList(chromosomeId);
            List<CellBaseDataResult> queryResults = new ArrayList<>(chromosomeList.size());
            for (String chromosome : chromosomeList) {
                CellBaseDataResult queryResult = dbAdaptor.getChromosomeInfo(chromosome, queryOptions);
                queryResult.setId(chromosome);
                queryResults.add(queryResult);
            }
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chromosomeName}/size")
    @Deprecated
    @ApiOperation(httpMethod = "GET", value = "Not properly implemented - to be fixed",
            response = Chromosome.class, responseContainer = "QueryResponse", hidden = true)
    public Response getChromosomeSize(@PathParam("chromosomeName") String chromosomeId) {
        try {
            parseQueryParams();
            GenomeDBAdaptor dbAdaptor = dbAdaptorFactory.getGenomeDBAdaptor(this.species, this.assembly);
            QueryOptions options = new QueryOptions("include", "chromosomes.size");
            List<String> chromosomeList = Splitter.on(",").splitToList(chromosomeId);
            List<CellBaseDataResult> queryResults = new ArrayList<>(chromosomeList.size());
            for (String chromosome : chromosomeList) {
                queryResults.add(dbAdaptor.getChromosomeInfo(chromosome, options));
            }
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/help")
    public Response help() {
        return createOkResponse("Usage:");
    }
}
