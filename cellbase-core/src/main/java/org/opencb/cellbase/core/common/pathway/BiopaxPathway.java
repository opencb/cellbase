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

package org.opencb.cellbase.core.common.pathway;

import java.util.*;


public class BiopaxPathway {
	String id, name, dbName, dbSource, dbVersion, parentPathway;
	List<Object> species = new ArrayList<Object>();
	List<String> displayName = new ArrayList<String>();
	List<Object> xref = new ArrayList<Object>();
	
	List<PhysicalEntity> physicalEntities = new ArrayList<PhysicalEntity>();
	List<SubPathway> subPathways = new ArrayList<SubPathway>();
	List<Interaction> interactions = new ArrayList<Interaction>();
	
	List<Map<String, Object>> allInteractionsIDs = new ArrayList<Map<String, Object>>();
	List<Map<String, Object>> allEntitiesIDs = new ArrayList<Map<String, Object>>();
	
	public Set<String> addedEntities = new HashSet<String>();
	public Set<String> addedInteractions = new HashSet<String>();
	
	public BiopaxPathway(String name, String dbName, String dbSource, String dbVersion, List<Object> species, List<String> displayName, List<Object> xref) {
		this.name = name;
		this.dbName = dbName;
		this.dbSource = dbSource;
		this.dbVersion = dbVersion;
		this.species = species;
		this.displayName = displayName;
		this.xref = xref;
		
		this.id = this.dbSource + "_" + this.dbVersion + "_" + this.name;
	}
	
	public String getName() {
		return name;
	}
	
	public List<SubPathway> getSubPathways() {
		return subPathways;
	}
	
	public List<Interaction> getInteractions() {
		return interactions;
	}
	
	public List<PhysicalEntity> getphysicalEntities() {
		return physicalEntities;
	}
	
	public List<Map<String, Object>> getAllInteractionsIDs() {
		return allInteractionsIDs;
	}
	
	public List<Map<String, Object>> getAllEntitiesIDs() {
		return allEntitiesIDs;
	}
	
	public void setParentPathway(String parent) {
		this.parentPathway = parent;
	}
	
	public void addInteraction(Interaction interaction) {
		if(!addedInteractions.contains(interaction.name)) {
			addedInteractions.add(interaction.name);
			interactions.add(interaction);
		}
	}
	
	public void addPhysicalEntity(PhysicalEntity entity) {
		if(!addedEntities.contains(entity.name)) {
			addedEntities.add(entity.name);
			physicalEntities.add(entity);
		}
	}
	
	public void addInteractionId(Map<String, Object> interactionObj) {
		if(!allInteractionsIDs.contains(interactionObj)) {
			allInteractionsIDs.add(interactionObj);
		}
	}
	
	public void addEntityId(Map<String, Object> entityObj) {
		if(!allEntitiesIDs.contains(entityObj)) {
			allEntitiesIDs.add(entityObj);
		}
	}
}
