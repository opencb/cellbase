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

package org.opencb.cellbase.core.common;


import java.util.*;

public class XRefs {
	
	private String id;
	
	private Map<String, List<XRefItem>> xrefItems;
	private List<String> dbNames;


	public XRefs(String id) {
		this.id = id;

		// create structures
		xrefItems = new HashMap<String, List<XRefItem>>();
		dbNames = new ArrayList<String>();
	}

	public XRefs(String id, String dbname) {
		this.id = id;

		// create structures
		xrefItems = new HashMap<String, List<XRefItem>>();
		dbNames = new ArrayList<String>();
		
		// init dbname
		addDbName(dbname);
	}

	public XRefs(String id, String dbname, String displayName) {
		this(id, dbname, displayName, "");
	}

	public XRefs(String id, String dbname, List<String> displayName) {
//		this(id, dbname, displayName, ListUtils.initialize(displayName.size(), ""));
        List<String> description = new ArrayList<>(displayName.size());
        for(int i=0; i<displayName.size(); i++)   description.add("");
//        this(id, dbname, displayName, description);
        this.id = id;

        // create structures
        xrefItems = new HashMap<>();
        dbNames = new ArrayList<>();

        addXRefItem(dbname, displayName, description);
	}


	public XRefs(String id, String dbname, String displayName, String description) {
		this(id, dbname, Arrays.asList(displayName), Arrays.asList(description));
	}

	public XRefs(String id, String dbname, List<String> displayName, List<String> description) {
		this.id = id;

		// create structures
		xrefItems = new HashMap<>();
		dbNames = new ArrayList<>();

		addXRefItem(dbname, displayName, description);
	}

	public void addXRefItem(String dbname, String displayName, String description) {
		addXRefItem(dbname, Arrays.asList(displayName), Arrays.asList(description));
	}

	public void addXRefItem(String dbname, List<String> displayName, List<String> description) {
		addDbName(dbname);
		if(displayName != null && description != null && displayName.size() == description.size()) {
			for(int i=0; i<displayName.size(); i++) {
				xrefItems.get(dbname).add(new XRefItem(displayName.get(i), description.get(i)));
			}
		}
	}

	public void removeXRefItem(String displayName) {
		for(String dbname: dbNames) {
			xrefItems.get(dbname).remove(displayName);
		}
	}


	public void addDbName(String dbName) {
		if(!xrefItems.containsKey(dbName)) {
			xrefItems.put(dbName, new ArrayList<XRefItem>());
			dbNames.add(dbName);
		}
	}

	public void removeDbNameItems(String dbName) {
		if(xrefItems.containsKey(dbName)) {
			xrefItems.remove(dbName);
			dbNames.remove(dbName);
		}
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(id);
		if(dbNames.size()>0) {
			sb.append("\t");
		}
		for(String dbname: dbNames) {
			sb.append(dbname).append(":");
			for(XRefItem xrefItem: xrefItems.get(dbname)) {
				sb.append(xrefItem.getDisplayName()).append(",");
			}
			if(sb.lastIndexOf(",") != -1) {
				sb.deleteCharAt(sb.length()-1);
			}
			sb.append("\t");
		}
		return sb.toString().trim();
	}


	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	
	/**
	 * @param xrefItems the xrefItems to set
	 */
	public void setXrefItems(Map<String, List<XRefItem>> xrefItems) {
		this.xrefItems = xrefItems;
	}

	/**
	 * @return the xrefItems
	 */
	public Map<String, List<XRefItem>> getXrefItems() {
		return xrefItems;
	}

	
	/**
	 * @return the dbNames
	 */
	public List<String> getDbNames() {
		return dbNames;
	}

	public class XRefItem {

		private String displayName;
		private String description;

		public XRefItem(String displayName) {
			this(displayName, "");
		}

		public XRefItem(String displayName, String description) {
			this.displayName = displayName;
			this.description = description;
		}

		@Override
		public String toString(){
			return displayName+"\t"+description;
		}

		
		/**
		 * @return the displayName
		 */
		public String getDisplayName() {
			return displayName;
		}

		/**
		 * @param displayName the displayName to set
		 */
		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		
		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * @param description the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}
	}

}
