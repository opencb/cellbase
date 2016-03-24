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

package org.opencb.cellbase.server.ws.utils;

import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;
import org.opencb.cellbase.server.ws.GenericRestWSServer;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;

@Path("/{version}/utils")
@Produces("text/plain")
public class UtilsWSServer extends GenericRestWSServer {


    public UtilsWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo,
                         @Context HttpServletRequest hsr) throws VersionException, SpeciesException, IOException {
        super(version, species, uriInfo, hsr);
    }


//    @GET
//    @Path("/proxy")
//    public Response proxy(@QueryParam("url") String url, @DefaultValue("get") @QueryParam("method") String method) {
//        try {
//            System.out.println(url);
//            HttpRequest req = null;
//            if("post".equalsIgnoreCase(method)) {
//                req = new HttpPostRequest(new URL(url));
//            } else {
//                req = new HttpGetRequest(new URL(url));
//            }
//            // It will not work for multipart-form post,
//            // only for GET and 'standard' POST
////            for(Object key: MapUtils.getKeys(request.getParameterMap())) {
////                if (!"url".equalsIgnoreCase((String) key)) {
////                    req.addParameter((String) key, request.getParameter((String) key));
////                }
////            }
//            String[] params = url.split("\\?");
//            if(params != null && params.length > 0) {
//                String[] urlParams = params[1].split("[&=]");
//                for(int i=0; i<urlParams.length; i+= 2) {
//                    req.addParameter((String) urlParams[i], urlParams[i+1]);
//                }
//            }
//
//            System.out.println("1");
//            String data = req.doCall();
//            System.out.println("2");
//            if(data == null) {
//                data = "ERROR: could not call " + url;
//            }
//            System.out.println(data);
//            return generateResponse("", Arrays.asList(data));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        }
//    }

}
