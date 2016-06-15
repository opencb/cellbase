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

package org.opencb.cellbase.server.ws.network;

import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

@Path("/{version}/{species}/network/pathway")
@Produces("text/plain")
public class PathwayWSServer extends GenericRestWSServer {

    public PathwayWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo,
                           @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }

    /*
        @GET
        @Path("/list")
        public Response getAllPathways(@QueryParam("subpathways") String subpathways, @QueryParam("search") String search) {
            try {
                parseQueryParams();
                boolean onlyTopLevel = false;
                if (subpathways!=null) {
                    onlyTopLevel=!Boolean.parseBoolean(subpathways);
                }

                StringBuilder sb = new StringBuilder();
                PathwayDBAdaptor bioPaxDBAdaptor = dbAdaptorFactory.getPathwayDBAdaptor(this.species, this.assembly);
                List<Pathway> pathways = bioPaxDBAdaptor.getPathways("Reactome", search, onlyTopLevel);
                return generateResponse("", pathways);
            } catch (Exception e) {
                e.printStackTrace();
                return createErrorResponse("getAllPathways", e.toString());
            }
        }

        @GET
        @Path("/{pathwayId}/info")
        public Response getPathwayInfo(@PathParam("pathwayId") String query) {
            try {
                parseQueryParams();
                StringBuilder sb = new StringBuilder();
                PathwayDBAdaptor bioPaxDBAdaptor = dbAdaptorFactory.getPathwayDBAdaptor(this.species, this.assembly);
                Pathway pathway = bioPaxDBAdaptor.getPathway(query, "Reactome");
                return generateResponse("", Arrays.asList(pathway));
            } catch (Exception e) {
                e.printStackTrace();
                return createErrorResponse("getPathwayInfo", e.toString());
            }
        }


        @GET
        @Path("/{pathwayId}/image")
        @Produces("image/jpeg")
        public Response getPathwayImage(@PathParam("pathwayId") String query) {
            try {
                parseQueryParams();
                PathwayDBAdaptor bioPaxDBAdaptor = dbAdaptorFactory.getPathwayDBAdaptor(this.species, this.assembly);
                Pathway pathway = bioPaxDBAdaptor.getPathway(query, "Reactome");

                if (pathway!=null) {
                    String contentType = "image/jpeg";
                    String outFormat = "jpg";

                    String filename = query.replace(" ", "_").replace("(", "").replace(")", "").replace("/", "_").replace(":", "_");

                    DotServer dotServer = new DotServer();
                    Dot dot = dotServer.generateDot(pathway);

                    try {

                        File dotFile = new File("/tmp/" + filename + ".in");
                        File imgFile = new File("/tmp/" + filename + "." + outFormat);

                        dot.save(dotFile);
                        String cmd;
                        if ("dot".equalsIgnoreCase(outFormat) || "dotp".equalsIgnoreCase(outFormat)) {
                            cmd = "dot " + dotFile.getAbsolutePath() + " -o " + imgFile.getAbsolutePath();
                        } else {
                            cmd = "dot -T" + outFormat + " " + dotFile.getAbsolutePath() + " -o " + imgFile.getAbsolutePath();
                        }
                        System.out.println("-----------------------> cmd = " + cmd);
                        Runtime.getRuntime().exec(cmd);
                        Thread.sleep(2000);
                        if (imgFile.exists()) {
                            System.out.println("-----------------------> image exists !!!");
                            if ("dotp".equalsIgnoreCase(outFormat)) {
                                String out = "var response = (" +  new Gson().toJson(IOUtils.readLines(imgFile)) + ")";
                                return Response.ok(out).build();
                            } else {
                                return Response.ok(imgFile, contentType).header("content-disposition","attachment;
                                filename ="+query+"_image."+outFormat).build();
                            }
                        } else {
                            System.out.println("-----------------------> image DO NOT exist !!!");
                            return Response.ok("An error occurred generating image for pathway '" + query + "'",
                            MediaType.valueOf("text/plain")).build();
                        }
                    } catch (Exception e) {
                        return Response.ok("An error occurred generating image for pathway '" + query + "': " + e.getMessage(),
                        MediaType.valueOf("text/plain")).build();
                    }
                } else {
                    return Response.ok("Could not find pathway '" + query + "'", MediaType.valueOf("text/plain")).build();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return createErrorResponse("getPathwayImage", e.toString());
            }
        }


        @GET
        @Path("/annotation")
        public Response getPathwayAnnotation() {
            try {
                parseQueryParams();
                TfbsDBAdaptor adaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species, this.assembly);
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return createErrorResponse("getPathwayAnnotation", e.toString());
            }
        }

        @GET
        @Path("/{pathwayId}/element")
        public Response getAllElements(@PathParam("pathwayId") String query) {
            try {
                parseQueryParams();
                TfbsDBAdaptor adaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species, this.assembly);
                return generateResponse(query, adaptor.getAllByTfGeneNameList(StringUtils.toList(query, ","), null, Integer.MIN_VALUE,
                Integer.MIN_VALUE));
            } catch (Exception e) {
                e.printStackTrace();
                return createErrorResponse("getAllElements", e.toString());
            }
        }


        @GET
        @Path("/{pathwayId}/gene")
        public Response getAllGenes(@PathParam("pathwayId") String query) {
            try {
                parseQueryParams();
                TfbsDBAdaptor adaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species, this.assembly);
                return generateResponse(query, adaptor.getAllByTfGeneNameList(StringUtils.toList(query, ","), null, Integer.MIN_VALUE,
                Integer.MIN_VALUE));
            } catch (Exception e) {
                e.printStackTrace();
                return createErrorResponse("getAllGenes", e.toString());
            }
        }

        @GET
        @Path("/{pathwayId}/protein")
        public Response getAllByTfbs(@PathParam("pathwayId") String query) {
            try {
                parseQueryParams();
                TfbsDBAdaptor adaptor = dbAdaptorFactory.getTfbsDBAdaptor(this.species, this.assembly);
                return generateResponse(query, adaptor.getAllByTfGeneNameList(StringUtils.toList(query, ","), null, Integer.MIN_VALUE,
                Integer.MIN_VALUE));
            } catch (Exception e) {
                e.printStackTrace();
                return createErrorResponse("getAllByTfbs", e.toString());
            }
        }

        private String getJsonPathway(Pathway pw) {
            StringBuilder sb = new StringBuilder();

            sb.append("{\"type\": \"pathway\",");
            sb.append("\"id\": ").append(pw.getPkPathway()).append(",");
            sb.append("\"name\": \"").append(getFirstName(pw.getBioEntity())).append("\",");
            sb.append("\"description\": \"");
            if (pw.getBioEntity().getComment()!=null) {
                sb.append(pw.getBioEntity().getComment().replace("\"", "'").replace("\n", "").replace("\r", "").replace("\n", ""));
            }
            sb.append("\",");
            sb.append("\"components\": [");
            if (pw.getPathwaiesForPathwayComponent()!=null) {
                int c=0;
                Iterator it = pw.getPathwaiesForPathwayComponent().iterator();
                while (it.hasNext()) {
                    if (c!=0) {
                        sb.append(",");
                    }
                    sb.append(getJsonPathway((Pathway) it.next()));
                    c++;
                }
                it = pw.getInteractions().iterator();
                Interaction interaction = null;
                while (it.hasNext()) {
                    if (c!=0) {
                        sb.append(",");
                    }
                    interaction = (Interaction) it.next();
                    sb.append("{\"type\": \"interaction\",");
                    sb.append("\"id\": ").append(interaction.getPkInteraction()).append(",");
                    sb.append("\"name\": \"").append(getFirstName(interaction.getBioEntity())).append("\",");
                    sb.append("\"description\": \"");
                    if (interaction.getBioEntity().getComment()!=null) {
                        sb.append(interaction.getBioEntity().getComment().replace("\"", "'").replace("\n", "").replace("\r", "")
                        .replace("\n", ""));
                    }
                    sb.append("\"}");
                    c++;
                }
            }
            sb.append("]}");

            return sb.toString();
        }

        public String getFirstName(BioEntity entity) {
            String name = "NO-NAME";
            try {
                String aux = "";
                Iterator it = entity.getNameEntities().iterator();
                NameEntity ne = null;
                while (it.hasNext()) {
                    ne = (NameEntity) it.next();
                    if (name.equalsIgnoreCase("NO-NAME") || ne.getNameEntity().length()<name.length()) {
                        name = ne.getNameEntity();
                    }
                }
                name = name.replace("\"", "'");
            } catch (Exception e) {
                name = "NO-NAME";
            }
            return name;
        }
        */
    @GET
    public Response defaultMethod() {
        return help();
    }

    @GET
    @Path("/help")
    public Response help() {
        StringBuilder sb = new StringBuilder();
        sb.append("Input:\n");
        sb.append("all id formats are accepted.\n\n\n");
        sb.append("Resources:\n");
        sb.append("- list: This subcategory is an informative WS that show the complete list of available pathways. This is an special "
                + "resource which does not need a pathway name as input.\n");
        sb.append(" Output columns: internal ID, pathway name, description.\n\n");
        sb.append("- info: Prints descriptive information about a pathway.\n");
        sb.append(" Output columns: internal ID, pathway name, description.\n\n");
        sb.append("- image: Download an image of the selected pathway.\n\n\n");
        sb.append("Documentation:\n");
        sb.append("http://docs.bioinfo.cipf.es/projects/cellbase/wiki/Network_rest_ws_api#Pathway");

        return createOkResponse(sb.toString());
    }

}
