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
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.opencb.biodata.models.feature.Region;
import org.opencb.cellbase.core.lib.api.core.GeneDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.ClinVarDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.ClinicalDBAdaptor;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.datastore.core.QueryResponse;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author imedina
 */
@Path("/{version}/{species}/feature/clinical")
@Produces("application/json")
@Api(value = "ClinVar", description = "ClinVar RESTful Web Services API")
public class ClinicalWSServer extends GenericRestWSServer {

    public ClinicalWSServer(@PathParam("version") String version, @PathParam("species") String species,
                            @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/all")
    @ApiOperation(httpMethod = "GET", value = "Retrieves all the clinvar objects", response = QueryResponse.class)
    public Response getAll(@DefaultValue("") @QueryParam("rcv") String rcv,
                           @DefaultValue("") @QueryParam("rs") String rs,
                           @DefaultValue("") @QueryParam("so") String so,
                           @DefaultValue("") @QueryParam("type") String type,
                           @DefaultValue("") @QueryParam("review") String review,
                           @DefaultValue("") @QueryParam("significance") String significance,
                           @DefaultValue("") @QueryParam("gene") String gene,
                           @DefaultValue("") @QueryParam("region") String region,
                           @DefaultValue("") @QueryParam("phenotype") String phenotype) {
        try {
            checkParams();
            Boolean noFilter = true;
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(this.species, this.assembly);
            if(rcv != null && !rcv.equals("")) {
                queryOptions.add("rcv", Arrays.asList(rcv.split(",")));
                noFilter = false;
            }
            if(rs != null && !rs.equals("")) {
                queryOptions.add("rs", Arrays.asList(rs.split(",")));
                noFilter = false;
            }
            if(so != null && !so.equals("")) {
                queryOptions.add("so", Arrays.asList(so.split(",")));
                noFilter = false;
            }
            if(type != null && !type.equals("")) {
                queryOptions.add("type", Arrays.asList(type.split(",")));
                noFilter = false;
            }
            if(review != null && !review.equals("")) {
                queryOptions.add("review", Arrays.asList(review.split(",")));
                noFilter = false;
            }
            if(significance != null && !significance.equals("")) {
                queryOptions.add("significance", Arrays.asList(significance.split(",")));
                noFilter = false;
            }
            if(gene != null && !gene.equals("")) {
                queryOptions.add("gene", Arrays.asList(gene.split(",")));
                noFilter = false;
            }
            if(region != null && !region.equals("")) {
                queryOptions.add("region", Region.parseRegions(region));
                noFilter = false;
            }
            if(phenotype != null && !phenotype.equals("")) {
                queryOptions.add("phenotype", Arrays.asList(phenotype.split(",")));
                noFilter = false;
            }
            if(noFilter && ((queryOptions.get("limit") == null || queryOptions.getInt("limit") > 1000))) {
                queryOptions.put("limit", 1000);
            }

            return createOkResponse(clinicalDBAdaptor.getAll(queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAll", e.toString());
        }
    }

    @GET
    @Path("/phenotype-gene")
    @ApiOperation(httpMethod = "GET", value = "Resource to get all phenotype-gene relations")
    public Response getPhenotypeGeneRelations() {

        try {
            checkParams();
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(this.species, this.assembly);
            return createOkResponse(clinicalDBAdaptor.getPhenotypeGeneRelations(queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getPhenotypeGeneRelations", e.toString());
        }
    }

    @GET
    @Path("/listAcc")
    @ApiOperation(httpMethod = "GET", value = "Resource to list all accession IDs")
    public Response getAllListAccessions() {
        try {
            checkParams();
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(this.species, this.assembly);
            return createOkResponse(clinicalDBAdaptor.getListClinvarAccessions(queryOptions));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("getAllListAccessions", e.toString());
        }
    }

}
