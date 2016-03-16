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

    private String id, name, dbName, dbSource, dbVersion, parentPathway;
    private List<Object> species = new ArrayList<>();
    private List<String> displayName = new ArrayList<>();
    private List<Object> xref = new ArrayList<>();

    private List<PhysicalEntity> physicalEntities = new ArrayList<>();
    private List<SubPathway> subPathways = new ArrayList<>();
    private List<Interaction> interactions = new ArrayList<>();

    private List<Map<String, Object>> allInteractionsIDs = new ArrayList<>();
    private List<Map<String, Object>> allEntitiesIDs = new ArrayList<>();

    private Set<String> addedEntities = new HashSet<>();
    private Set<String> addedInteractions = new HashSet<>();

    public BiopaxPathway(String name, String dbName, String dbSource, String dbVersion, List<Object> species,
                         List<String> displayName, List<Object> xref) {
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
        if (!addedInteractions.contains(interaction.getName())) {
            addedInteractions.add(interaction.getName());
            interactions.add(interaction);
        }
    }

    public void addPhysicalEntity(PhysicalEntity entity) {
        if (!addedEntities.contains(entity.getName())) {
            addedEntities.add(entity.getName());
            physicalEntities.add(entity);
        }
    }

    public void addInteractionId(Map<String, Object> interactionObj) {
        if (!allInteractionsIDs.contains(interactionObj)) {
            allInteractionsIDs.add(interactionObj);
        }
    }

    public void addEntityId(Map<String, Object> entityObj) {
        if (!allEntitiesIDs.contains(entityObj)) {
            allEntitiesIDs.add(entityObj);
        }
    }
}
