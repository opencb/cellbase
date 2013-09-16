package org.opencb.cellbase.core.lib;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bioinfo.commons.Config;

import java.io.IOException;
import java.util.ResourceBundle;

public abstract class DBAdaptor {

	protected Logger logger= Logger.getLogger(this.getClass().getSimpleName());
	
	protected static ResourceBundle resourceBundle;
	protected static Config applicationProperties;
	
	
	static {
		// reading application.properties file
		resourceBundle = ResourceBundle.getBundle("cellbase");
		try {
			applicationProperties = new Config(resourceBundle);
		} catch (IOException e) {
			applicationProperties = new Config();
			e.printStackTrace();
		}
	}
	
	public DBAdaptor() {
		logger= Logger.getLogger(this.getClass().getSimpleName());
		logger.setLevel(Level.DEBUG);
		
		logger.info(applicationProperties.toString());
		
	}
	
	public Logger getLogger() {
		return logger;
	}
}
