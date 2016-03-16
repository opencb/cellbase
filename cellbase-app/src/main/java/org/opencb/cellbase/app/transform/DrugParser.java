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

package org.opencb.cellbase.app.transform;

import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

//import org.opencb.commons.bioformats.drug.drugbank.DrugbankParser;
//import org.opencb.commons.bioformats.drug.drugbank.v201312jaxb.*;

//import drugbank.drugbank.*;
//import drugbank.drugbank.DrugPartnerInteraction;
//import drugbank.xjc20131209.*;

/**
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 12/9/13
 * Time: 4:43 PM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
public class DrugParser {

    private final Path drugbankPath;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public DrugParser(Path drugbankPath, CellBaseSerializer serializer) {
//        super(serializer);
        this.drugbankPath = drugbankPath;
    }

//    public void parse() throws JAXBException, IOException {
//
////        System.out.println("Number of drugs: "+ drugs.getDrug().size());
////        System.out.println("Number of partners: "+ drugs.getPartners().getPartner().size());
//
//        DrugbankParser dxp = new DrugbankParser();
//        Drugs drugs = (Drugs) dxp.loadXMLInfo(drugbankPath.toString());
//
//        /**
//         * Store all Partner Uniprot ID into a Map to speed up searching
//         * Partner ID --> Uniprot ID
//         */
//        Map<BigInteger, PartnerType> partnersMap = new HashMap<>(20000);
//        for (PartnerType partner: drugs.getPartners().getPartner()){
//            partnersMap.put(partner.getId(), partner);
//        }
//
////        BufferedWriter bw = Files.newBufferedWriter(drugbankPath, Charset.defaultCharset());
//
//        // Setting drug paramenters
//        //=================================================
//        for (DrugType dt : drugs.getDrug()){
//            Drug drug = new Drug();
//            List<XRef> xRefList = new ArrayList<>();
//
//            // ID
//            drug.setId(dt.getDrugbankId());
//
//            // Name
//            drug.setName(dt.getName());
//
//            // Type
//            drug.setType(dt.getType());
//
//            // ATC codes
//            drug.setAtcCode(dt.getAtcCodes().getAtcCode());
//            for (String atc : dt.getAtcCodes().getAtcCode()){
//                xRefList.add(new XRef(atc, "ATC"));
//            }
//
//            // Indication
//            drug.setIndication(dt.getIndication());
//
//            // Pharmacology
//            drug.setPharmacology(dt.getPharmacology());
//
//            // Mechanism of action
//            drug.setMechanismOfAction(dt.getMechanismOfAction());
//
//            // Toxicity
//            drug.setToxicity(dt.getToxicity());
//
//            // Route of elimination
//            drug.setRouteOfElimination(dt.getRouteOfElimination());
//
//            // Volume of distribution
//            drug.setVolumeOfDistribution(dt.getVolumeOfDistribution());
//
//            // Half life
//            drug.setHalfLife(dt.getHalfLife());
//
//            // Clearance
//            drug.setClearance(dt.getClearance());
//
//            // Category
//            drug.setCategory(dt.getCategories().getCategory());
//
//            // Groups
//            drug.setGroup(dt.getGroups().getGroup());
//
//            // Description
//            drug.setDescription(dt.getDescription());
//
//            // Absorption
//            drug.setAbsorption(dt.getAbsorption());
//
//            // Food interactions
//            List<Interactor> foodInteractionList = new ArrayList<>();
//            for (String food : dt.getFoodInteractions().getFoodInteraction()){
//                Interactor interactor = new Interactor(food);
//                foodInteractionList.add(interactor);
//            }
//            drug.setFoodInteractor(foodInteractionList);
//
//            // Drug interactions
//            List<Interactor> drugInteractorList = new ArrayList<>();
//            for (DrugInteraction drugInt : dt.getDrugInteractions().getDrugInteraction()){
//                Interactor drugInterator = new Interactor(drugInt.getName(), drugInt.getDrug(), drugInt.getDescription());
//                drugInteractorList.add(drugInterator);
//            }
//            drug.setDrugInteractor(drugInteractorList);
//
//            // Xrefs
//            xRefList.add(new XRef(dt.getDrugbankId(), "Drugbank"));
//
//            xRefList.add(new XRef(dt.getName(), "name"));
//
//            for ( String sa : dt.getSecondaryAccessionNumbers().getSecondaryAccessionNumber()){
//                xRefList.add(new XRef(sa, "Drugbank Secondary Accession"));
//            }
//
//            for ( String synonym : dt.getSynonyms().getSynonym()){
//                xRefList.add(new XRef(synonym, "Synonym"));
//            }
//
//            for (IdentifiersType.ExternalIdentifier xr : dt.getExternalIdentifiers().getExternalIdentifier()){
//                xRefList.add(new XRef(xr.getIdentifier(), xr.getResource()));
//            }
//
//            for (String xr : dt.getAhfsCodes().getAhfsCode()){
//                xRefList.add(new XRef(xr, "AHFS"));
//            }
//
//            xRefList.add(new XRef(dt.getCasNumber(), "CAS number"));
//
//            for (String brand : dt.getBrands().getBrand()){
//                xRefList.add(new XRef(brand, "Brand"));
//            }
//
//            for (Manufacturer manufacturer : dt.getManufacturers().getManufacturer()){
//                xRefList.add(new XRef(manufacturer.getValue(), "Manufacturer"));
//            }
//            drug.setXrefs(xRefList);
//
//
//            // Setting protein targets
//            //=================================================
//            for (TargetBondType target : dt.getTargets().getTarget()){
//                Partner targetPartner = new Partner();
//                PartnerType partner = partnersMap.get(target.getPartner());
//                List<XRef> targetXref = new ArrayList<>();
//
//                targetPartner.setName(partner.getName());
//                targetXref.add(new XRef(partner.getName(), "Name"));
//
//                targetPartner.setGeneralFunction(partner.getGeneralFunction());
//                targetPartner.setSpecificFunction(partner.getSpecificFunction());
//                targetPartner.setCellularLocation(partner.getCellularLocation());
//                targetPartner.setEssentiality(partner.getEssentiality());
//                targetPartner.setReferences(partner.getReferences());
//                if (partner.getProteinSequence() != null){
//                    targetPartner.setSequence(partner.getProteinSequence().getChain());
//                }
//
//                targetXref.add(new XRef(partner.getGeneName(), "Gene name"));
//                for (IdentifiersType.ExternalIdentifier txr : partner.getExternalIdentifiers().getExternalIdentifier()){
//                    targetXref.add(new XRef(txr.getIdentifier(), txr.getResource()));
//                    if (txr.getResource().equals("UniProtKB")){
//                        targetPartner.setId(txr.getIdentifier());
//                    }
//                }
//                for (String txr : partner.getSynonyms().getSynonym()){
//                    targetXref.add(new XRef(txr, "Synonym"));
//                }
//                for (Pfam pfam : partner.getPfams().getPfam()){
//                    targetXref.add(new XRef(pfam.getIdentifier(), "Pfam", pfam.getName()));
//                }
//                for (GoClassifier go : partner.getGoClassifiers().getGoClassifier()){
//                    targetXref.add(new XRef(go.getDescription(), "GO", go.getDescription()));
//                }
//                targetPartner.setXrefs(targetXref);
//
//
//                // Create drug-target protein interaction
//                //=================================================
//                DrugPartnerInteraction di = new DrugPartnerInteraction();
//                di.setDrug(drug);
//                di.setPartner(targetPartner);
//                di.setKnownAction(target.getKnownAction());
//                di.setAction(target.getActions().getAction());
//                di.setType("target");
//
//                // Add interaction
//                //==================================================
//                serializer.serialize(di);
////                bw.write(jsonMapper.writeValueAsString(di));
////                bw.write("\n");
////              drugInteractions.add(di);
//            }
//
//            // Setting protein enzymes
//            //=================================================
//            for (BondType enzyme : dt.getEnzymes().getEnzyme()){
//                Partner enzymePartner = new Partner();
//                PartnerType partner = partnersMap.get(enzyme.getPartner());
//                List<XRef> enzymeXref = new ArrayList<>();
//
//                enzymePartner.setName(partner.getName());
//                enzymeXref.add(new XRef(partner.getName(), "Name"));
//
//                enzymePartner.setGeneralFunction(partner.getGeneralFunction());
//                enzymePartner.setSpecificFunction(partner.getSpecificFunction());
//                enzymePartner.setCellularLocation(partner.getCellularLocation());
//                enzymePartner.setEssentiality(partner.getEssentiality());
//                if (partner.getProteinSequence() != null){
//                    enzymePartner.setSequence(partner.getProteinSequence().getChain());
//                }
//                enzymePartner.setReferences(partner.getReferences());
//
//                enzymeXref.add(new XRef(partner.getGeneName(), "Gene name"));
//                for (IdentifiersType.ExternalIdentifier txr : partner.getExternalIdentifiers().getExternalIdentifier()){
//                    enzymeXref.add(new XRef(txr.getIdentifier(), txr.getResource()));
//                    if (txr.getResource().equals("UniProtKB")){
//                        enzymePartner.setId(txr.getIdentifier());
//                    }
//                }
//                for (String txr : partner.getSynonyms().getSynonym()){
//                    enzymeXref.add(new XRef(txr, "Synonym"));
//                }
//                for (Pfam pfam : partner.getPfams().getPfam()){
//                    enzymeXref.add(new XRef(pfam.getIdentifier(), "Pfam", pfam.getName()));
//                }
//                for (GoClassifier go : partner.getGoClassifiers().getGoClassifier()){
//                    enzymeXref.add(new XRef(go.getDescription(), "GO", go.getDescription()));
//                }
//                enzymePartner.setXrefs(enzymeXref);
//
//
//                // Create drug-target protein interaction
//                //=================================================
//                DrugPartnerInteraction di = new DrugPartnerInteraction();
//                di.setDrug(drug);
//                di.setPartner(enzymePartner);
//                di.setAction(enzyme.getActions().getAction());
//                di.setType("enzyme");
//
//                // Add interaction
//                //==================================================
//                serializer.serialize(di);
////                bw.write(jsonMapper.writeValueAsString(di));
////                bw.write("\n");
////                drugInteractions.add(di);
//            }
//
//            // Setting protein transporter
//            //=================================================
//            for (BondType transporter : dt.getTransporters().getTransporter()){
//                Partner transporterPartner = new Partner();
//                PartnerType partner = partnersMap.get(transporter.getPartner());
//                List<XRef> transporterXref = new ArrayList<>();
//
//                transporterPartner.setName(partner.getName());
//                transporterXref.add(new XRef(partner.getName(), "Name"));
//
//                transporterPartner.setGeneralFunction(partner.getGeneralFunction());
//                transporterPartner.setSpecificFunction(partner.getSpecificFunction());
//                transporterPartner.setCellularLocation(partner.getCellularLocation());
//                transporterPartner.setEssentiality(partner.getEssentiality());
//                if (partner.getProteinSequence() != null){
//                    transporterPartner.setSequence(partner.getProteinSequence().getChain());
//                }
//                if (partner.getReferences() != null){
//                    transporterPartner.setReferences(partner.getReferences());
//                }
//
//                transporterXref.add(new XRef(partner.getGeneName(), "Gene name"));
//                for (IdentifiersType.ExternalIdentifier txr : partner.getExternalIdentifiers().getExternalIdentifier()){
//                    transporterXref.add(new XRef(txr.getIdentifier(), txr.getResource()));
//                    if (txr.getResource().equals("UniProtKB")){
//                        transporterPartner.setId(txr.getIdentifier());
//                    }
//                }
//                for (String txr : partner.getSynonyms().getSynonym()){
//                    transporterXref.add(new XRef(txr, "Synonym"));
//                }
//                for (Pfam pfam : partner.getPfams().getPfam()){
//                    transporterXref.add(new XRef(pfam.getIdentifier(), "Pfam", pfam.getName()));
//                }
//                for (GoClassifier go : partner.getGoClassifiers().getGoClassifier()){
//                    transporterXref.add(new XRef(go.getDescription(), "GO", go.getDescription()));
//                }
//                transporterPartner.setXrefs(transporterXref);
//
//
//                // Create drug-target protein interaction
//                //=================================================
//                DrugPartnerInteraction di = new DrugPartnerInteraction();
//                di.setDrug(drug);
//                di.setPartner(transporterPartner);
//                di.setAction(transporter.getActions().getAction());
//                di.setType("transporter");
//
//                // Add interaction
//                //==================================================
//                serializer.serialize(di);
////                bw.write(jsonMapper.writeValueAsString(di));
////                bw.write("\n");
////                drugInteractions.add(di);
//            }
//
//            // Setting protein targets
//            //=================================================
//            for (BondType carrier : dt.getCarriers().getCarrier()){
//                Partner carrierPartner = new Partner();
//                PartnerType partner = partnersMap.get(carrier.getPartner());
//                List<XRef> carrierXref = new ArrayList<>();
//
//                carrierPartner.setName(partner.getName());
//                carrierXref.add(new XRef(partner.getName(), "Name"));
//
//                carrierPartner.setGeneralFunction(partner.getGeneralFunction());
//                carrierPartner.setSpecificFunction(partner.getSpecificFunction());
//                carrierPartner.setCellularLocation(partner.getCellularLocation());
//                carrierPartner.setEssentiality(partner.getEssentiality());
//                carrierPartner.setSequence(partner.getProteinSequence().getChain());
//                carrierPartner.setReferences(partner.getReferences());
//
//                carrierXref.add(new XRef(partner.getGeneName(), "Gene name"));
//                for (IdentifiersType.ExternalIdentifier txr : partner.getExternalIdentifiers().getExternalIdentifier()){
//                    carrierXref.add(new XRef(txr.getIdentifier(), txr.getResource()));
//                    if (txr.getResource().equals("UniProtKB")){
//                        carrierPartner.setId(txr.getIdentifier());
//                    }
//                }
//                for (String txr : partner.getSynonyms().getSynonym()){
//                    carrierXref.add(new XRef(txr, "Synonym"));
//                }
//                for (Pfam pfam : partner.getPfams().getPfam()){
//                    carrierXref.add(new XRef(pfam.getIdentifier(), "Pfam", pfam.getName()));
//                }
//                for (GoClassifier go : partner.getGoClassifiers().getGoClassifier()){
//                    carrierXref.add(new XRef(go.getDescription(), "GO", go.getDescription()));
//                }
//                carrierPartner.setXrefs(carrierXref);
//
//
//                // Create drug-target protein interaction
//                //=================================================
//                DrugPartnerInteraction di = new DrugPartnerInteraction();
//                di.setDrug(drug);
//                di.setPartner(carrierPartner);
//                di.setAction(carrier.getActions().getAction());
//                di.setType("target");
//
//                // Add interaction
//                //==================================================
//                serializer.serialize(di);
////                bw.write(jsonMapper.writeValueAsString(di));
////                bw.write("\n");
////                drugInteractions.add(di);
//            }
//        }
////        bw.close();
//
//    }
}
