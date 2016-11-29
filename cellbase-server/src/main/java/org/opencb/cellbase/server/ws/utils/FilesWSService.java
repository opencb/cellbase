package org.opencb.cellbase.server.ws.utils;

import htsjdk.samtools.SAMRecord;
import htsjdk.variant.variantcontext.VariantContext;
import io.swagger.annotations.*;
import org.apache.commons.lang3.time.StopWatch;
import org.ga4gh.models.ReadAlignment;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.tools.alignment.AlignmentOptions;
import org.opencb.biodata.tools.alignment.BamManager;
import org.opencb.biodata.tools.alignment.filters.AlignmentFilters;
import org.opencb.biodata.tools.alignment.filters.SamRecordFilters;
import org.opencb.biodata.tools.variant.VariantOptions;
import org.opencb.biodata.tools.variant.VcfManager;
import org.opencb.biodata.tools.variant.filters.VariantContextFilters;
import org.opencb.biodata.tools.variant.filters.VariantFilters;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.commons.utils.FileUtils;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        try {
            java.nio.file.Path folderPath = Paths.get(cellBaseConfiguration.getFilePath());
            FileUtils.checkDirectory(folderPath);

            List<File> files = new ArrayList<>();
            File[] fileArray = folderPath.toFile().listFiles();

            if (fileArray != null) {
                files = new ArrayList<>(Arrays.asList(fileArray));
            }
            QueryResult queryResult = new QueryResult(cellBaseConfiguration.getFilePath(), 0, files.size(), files.size(), null,
                    null, files);
            return createOkResponse(queryResult);
        } catch (Exception e) {
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

            java.nio.file.Path folderPath = Paths.get(cellBaseConfiguration.getFilePath()).resolve(folderId);
            FileUtils.checkDirectory(folderPath);

            List<File> files = new ArrayList<>();
            File[] fileArray;
            if (query != null && query.get("format") != null) {
                fileArray = folderPath.toFile().listFiles((dir, name) -> name.endsWith("." + query.getString("format")));
            } else {
                fileArray = folderPath.toFile().listFiles();
            }

            if (fileArray != null) {
                files = new ArrayList<>(Arrays.asList(fileArray));
            }
            QueryResult queryResult = new QueryResult(folderId, 0, files.size(), files.size(), null, null, files);
            return createOkResponse(queryResult);
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
            StopWatch watch = new StopWatch();
            watch.start();
            parseQueryParams();
            java.nio.file.Path filePath = Paths.get(cellBaseConfiguration.getFilePath())
                    .resolve(folderId)
                    .resolve(query.getString("file"));
            FileUtils.checkFile(filePath);

            BamManager alignmentManager = new BamManager(filePath);
            AlignmentFilters<SAMRecord> filters = SamRecordFilters.create().addMappingQualityFilter(query.getInt("mapQ", 0));
            AlignmentOptions options = new AlignmentOptions(false, false, false, AlignmentOptions.DEFAULT_LIMIT);

            String queryResultId = null;
            List<ReadAlignment> readAlignmentList = new ArrayList<>();
            String regionString = query.getString("region");
            if (regionString != null && !regionString.isEmpty()) {
                readAlignmentList = alignmentManager.query(Region.parseRegion(regionString), filters, options, ReadAlignment.class);
                queryResultId = regionString;
            }

            alignmentManager.close();
            watch.stop();
            QueryResult<ReadAlignment> queryResult = new QueryResult(queryResultId, ((int) watch.getTime()),
                    readAlignmentList.size(), readAlignmentList.size(), null, null,
                    readAlignmentList);
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
            StopWatch watch = new StopWatch();
            watch.start();
            parseQueryParams();

            java.nio.file.Path filePath = Paths.get(cellBaseConfiguration.getFilePath())
                    .resolve(folderId)
                    .resolve(query.getString("file"));
            FileUtils.checkFile(filePath);

            VcfManager vcfManager = new VcfManager(filePath);
            VariantFilters<VariantContext> variantContextVariantFilters = new VariantContextFilters();
            VariantOptions variantOptions = new VariantOptions();
            variantContextVariantFilters.addQualFilter(query.getDouble("qual", 0));
            if (query.get("filter") != null) {
                variantContextVariantFilters.addPassFilter(query.getString("filter"));
            }

            String regionString = query.getString("region");
            String queryResultId = null;
            List<Variant> variants = new ArrayList<>();
            if (regionString != null && !regionString.isEmpty()) {
                variants = vcfManager.query(Region.parseRegion(regionString), variantContextVariantFilters, variantOptions, Variant.class);
                queryResultId = regionString;
            } else {
                variants = vcfManager.query(null, variantContextVariantFilters, variantOptions, Variant.class);
            }
            vcfManager.close();
            watch.stop();

            QueryResult<Variant> queryResult = new QueryResult(queryResultId, ((int) watch.getTime()), variants.size(),
                    variants.size(), null, null, variants);
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
}
