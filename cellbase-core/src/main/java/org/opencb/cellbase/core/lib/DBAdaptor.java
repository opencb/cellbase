package org.opencb.cellbase.core.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DBAdaptor {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
//	protected Logger logger= Logger.getLogger(this.getClass().getSimpleName());
	
//	protected static ResourceBundle resourceBundle;
//	protected static Config applicationProperties;
//
//
//	static {
//		// reading application.properties file
//		resourceBundle = ResourceBundle.getBundle("cellbase");
//		try {
//			applicationProperties = new Config(resourceBundle);
//		} catch (IOException e) {
//			applicationProperties = new Config();
//			e.printStackTrace();
//		}
//	}
	
	public DBAdaptor() {
		logger= LoggerFactory.getLogger(this.getClass().getSimpleName());
//		logger.setLevel(Level.DEBUG);
		
//		logger.info(applicationProperties.toString());
		
	}
	
	public Logger getLogger() {
		return logger;
	}
}
