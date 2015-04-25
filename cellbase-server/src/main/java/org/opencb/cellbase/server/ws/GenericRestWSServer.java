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

package org.opencb.cellbase.server.ws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Splitter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.common.Species;
import org.opencb.cellbase.core.lib.DBAdaptorFactory;
import org.opencb.cellbase.core.lib.api.core.ChromosomeDBAdaptor;
import org.opencb.cellbase.mongodb.db.MongoDBAdaptorFactory;
import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResponse;
import org.opencb.datastore.core.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.ResponseBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@Path("/{version}/{species}")
@Produces("text/plain")
@Api(value = "Generic", description = "Generic RESTful Web Services API")
public class GenericRestWSServer implements IWSServer {

    // Common application parameters
//    @DefaultValue("")
//    @PathParam("version")
    @ApiParam(name = "version", value = "CellBase version. Use 'latest' for last version stable.",
            allowableValues = "v3,latest", defaultValue = "v3")
    protected String version;

    @DefaultValue("")
    @PathParam("species")
    @ApiParam(name = "species", value = "Name of the species to query",
            defaultValue = "hsapiens", allowableValues = "hsapiens,mmusculus,drerio,rnorvegicus,ptroglodytes,ggorilla,pabelii," +
            "mmulatta,sscrofa,cfamiliaris,ecaballus,ocuniculus,ggallus,btaurus,fcatus,cintestinalis,ttruncatus,lafricana,cjacchus," +
            "nleucogenys,aplatyrhynchos,falbicollis,celegans,dmelanogaster,dsimulans,dyakuba,agambiae,adarlingi,nvectensis," +
            "spurpuratus,bmori,aaegypti,apisum,scerevisiae,spombe,afumigatus,aniger,anidulans,aoryzae,foxysporum,pgraminis," +
            "ptriticina,moryzae,umaydis,ssclerotiorum,cneoformans,ztritici,pfalciparum,lmajor,ddiscoideum,pinfestans,glamblia," +
            "pultimum,alaibachii,athaliana,alyrata,bdistachyon,osativa,gmax,vvinifera,zmays,hvulgare,macuminata,sbicolor,sitalica," +
            "taestivum,brapa,ptrichocarpa,slycopersicum,stuberosum,smoellendorffii,creinhardtii,cmerolae,ecolimg1655,spneumoniae70585," +
            "sagalactiaenem316,saureusst398,saureusn315,smelilotiak83,sfrediingr234,sentericact18,sentericalt2,pluminescenstto1," +
            "nmeningitidisz2491,mgenitaliumg37,mtuberculosisasm19595v2,mavium104,lmonocytogenesegde,lplantarumwcfs1,hinfluenzaekw20," +
            "cglutamicumasm19633v1,cbotulinumhall,ctrachomatisduw3cx,blongumncc2705,bsubtilis168,blicheniformisasm1164v1," +
            "abaumanniiaye,paeruginosapa7,paeruginosapa14,paeruginosampao1p1,paeruginosampao1p2,cpneumoniaecwl029,pacanthamoebaeuv7," +
            "wchondrophila861044,cprotochlamydiauwe25,snegevensisz,csabeus,oaries,olatipes")
    protected String species;

    protected String assembly = null;
    protected UriInfo uriInfo;
    protected HttpServletRequest httpServletRequest;

    protected QueryOptions queryOptions;

    // file name without extension which server will give back when file format is !null
    private String filename;

    @DefaultValue("")
    @QueryParam("exclude")
    @ApiParam(name = "excluded fields", value = "Fields excluded in response. Whole JSON path e.g.: transcripts.id")
    protected String exclude;

    @DefaultValue("")
    @QueryParam("include")
    @ApiParam(name = "included fields", value = "Only fields included in response. Whole JSON path e.g.: transcripts.id")
    protected String include;

    @DefaultValue("-1")
    @QueryParam("limit")
    @ApiParam(name = "limit", value = "Max number of results to be returned. No limit applied when -1. No limit is set by default.")
    protected int limit;

    @DefaultValue("-1")
    @QueryParam("skip")
    @ApiParam(name = "skip", value = "Number of results to be skipped. No skip applied when -1. No skip by default.")
    protected int skip;

