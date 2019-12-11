/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.lib.builders;


import org.junit.Test;
import org.opencb.biodata.formats.feature.gff.Gff2;
import org.opencb.biodata.models.core.Constraint;

import org.opencb.biodata.models.core.Xref;
import org.opencb.biodata.models.variant.avro.Expression;
import org.opencb.biodata.models.variant.avro.ExpressionCall;
import org.opencb.biodata.models.variant.avro.GeneDrugInteraction;
import org.opencb.biodata.models.variant.avro.GeneTraitAssociation;


import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class GeneParserUtilsTest {

    @Test
    public void testGetTfbsMap() throws Exception {
        final String SEQUENCE_NAME = "chr10";
        final String SOURCE = "RegulatoryBuild TFBS Motifs v13.0";
        final String FEATURE = "TF_binding_site";

        Path tfbsFile = Paths.get(getClass().getResource("/MotifFeatures.gff.gz").getFile());
        Map<String, SortedSet<Gff2>> tfbsMap = GeneParserUtils.getTfbsMap(tfbsFile);
        assertEquals(1, tfbsMap.size());
        SortedSet<Gff2> features = tfbsMap.get("10");
        assertEquals(2, features.size());

        Gff2 expectedFeature1 = new Gff2(SEQUENCE_NAME, SOURCE, FEATURE, 10365, 10384, "5.864", "-", ".", "Name=PPARG::RXRA:MA0065.1");
        Gff2 expectedFeature2 = new Gff2(SEQUENCE_NAME, SOURCE, FEATURE, 10442, 10456, "10.405", "-", ".", "Name=Tr4:MA0504.1");

        Iterator<Gff2> iter = features.iterator();
        Gff2 actualFeature1 = iter.next();
        Gff2 actualFeature2 = iter.next();

        assertEquals(expectedFeature1.toString(), actualFeature1.toString());
        assertEquals(expectedFeature2.toString(), actualFeature2.toString());
    }

    @Test
    public void testGetXrefMap() throws IOException {
        // TODO test xref file too. but I don't know what it looks like.

        Path idmappingFile = Paths.get(getClass().getResource("/idmapping_selected.tab.gz").getFile());
        Map<String, ArrayList<Xref>> xrefMap = GeneParserUtils.getXrefMap(null, idmappingFile);
        assertEquals(2, xrefMap.size());

        assertTrue(xrefMap.containsKey("ENST00000372839"));
        assertTrue(xrefMap.containsKey("ENST00000353703"));
        assertEquals(xrefMap.get("ENST00000372839"), xrefMap.get("ENST00000353703"));

        ArrayList<Xref> xrefs = xrefMap.get("ENST00000372839");
        Iterator<Xref> iter = xrefs.iterator();
        Xref actualXref1 = iter.next();
        Xref actualXref2 = iter.next();

        assertEquals("UniProtKB ACC", actualXref1.getDbDisplayName());
        assertEquals("uniprotkb_acc", actualXref1.getDbName());
        assertEquals("", actualXref1.getDescription());
        assertEquals("P31946", actualXref1.getId());

        assertEquals("UniProtKB ID", actualXref2.getDbDisplayName());
        assertEquals("uniprotkb_id", actualXref2.getDbName());
        assertEquals("", actualXref2.getDescription());
        assertEquals("1433B_HUMAN", actualXref2.getId());
    }

    @Test
    public void testGetGeneDrugMap() throws IOException {
        Path geneDrugFile = Paths.get(getClass().getResource("/dgidb.tsv").getFile());
        Map<String, List<GeneDrugInteraction>> geneDrugMap = GeneParserUtils.getGeneDrugMap(geneDrugFile);
        assertEquals(1, geneDrugMap.size());
        assertTrue(geneDrugMap.containsKey("CDK7"));
        List<GeneDrugInteraction> interactions = geneDrugMap.get("CDK7");
        assertEquals(1, interactions.size());
        Iterator<GeneDrugInteraction> iter = interactions.iterator();
        GeneDrugInteraction actionInteraction = iter.next();
        assertEquals("inhibitor", actionInteraction.getDrugName());
        assertEquals("CDK7", actionInteraction.getGeneName());
        assertEquals("dgidb", actionInteraction.getSource());
        assertEquals("1022", actionInteraction.getStudyType());
        assertEquals("CancerCommons", actionInteraction.getType());
    }

    @Test
    public void testGetGeneExpressionMap() throws IOException {
        Path geneExpressionFile = Paths.get(getClass().getResource("/allgenes_updown_in_organism_part.tab.gz").getFile());
        Map<String, List<Expression>> geneExpressionMap = GeneParserUtils.getGeneExpressionMap("Arabidopsis thaliana", geneExpressionFile);
        assertEquals(2, geneExpressionMap.size());
        assertTrue(geneExpressionMap.containsKey("AT4G08410"));

        List<Expression> results = geneExpressionMap.get("AT4G08410");
        assertEquals(1, results.size());
        Iterator<Expression> iter = results.iterator();
        Expression expression = iter.next();
        assertEquals("organism_part", expression.getExperimentalFactor());
        assertEquals("AT4G08410", expression.getGeneName());
        assertEquals("E-GEOD-16722", expression.getExperimentId());
        assertEquals("shoot", expression.getFactorValue());
        assertEquals("A-AFFY-2", expression.getTechnologyPlatform());
        assertEquals(ExpressionCall.DOWN, expression.getExpression());
        assertEquals(1.2705652E-10, expression.getPvalue(), 0.001);
    }

    @Test
    public void testGetGeneDiseaseAssociationMap() throws IOException {
        Path hpoFilePath = Paths.get(getClass().getResource("/ALL_SOURCES_ALL_FREQUENCIES_diseases_to_genes_to_phenotypes.txt").getFile());
        Path disgenetFilePath = Paths.get(getClass().getResource("/all_gene_disease_associations.tsv.gz").getFile());

        Map<String, List<GeneTraitAssociation>> geneDiseaseAssociationMap = GeneParserUtils.getGeneDiseaseAssociationMap(hpoFilePath, disgenetFilePath);

        assertEquals(3, geneDiseaseAssociationMap.size());
        assertTrue(geneDiseaseAssociationMap.containsKey("LIPA"));

        List<GeneTraitAssociation> results = geneDiseaseAssociationMap.get("LIPA");
        assertEquals(1, results.size());
        Iterator<GeneTraitAssociation> iter = results.iterator();
        GeneTraitAssociation geneTraitAssociation = iter.next();
        assertEquals("HP:0002092", geneTraitAssociation.getHpo());
        assertEquals("OMIM:278000", geneTraitAssociation.getId());
        assertEquals("Pulmonary arterial hypertension", geneTraitAssociation.getName());
        assertEquals(1, geneTraitAssociation.getNumberOfPubmeds(), 1);
        assertEquals(0, geneTraitAssociation.getScore(), 0.001);
        assertEquals("hpo", geneTraitAssociation.getSource());

        results = geneDiseaseAssociationMap.get("A1BG");
        iter = results.iterator();
        assertEquals(6, results.size());
        geneTraitAssociation = iter.next();
        assertEquals("C0001418", geneTraitAssociation.getId());
        assertEquals("Adenocarcinoma", geneTraitAssociation.getName());
        assertEquals("disgenet", geneTraitAssociation.getSource());
        assertEquals(1, geneTraitAssociation.getNumberOfPubmeds(), 1);
        assertEquals(0.009999999776482582, geneTraitAssociation.getScore(), 0.001);
    }

    @Test
    public void testGetConstraints() throws Exception {
        Path gnmoadFile = Paths.get(getClass().getResource("/gnomad.v2.1.1.lof_metrics.by_transcript.txt.gz").getFile());
        Map<String, List<Constraint>> constraints = GeneParserUtils.getConstraints(gnmoadFile);

        Constraint constraint1 = new Constraint("gnomAD", "pLoF", "oe_mis", 1.0187);
        Constraint constraint2 = new Constraint("gnomAD", "pLoF", "oe_syn", 1.0252);
        Constraint constraint3 = new Constraint("gnomAD", "pLoF", "oe_lof", 0.73739);
        List<Constraint> expected = new ArrayList<>();
        expected.add(constraint1);
        expected.add(constraint2);
        expected.add(constraint3);

        List<Constraint> transcriptConstraints = constraints.get("ENST00000600966");
        assertEquals(3, transcriptConstraints.size());
        assertEquals(expected, transcriptConstraints);

        constraint1 = new Constraint("gnomAD", "pLoF", "oe_mis", 1.0141);
        constraint2 = new Constraint("gnomAD", "pLoF", "oe_syn", 1.0299);
        constraint3 = new Constraint("gnomAD", "pLoF", "oe_lof", 0.78457);
        Constraint constraint4 = new Constraint("gnomAD", "pLoF", "exac_pLI", 9.0649E-5);
        Constraint constraint5 = new Constraint("gnomAD", "pLoF", "exac_oe_lof", 0.65033);
        expected = new ArrayList<>();
        expected.add(constraint1);
        expected.add(constraint2);
        expected.add(constraint3);
        expected.add(constraint4);
        expected.add(constraint5);

        transcriptConstraints = constraints.get("ENST00000263100");
        assertEquals(5, transcriptConstraints.size());
        assertEquals(expected, transcriptConstraints);

        List<Constraint> geneConstraints = constraints.get("ENSG00000121410");
        assertEquals(5, geneConstraints.size());
        assertEquals(expected, geneConstraints);
    }
}
