package org.opencb.cellbase.ws.server;


import org.opencb.cellbase.ws.server.exception.SpeciesException;
import org.opencb.cellbase.ws.server.exception.VersionException;

import javax.ws.rs.core.Response;

public interface IWSServer {

	public void checkVersionAndSpecies() throws VersionException, SpeciesException;
		
	public String stats();
	
	public Response help();

}