    @DefaultValue("false")
    @QueryParam("count")
    @ApiParam(name = "count", value = "Get a count of the number of results obtained. Deactivated by default.",
            defaultValue = "false", allowableValues = "false,true")
    protected String count;

    @DefaultValue("json")
    @QueryParam("of")
    @ApiParam(name = "Output format", value = "Output format, Protobuf is not yet implemented", defaultValue = "json", allowableValues = "json,pb (Not implemented yet)")
    protected String outputFormat;

    protected static ObjectMapper jsonObjectMapper;
    protected static ObjectWriter jsonObjectWriter;

    protected long startTime;
    protected long endTime;
    protected QueryResponse queryResponse;

    protected static Logger logger;

    /**
     * Loading properties file just one time to be more efficient. All methods
     * will check parameters so to avoid extra operations this config can load
     * versions and species
     */
    protected static CellBaseConfiguration cellBaseConfiguration = new CellBaseConfiguration();

    /**
     * DBAdaptorFactory creation, this object can be initialize with an
     * HibernateDBAdaptorFactory or an HBaseDBAdaptorFactory. This object is a
     * factory for creating adaptors like GeneDBAdaptor
     */
    protected static DBAdaptorFactory dbAdaptorFactory;

    static {
//        dbAdaptorFactory = new MongoDBAdaptorFactory(config);

        logger = LoggerFactory.getLogger("org.opencb.cellbase.server.ws.GenericRestWSServer");

        logger.info("Static block, creating MongoDBAdapatorFactory");
        try {
            if (System.getenv("CELLBASE_HOME") != null) {
                logger.info("Loading configuration from '{}'", System.getenv("CELLBASE_HOME")+"/configuration.json");
                cellBaseConfiguration = CellBaseConfiguration
                        .load(new FileInputStream(new File(System.getenv("CELLBASE_HOME") + "/configuration.json")));
            } else {
                logger.info("Loading configuration from '{}'",
                        CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json").toString());
                cellBaseConfiguration = CellBaseConfiguration
                        .load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
            }

            // If Configuration has been loaded we can create the DBAdaptorFactory
            dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        jsonObjectMapper = new ObjectMapper();
        jsonObjectWriter = jsonObjectMapper.writer();
    }

    public GenericRestWSServer(@PathParam("version") String version, @PathParam("species") String species,
                               @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
        logger.debug("Executing GenericRestWSServer constructor");

        this.version = version;
        this.species = species;
        this.uriInfo = uriInfo;
        this.httpServletRequest = hsr;

        init(version, species, uriInfo);
    }

    protected void init(String version, String species, UriInfo uriInfo) throws VersionException, IOException {

        startTime = System.currentTimeMillis();
        queryResponse = new QueryResponse();

        // mediaType = MediaType.valueOf("text/plain");
        queryOptions = new QueryOptions();

        try {
            checkParams();
        } catch (SpeciesException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void checkParams() throws VersionException, SpeciesException {
        if (version == null) {
            throw new VersionException("Version not valid: '" + version + "'");
        }
        if (species == null) {
            throw new SpeciesException("Species not valid: '" + species + "'");
        }

        /**
         * Check version parameter, must be: v1, v2, ... If 'latest' then is
         * converted to appropriate version
         */
        if (version.equalsIgnoreCase("latest")) {
            version = cellBaseConfiguration.getVersion();
            logger.info("Version 'latest' detected, setting version parameter to '{}'", version);
        }

        if (!cellBaseConfiguration.getVersion().equalsIgnoreCase(this.version)) {
            logger.error("Version '{}' does not match configuration '{}'", this.version, cellBaseConfiguration.getVersion());
            throw new VersionException("Version not valid: '" + version + "'");
        }

//        parseCommonQueryParameters(uriInfo.getQueryParameters());
        MultivaluedMap<String, String> multivaluedMap = uriInfo.getQueryParameters();
        queryOptions.put("metadata", (multivaluedMap.get("metadata") != null) ? multivaluedMap.get("metadata").get(0).equals("true") : true);

        queryOptions.put("exclude", (exclude != null && !exclude.equals("")) ? new LinkedList<>(Splitter.on(",").splitToList(exclude)) : null);
        queryOptions.put("include", (include != null && !include.equals("")) ? new LinkedList<>(Splitter.on(",").splitToList(include)) : null);
        queryOptions.put("limit", (limit > 0) ? limit : -1);
        queryOptions.put("skip", (skip > 0) ? skip : -1);
        queryOptions.put("count", (count != null && !count.equals("")) ? Boolean.parseBoolean(count) : false);

        outputFormat = (outputFormat != null && !outputFormat.equals("")) ? outputFormat : "json";
        filename = (multivaluedMap.get("filename") != null) ? multivaluedMap.get("filename").get(0) : "result";
    }

    @Override
    public String stats() {
        return null;
    }

    @GET
    @Path("/help")
    public Response help() {
        return createOkResponse("No help available");
    }

    /**
     * Auxiliar methods
     */
    @GET
    @Path("/{species}")
    public Response getCategories(@PathParam("species") String species) {
        if (isSpecieAvailable(species)) {
            return createOkResponse("feature\ngenomic\nnetwork\nregulatory");
        }
        return getSpecies();
    }

    @GET
    @Path("/{species}/{category}")
    public Response getCategory(@PathParam("species") String species, @PathParam("category") String category) {
        if (isSpecieAvailable(species)) {
            if ("feature".equalsIgnoreCase(category)) {
                return createOkResponse("exon\ngene\nkaryotype\nprotein\nsnp\ntranscript");
            }
            if ("genomic".equalsIgnoreCase(category)) {
                return createOkResponse("position\nregion\nvariant");
            }
            if ("network".equalsIgnoreCase(category)) {
                return createOkResponse("pathway");
            }
            if ("regulatory".equalsIgnoreCase(category)) {
                return createOkResponse("mirna_gene\nmirna_mature\ntf");
            }
            return createOkResponse("feature\ngenomic\nnetwork\nregulatory");
        } else {
            return getSpecies();
        }
    }

    @GET
    @Path("/{species}/{category}/{subcategory}")
    public Response getSubcategory(@PathParam("species") String species, @PathParam("category") String category,
                                   @PathParam("subcategory") String subcategory) {
        return getCategory(species, category);
    }

    @GET
    @Path("/version")
    public Response getVersion() {
        StringBuilder versionMessage = new StringBuilder();
        versionMessage.append("Homo sapiens").append("\t").append("Ensembl 64").append("\n");
        versionMessage.append("Mus musculus").append("\t").append("Ensembl 65").append("\n");
        versionMessage.append("Rattus norvegicus").append("\t").append("Ensembl 65").append("\n");
        versionMessage.append("Drosophila melanogaster").append("\t").append("Ensembl 65").append("\n");
        versionMessage.append("Canis familiaris").append("\t").append("Ensembl 65").append("\n");
        versionMessage.append("...").append("\n\n");
        versionMessage
                .append("The rest of nfo will be added soon, sorry for the inconveniences. You can find mor info at:")
                .append("\n\n").append("http://docs.bioinfo.cipf.es/projects/variant/wiki/Databases");
        return createOkResponse(versionMessage.toString(), MediaType.valueOf("text/plain"));
    }

    @GET
    @Path("/species")
    @Deprecated
    public Response getSpecies() {
        List<Species> speciesList = getSpeciesList();
        MediaType mediaType = MediaType.valueOf("application/javascript");
        if (uriInfo.getQueryParameters().get("of") != null
                && uriInfo.getQueryParameters().get("of").get(0).equalsIgnoreCase("json")) {
            try {
                return createOkResponse(jsonObjectWriter.writeValueAsString(speciesList), mediaType);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (Species sp : speciesList) {
                stringBuilder.append(sp.toString()).append("\n");
            }
            mediaType = MediaType.valueOf("text/plain");
            return createOkResponse(stringBuilder.toString(), mediaType);
        }
    }

    @GET
    @Path("/i")
    @ApiOperation(httpMethod = "GET", value = "Retrieves genome info for current species", response = QueryResponse.class)
    public Response getSpeciesInfo2(@ApiParam(value = "String indicating the output format.", allowableValues = "json", defaultValue = "json")
                                        @DefaultValue("json") @QueryParam("of") String of) {
        return createOkResponse(getSpeciesDataFromDB(species));
    }

    /**
     * Given a species return all data regarding its genome stored in the DB.
     *
     * @param species    String containing the species id, either the short name or the scientific name (e.g. hsapiens, Homo sapiens)
     * @return A QueryResult containing genome data.
     */
    private QueryResult getSpeciesDataFromDB(String species) {
        // Not all species indicated at configuration.json are necessarily installed in the DB.
        try {
            ChromosomeDBAdaptor chromosomeDBAdaptor = dbAdaptorFactory.getChromosomeDBAdaptor(species, this.assembly);
            return chromosomeDBAdaptor.speciesInfoTmp(getSpecies(species).getScientificName(), queryOptions);
        } catch (com.mongodb.MongoException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Given a species name returns all species info stored at the configuration data.
     *
     * @param speciesName    String containing the species id, either the short name or the scientific name (e.g. hsapiens, Homo sapiens)
     * @return A Species object containing all species info stored with configuration data.
     */
    private CellBaseConfiguration.SpeciesProperties.Species getSpecies(String speciesName) {
        CellBaseConfiguration.SpeciesProperties.Species species = null;
        for (CellBaseConfiguration.SpeciesProperties.Species sp: cellBaseConfiguration.getAllSpecies()) {
            if (speciesName.equalsIgnoreCase(sp.getId()) || speciesName.equalsIgnoreCase(sp.getScientificName())) {
                species = sp;
                break;
            }
        }
        return species;
    }

    @GET
    @Path("/speciesinfo")
    @ApiOperation(httpMethod = "GET", value = "Retrieves genome info for all available species in current installation", response = QueryResponse.class)
    public Response getSpeciesInfo() {
        List<QueryResult> queryResults = new ArrayList<>();
        for (CellBaseConfiguration.SpeciesProperties.Species sp: cellBaseConfiguration.getAllSpecies()) {
            QueryResult queryResult = getSpeciesDataFromDB(sp.getId());
            if(queryResult!=null) {
                queryResults.add(queryResult);
            }
        }
        return createOkResponse(queryResults);
    }

    @GET
    @Path("/{species}/chromosomes")
    public Response getChromosomes(@PathParam("species") String species) {
        return null;
//        // TODO uncomment
//        return createOkResponse(config.getProperty("CELLBASE." + species.toUpperCase() + ".CHROMOSOMES"),
//                MediaType.valueOf("text/plain"));
    }

    @GET
    @Path("/echo/{msg}")
    public Response echo(@PathParam("msg") String msg) {
        logger.info(msg);
        logger.warn(msg);
        logger.debug(msg);
        logger.error(msg);
        return createStringResponse(msg);
    }


    @Deprecated
    protected Response generateResponse(String queryString, List features) throws IOException {
        return createOkResponse("TODO: generateResponse is drepecated");
    }

    @Deprecated
    protected Response generateResponse(String queryString, String headerTag, List features) throws IOException {
        return createOkResponse("TODO: generateResponse is drepecated");
    }


    protected Response createErrorResponse(String method, String errorMessage) {
        if (!errorMessage.contains("Species") && !errorMessage.contains("Version")) {
            // StringBuilder message = new StringBuilder();
            // message.append("URI: "+uriInfo.getAbsolutePath().toString()).append("\n");
            // message.append("Method: "+httpServletRequest.getMethod()+" "+method).append("\n");
            // message.append("Message: "+errorMessage).append("\n");
            // message.append("Remote Addr: http://ipinfodb.com/ip_locator.php?ip="+httpServletRequest.getRemoteAddr()).append("\n");
            // HttpUtils.send("correo.cipf.es", "fsalavert@cipf.es",
            // "babelomics@cipf.es", "Infrared error notice",
            // message.toString());
        }
        if (outputFormat.equalsIgnoreCase("json")) {
            try {
                return buildResponse(Response.ok(jsonObjectWriter.writeValueAsString(new HashMap<>().put("error", errorMessage)), MediaType.APPLICATION_JSON_TYPE));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else {
            String error = "An error occurred: " + errorMessage;
            return buildResponse(Response.ok(error, MediaType.valueOf("text/plain")));
        }
        return null;
    }

    protected Response createErrorResponse(Object o) {
        String objMsg = o.toString();
        if (objMsg.startsWith("ERROR:")) {
            return buildResponse(Response.ok("" + o));
        } else {
            return buildResponse(Response.ok("ERROR: " + o));
        }
    }

    protected Response createOkResponse(String message) {
        return buildResponse(Response.ok(message));
    }

    protected Response createOkResponse(QueryResult queryResult) {
        return createOkResponse(Arrays.asList(queryResult));
    }

    protected Response createOkResponse(List<QueryResult> queryResults) {
        switch (outputFormat.toLowerCase()) {
            case "json":
                return createJsonResponse(queryResults);
            case "xml":
                return createOkResponse(queryResults, MediaType.APPLICATION_XML_TYPE);
            default:
                return buildResponse(Response.ok(queryResults));
        }
    }

    protected Response createJsonResponse(List<QueryResult> obj) {
        endTime = System.currentTimeMillis() - startTime;
        queryResponse.setTime((int) endTime);
        queryResponse.setApiVersion(version);
        queryResponse.setQueryOptions(queryOptions);
        queryResponse.setResponse(obj);

//        queryResponse.put("species", species);
//        queryResponse.put("queryOptions", queryOptions);
//        queryResponse.put("response", obj);

        try {
            return buildResponse(Response.ok(jsonObjectWriter.writeValueAsString(queryResponse), MediaType.APPLICATION_JSON_TYPE));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Error parsing queryResponse object");
            return null;
        }
    }

    protected Response createOkResponse(Object obj, MediaType mediaType) {
        return buildResponse(Response.ok(obj, mediaType));
    }

    protected Response createOkResponse(Object obj, MediaType mediaType, String fileName) {
        return buildResponse(Response.ok(obj, mediaType).header("content-disposition", "attachment; filename =" + fileName));
    }

    protected Response createStringResponse(String str) {
        return buildResponse(Response.ok(str));
    }

    private Response buildResponse(ResponseBuilder responseBuilder) {
        return responseBuilder.header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    public Response getHelp() {
        return getSpecies();
    }

    private List<Species> getSpeciesList() {
        List<Species> speciesList = new ArrayList<>(11);
        speciesList.add(new Species("hsa", "human", "Homo sapiens", "GRCh37.p7"));
        speciesList.add(new Species("mmu", "mouse", "Mus musculus", "NCBIM37"));
        speciesList.add(new Species("rno", "rat", "Rattus norvegicus", "RGSC 3.4"));
        speciesList.add(new Species("dre", "zebrafish", "Danio rerio", "Zv9"));
        speciesList.add(new Species("cel", "worm", "Caenorhabditis elegans", "WS230"));
        speciesList.add(new Species("dme", "fruitfly", "Drosophila melanogaster", "BDGP 5.39"));
        speciesList.add(new Species("sce", "yeast", "Saccharomyces cerevisiae", "EF 4"));
        speciesList.add(new Species("cfa", "dog", "Canis familiaris", "CanFam 2.0"));
        speciesList.add(new Species("ssc", "pig", "Sus scrofa", "Sscrofa10.2"));
        speciesList.add(new Species("aga", "mosquito", "Anopheles gambiae", "AgamP3"));
        speciesList.add(new Species("pfa", "malaria parasite", "Plasmodium falciparum", "3D7"));
        speciesList.add(new Species("hsapiens", "", "", ""));
        speciesList.add(new Species("mmusculus", "", "", ""));
        speciesList.add(new Species("rnorvegicus", "", "", ""));
        speciesList.add(new Species("ptroglodytes", "", "", ""));
        speciesList.add(new Species("ggorilla", "", "", ""));
        speciesList.add(new Species("pabelii", "", "", ""));
        speciesList.add(new Species("mmulatta", "", "", ""));
        speciesList.add(new Species("sscrofa", "", "", ""));
        speciesList.add(new Species("cfamiliaris", "", "", ""));
        speciesList.add(new Species("ecaballus", "", "", ""));
        speciesList.add(new Species("ocuniculus", "", "", ""));
        speciesList.add(new Species("ggallus", "", "", ""));
        speciesList.add(new Species("btaurus", "", "", ""));
        speciesList.add(new Species("fcatus", "", "", ""));
        speciesList.add(new Species("drerio", "", "", ""));
        speciesList.add(new Species("cintestinalis", "", "", ""));
        speciesList.add(new Species("dmelanogaster", "", "", ""));
        speciesList.add(new Species("dsimulans", "", "", ""));
        speciesList.add(new Species("dyakuba", "", "", ""));
        speciesList.add(new Species("agambiae", "", "", ""));
        speciesList.add(new Species("celegans", "", "", ""));
        speciesList.add(new Species("scerevisiae", "", "", ""));
        speciesList.add(new Species("spombe", "", "", ""));
        speciesList.add(new Species("afumigatus", "", "", ""));
        speciesList.add(new Species("aniger", "", "", ""));
        speciesList.add(new Species("anidulans", "", "", ""));
        speciesList.add(new Species("aoryzae", "", "", ""));
        speciesList.add(new Species("pfalciparum", "", "", ""));
        speciesList.add(new Species("lmajor", "", "", ""));
        speciesList.add(new Species("athaliana", "", "", ""));
        speciesList.add(new Species("alyrata", "", "", ""));
        speciesList.add(new Species("bdistachyon", "", "", ""));
        speciesList.add(new Species("osativa", "", "", ""));
        speciesList.add(new Species("gmax", "", "", ""));
        speciesList.add(new Species("vvinifera", "", "", ""));
        speciesList.add(new Species("zmays", "", "", ""));


//        speciesList.add(new Species("hsapiens", "human", "Homo sapiens", "GRCh37.p13");
//        speciesList.add(new Species("mmusculus", "mouse", "Mus musculus", "GRCm38.p2"));
//        speciesList.add(new Species("rnorvegicus", "rat", "Rattus norvegicus", "Rnor_5.0"));
//        speciesList.add(new Species("ptroglodytes", "chimp", "Pan troglodytes", "CHIMP2.1.4"));
//        speciesList.add(new Species("ggorilla", "gorilla", "Gorilla gorilla", "gorGor3.1"));
//        speciesList.add(new Species("pabelii", "orangutan", "Pongo abelii", "PPYG2"));
//        speciesList.add(new Species("mmulatta", "macaque", "Macaca mulatta", "MMUL 1.0"));
//        speciesList.add(new Species("sscrofa", "pig", "Sus scrofa", "Sscrofa10.2"));
//        speciesList.add(new Species("cfamiliaris", "dog", "Canis familiaris", "CanFam 3.1"));
//        speciesList.add(new Species("ecaballus", "horse", "Equus caballus", "Equ Cab 2"));
//        speciesList.add(new Species("ocuniculus", "rabbit", "Oryctolagus cuniculus", "OryCun2.0"));
//        speciesList.add(new Species("ggallus", "chicken", "Gallus gallus", "Galgal4"));
//        speciesList.add(new Species("btaurus", "cow", "Bos taurus", "UMD3.1"));
//        speciesList.add(new Species("fcatus", "cat", "Felis catus", "Felis_catus_6.2"));
//        speciesList.add(new Species("drerio", "zebrafish", "Danio rerio", "Zv9"));
//        speciesList.add(new Species("cintestinalis", "", "Ciona intestinalis", "KH"));
//        speciesList.add(new Species("dmelanogaster", "fruitfly", "Drosophila melanogaster", "BDGP 5"));
//        speciesList.add(new Species("dsimulans", "", "Drosophila simulans", "dsim_caf1"));
//        speciesList.add(new Species("dyakuba", "", "Drosophila yakuba", "dyak_caf1"));
//        speciesList.add(new Species("agambiae", "mosquito", "Anopheles gambiae", "AgamP4"));
//        speciesList.add(new Species("celegans", "worm", "Caenorhabditis elegans", "WS235"));
//        speciesList.add(new Species("scerevisiae", "yeast", "Saccharomyces cerevisiae", "R64-1-1"));
//        speciesList.add(new Species("spombe", "", "Schizosaccharomyces pombe", "ASM294v2"));
//        speciesList.add(new Species("afumigatus", "", "Aspergillus fumigatus", "TIGR"));
//        speciesList.add(new Species("aniger", "", "Aspergillus niger", "DSM"));
//        speciesList.add(new Species("anidulans", "", "Aspergillus nidulans", "ASM1142v1"));
//        speciesList.add(new Species("aoryzae", "", "Aspergillus oryzae", "NITE"));
//        speciesList.add(new Species("pfalciparum", "malaria parasite", "Plasmodium falciparum", "3D7"));
//        speciesList.add(new Species("lmajor", "", "Plasmodium falciparum", "ASM276v1"));
//        speciesList.add(new Species("athaliana", "", "Arabidopsis thaliana", "TAIR10"));
//        speciesList.add(new Species("alyrata", "", "Arabidopsis lyrata", "v.1.0"));
//        speciesList.add(new Species("bdistachyon", "", "Brachypodium distachyon", "v1.0"));
//        speciesList.add(new Species("osativa", "", "Oryza sativa Indica", "ASM465v1"));
//        speciesList.add(new Species("gmax", "", "Glycine max", "V1.0"));
//        speciesList.add(new Species("vvinifera", "", "Vitis vinifera", "IGGP_12x"));
//        speciesList.add(new Species("zmays", "", "Zea mays", "AGPv3"));


        return speciesList;
    }

    /**
     * TO DELETE
     */
    @Deprecated
    private boolean isSpecieAvailable(String species) {
        List<Species> speciesList = getSpeciesList();
        for (int i = 0; i < speciesList.size(); i++) {
            // This only allows to show the information if species is in 3
            // letters format
            if (species.equalsIgnoreCase(speciesList.get(i).getSpecies())) {
                return true;
            }
        }
        return false;
    }

    //    protected Response createResponse(String response, MediaType mediaType) throws IOException {
//        logger.debug("CellBase - CreateResponse, QueryParams: FileFormat => " + fileFormat + ", OutputFormat => " + outputFormat + ", Compress => " + outputCompress);
//        logger.debug("CellBase - CreateResponse, Inferred media type: " + mediaType.toString());
//        logger.debug("CellBase - CreateResponse, Response: " + ((response.length() > 50) ? response.substring(0, 49) + "..." : response));
//
//        if (fileFormat == null || fileFormat.equalsIgnoreCase("")) {
//            if (outputCompress != null && outputCompress.equalsIgnoreCase("true")
//                    && !outputFormat.equalsIgnoreCase("jsonp") && !outputFormat.equalsIgnoreCase("jsontext")) {
//                response = Arrays.toString(StringUtils.gzipToBytes(response)).replace(" ", "");
//            }
//        } else {
//            mediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
//            logger.debug("\t\t - Creating byte stream ");
//
//            if (outputCompress != null && outputCompress.equalsIgnoreCase("true")) {
//                OutputStream bos = new ByteArrayOutputStream();
//                bos.write(response.getBytes());
//
//                ZipOutputStream zipstream = new ZipOutputStream(bos);
//                zipstream.setLevel(9);
//
//                logger.debug("CellBase - CreateResponse, zipping... Final media Type: " + mediaType.toString());
//
//                return this.createOkResponse(zipstream, mediaType, filename + ".zip");
//
//            } else {
//                if (fileFormat.equalsIgnoreCase("xml")) {
//                    // mediaType = MediaType.valueOf("application/xml");
//                }
//
//                if (fileFormat.equalsIgnoreCase("excel")) {
//                    // mediaType =
//                    // MediaType.valueOf("application/vnd.ms-excel");
//                }
//                if (fileFormat.equalsIgnoreCase("txt") || fileFormat.equalsIgnoreCase("text")) {
//                    logger.debug("\t\t - text File ");
//
//                    byte[] streamResponse = response.getBytes();
//                    // return Response.ok(streamResponse,
//                    // mediaType).header("content-disposition","attachment; filename = "+
//                    // filename + ".txt").build();
//                    return this.createOkResponse(streamResponse, mediaType, filename + ".txt");
//                }
//            }
//        }
//        logger.debug("CellBase - CreateResponse, Final media Type: " + mediaType.toString());
//        // return Response.ok(response, mediaType).build();
//        return this.createOkResponse(response, mediaType);
//    }

}
