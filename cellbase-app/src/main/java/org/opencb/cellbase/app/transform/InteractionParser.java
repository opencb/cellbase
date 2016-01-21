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

import org.opencb.biodata.models.protein.*;
import org.opencb.biodata.models.protein.Interactor;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/***
 * Created with IntelliJ IDEA.
 * User: mbleda
 * Date: 12/3/13
 * Time: 4:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class InteractionParser extends CellBaseParser {

    private final String species;
    private final Path psimiTabFile;

    public InteractionParser(Path psimiTabFile, String species, CellBaseSerializer serializer) {
        super(serializer);
        this.species = species;
        this.psimiTabFile = psimiTabFile;
    }

    public void parse() throws IOException {

        boolean isSpeciesA = false;
        boolean isSpeciesB = false;
        Set<String> speciesSet = new HashSet<>(Arrays.asList(species.split(",")));
        Set<String> nonCuratedTerms = new HashSet<>(Arrays
                .asList(species.split("MI:0001,MI:0024,MI:0026,MI:0035,MI:0036,MI:0037,MI:0045,MI:0046,MI:0057,MI:0058,MI:0063,MI:0064,"
                        + "MI:0085,MI:0087,MI:0100,MI:0101,MI:0105,MI:0110,MI:0254,MI:0362,MI:0363,MI:0364,MI:0439,MI:0441,MI:0686,MI:1176,"
                        + "MI:1177,MI:1178")));

        Interaction interaction;
        Interactor interactorA;
        Interactor interactorB;

        PsimiTabReader intactPsimiTabReader = new PsimiTabReader();
        Iterator<BinaryInteraction> iterator = intactPsimiTabReader.iterate(psimiTabFile.toFile());

        // Iterate through Interaction PSIMI TAB
        while (iterator.hasNext()) {
            BinaryInteraction binaryInteraction = iterator.next();

            isSpeciesA = false;
            isSpeciesB = false;

            if (binaryInteraction.getInteractorA() == null || binaryInteraction.getInteractorB() == null) {
                continue;
            }

            // Check if interactors are from the desired species
            Collection<CrossReference> crA = binaryInteraction.getInteractorA().getOrganism().getIdentifiers();
            for (CrossReference crAElement : crA) {
                if (speciesSet.contains(crAElement.getText())) {
                    isSpeciesA = true;
                    break;
                }
            }
            Collection<CrossReference> crB = binaryInteraction.getInteractorB().getOrganism().getIdentifiers();
            for (CrossReference crBElement : crB) {
                if (speciesSet.contains(crBElement.getText())) {
                    isSpeciesB = true;
                    break;
                }
            }

            // If both interactors from the same species
            if (isSpeciesA && isSpeciesB) {
                // Create new objects
                interaction = new Interaction();
                interactorA = new Interactor();
                interactorB = new Interactor();

                List<XRef> xRefList = new ArrayList<>();
                List<XRef> xRefAList = new ArrayList<>();
                List<XRef> xRefBList = new ArrayList<>();

                // Main ID
                //---------------------------------------------
                Collection<CrossReference> mainIdA = binaryInteraction.getInteractorA().getIdentifiers();
                for (CrossReference idA : mainIdA) {
                    interactorA.setId(idA.getIdentifier());
                    interactorA.setDbName(idA.getDatabase());
                    xRefAList.add(new XRef(idA.getIdentifier(), idA.getDatabase()));
                }

                Collection<CrossReference> mainIdB = binaryInteraction.getInteractorB().getIdentifiers();
                for (CrossReference idB : mainIdB) {
                    interactorB.setId(idB.getIdentifier());
                    interactorB.setDbName(idB.getDatabase());
                    xRefBList.add(new XRef(idB.getIdentifier(), idB.getDatabase()));
                }

                // Alternative IDs (alternatives and aliases)
                //---------------------------------------------
                Collection<CrossReference> altIdsA = binaryInteraction.getInteractorA().getAlternativeIdentifiers();
                for (CrossReference altIdsAElement : altIdsA) {
                    xRefAList.add(new XRef(altIdsAElement.getIdentifier(), altIdsAElement.getDatabase()));
                }
                Collection<Alias> aliasesA = binaryInteraction.getInteractorA().getAliases();
                for (Alias aliasA : aliasesA) {
                    xRefAList.add(new XRef(aliasA.getName(), aliasA.getAliasType()));
                }

                Collection<CrossReference> altIdsB = binaryInteraction.getInteractorB().getAlternativeIdentifiers();
                for (CrossReference altIdsBElement : altIdsB) {
                    xRefBList.add(new XRef(altIdsBElement.getIdentifier(), altIdsBElement.getDatabase()));
                }
                Collection<Alias> aliasesB = binaryInteraction.getInteractorB().getAliases();
                for (Alias aliasB : aliasesB) {
                    xRefBList.add(new XRef(aliasB.getName(), aliasB.getAliasType()));
                }

                // Interaction detection method
                //---------------------------------------------
                List<CrossReference> interactionDetectionMethod = binaryInteraction.getDetectionMethods();
                List<Psimi> psimiList = new ArrayList<>();
                boolean isCurated = false;
                for (CrossReference idm : interactionDetectionMethod) {
                    psimiList.add(new Psimi(idm.getIdentifier(), idm.getText()));
                    isCurated |= !nonCuratedTerms.contains(idm.getIdentifier());
//                    if(nonCuratedTerms.contains(idm.getIdentifier())) {
//                    }
                }
                interaction.setDetectionMethod(psimiList);
                interaction.setStatus((isCurated) ? "curated" : "non-curated");

                // Publication authors
                //---------------------------------------------
                List<Author> authors = binaryInteraction.getAuthors();
                List<String> authorsList = new ArrayList<>();
                for (Author author : authors) {
                    authorsList.add(author.getName());
                }
                interaction.setAuthors(authorsList);

                // Publication
                //---------------------------------------------
                String pubmed = "";
                List<CrossReference> publications = binaryInteraction.getPublications();
                for (CrossReference publication : publications) {
//                    System.out.println("publication --> "+publication.toString());
                    if (publication.getDatabase().equals("pubmed")) {
                        pubmed = publication.getIdentifier();
                    } else {
                        xRefList.add(new XRef(publication.getIdentifier(), publication.getDatabase()));
                    }
                }
                interaction.setPubmed(pubmed);

                // Interaction Type
                //---------------------------------------------
                List<CrossReference> interactionTypes = binaryInteraction.getInteractionTypes();
                List<Psimi> interactionTypesList = new ArrayList<>();
                for (CrossReference interactionType : interactionTypes) {
                    interactionTypesList.add(new Psimi(interactionType.getIdentifier(), interactionType.getText()));
                }
                interaction.setType(interactionTypesList);

                // Source databases
                //---------------------------------------------
                List<CrossReference> sourceDatabases = binaryInteraction.getSourceDatabases();
                List<Psimi> sourceDatabasesList = new ArrayList<>();
                for (CrossReference db : sourceDatabases) {
                    sourceDatabasesList.add(new Psimi(db.getIdentifier(), db.getText()));
                }
                interaction.setSource(sourceDatabasesList);

                // Interaction identifiers
                //---------------------------------------------
                List<CrossReference> interactionIds = binaryInteraction.getInteractionAcs();
                for (CrossReference interactionId : interactionIds) {
                    xRefList.add(new XRef(interactionId.getIdentifier(), interactionId.getDatabase()));
                }

                // Confidence value
                //---------------------------------------------
                List<Confidence> confidenceValues = binaryInteraction.getConfidenceValues();
                List<Score> confidenceValuesList = new ArrayList<>();
                for (Confidence confidence : confidenceValues) {
                    float value = -1.0f;
                    try {
                        value = Float.parseFloat(confidence.getValue());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }

                    confidenceValuesList.add(new Score(confidence.getType(), value));
                }
                interaction.setScore(confidenceValuesList);

                // Biological role
                //---------------------------------------------
                List<CrossReference> biologicalRolesA = binaryInteraction.getInteractorA().getBiologicalRoles();
                List<Psimi> biologicalRolesAList = new ArrayList<>();
                for (CrossReference brA : biologicalRolesA) {
                    biologicalRolesAList.add(new Psimi(brA.getIdentifier(), brA.getText()));
                }
                interactorA.setBiologicalRole(biologicalRolesAList);

                List<CrossReference> biologicalRolesB = binaryInteraction.getInteractorB().getBiologicalRoles();
                List<Psimi> biologicalRolesBList = new ArrayList<>();
                for (CrossReference brB : biologicalRolesB) {
                    biologicalRolesBList.add(new Psimi(brB.getIdentifier(), brB.getText()));
                }
                interactorB.setBiologicalRole(biologicalRolesBList);

                // Molecule type
                //---------------------------------------------
                List<CrossReference> interactorTypesA = binaryInteraction.getInteractorA().getInteractorTypes();
                for (CrossReference itA : interactorTypesA) {
                    interactorA.setMoleculeType(new Psimi(itA.getIdentifier(), itA.getText()));
                }

                List<CrossReference> interactorTypesB = binaryInteraction.getInteractorB().getInteractorTypes();
                for (CrossReference itB : interactorTypesB) {
                    interactorB.setMoleculeType(new Psimi(itB.getIdentifier(), itB.getText()));
                }

                // Xrefs
                //---------------------------------------------
                List<CrossReference> xRefsA = binaryInteraction.getInteractorA().getXrefs();
                for (CrossReference xrA : xRefsA) {
                    xRefAList.add(new XRef(xrA.getIdentifier(), xrA.getDatabase(), xrA.getText()));
                }

                List<CrossReference> xRefsB = binaryInteraction.getInteractorB().getXrefs();
                for (CrossReference xrB : xRefsB) {
                    xRefBList.add(new XRef(xrB.getIdentifier(), xrB.getDatabase(), xrB.getText()));
                }
                // Set xrefs
                interaction.setXrefs(xRefList);
                interactorA.setXrefs(xRefAList);
                interactorB.setXrefs(xRefBList);

                // Set interactors information
                interaction.setInteractorA(interactorA);
                interaction.setInteractorB(interactorB);

//            intactInteractions.add(interaction);
                serializer.serialize(interaction);
            }
        }
    }
}
