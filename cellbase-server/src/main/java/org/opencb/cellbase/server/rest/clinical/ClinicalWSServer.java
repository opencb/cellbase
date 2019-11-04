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
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.ClinicalDBAdaptor;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.rest.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

/**
 * Created by fjlopez on 06/12/16.
 */
@Path("/{version}/{species}/clinical")
@Produces("application/json")
@Api(value = "Clinical", description = "Clinical RESTful Web Services API")
public class ClinicalWSServer extends GenericRestWSServer {


    public ClinicalWSServer(@PathParam("version")
                                  @ApiParam(name = "version", value = "Possible values: v3, v4",
                                          defaultValue = "v4") String version,
                                  @PathParam("species")
                                  @ApiParam(name = "species", value = "Name of the species, e.g.: hsapiens. For a full list "
                                          + "of potentially available species ids, please refer to: "
                                          + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/meta/species") String species,
                                  @Context UriInfo uriInfo, @Context HttpServletRequest hsr)
            throws VersionException, SpeciesException, IOException, CellbaseException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/variant/search")
    @ApiOperation(httpMethod = "GET", notes = "No more than 1000 objects are allowed to be returned at a time. ",
            value = "Retrieves all clinical variants", response = Variant.class, responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "source",
                    value = "Comma separated list of database sources of the documents to be returned. Possible values "
                            + " are clinvar,cosmic or iarctp53. E.g.: clinvar,cosmic",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "region",
                    value = "Comma separated list of genomic regions to be queried, e.g.: 1:6635137-6635325",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "so",
                    value = "Comma separated list of sequence ontology term names, e.g.: missense_variant. Exact text "
                            + "matches will be returned. A list of searchable SO term names can be accessed at "
                            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/feature/variation/consequence_types",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "feature",
                    value = "Comma separated list of feature ids, which can be either ENSEMBL gene ids, HGNC gene symbols,"
                            + " transcript symbols or ENSEMBL transcript ids, e.g.: BRCA2, ENST00000409047. Exact text"
                            + " matches will be returned.",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "trait",
                    value = "Keywords search. Comma separated (no spaces in between) list of keywords describing required"
                            + " phenotype/disease. All variants related somehow with all those keywords (case insensitive) "
                            + " will be returned, e.g: carcinoma,lung or acute,myeloid,leukaemia. WARNING: returned "
                            + " numTotalResults will always be -1 when searching by trait keywords.",
                    required = false, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "accession",
                    value = "Comma separated list of database accesions, e.g.: RCV000033215,COSM306824 Exact text "
                            + "matches will be returned.",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "id",
                    value = "Comma separated list of ids, e.g.: rs6025 Exact text matches will be returned.",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "type",
                    value = "Comma separated list of variant types, e.g. \"SNV\" A list of searchable types can be accessed at "
                            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/clinical/variant/type",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "consistencyStatus",
                    value = "Comma separated list of consistency labels. A list of searchable consistency labels can be accessed at "
                            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/clinical/variant/consistency_labels",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "clinicalSignificance",
                    value = "Comma separated list of clinical significance labels. A list of searchable clinical "
                            + " significance labels can be accessed at "
                            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/clinical/variant/clinsig_labels"
                            + " WARNING: returned numTotalResults will always be -1 if more than 1 label is provided.",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "modeInheritance",
                    value = "Comma separated list of mode of inheritance labels. A list of searchable mode of inheritance "
                            + " labels can be accessed at "
                            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/"
                                + "clinical/variant/mode_inheritance_labels",
                    required = false, dataType = "java.util.List", paramType = "query"),
            @ApiImplicitParam(name = "alleleOrigin",
                    value = "Comma separated list of allele origin labels. A list of searchable allele origin "
                            + " labels can be accessed at "
                            + "https://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/v4/hsapiens/clinical/variant/allele_origin_labels",
                    required = false, dataType = "java.util.List", paramType = "query")
    })
    public Response getAll() {
        try {
            parseQueryParams();
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(this.species, this.assembly);

            return createOkResponse(clinicalDBAdaptor.nativeGet(query, queryOptions));
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/variant/allele_origin_labels")
    @ApiOperation(httpMethod = "GET", notes = "",
            value = "Retrieves all available allele origin labels", response = Variant.class,
            responseContainer = "QueryResponse")
    public Response getAlleleOriginLabels() {
        try {
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(this.species, this.assembly);

            return createOkResponse(clinicalDBAdaptor.getAlleleOriginLabels());
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/variant/mode_inheritance_labels")
    @ApiOperation(httpMethod = "GET", notes = "",
            value = "Retrieves all available mode of inheritance labels", response = Variant.class,
            responseContainer = "QueryResponse")
    public Response getModeInheritanceLabels() {
        try {
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(this.species, this.assembly);

            return createOkResponse(clinicalDBAdaptor.getModeInheritanceLabels());
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/variant/clinsig_labels")
    @ApiOperation(httpMethod = "GET", notes = "",
            value = "Retrieves all available clinical significance labels", response = Variant.class,
            responseContainer = "QueryResponse")
    public Response getClinicalSignificanceLabels() {
        try {
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(this.species, this.assembly);

            return createOkResponse(clinicalDBAdaptor.getClinsigLabels());
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/variant/consistency_labels")
    @ApiOperation(httpMethod = "GET", notes = "",
            value = "Retrieves all available consistency labels", response = Variant.class,
            responseContainer = "QueryResponse")
    public Response getConsistencyLabels() {
        try {
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(this.species, this.assembly);

            return createOkResponse(clinicalDBAdaptor.getConsistencyLabels());
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/variant/type")
    @ApiOperation(httpMethod = "GET", notes = "",
            value = "Retrieves all available variant types", response = Variant.class,
            responseContainer = "QueryResponse")
    public Response getVariantTypes() {
        try {
            ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor(this.species, this.assembly);

            return createOkResponse(clinicalDBAdaptor.getVariantTypes());
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

}
