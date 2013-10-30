package org.opencb.cellbase.server.ws;


import org.opencb.cellbase.server.exception.SpeciesException;
import org.opencb.cellbase.server.exception.VersionException;

import javax.ws.rs.core.Response;

public interface IWSServer {

	public void checkVersionAndSpecies() throws VersionException, SpeciesException;
		
	public String stats();
	
	public Response help();

}
