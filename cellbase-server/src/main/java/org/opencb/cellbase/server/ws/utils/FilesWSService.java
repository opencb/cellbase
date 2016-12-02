package org.opencb.cellbase.server.ws.utils;

import io.swagger.annotations.*;
import org.ga4gh.models.ReadAlignment;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.lib.impl.FilesDBAdaptor;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.commons.datastore.core.QueryResult;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by swaathi on 24/11/16.
 */
@Path("/{version}/{species}/files")
@Api(value = "File", description = "File RESTful Web Services API")
@Produces(MediaType.APPLICATION_JSON)
public class FilesWSService extends GenericRestWSServer {

    public FilesWSService(@PathParam("version")
                        @ApiParam(name = "version", value = "Use 'latest' for last stable version", defaultValue = "latest") String version,
                          @PathParam("species")
                        @ApiParam(name = "species", value = "Name of the species, e.g.: hsapiens. For a full list "
                                + "of potentially available species ids, please refer to: "
                                + "http://bioinfo.hpc.cam.ac.uk/cellbase/webservices/rest/latest/meta/species") String species,
                        @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    @GET
    @Path("/list")
    @ApiOperation(httpMethod = "GET", value = "List all the contents in the file path", response = File.class,
            responseContainer = "QueryResponse")
    public Response listAllFolders() {
        FilesDBAdaptor filesDBAdaptor = new FilesDBAdaptor(cellBaseConfiguration);
        try {
            QueryResult<File> queryResult = filesDBAdaptor.list(Paths.get(cellBaseConfiguration.getFilePath()));
            return createOkResponse(queryResult);
        } catch (IOException e) {
            return createErrorResponse(e);
        }

    }

    @GET
    @Path("/{folderId}/list")
    @ApiOperation(httpMethod = "GET", value = "List the files present in the given folder", response = File.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "format", value = "Different formats like bam or vcf", dataType = "string", paramType = "query")
    })
    public Response listByFolder(@PathParam("folderId")
                                     @ApiParam(name = "folderId", value = "Folder Name", required = true) String folderId) {
        try {
            parseQueryParams();
            FilesDBAdaptor filesDBAdaptor = new FilesDBAdaptor(cellBaseConfiguration);
            QueryResult<File> result = filesDBAdaptor.listByFolder(Paths.get(cellBaseConfiguration.getFilePath()).resolve(folderId), query);
            result.setId(folderId);
            return createOkResponse(result);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{folderId}/bam")
    @ApiOperation(httpMethod = "GET", value = "Query the given bam file", response = ReadAlignment.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "Bam file name", required = true, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "region", value = "region to be queried in the file Ex: 3:444-5555", dataType = "string",
                    paramType = "query"),
            @ApiImplicitParam(name = "mapQ", value = "Mapping quality", dataType = "string", paramType = "query")
    })
    public Response getBamFile(@PathParam("folderId")
                                   @ApiParam(name = "folderId", value = "Folder in which the file is located") String folderId) {
        try {
            parseQueryParams();
            FilesDBAdaptor filesDBAdaptor = new FilesDBAdaptor(cellBaseConfiguration);
            java.nio.file.Path filePath = Paths.get(cellBaseConfiguration.getFilePath())
                    .resolve(folderId)
                    .resolve(query.getString("file"));
            QueryResult<ReadAlignment> queryResult = filesDBAdaptor.getBamFile(filePath, query, queryOptions);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/{folderId}/vcf")
    @ApiOperation(httpMethod = "GET", value = "Query the given vcf file", response = Variant.class,
            responseContainer = "QueryResponse")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "VCF file name", required = true, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "region", value = "region to be queried in the file Ex: 3:444-5555", dataType = "string",
                    paramType = "query"),
            @ApiImplicitParam(name = "qual", value = "phred-scaled quality score for the assertion made in ALT", dataType = "string",
                    paramType = "query"),
            @ApiImplicitParam(name = "filter", value = "PASS if this position has passed all filters or if the site has not"
                    + " passed all filters, a semicolon-separated list of codes for filters that fail  e.g. “q10;s50”", dataType = "string",
                    paramType = "query")
    })
    public Response getVcfFile(@PathParam("folderId")
                               @ApiParam(name = "folderId", value = "Folder in which the file is located") String folderId) {
        try {
            parseQueryParams();
            FilesDBAdaptor filesDBAdaptor = new FilesDBAdaptor(cellBaseConfiguration);
            java.nio.file.Path filePath = Paths.get(cellBaseConfiguration.getFilePath())
                    .resolve(folderId)
                    .resolve(query.getString("file"));
            QueryResult<Variant> variantQueryResult = filesDBAdaptor.getVcfFile(filePath, query, queryOptions);
            return createOkResponse(variantQueryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
}
