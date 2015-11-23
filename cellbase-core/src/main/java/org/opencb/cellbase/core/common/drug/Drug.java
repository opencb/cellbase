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

package org.opencb.cellbase.core.common.drug;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 12/9/13
 * Time: 5:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class Drug {

    private String id;
    private String name;
    private String type;
    private List<XRef> xrefs;
    private List<String> atcCode;
    private List<String> group;
    private String absorption;
    private List<String> category;
    private String description;
    private String indication;
    private String pharmacology;
    private String mechanismOfAction;
    private String toxicity;
    private String halfLife;
    private String routeOfElimination;
    private String volumeOfDistribution;
    private String clearance;
    private List<Interactor> drugInteractor;
    private List<Interactor> foodInteractor;

    public Drug() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<XRef> getXrefs() {
        return xrefs;
    }

    public void setXrefs(List<XRef> xrefs) {
        this.xrefs = xrefs;
    }

    public List<String> getAtcCode() {
        return atcCode;
    }

    public void setAtcCode(List<String> atcCode) {
        this.atcCode = atcCode;
    }

    public List<String> getGroup() {
        return group;
    }

    public void setGroup(List<String> group) {
        this.group = group;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIndication() {
        return indication;
    }

    public void setIndication(String indication) {
        this.indication = indication;
    }

    public String getAbsorption() {
        return absorption;
    }

    public void setAbsorption(String absorption) {
        this.absorption = absorption;
    }

    public String getPharmacology() {
        return pharmacology;
    }

    public void setPharmacology(String pharmacology) {
        this.pharmacology = pharmacology;
    }

    public String getMechanismOfAction() {
        return mechanismOfAction;
    }

    public void setMechanismOfAction(String mechanismOfAction) {
        this.mechanismOfAction = mechanismOfAction;
    }

    public String getToxicity() {
        return toxicity;
    }

    public void setToxicity(String toxicity) {
        this.toxicity = toxicity;
    }

    public String getHalfLife() {
        return halfLife;
    }

    public void setHalfLife(String halfLife) {
        this.halfLife = halfLife;
    }

    public String getRouteOfElimination() {
        return routeOfElimination;
    }

    public void setRouteOfElimination(String routeOfElimination) {
        this.routeOfElimination = routeOfElimination;
    }

    public String getVolumeOfDistribution() {
        return volumeOfDistribution;
    }

    public void setVolumeOfDistribution(String volumeOfDistribution) {
        this.volumeOfDistribution = volumeOfDistribution;
    }

    public String getClearance() {
        return clearance;
    }

    public void setClearance(String clearance) {
        this.clearance = clearance;
    }

    public List<String> getCategory() {
        return category;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }

    public List<Interactor> getDrugInteractor() {
        return drugInteractor;
    }

    public void setDrugInteractor(List<Interactor> drugInteractor) {
        this.drugInteractor = drugInteractor;
    }

    public List<Interactor> getFoodInteractor() {
        return foodInteractor;
    }

    public void setFoodInteractor(List<Interactor> foodInteractor) {
        this.foodInteractor = foodInteractor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
