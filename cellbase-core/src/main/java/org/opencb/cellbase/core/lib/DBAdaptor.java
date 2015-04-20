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

package org.opencb.cellbase.core.lib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
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
