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

package org.opencb.cellbase.core.db.api.systems;


public interface PathwayDBAdaptor {

    String getPathways();

    String getTree();

    String getPathway(String pathwayId);

    String search(String searchBy, String searchText, boolean returnOnlyIds);

    /*
    public boolean isDna(PhysicalEntity physicalEntity) ;

    public boolean isDna(int physicalEntityId) ;

    public Dna getDna(PhysicalEntity ph) ;

    public boolean isDnaregion(PhysicalEntity physicalEntity) ;

    public boolean isDnaregion(int physicalEntityId) ;

    public Dnaregion getDnaregion(PhysicalEntity ph) ;

    public boolean isRna(PhysicalEntity physicalEntity) ;

    public boolean isRna(int physicalEntityId) ;

    public Rna getRna(PhysicalEntity ph) ;

    public boolean isRnaregion(PhysicalEntity physicalEntity) ;

    public boolean isRnaregion(int physicalEntityId) ;

    public Rnaregion getRnaregion(PhysicalEntity ph) ;

    public boolean isSmallMolecule(PhysicalEntity physicalEntity) ;

    public boolean isSmallMolecule(int physicalEntityId) ;

    public SmallMolecule getSmallMolecule(PhysicalEntity ph) ;

    public int getNumberOfComplexes(DataSource ds) ;

    public boolean isComplex(PhysicalEntity physicalEntity) ;

    public boolean isComplex(int physicalEntityId) ;

    public Complex getComplex(PhysicalEntity ph) ;

    public Complex getComplex(int complexId) ;

    public Complex getComplex(String complexName, String dataSourceName) ;

    public List<ComplexComponent> getComplexComponents(String complexName, String dataSourceName);

    public List<ComplexComponent> getComplexComponents(Complex input) ;

    public List<Pathway> getPathways(String complexName, String dataSourceName) ;

    public List<Interaction> getInteractions(Complex input) ;

    public List<Pathway> getPathways(Complex input) ;

    public List<Protein> getProteins(Complex input) ;

    public String toString(Complex input) ;

    public String toStringComplexes(List<Complex> input) ;

    public String toJson(Complex input) ;

    public String toJsonComplexes(List<Complex> input) ;

    public int getNumberOfProteins(DataSource ds) ;

    public boolean isProtein(PhysicalEntity physicalEntity) ;

    public boolean isProtein(int physicalEntityId) ;

    public Protein getProtein(PhysicalEntity ph) ;

    public Protein getProtein(int proteinId) ;

    public Protein getProteinByXrefId(String proteinId, DataSource ds) ;

    public List<String> getProteinReferenceNames(Protein protein) ;

    public List<Complex> getComplexes(Protein input) ;

    public List<Interaction> getInteractions(Protein input) ;

    public List<Pathway> getPathways(Protein input) ;

    public boolean isCatalysis(Control control) ;

    public boolean isCatalysis(int controlId) ;

    public Catalysis getCatalysis(Control c) ;

    public boolean isControl(Interaction interaction) ;

    public boolean isControl(int interactionId) ;

    public Control getControl(Interaction ph) ;

    public List<Protein> getProteins(Control input) ;

    public boolean isConversion(Interaction interaction) ;

    public boolean isConversion(int interactionId) ;

    public Conversion getConversion(Interaction ph) ;

    public List<Protein> getProteins(Conversion input) ;

    public boolean isGeneticInteraction(Interaction interaction) ;

    public boolean isGeneticInteraction(int interactionId) ;

    public GeneticInteraction getGeneticInteraction(Interaction ph) ;

    public boolean isMolecularInteraction(Interaction interaction) ;

    public boolean isMolecularInteraction(int interactionId) ;

    public MolecularInteraction getMolecularInteraction(Interaction ph) ;

    public boolean isTemplateReaction(Interaction interaction) ;

    public boolean isTemplateReaction(int interactionId) ;

    public TemplateReaction getTemplateReaction(Interaction ph) ;

    public int getNumberOfPhysicalEntities(DataSource ds) ;

    public List<Interaction> getInteractions(PhysicalEntity input) ;

    public List<Pathway> getPathways(PhysicalEntity input) ;

    public int getNumberOfGenes(DataSource ds) ;

    public int getNumberOfInteractions(DataSource ds) ;

    public List<Protein> getProteins(Interaction input) ;

    public String toString(Interaction input) ;

    public String toStringInteractions(List<Interaction> input) ;

    public String toJson(Interaction input) ;

    public String toJsonInteractions(List<Interaction> input) ;

    public int getNumberOfPathways(DataSource ds) ;

    public Pathway getPathway(String pathwayName, String dataSourceName) ;

    public Pathway getPathway(int pathwayId) ;

    public List<Pathway> getPathways(String dataSourceName) ;

    public List<Pathway> getPathways(String dataSourceName, String search, boolean onlyTopLevel) ;

    public List<Interaction> getInteractions(Pathway input) ;

    public List<Protein> getProteins(Pathway input) ;

    public String toString(Pathway input) ;

    public String toStringPathways(List<Pathway> input) ;

    public String toJson(Pathway input) ;

    public String toJsonPathways(List<Pathway> input);

    public int getNumberOfCellularLocations(DataSource ds);
    */
}
