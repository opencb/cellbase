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
import io.swagger.annotations.ApiParam;
import org.opencb.biodata.models.core.Chromosome;
import org.opencb.cellbase.core.api.GenomeDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResponse;
import org.opencb.commons.datastore.core.QueryResult;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author imedina
 */
@Path("/{version}/{species}/genomic/chromosome")
@Produces("application/json")
@Api(value = "Genome Sequence", description = "Genome Sequence RESTful Web Services API")
public class ChromosomeWSServer extends GenericRestWSServer {


    public ChromosomeWSServer(@PathParam("version")
                              @ApiParam(name = "version", value = "Use 'latest' for last stable version",
                                      defaultValue = "latest") String version,
                              @PathParam("species")
                              @ApiParam(name = "species", value = "Name of the species, e.g.: hsapiens. For a full list "
                                      + "of potentially available species ids, please refer to: "
                                      + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/latest/meta/species") String species,
                              @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException {
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
    @Path("/all")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the chromosome objects", response = Chromosome.class,
        responseContainer = "QueryResponse")
    public Response getChromosomesAll() {
        try {
            parseQueryParams();
            GenomeDBAdaptor dbAdaptor = dbAdaptorFactory2.getGenomeDBAdaptor(this.species, this.assembly);
            return createOkResponse(dbAdaptor.getGenomeInfo(queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/list")
    @ApiOperation(httpMethod = "GET", value = "Retrieves the chromosomes names", response = QueryResponse.class)
    public Response getChromosomes() {
        try {
            parseQueryParams();
            GenomeDBAdaptor dbAdaptor = dbAdaptorFactory2.getGenomeDBAdaptor(this.species, this.assembly);
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
            GenomeDBAdaptor dbAdaptor = dbAdaptorFactory2.getGenomeDBAdaptor(this.species, this.assembly);
//            return createOkResponse(dbAdaptor.getAllByChromosomeIdList(Splitter.on(",").splitToList(query), queryOptions));
            List<String> chromosomeList = Splitter.on(",").splitToList(chromosomeId);
            List<QueryResult> queryResults = new ArrayList<>(chromosomeList.size());
            for (String chromosome : chromosomeList) {
                queryResults.add(dbAdaptor.getChromosomeInfo(chromosome, queryOptions));
            }
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{chromosomeName}/size")
    @ApiOperation(httpMethod = "GET", value = "Not properly implemented - to be fixed",
            response = Chromosome.class, responseContainer = "QueryResponse", hidden = true)
    public Response getChromosomeSize(@PathParam("chromosomeName") String chromosomeId) {
        try {
            parseQueryParams();
            GenomeDBAdaptor dbAdaptor = dbAdaptorFactory2.getGenomeDBAdaptor(this.species, this.assembly);
            QueryOptions options = new QueryOptions("include", "chromosomes.size");
//            return createOkResponse(dbAdaptor.getChromosomeById(query, options));
            List<String> chromosomeList = Splitter.on(",").splitToList(chromosomeId);
            List<QueryResult> queryResults = new ArrayList<>(chromosomeList.size());
            for (String chromosome : chromosomeList) {
                queryResults.add(dbAdaptor.getChromosomeInfo(chromosome, options));
            }
            return createOkResponse(queryResults);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

//    @GET
//    @Path("/{chromosomeName}/cytoband")
//    public Response getByChromosomeName(@PathParam("chromosomeName") String query) {
//        try {
//            parseQueryParams();
//            GenomeDBAdaptor dbAdaptor = dbAdaptorFactory2.getGenomeDBAdaptor(this.species, this.assembly);
//            return createOkResponse(dbAdaptor.getAllCytobandsByIdList(Splitter.on(",").splitToList(query), queryOptions));
//        } catch (Exception e) {
//            return createErrorResponse(e);
//        }
//    }


    @GET
    public Response defaultMethod() {
        return help();
    }

    @GET
    @Path("/help")
    public Response help() {
        return createOkResponse("Usage:");
    }
}
