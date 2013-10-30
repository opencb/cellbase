package org.opencb.cellbase.server.ws.utils;

import org.opencb.cellbase.server.ws.GenericRestWSServer;
import org.opencb.cellbase.server.exception.VersionException;

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

//	public UtilsWSServer(@Context UriInfo uriInfo) throws VersionException, IOException {
//		super(uriInfo);
//	}
	
	public UtilsWSServer(@PathParam("version") String version, @Context UriInfo uriInfo, @Context HttpServletRequest hsr) throws VersionException, IOException {
		super(version, uriInfo, hsr);
	}
	
//	public UtilsWSServer(@PathParam("version") String version, @PathParam("species") String species, @Context UriInfo uriInfo) throws VersionException, IOException {
//		super(version, species, uriInfo);
//	}
	
//	@GET
//	@Path("/proxy")
//	public Response proxy(@QueryParam("url") String url, @DefaultValue("get") @QueryParam("method") String method) {
//		try {
//			System.out.println(url);
//			HttpRequest req = null;
//			if("post".equalsIgnoreCase(method)) {
//				req = new HttpPostRequest(new URL(url));
//			} else {
//				req = new HttpGetRequest(new URL(url));
//			}
//			// It will not work for multipart-form post,
//			// only for GET and 'standard' POST
////			for(Object key: MapUtils.getKeys(request.getParameterMap())) {
////				if (!"url".equalsIgnoreCase((String) key)) {
////					req.addParameter((String) key, request.getParameter((String) key));
////				}
////			}
//			String[] params = url.split("\\?");
//			if(params != null && params.length > 0) {
//				String[] urlParams = params[1].split("[&=]");
//				for(int i=0; i<urlParams.length; i+= 2) {
//					req.addParameter((String) urlParams[i], urlParams[i+1]);
//				}
//			}
//
//			System.out.println("1");
//			String data = req.doCall();
//			System.out.println("2");
//			if(data == null) {
//				data = "ERROR: could not call " + url;
//			}
//			System.out.println(data);
//			return generateResponse("", Arrays.asList(data));
//		} catch (Exception e) {
//			e.printStackTrace();
//			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//		}
//	}
	
}
