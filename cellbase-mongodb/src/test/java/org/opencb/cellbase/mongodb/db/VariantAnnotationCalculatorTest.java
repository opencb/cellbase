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

package org.opencb.cellbase.mongodb.db;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.util.JSON;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opencb.biodata.formats.variant.vcf4.VcfRecord;
import org.opencb.biodata.formats.variant.vcf4.io.VcfRawReader;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.ConsequenceType;
import org.opencb.biodata.models.variant.avro.SequenceOntologyTerm;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.cellbase.core.CellBaseConfiguration;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationCalculator;
import org.opencb.cellbase.mongodb.impl.MongoDBAdaptorFactory;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;


public class VariantAnnotationCalculatorTest {

    ObjectMapper jsonObjectMapper;
    VariantAnnotationCalculator variantAnnotationCalculator;

    @Before
    public void setUp() {
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        CellBaseConfiguration cellBaseConfiguration = new CellBaseConfiguration();
        try {
            cellBaseConfiguration = CellBaseConfiguration
                    .load(CellBaseConfiguration.class.getClassLoader().getResourceAsStream("configuration.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        MongoDBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(cellBaseConfiguration);
        variantAnnotationCalculator = new VariantAnnotationCalculator("hsapiens", "GRCh37", dbAdaptorFactory);


    }

    @Ignore
    @Test
    public void testGetAnnotationByVariantList() throws Exception {

        List<VariantAnnotation> variantAnnotation =
                variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("1:40768842:C:G"),
                        new QueryOptions()).get(0).getResult();
        assertObjectListEquals("[{\"chromosome\":\"1\",\"start\":40768842,\"reference\":\"C\",\"alternate\":\"G\",\"displayConsequenceType\":\"missense_variant\",\"consequenceTypes\":[{\"geneName\":\"COL9A2\",\"ensemblGeneId\":\"ENSG00000049089\",\"ensemblTranscriptId\":\"ENST00000372748\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1661,\"cdsPosition\":1564,\"codon\":\"Gac/Cac\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"Q14055\",\"position\":522,\"reference\":\"ASP\",\"alternate\":\"HIS\",\"substitutionScores\":[{\"score\":0,\"source\":\"sift\",\"description\":\"deleterious\"},{\"score\":0.996,\"source\":\"polyphen\",\"description\":\"probably damaging\"}],\"keywords\":[\"Collagen\",\"Complete proteome\",\"Deafness\",\"Disease mutation\",\"Disulfide bond\",\"Dwarfism\",\"Extracellular matrix\",\"Glycoprotein\",\"Hydroxylation\",\"Polymorphism\",\"Proteoglycan\",\"Reference proteome\",\"Repeat\",\"Secreted\",\"Signal\",\"Stickler syndrome\"],\"features\":[{\"start\":520,\"end\":549,\"type\":\"region of interest\",\"description\":\"Nonhelical region 3 (NC3)\"},{\"id\":\"PRO_0000005837\",\"start\":24,\"end\":689,\"type\":\"chain\",\"description\":\"Collagen alpha-2(IX) chain\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"COL9A2\",\"ensemblGeneId\":\"ENSG00000049089\",\"ensemblTranscriptId\":\"ENST00000482722\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"cdnaPosition\":1867,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001792\",\"name\":\"non_coding_transcript_exon_variant\"},{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"}]},{\"geneName\":\"COL9A2\",\"ensemblGeneId\":\"ENSG00000049089\",\"ensemblTranscriptId\":\"ENST00000466267\",\"strand\":\"-\",\"biotype\":\"processed_transcript\",\"cdnaPosition\":529,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001792\",\"name\":\"non_coding_transcript_exon_variant\"},{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"}]},{\"geneName\":\"COL9A2\",\"ensemblGeneId\":\"ENSG00000049089\",\"ensemblTranscriptId\":\"ENST00000427563\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\",\"cds_start_NF\",\"mRNA_start_NF\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"geneName\":\"COL9A2\",\"ensemblGeneId\":\"ENSG00000049089\",\"ensemblTranscriptId\":\"ENST00000488463\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_gene_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}],\"conservation\":[{\"score\":5.570000171661377,\"source\":\"gerp\"},{\"score\":0.9539999961853027,\"source\":\"phastCons\"},{\"score\":0.45899999141693115,\"source\":\"phylop\"}],\"geneExpression\":[{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"muscle\",\"experimentId\":\"E-MTAB-37\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.0002516439},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"colon\",\"experimentId\":\"E-MTAB-37\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.000048828566},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"mammary gland\",\"experimentId\":\"E-MTAB-37\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.0067384248},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"skin\",\"experimentId\":\"E-GEOD-17539\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.017698713},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"cell_type\",\"factorValue\":\"PBMCs\",\"experimentId\":\"E-GEOD-20677\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.025411168},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"trigeminal ganglion\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.0017803162},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"lung\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.022611883},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"thymus\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.009844733},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"parietal lobe\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.00003783583},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"corpus callosum\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":4.9933838e-29},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"subthalamic nucleus\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":7.6213657e-25},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"fallopian tube\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.008179443},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"midbrain\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":1.0599181e-27},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"dorsal root ganglion\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.00017006171},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"globus pallidus\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.042676058},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"endometrium\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.0012586314},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"pituitary gland\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":7.934848e-22},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"testis\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.00023282325},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"spleen\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.0062033166},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"hypothalamus\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":9.117032e-19},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"medulla\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":9.161488e-16},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"myometrium\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":1.5053253e-20},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"thalamus\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":7.274325e-12},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"skeletal muscle\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.02625296},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"cerebral cortex\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":7.651638e-9},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"penis\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.000012155246},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"occipital lobe\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":2.6007797e-7},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"spinal cord\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":2.2035779e-14},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"esophagus\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.000051651532},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"hippocampus\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.000057600715},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"synovial membrane\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":1.395659e-8},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"amygdala\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.0021572404},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"ventral tegmental area\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":1.502487e-15},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"adrenal gland cortex\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.026741324},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"trachea\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.000065284716},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"vulva\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.0005150674},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"bronchus\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.00012123147},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"substantia nigra pars compacta\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":7.179334e-15},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"tongue\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.013838354},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"skin\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.003486102},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"temporal lobe\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.000335766},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"prostate\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":5.4522735e-7},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"ovary\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.001103717},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"adipose tissue\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.00088530587},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"vagina\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.00010067487},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"heart atrium\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.0005993707},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"deltoid muscle\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.010671354},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"cervix\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.000693961},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"substantia nigra\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":2.2879903e-16},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"frontal lobe\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.0060153594},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"heart ventricle\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.010866529},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"liver\",\"experimentId\":\"E-GEOD-7307\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.005081312},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"lymph node\",\"experimentId\":\"E-GEOD-2665\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.042308543},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"tonsil\",\"experimentId\":\"E-GEOD-2665\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.042308543},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_369\",\"factorValue\":\"brain\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":1.6520426e-23},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"muscle\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.00069640425},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_369\",\"factorValue\":\"prostate gland\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.000039212973},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_96\",\"factorValue\":\"frontal cortex\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":6.299987e-14},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"hypothalamus\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":6.834825e-13},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_369\",\"factorValue\":\"amygdala\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.005571558},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_96\",\"factorValue\":\"prostate gland\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.0001006529},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_96\",\"factorValue\":\"smooth muscle\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.002106012},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"skeletal muscle\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.0009866538},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_369\",\"factorValue\":\"prefrontal cortex\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.000011158763},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"esophagus\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.0043907617},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_369\",\"factorValue\":\"hippocampus CA1\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.0000103980055},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_96\",\"factorValue\":\"hypothalamus\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":5.502912e-19},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"connective tissue\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.014048692},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_369\",\"factorValue\":\"hypothalamus\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":1.3351791e-21},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"hippocampus CA1\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":8.432408e-9},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"skin\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.000049438066},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"conjunctiva\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.015122785},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"ovary\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.0009825447},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_369\",\"factorValue\":\"hypopharynx\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.027954023},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_369\",\"factorValue\":\"ovary\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.021163834},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_96\",\"factorValue\":\"heart\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.04655231},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"smooth muscle\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.004755713},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"thymus\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.0000170875},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"coronary artery\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.037615106},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_369\",\"factorValue\":\"smooth muscle\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.0052799387},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"placental basal plate\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":1.5845197e-7},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_96\",\"factorValue\":\"brain\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":1.493401e-20},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"colon\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.00034219274},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"larynx\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.00077475107},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_369\",\"factorValue\":\"conjunctiva\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.009521736},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_369\",\"factorValue\":\"caudate nucleus\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":7.058743e-36},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"metagroups_6\",\"factorValue\":\"brain\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\"},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"umbilical vein\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.000012556261},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"cardiac ventricle\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.00016363186},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"amygdala\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.035996962},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"trachea\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.048021894},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"bone\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.00004352134},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_369\",\"factorValue\":\"frontal cortex\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":6.5212087e-16},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_4_blood\",\"factorValue\":\"blood\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":4.2867506e-16},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"brain\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":7.7347164e-17},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"kidney\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.032165505},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"groups_96\",\"factorValue\":\"caudate nucleus\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":1.4413553e-31},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"cervix\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":3.0264322e-11},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"metagroups_6\",\"factorValue\":\"muscle\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":5.1066614e-8},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"liver\",\"experimentId\":\"E-MTAB-62\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.00009901442},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"prostate\",\"experimentId\":\"E-GEOD-6919\",\"technologyPlatform\":\"A-AFFY-1\",\"expression\":\"UP\",\"pvalue\":0.031478867},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"prostate\",\"experimentId\":\"E-GEOD-24283\",\"technologyPlatform\":\"A-ENST-3\",\"expression\":\"UP\",\"pvalue\":0.0000012199339},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"brain\",\"experimentId\":\"E-GEOD-24283\",\"technologyPlatform\":\"A-ENST-3\",\"expression\":\"UP\",\"pvalue\":0.0034295258},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"parietal lobe\",\"experimentId\":\"E-AFMX-5\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.04952559},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"cingulate cortex\",\"experimentId\":\"E-AFMX-5\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.0025887166},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"thalamus\",\"experimentId\":\"E-AFMX-5\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.00046139013},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"caudate nucleus\",\"experimentId\":\"E-AFMX-5\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.024267225},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"spinal cord\",\"experimentId\":\"E-AFMX-5\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.009105009},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"amygdala\",\"experimentId\":\"E-AFMX-5\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.003835893},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"prefrontal cortex\",\"experimentId\":\"E-AFMX-5\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.000952306},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"trigeminal ganglion\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":1.7605502e-7},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"lung\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.024477568},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"parietal lobe\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.000058426856},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"corpus callosum\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":4.7856814e-25},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"subthalamic nucleus\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":6.0647405e-26},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"midbrain\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":1.997098e-23},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"dorsal root ganglion\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.000033595414},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"ventral tegmental area\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":2.4755471e-14},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"pituitary gland\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":7.723628e-24},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"adrenal gland cortex\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.01032231},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"trachea\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.0012608754},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"vulva\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.0000018946247},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"spleen\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.0026265385},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"hypothalamus\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":1.4810887e-16},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"bronchus\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.00044854655},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"medulla\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":1.5498128e-15},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"cerebellum\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.028688824},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"myometrium\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.00003031999},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"temporal lobe\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.004516169},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"thalamus\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":9.50461e-12},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"skeletal muscle\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.029534005},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"ovary\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.00031447032},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"adipose tissue\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.008063586},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"vagina\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.000035144712},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"cerebral cortex\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":1.5757351e-7},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"heart atrium\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.0005847258},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"cervix\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.00015777654},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"occipital lobe\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.0000010786769},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"spinal cord\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":2.3976394e-14},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"substantia nigra\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":2.3600236e-19},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"esophagus\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.000004123037},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"frontal lobe\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.034114312},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"hippocampus\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.000018136208},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"heart ventricle\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.013237499},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"liver\",\"experimentId\":\"E-GEOD-3526\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.029320657},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"skeletal muscle\",\"experimentId\":\"E-GEOD-803\",\"technologyPlatform\":\"A-AFFY-1\",\"expression\":\"DOWN\",\"pvalue\":0.000015929641},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"spinal cord\",\"experimentId\":\"E-GEOD-803\",\"technologyPlatform\":\"A-AFFY-1\",\"expression\":\"UP\",\"pvalue\":0.013622403},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"liver\",\"experimentId\":\"E-GEOD-803\",\"technologyPlatform\":\"A-AFFY-1\",\"expression\":\"DOWN\",\"pvalue\":0.0073676435},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"brain\",\"experimentId\":\"E-GEOD-803\",\"technologyPlatform\":\"A-AFFY-1\",\"expression\":\"UP\",\"pvalue\":0.013831801},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"stomach\",\"experimentId\":\"E-GEOD-13911\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.021978276},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"cervix\",\"experimentId\":\"E-GEOD-20081\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":1.8054633e-18},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"mammary gland\",\"experimentId\":\"E-GEOD-20081\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":1.8054633e-18},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"material_type\",\"factorValue\":\"organism_part\",\"experimentId\":\"E-GEOD-9171\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.0013149051},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"material_type\",\"factorValue\":\"organism_part\",\"experimentId\":\"E-GEOD-9171\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.0013149051},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"prostate\",\"experimentId\":\"E-GEOD-9196\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.0013828055},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"thymus\",\"experimentId\":\"E-GEOD-9531\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.0018578727},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"pancreas\",\"experimentId\":\"E-GEOD-9531\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.017078865},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"bone marrow\",\"experimentId\":\"E-GEOD-26672\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.016172715},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"foreskin\",\"experimentId\":\"E-GEOD-26672\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.034676556},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"cerebrospinal fluid\",\"experimentId\":\"E-MTAB-69\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.0005130881},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"kidney\",\"experimentId\":\"E-GEOD-2004\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.00007087209},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"liver\",\"experimentId\":\"E-GEOD-2004\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.017733702},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"testis\",\"experimentId\":\"E-GEOD-15431\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":1.2282165e-9},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"ovary\",\"experimentId\":\"E-GEOD-15431\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":1.2282165e-9},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"pleura\",\"experimentId\":\"E-MTAB-47\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.0001437664},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"cell_type\",\"factorValue\":\"whole blood\",\"experimentId\":\"E-GEOD-3026\",\"technologyPlatform\":\"A-AFFY-1\",\"expression\":\"DOWN\",\"pvalue\":0.026459018},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"bone\",\"experimentId\":\"E-GEOD-13548\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.0015492856},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"cervix\",\"experimentId\":\"E-GEOD-13548\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.008820433},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"stomach\",\"experimentId\":\"E-GEOD-13548\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"UP\",\"pvalue\":0.0001218918},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"cingulate cortex\",\"experimentId\":\"E-TABM-145\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.0030244992},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"thalamus\",\"experimentId\":\"E-TABM-145\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.00054095563},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"caudate nucleus\",\"experimentId\":\"E-TABM-145\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.027810834},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"spinal cord\",\"experimentId\":\"E-TABM-145\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.010529352},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"amygdala\",\"experimentId\":\"E-TABM-145\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.004468766},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"prefrontal cortex\",\"experimentId\":\"E-TABM-145\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.0011154461},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"lung\",\"experimentId\":\"E-MTAB-513\",\"technologyPlatform\":\"A-ENST-3\",\"expression\":\"UP\",\"pvalue\":0.001640682},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"brain\",\"experimentId\":\"E-MTAB-513\",\"technologyPlatform\":\"A-ENST-3\",\"expression\":\"UP\",\"pvalue\":0.00084893615},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"prostate\",\"experimentId\":\"E-MTAB-513\",\"technologyPlatform\":\"A-ENST-3\",\"expression\":\"UP\",\"pvalue\":0.004744817},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"kidney\",\"experimentId\":\"E-MTAB-513\",\"technologyPlatform\":\"A-ENST-3\",\"expression\":\"UP\",\"pvalue\":0.008317296},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"hypothalamus\",\"experimentId\":\"E-MTAB-24\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.013623003},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"placenta\",\"experimentId\":\"E-MTAB-24\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.008736392},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"thymus\",\"experimentId\":\"E-MTAB-24\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.027611377},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"uterus\",\"experimentId\":\"E-MTAB-24\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.008327977},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"blood\",\"experimentId\":\"E-MTAB-24\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.000014444673},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"amygdala\",\"experimentId\":\"E-MTAB-24\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.00002221321},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"pituitary gland\",\"experimentId\":\"E-MTAB-24\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.0066400534},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"trachea\",\"experimentId\":\"E-MTAB-24\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.0023300627},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"kidney\",\"experimentId\":\"E-GEOD-1563\",\"technologyPlatform\":\"A-AFFY-1\",\"expression\":\"DOWN\",\"pvalue\":0.0060848985},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"peripheral blood\",\"experimentId\":\"E-GEOD-1563\",\"technologyPlatform\":\"A-AFFY-1\",\"expression\":\"UP\",\"pvalue\":0.0060848985},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"caudate nucleus\",\"experimentId\":\"E-AFMX-6\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\"},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"cerebellum\",\"experimentId\":\"E-AFMX-6\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\"},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"blood\",\"experimentId\":\"E-MEXP-433\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.014563946},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"placenta\",\"experimentId\":\"E-MTAB-25\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.045199595},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"blood\",\"experimentId\":\"E-MTAB-25\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.0000016859301},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"amygdala\",\"experimentId\":\"E-MTAB-25\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.00023633506},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"pituitary gland\",\"experimentId\":\"E-MTAB-25\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.048559316},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"trachea\",\"experimentId\":\"E-MTAB-25\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.028341593},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"hypothalamus\",\"experimentId\":\"E-MTAB-25\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.04035536},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"uterus\",\"experimentId\":\"E-MTAB-25\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"DOWN\",\"pvalue\":0.014059544},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"prefrontal cortex\",\"experimentId\":\"E-MTAB-25\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.000010798974},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"spinal cord\",\"experimentId\":\"E-MTAB-25\",\"technologyPlatform\":\"A-AFFY-33\",\"expression\":\"UP\",\"pvalue\":0.00003090292},{\"geneName\":\"ENSG00000049089\",\"experimentalFactor\":\"organism_part\",\"factorValue\":\"myometrium\",\"experimentId\":\"E-GEOD-13319\",\"technologyPlatform\":\"A-AFFY-44\",\"expression\":\"DOWN\",\"pvalue\":0.0000023635312}],\"geneTraitAssociation\":[{\"id\":\"OMIM:600204\",\"name\":\"Irregular epiphyses\",\"hpo\":\"HP:0010582\",\"source\":\"hpo\"},{\"id\":\"OMIM:600204\",\"name\":\"Autosomal dominant inheritance\",\"hpo\":\"HP:0000006\",\"source\":\"hpo\"},{\"id\":\"OMIM:600204\",\"name\":\"Genu varum\",\"hpo\":\"HP:0002970\",\"source\":\"hpo\"},{\"id\":\"OMIM:600204\",\"name\":\"Mild short stature\",\"hpo\":\"HP:0003502\",\"source\":\"hpo\"},{\"id\":\"OMIM:600204\",\"name\":\"Epiphyseal dysplasia\",\"hpo\":\"HP:0002656\",\"source\":\"hpo\"},{\"id\":\"OMIM:600204\",\"name\":\"Waddling gait\",\"hpo\":\"HP:0002515\",\"source\":\"hpo\"},{\"id\":\"OMIM:600204\",\"name\":\"Flattened epiphysis\",\"hpo\":\"HP:0003071\",\"source\":\"hpo\"},{\"id\":\"OMIM:600204\",\"name\":\"Short palm\",\"hpo\":\"HP:0004279\",\"source\":\"hpo\"},{\"id\":\"OMIM:600204\",\"name\":\"Knee osteoarthritis\",\"hpo\":\"HP:0005086\",\"source\":\"hpo\"},{\"id\":\"OMIM:614284\",\"name\":\"Autosomal recessive inheritance\",\"hpo\":\"HP:0000007\",\"source\":\"hpo\"},{\"id\":\"OMIM:614284\",\"name\":\"Vitreoretinal degeneration\",\"hpo\":\"HP:0000655\",\"source\":\"hpo\"},{\"id\":\"OMIM:614284\",\"name\":\"Retinal detachment\",\"hpo\":\"HP:0000541\",\"source\":\"hpo\"},{\"id\":\"OMIM:614284\",\"name\":\"Severe Myopia\",\"hpo\":\"HP:0011003\",\"source\":\"hpo\"},{\"id\":\"OMIM:600969\",\"name\":\"Small epiphyses\",\"hpo\":\"HP:0010585\",\"source\":\"hpo\"},{\"id\":\"OMIM:600969\",\"name\":\"Mild short stature\",\"hpo\":\"HP:0003502\",\"source\":\"hpo\"},{\"id\":\"OMIM:600969\",\"name\":\"Irregular epiphyses\",\"hpo\":\"HP:0010582\",\"source\":\"hpo\"},{\"id\":\"OMIM:600969\",\"name\":\"Autosomal dominant inheritance\",\"hpo\":\"HP:0000006\",\"source\":\"hpo\"},{\"id\":\"OMIM:600969\",\"name\":\"Delayed epiphyseal ossification\",\"hpo\":\"HP:0002663\",\"source\":\"hpo\"},{\"id\":\"OMIM:600969\",\"name\":\"Proximal muscle weakness\",\"hpo\":\"HP:0003701\",\"source\":\"hpo\"},{\"id\":\"OMIM:600969\",\"name\":\"Abnormality of the hip joint\",\"hpo\":\"HP:0001384\",\"source\":\"hpo\"},{\"id\":\"OMIM:600969\",\"name\":\"Epiphyseal dysplasia\",\"hpo\":\"HP:0002656\",\"source\":\"hpo\"},{\"id\":\"OMIM:600969\",\"name\":\"Osteoarthritis\",\"hpo\":\"HP:0002758\",\"source\":\"hpo\"},{\"id\":\"OMIM:600969\",\"name\":\"Elevated serum creatine phosphokinase\",\"hpo\":\"HP:0003236\",\"source\":\"hpo\"},{\"id\":\"OMIM:600969\",\"name\":\"Short metacarpal\",\"hpo\":\"HP:0010049\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:166002\",\"name\":\"Osteoarthritis\",\"hpo\":\"HP:0002758\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:166002\",\"name\":\"Limitation of joint mobility\",\"hpo\":\"HP:0001376\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:166002\",\"name\":\"Genu varum\",\"hpo\":\"HP:0002970\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:166002\",\"name\":\"Abnormality of the hip bone\",\"hpo\":\"HP:0003272\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:166002\",\"name\":\"Micromelia\",\"hpo\":\"HP:0002983\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:166002\",\"name\":\"Arthralgia\",\"hpo\":\"HP:0002829\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:166002\",\"name\":\"Short stature\",\"hpo\":\"HP:0004322\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:166002\",\"name\":\"Genu valgum\",\"hpo\":\"HP:0002857\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:166002\",\"name\":\"Gait disturbance\",\"hpo\":\"HP:0001288\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:166002\",\"name\":\"Abnormality of epiphysis morphology\",\"hpo\":\"HP:0005930\",\"source\":\"hpo\"},{\"id\":\"OMIM:614134\",\"name\":\"Flat capital femoral epiphysis\",\"hpo\":\"HP:0003370\",\"source\":\"hpo\"},{\"id\":\"OMIM:614134\",\"name\":\"Severe Myopia\",\"hpo\":\"HP:0011003\",\"source\":\"hpo\"},{\"id\":\"OMIM:614134\",\"name\":\"Epiphyseal dysplasia\",\"hpo\":\"HP:0002656\",\"source\":\"hpo\"},{\"id\":\"OMIM:614134\",\"name\":\"Flat face\",\"hpo\":\"HP:0012368\",\"source\":\"hpo\"},{\"id\":\"OMIM:614134\",\"name\":\"Autosomal recessive inheritance\",\"hpo\":\"HP:0000007\",\"source\":\"hpo\"},{\"id\":\"OMIM:614134\",\"name\":\"Short stature\",\"hpo\":\"HP:0004322\",\"source\":\"hpo\"},{\"id\":\"OMIM:614134\",\"name\":\"Astigmatism\",\"hpo\":\"HP:0000483\",\"source\":\"hpo\"},{\"id\":\"OMIM:614134\",\"name\":\"Genu valgum\",\"hpo\":\"HP:0002857\",\"source\":\"hpo\"},{\"id\":\"OMIM:614134\",\"name\":\"Sensorineural hearing impairment\",\"hpo\":\"HP:0000407\",\"source\":\"hpo\"},{\"id\":\"OMIM:614134\",\"name\":\"Degenerative vitreoretinopathy\",\"hpo\":\"HP:0007964\",\"source\":\"hpo\"},{\"id\":\"OMIM:614134\",\"name\":\"Irregular capital femoral epiphysis\",\"hpo\":\"HP:0005041\",\"source\":\"hpo\"},{\"id\":\"OMIM:614135\",\"name\":\"Flat capital femoral epiphysis\",\"hpo\":\"HP:0003370\",\"source\":\"hpo\"},{\"id\":\"OMIM:614135\",\"name\":\"Childhood onset\",\"hpo\":\"HP:0011463\",\"source\":\"hpo\"},{\"id\":\"OMIM:614135\",\"name\":\"Autosomal dominant inheritance\",\"hpo\":\"HP:0000006\",\"source\":\"hpo\"},{\"id\":\"OMIM:614135\",\"name\":\"Arthralgia\",\"hpo\":\"HP:0002829\",\"source\":\"hpo\"},{\"id\":\"OMIM:614135\",\"name\":\"Multiple epiphyseal dysplasia\",\"hpo\":\"HP:0002654\",\"source\":\"hpo\"},{\"id\":\"OMIM:614135\",\"name\":\"Flat distal femoral epiphysis\",\"hpo\":\"HP:0006398\",\"source\":\"hpo\"},{\"id\":\"OMIM:614135\",\"name\":\"Osteoarthritis\",\"hpo\":\"HP:0002758\",\"source\":\"hpo\"},{\"id\":\"OMIM:614135\",\"name\":\"Arthralgia of the hip\",\"hpo\":\"HP:0003365\",\"source\":\"hpo\"},{\"id\":\"OMIM:614135\",\"name\":\"Small epiphyses\",\"hpo\":\"HP:0010585\",\"source\":\"hpo\"},{\"id\":\"OMIM:614135\",\"name\":\"Irregular epiphyses\",\"hpo\":\"HP:0010582\",\"source\":\"hpo\"},{\"id\":\"OMIM:614135\",\"name\":\"Irregular vertebral endplates\",\"hpo\":\"HP:0003301\",\"source\":\"hpo\"},{\"id\":\"OMIM:614135\",\"name\":\"Irregular distal femoral epiphysis\",\"hpo\":\"HP:0006407\",\"source\":\"hpo\"},{\"id\":\"OMIM:614135\",\"name\":\"Abnormality of the knees\",\"hpo\":\"HP:0002815\",\"source\":\"hpo\"},{\"id\":\"OMIM:614135\",\"name\":\"Schmorl's node\",\"hpo\":\"HP:0030041\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:250984\",\"name\":\"Visual impairment\",\"hpo\":\"HP:0000505\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:250984\",\"name\":\"Genu valgum\",\"hpo\":\"HP:0002857\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:250984\",\"name\":\"Abnormality of epiphysis morphology\",\"hpo\":\"HP:0005930\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:250984\",\"name\":\"Joint hypermobility\",\"hpo\":\"HP:0001382\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:250984\",\"name\":\"Abnormality of the vitreous humor\",\"hpo\":\"HP:0004327\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:250984\",\"name\":\"Skeletal dysplasia\",\"hpo\":\"HP:0002652\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:250984\",\"name\":\"Sensorineural hearing impairment\",\"hpo\":\"HP:0000407\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:250984\",\"name\":\"Short stature\",\"hpo\":\"HP:0004322\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:250984\",\"name\":\"Malar flattening\",\"hpo\":\"HP:0000272\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:250984\",\"name\":\"Astigmatism\",\"hpo\":\"HP:0000483\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:250984\",\"name\":\"Platyspondyly\",\"hpo\":\"HP:0000926\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:250984\",\"name\":\"Myopia\",\"hpo\":\"HP:0000545\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:250984\",\"name\":\"Abnormality of retinal pigmentation\",\"hpo\":\"HP:0007703\",\"source\":\"hpo\"},{\"id\":\"ORPHANET:250984\",\"name\":\"Retinopathy\",\"hpo\":\"HP:0000488\",\"source\":\"hpo\"},{\"id\":\"umls:C0158252\",\"name\":\"Intervertebral disc disease\",\"score\":0.42548466,\"numberOfPubmeds\":3,\"associationTypes\":[\"Biomarker, GeneticVariation\"],\"sources\":[\"BeFree\",\"CTD_human\",\"GAD\",\"UNIPROT\"],\"source\":\"disgenet\"},{\"id\":\"umls:C1838429\",\"name\":\"Epiphyseal dysplasia, multiple, 2\",\"score\":0.41,\"associationTypes\":[\"Biomarker, GeneticVariation\"],\"sources\":[\"CLINVAR\",\"CTD_human\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0000786\",\"name\":\"Abortion, Spontaneous\",\"score\":0.21,\"numberOfPubmeds\":1,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"CTD_human\"],\"source\":\"disgenet\"},{\"id\":\"umls:C3280342\",\"name\":\"STICKLER SYNDROME, TYPE V\",\"score\":0.2,\"associationTypes\":[\"GeneticVariation\"],\"sources\":[\"CLINVAR\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0158266\",\"name\":\"Intervertebral Disc Degeneration\",\"score\":0.008085221,\"numberOfPubmeds\":6,\"associationTypes\":[\"Biomarker, GeneticVariation\"],\"sources\":[\"BeFree\",\"GAD\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0029422\",\"name\":\"Osteochondrodysplasias\",\"score\":0.005045154,\"numberOfPubmeds\":2,\"associationTypes\":[\"GeneticVariation\"],\"sources\":[\"LHGDN\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0037933\",\"name\":\"Spinal Diseases\",\"score\":0.004634135,\"numberOfPubmeds\":2,\"associationTypes\":[\"GeneticVariation\"],\"sources\":[\"GAD\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0021818\",\"name\":\"Intervertebral Disc Displacement\",\"score\":0.004634135,\"numberOfPubmeds\":2,\"associationTypes\":[\"GeneticVariation\"],\"sources\":[\"GAD\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0026760\",\"name\":\"Multiple epiphyseal dysplasia\",\"score\":0.004536073,\"numberOfPubmeds\":16,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0221775\",\"name\":\"LUMBAR DISC DISEASE\",\"score\":0.0037345905,\"numberOfPubmeds\":5,\"associationTypes\":[\"Biomarker, GeneticVariation\"],\"sources\":[\"BeFree\",\"GAD\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0036396\",\"name\":\"Sciatica\",\"score\":0.0028840767,\"numberOfPubmeds\":2,\"associationTypes\":[\"Biomarker, GeneticVariation\"],\"sources\":[\"BeFree\",\"GAD\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0029408\",\"name\":\"Osteoarthritis\",\"score\":0.0028840767,\"numberOfPubmeds\":3,\"associationTypes\":[\"Biomarker, GeneticVariation\"],\"sources\":[\"BeFree\",\"GAD\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0036439\",\"name\":\"Scoliosis\",\"score\":0.0023170675,\"numberOfPubmeds\":1,\"associationTypes\":[\"GeneticVariation\"],\"sources\":[\"GAD\"],\"source\":\"disgenet\"},{\"id\":\"umls:C2825055\",\"name\":\"Recurrence\",\"score\":0.0023170675,\"numberOfPubmeds\":1,\"associationTypes\":[\"GeneticVariation\"],\"sources\":[\"GAD\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0024031\",\"name\":\"Low Back Pain\",\"score\":0.0023170675,\"numberOfPubmeds\":1,\"associationTypes\":[\"GeneticVariation\"],\"sources\":[\"GAD\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0025149\",\"name\":\"Medulloblastoma\",\"score\":0.0022680366,\"numberOfPubmeds\":8,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0410538\",\"name\":\"Pseudoachondroplasia\",\"score\":0.0008505137,\"numberOfPubmeds\":3,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0041834\",\"name\":\"Erythema\",\"score\":0.0008505137,\"numberOfPubmeds\":3,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C1456873\",\"name\":\"alpha^+^ Thalassemia\",\"score\":0.0008505137,\"numberOfPubmeds\":3,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0002312\",\"name\":\"alpha-Thalassemia\",\"score\":0.0008505137,\"numberOfPubmeds\":3,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0272002\",\"name\":\"alpha^0^ Thalassemia\",\"score\":0.00056700915,\"numberOfPubmeds\":2,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0263874\",\"name\":\"Degeneration of lumbar intervertebral disc\",\"score\":0.00056700915,\"numberOfPubmeds\":2,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0265253\",\"name\":\"Stickler Syndrome\",\"score\":0.00056700915,\"numberOfPubmeds\":2,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0017638\",\"name\":\"Glioma\",\"score\":0.00056700915,\"numberOfPubmeds\":2,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0020445\",\"name\":\"Hyperlipoproteinemia Type II\",\"score\":0.00056700915,\"numberOfPubmeds\":2,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0751214\",\"name\":\"Hyperalgesia, Thermal\",\"score\":0.00028350457,\"numberOfPubmeds\":1,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0009782\",\"name\":\"Connective Tissue Diseases\",\"score\":0.00028350457,\"numberOfPubmeds\":1,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0585052\",\"name\":\"Chronic sciatica\",\"score\":0.00028350457,\"numberOfPubmeds\":1,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0025517\",\"name\":\"Metabolic Diseases\",\"score\":0.00028350457,\"numberOfPubmeds\":1,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0029410\",\"name\":\"Osteoarthritis, Hip\",\"score\":0.00028350457,\"numberOfPubmeds\":1,\"associationTypes\":[\"GeneticVariation\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0002726\",\"name\":\"Amyloidosis\",\"score\":0.00028350457,\"numberOfPubmeds\":1,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0220726\",\"name\":\"Diastrophic dysplasia\",\"score\":0.00028350457,\"numberOfPubmeds\":1,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0031069\",\"name\":\"Familial Mediterranean Fever\",\"score\":0.00028350457,\"numberOfPubmeds\":1,\"associationTypes\":[\"GeneticVariation\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0020305\",\"name\":\"Hydrops Fetalis\",\"score\":0.00028350457,\"numberOfPubmeds\":1,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0027765\",\"name\":\"Nervous System Diseases\",\"score\":0.00028350457,\"numberOfPubmeds\":1,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C3697376\",\"name\":\"Oculoskeletal dysplasia\",\"score\":0.00028350457,\"numberOfPubmeds\":1,\"associationTypes\":[\"GeneticVariation\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0472761\",\"name\":\"Homozygous alpha thalassemia\",\"score\":0.00028350457,\"numberOfPubmeds\":1,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C1261473\",\"name\":\"Sarcoma\",\"score\":0.00028350457,\"numberOfPubmeds\":1,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0019693\",\"name\":\"HIV Infections\",\"score\":0.00028350457,\"numberOfPubmeds\":1,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0039730\",\"name\":\"Thalassemia\",\"score\":0.00028350457,\"numberOfPubmeds\":1,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0205748\",\"name\":\"Dysplastic Nevus\",\"score\":0.00028350457,\"numberOfPubmeds\":1,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"},{\"id\":\"umls:C0343284\",\"name\":\"Chondrodysplasia\",\"score\":0.00028350457,\"numberOfPubmeds\":1,\"associationTypes\":[\"Biomarker\"],\"sources\":[\"BeFree\"],\"source\":\"disgenet\"}],\"geneDrugInteraction\":[],\"variantTraitAssociation\":{\"clinvar\":[{\"accession\":\"RCV000176928\",\"clinicalSignificance\":\"Uncertain significance\",\"traits\":[\"not provided\"],\"geneNames\":[\"COL9A2\"],\"reviewStatus\":\"CRITERIA_PROVIDED_SINGLE_SUBMITTER\"}],\"gwas\":[],\"cosmic\":[]},\"functionalScore\":[{\"score\":32,\"source\":\"cadd_scaled\"},{\"score\":6.770000457763672,\"source\":\"cadd_raw\"}]}]",
                variantAnnotation, VariantAnnotation.class);

        ////        http://wwwdev.ebi.ac.uk/cellbase/webservices/rest/v3/hsapiens/genomic/variant/2:114340663:GCTGGGCATCC:ACTGGGCATCC/full_annotation
////        http://wwwdev.ebi.ac.uk/cellbase/webservices/rest/v3/hsapiens/genomic/variant/2:114340663:GCTGGGCATCCT:ACTGGGCATCCT/full_annotation

//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("1:40768842:C:G")  // Should not raise NPE
//                , new QueryOptions()).get(0).getResult()).get(0));  // should not return NPE
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("22:16275272:C:T")  // Should not raise NPE
//                , new QueryOptions()).get(0).getResult()).get(0));  // should not return NPE
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("22:16050654:A:<CN0>")  // Should not raise NPE
//                , new QueryOptions()).get(0).getResult()).get(0));  // should not return NPE
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("MT:7443:A:G")  // Should not raise NPE
//                , new QueryOptions()).get(0).getResult()).get(0));  // should not return NPE
//        variantAnnotationList.add((VariantAnnotatio<n) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("2:48025849:CGCCAAATAAA:CCACAAATAAA")  // Should return drug interactions
//                , new QueryOptions()).get(0).getResult()).get(0));  // should not return NPE
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("2:48025849:CGCCAAATAAA:CTGTTTATAAA")  // Should return drug interactions
//                , new QueryOptions()).get(0).getResult()).get(0));  // should not return NPE
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("2:114340663:GCTGGGTATT:ACTGGGCATCCT")  // Should return drug interactions
//                , new QueryOptions()).get(0).getResult()).get(0));  // should not return NPE
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("10:61482087:G:C")  // Should not fail
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("10:56370672:A:C")  // Should not fail
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("10:102672886:C:A")  // Should not fail
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("10:108338984:C:T")  // Should not fail
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("1:144854598:C:T")  // Should return cadd values
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("1:69100:G:T")  // Should return cadd values
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("10:14981854:G:A")  // Should return cadd values
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("1:249240621:G:T")  // Should return cadd values
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("6:160990451:C:G")  // Should return drug interactions
//                , new QueryOptions()).get(0).getResult()).get(0));  // should not return NPE
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Variant.parseVariants("22:16051722:TA:T")  // Should return drug interactions
//                , new QueryOptions()).get(0).getResult()).get(0));  // should not return NPE
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("1", 948813, "G", "C"))  // Should return drug interactions
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("1", 167385325, "A", "-"))  // Should not return null
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("1", 220603289, "-", "GTGT"))  // Should not return null
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("19", 45411941, "T", "C"))  // Should return any result
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("22", 16050612, "C", "G"))  // Should return any result
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("13", 45411941, "T", "C"))  // Should return any result
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("21", 18992155, "T", "C"))  // Should return any result
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("2", 130498751, "A", "G"))  // Should return any result
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationList.add((VariantAnnotation) ((List) variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("19", 45411941, "T", "C"))  // Should return any result
//                , new QueryOptions()).get(0).getResult()).get(0));
//        variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("22", 21982892, "C", "T"))  // Should return any result
//                , new QueryOptions());
//        variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("22", 21982892, "C", "G"))  // Should return any result
//                , new QueryOptions());
//        variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("10", 78444456, "G", "T"))  // Should include population frequencies
//                , new QueryOptions());
//        variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("22", 22022872, "T", "C"))  // Should not raise java.lang.NullPointerException
//                , new QueryOptions());
//        variantAnnotationCalculator.getAnnotationByVariantList(Collections.singletonList(new Variant("22", 16123409, "-", "A"))
//                , new QueryOptions());

//        VepFormatWriter vepFormatWriter = new VepFormatWriter("/tmp/test.vep");
//        vepFormatWriter.open();
//        vepFormatWriter.pre();
//        vepFormatWriter.write(variantAnnotationList);
//        vepFormatWriter.post();
//        vepFormatWriter.close();



    }

    private int countLines(String fileName) throws IOException {
        System.out.println("Counting lines...");
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        int lines = 0;
        while (reader.readLine() != null) lines++;
        reader.close();

        return lines;
    }

    public class ConsequenceTypeComparator implements Comparator<ConsequenceType> {
        public int compare(ConsequenceType consequenceType1, ConsequenceType consequenceType2) {
            String geneId1 = consequenceType1.getEnsemblGeneId()==null?"":consequenceType1.getEnsemblGeneId();
            String geneId2 = consequenceType2.getEnsemblGeneId()==null?"":consequenceType2.getEnsemblGeneId();

            int geneComparison = geneId1.compareTo(geneId2);
            if(geneComparison == 0 && !geneId1.equals("")) {
                return consequenceType1.getEnsemblTranscriptId().compareTo(consequenceType2.getEnsemblTranscriptId());
            } else {
                return geneComparison;
            }
        }
    }

    private class AnnotationComparisonObject {
        String chr;
        String pos;
        String alt;
        String ensemblGeneId;
        String ensemblTranscriptId;
        String biotype;
        String SOname;

        public AnnotationComparisonObject(String chr, String pos, String alt, String ensemblGeneId,
                                          String ensemblTranscriptId, String SOname) {
            this(chr, pos, alt, ensemblGeneId, ensemblTranscriptId, "-", SOname);
        }

        public AnnotationComparisonObject(String chr, String pos, String alt, String ensemblGeneId,
                                          String ensemblTranscriptId, String biotype, String SOname) {
            this.chr = chr;
            this.pos = pos;
            this.alt = alt;
            this.ensemblGeneId = ensemblGeneId;
            this.ensemblTranscriptId = ensemblTranscriptId;
            this.biotype = biotype;
            this.SOname = SOname;
        }

        public String getChr() {
            return chr;
        }

        public String getPos() {
            return pos;
        }

        public String getAlt() {
            return alt;
        }

        public String getEnsemblGeneId() {
            return ensemblGeneId;
        }

        public String getSOname() {
            return SOname;
        }

        public String getEnsemblTranscriptId() {
            return ensemblTranscriptId;
        }

        public void setEnsemblTranscriptId(String ensemblTranscriptId) {
            this.ensemblTranscriptId = ensemblTranscriptId;
        }

        public String getBiotype() {
            return biotype;
        }

        public void setBiotype(String biotype) {
            this.biotype = biotype;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AnnotationComparisonObject)) return false;

            AnnotationComparisonObject that = (AnnotationComparisonObject) o;

            if (SOname != null ? !SOname.equals(that.SOname) : that.SOname != null) return false;
            if (alt != null ? !alt.equals(that.alt) : that.alt != null) return false;
            if (chr != null ? !chr.equals(that.chr) : that.chr != null) return false;
            if (ensemblGeneId != null ? !ensemblGeneId.equals(that.ensemblGeneId) : that.ensemblGeneId != null)
                return false;
            if (ensemblTranscriptId != null ? !ensemblTranscriptId.equals(that.ensemblTranscriptId) : that.ensemblTranscriptId != null)
                return false;
            if (pos != null ? !pos.equals(that.pos) : that.pos != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = chr != null ? chr.hashCode() : 0;
            result = 31 * result + (pos != null ? pos.hashCode() : 0);
            result = 31 * result + (alt != null ? alt.hashCode() : 0);
            result = 31 * result + (ensemblGeneId != null ? ensemblGeneId.hashCode() : 0);
            result = 31 * result + (ensemblTranscriptId != null ? ensemblTranscriptId.hashCode() : 0);
            result = 31 * result + (SOname != null ? SOname.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return chr+"\t"+pos+"\t"+alt+"\t"+ensemblGeneId+"\t"+ensemblTranscriptId+"\t"+biotype+"\t"+SOname+"\n";
        }
    }

    public class AnnotationComparisonObjectComparator implements Comparator<AnnotationComparisonObject> {
        public int compare(AnnotationComparisonObject annotationComparisonObject1, AnnotationComparisonObject annotationComparisonObject2) {

            int chrComparison = annotationComparisonObject1.getChr().compareTo(annotationComparisonObject2.getChr());
            if(chrComparison == 0) {
                return annotationComparisonObject1.getPos().compareTo(annotationComparisonObject2.getPos());
            } else {
                return chrComparison;
            }
        }

    }

    @Test
    public void testGetAllConsequenceTypesByVariant() throws IOException, URISyntaxException {
        QueryResult<ConsequenceType> consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16287261, "G", "A"),
                        new QueryOptions());  // should not return things like {"score":0.0,"source":null,"description":null}  for ENST00000343518 substitution scores
        assertObjectListEquals("[{\"geneName\":\"POTEH\",\"ensemblGeneId\":\"ENSG00000198062\",\"ensemblTranscriptId\":\"ENST00000452800\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"cdnaPosition\":457,\"cdsPosition\":457,\"codon\":\"Caa/Taa\",\"proteinVariantAnnotation\":{\"position\":153,\"reference\":\"GLN\",\"alternate\":\"STOP\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001587\",\"name\":\"stop_gained\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"POTEH\",\"ensemblGeneId\":\"ENSG00000198062\",\"ensemblTranscriptId\":\"ENST00000343518\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":677,\"cdsPosition\":625,\"codon\":\"Caa/Taa\",\"proteinVariantAnnotation\":{\"position\":209,\"reference\":\"GLN\",\"alternate\":\"STOP\"},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001587\",\"name\":\"stop_gained\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16057210, "C", "T"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"LA16c-4G1.3\",\"ensemblGeneId\":\"ENSG00000233866\",\"ensemblTranscriptId\":\"ENST00000424770\",\"strand\":\"+\",\"biotype\":\"lincRNA\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_gene_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("2", 163395, "C", "G"),
                        new QueryOptions());
        assertObjectListEquals("[{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001628\",\"name\":\"intergenic_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("18", 163395, "C", "G"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000582707\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":420,\"cdsPosition\":104,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P54578\",\"position\":35,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.01,\"source\":\"sift\",\"description\":\"deleterious\"},{\"score\":0.967,\"source\":\"polyphen\",\"description\":\"probably damaging\"}],\"keywords\":[\"3D-structure\",\"Acetylation\",\"Alternative splicing\",\"Cell membrane\",\"Complete proteome\",\"Cytoplasm\",\"Hydrolase\",\"Membrane\",\"Phosphoprotein\",\"Protease\",\"Proteasome\",\"Reference proteome\",\"Thiol protease\",\"Ubl conjugation pathway\"],\"features\":[{\"id\":\"IPR029071\",\"start\":2,\"end\":92,\"description\":\"Ubiquitin-related domain\"},{\"id\":\"IPR019954\",\"start\":30,\"end\":55,\"description\":\"Ubiquitin conserved site\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":74,\"description\":\"Ubiquitin domain\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":72,\"description\":\"Ubiquitin domain\"},{\"start\":4,\"end\":80,\"type\":\"domain\",\"description\":\"Ubiquitin-like\"},{\"id\":\"PRO_0000080636\",\"start\":1,\"end\":494,\"type\":\"chain\",\"description\":\"Ubiquitin carboxyl-terminal hydrolase 14\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000400266\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":267,\"cdsPosition\":104,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P54578\",\"position\":35,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.01,\"source\":\"sift\",\"description\":\"deleterious\"},{\"score\":0.881,\"source\":\"polyphen\",\"description\":\"possibly damaging\"}],\"keywords\":[\"3D-structure\",\"Acetylation\",\"Alternative splicing\",\"Cell membrane\",\"Complete proteome\",\"Cytoplasm\",\"Hydrolase\",\"Membrane\",\"Phosphoprotein\",\"Protease\",\"Proteasome\",\"Reference proteome\",\"Thiol protease\",\"Ubl conjugation pathway\"],\"features\":[{\"id\":\"IPR029071\",\"start\":2,\"end\":92,\"description\":\"Ubiquitin-related domain\"},{\"id\":\"IPR019954\",\"start\":30,\"end\":55,\"description\":\"Ubiquitin conserved site\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":74,\"description\":\"Ubiquitin domain\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":72,\"description\":\"Ubiquitin domain\"},{\"start\":4,\"end\":80,\"type\":\"domain\",\"description\":\"Ubiquitin-like\"},{\"id\":\"PRO_0000080636\",\"start\":1,\"end\":494,\"type\":\"chain\",\"description\":\"Ubiquitin carboxyl-terminal hydrolase 14\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000580410\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":438,\"cdsPosition\":26,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"position\":9,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.926,\"source\":\"polyphen\",\"description\":\"probably damaging\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000578942\",\"strand\":\"+\",\"biotype\":\"retained_intron\",\"cdnaPosition\":242,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001792\",\"name\":\"non_coding_transcript_exon_variant\"},{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000383589\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":212,\"cdsPosition\":104,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"position\":35,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.01,\"source\":\"sift\",\"description\":\"deleterious\"},{\"score\":0.985,\"source\":\"polyphen\",\"description\":\"probably damaging\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000261601\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":195,\"cdsPosition\":104,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P54578\",\"position\":35,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.01,\"source\":\"sift\",\"description\":\"deleterious\"},{\"score\":0.926,\"source\":\"polyphen\",\"description\":\"probably damaging\"}],\"keywords\":[\"3D-structure\",\"Acetylation\",\"Alternative splicing\",\"Cell membrane\",\"Complete proteome\",\"Cytoplasm\",\"Hydrolase\",\"Membrane\",\"Phosphoprotein\",\"Protease\",\"Proteasome\",\"Reference proteome\",\"Thiol protease\",\"Ubl conjugation pathway\"],\"features\":[{\"id\":\"IPR029071\",\"start\":2,\"end\":92,\"description\":\"Ubiquitin-related domain\"},{\"id\":\"IPR019954\",\"start\":30,\"end\":55,\"description\":\"Ubiquitin conserved site\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":74,\"description\":\"Ubiquitin domain\"},{\"id\":\"IPR000626\",\"start\":4,\"end\":72,\"description\":\"Ubiquitin domain\"},{\"start\":4,\"end\":80,\"type\":\"domain\",\"description\":\"Ubiquitin-like\"},{\"id\":\"PRO_0000080636\",\"start\":1,\"end\":494,\"type\":\"chain\",\"description\":\"Ubiquitin carboxyl-terminal hydrolase 14\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000581983\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":444,\"cdsPosition\":26,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"position\":9,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[{\"score\":0.926,\"source\":\"polyphen\",\"description\":\"probably damaging\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"USP14\",\"ensemblGeneId\":\"ENSG00000101557\",\"ensemblTranscriptId\":\"ENST00000583119\",\"strand\":\"+\",\"biotype\":\"nonsense_mediated_decay\",\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"cdnaPosition\":3,\"cdsPosition\":5,\"codon\":\"gCg/gGg\",\"proteinVariantAnnotation\":{\"position\":2,\"reference\":\"ALA\",\"alternate\":\"GLY\",\"substitutionScores\":[]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17054103, "G", "A"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"KB-67B5.12\",\"ensemblGeneId\":\"ENSG00000233995\",\"ensemblTranscriptId\":\"ENST00000454360\",\"strand\":\"+\",\"biotype\":\"unprocessed_pseudogene\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"},{\"accession\":\"SO:0001575\",\"name\":\"splice_donor_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("18", 30913143, "T", ""),
                        new QueryOptions());  // should not return String Index Out of Bounds
        assertObjectListEquals("[{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000403303\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1016,\"cdsPosition\":874,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000383096\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1057,\"cdsPosition\":874,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000300227\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1085,\"cdsPosition\":874,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000579916\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001627\",\"name\":\"intron_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000583930\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":953,\"cdsPosition\":874,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000406524\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":898,\"cdsPosition\":874,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000402325\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":874,\"cdsPosition\":874,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000579947\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":1057,\"cdsPosition\":874,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000577268\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"cdnaPosition\":228,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001792\",\"name\":\"non_coding_transcript_exon_variant\"},{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"}]},{\"geneName\":\"CCDC178\",\"ensemblGeneId\":\"ENSG00000166960\",\"ensemblTranscriptId\":\"ENST00000399177\",\"strand\":\"-\",\"biotype\":\"non_stop_decay\",\"cdnaPosition\":1017,\"cdsPosition\":874,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001626\",\"name\":\"incomplete_terminal_codon_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("14", 38679764, "-", "GATCTGAGAAGNGGAANANAAGGG"),
                        new QueryOptions());  // should not return NPE
        assertObjectListEquals("[{\"geneName\":\"SSTR1\",\"ensemblGeneId\":\"ENSG00000139874\",\"ensemblTranscriptId\":\"ENST00000267377\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":1786,\"cdsPosition\":1169,\"proteinVariantAnnotation\":{\"position\":390},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001587\",\"name\":\"stop_gained\"},{\"accession\":\"SO:0001821\",\"name\":\"inframe_insertion\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("20", 44485953, "-", "ATCT"),
                        new QueryOptions());  // should return ENSG00000101473 ENST00000217455 -       initiator_codon_variant
        assertObjectListEquals("[{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000217455\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":93,\"cdsPosition\":2,\"proteinVariantAnnotation\":{\"position\":1},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001582\",\"name\":\"initiator_codon_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000461272\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"cdnaPosition\":76,\"cdsPosition\":2,\"proteinVariantAnnotation\":{\"position\":1},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001582\",\"name\":\"initiator_codon_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000488679\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"cdnaPosition\":89,\"cdsPosition\":2,\"proteinVariantAnnotation\":{\"position\":1},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001582\",\"name\":\"initiator_codon_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000487205\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_gene_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000493118\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"cdnaPosition\":58,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001792\",\"name\":\"non_coding_transcript_exon_variant\"},{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000483141\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"cdnaPosition\":56,\"cdsPosition\":2,\"proteinVariantAnnotation\":{\"position\":1},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001582\",\"name\":\"initiator_codon_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000484783\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"cdnaPosition\":84,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001792\",\"name\":\"non_coding_transcript_exon_variant\"},{\"accession\":\"SO:0001619\",\"name\":\"non_coding_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000486165\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"cdnaPosition\":58,\"cdsPosition\":2,\"proteinVariantAnnotation\":{\"position\":1},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001582\",\"name\":\"initiator_codon_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000481938\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"cdnaPosition\":92,\"cdsPosition\":2,\"proteinVariantAnnotation\":{\"position\":1},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001582\",\"name\":\"initiator_codon_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000457981\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"2KB_upstream_gene_variant\"}]},{\"geneName\":\"ACOT8\",\"ensemblGeneId\":\"ENSG00000101473\",\"ensemblTranscriptId\":\"ENST00000426915\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"2KB_upstream_gene_variant\"}]},{\"geneName\":\"ZSWIM3\",\"ensemblGeneId\":\"ENSG00000132801\",\"ensemblTranscriptId\":\"ENST00000255152\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"2KB_upstream_gene_variant\"}]},{\"geneName\":\"ZSWIM3\",\"ensemblGeneId\":\"ENSG00000132801\",\"ensemblTranscriptId\":\"ENST00000454862\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"2KB_upstream_gene_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("15", 78224189, "-", "C"),
                        new QueryOptions());  // should return ENSG00000101473 ENST00000217455 -       initiator_codon_variant
        assertObjectListEquals("[{\"geneName\":\"RP11-114H24.2\",\"ensemblGeneId\":\"ENSG00000260776\",\"ensemblTranscriptId\":\"ENST00000567226\",\"strand\":\"-\",\"biotype\":\"processed_transcript\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_gene_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("13", 52718051, "-", "T"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000258597\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"cdnaPosition\":951,\"cdsPosition\":876,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000548127\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"cdnaPosition\":1252,\"cdsPosition\":876,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000339406\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":1252,\"cdsPosition\":876,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000378101\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":1111,\"cdsPosition\":876,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000400357\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":2170,\"cdsPosition\":876,\"proteinVariantAnnotation\":{\"position\":292},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000452082\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"cdnaPosition\":2170,\"cdsPosition\":939,\"proteinVariantAnnotation\":{\"position\":313},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000547820\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"transcriptAnnotationFlags\":[\"cds_start_NF\",\"mRNA_start_NF\"],\"cdnaPosition\":65,\"cdsPosition\":66,\"proteinVariantAnnotation\":{\"position\":22},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001630\",\"name\":\"splice_region_variant\"},{\"accession\":\"SO:0001589\",\"name\":\"frameshift_variant\"},{\"accession\":\"SO:0001621\",\"name\":\"NMD_transcript_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000551355\",\"strand\":\"-\",\"biotype\":\"nonsense_mediated_decay\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"2KB_downstream_gene_variant\"}]},{\"geneName\":\"NEK3\",\"ensemblGeneId\":\"ENSG00000136098\",\"ensemblTranscriptId\":\"ENST00000552973\",\"strand\":\"-\",\"biotype\":\"retained_intron\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_gene_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("13", 32893271, "A", "G"),
                        new QueryOptions());  // should set functional description "In BC and ovarian cancer; unknown pathological significance; dbSNP:rs4987046" for ENST00000380152
        assertObjectListEquals("[{\"geneName\":\"ZAR1L\",\"ensemblGeneId\":\"ENSG00000189167\",\"ensemblTranscriptId\":\"ENST00000533490\",\"strand\":\"-\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001631\",\"name\":\"upstream_gene_variant\"}]},{\"geneName\":\"BRCA2\",\"ensemblGeneId\":\"ENSG00000139618\",\"ensemblTranscriptId\":\"ENST00000380152\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":358,\"cdsPosition\":125,\"codon\":\"tAt/tGt\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P51587\",\"position\":42,\"reference\":\"TYR\",\"alternate\":\"CYS\",\"uniprotVariantId\":\"VAR_020705\",\"functionalDescription\":\"In BC and ovarian cancer; unknown pathological significance; dbSNP:rs4987046.\",\"substitutionScores\":[{\"score\":0.11,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0.032,\"source\":\"polyphen\",\"description\":\"benign\"}],\"keywords\":[\"3D-structure\",\"Cell cycle\",\"Complete proteome\",\"Cytoplasm\",\"Cytoskeleton\",\"Disease mutation\",\"DNA damage\",\"DNA recombination\",\"DNA repair\",\"DNA-binding\",\"Fanconi anemia\",\"Nucleus\",\"Phosphoprotein\",\"Polymorphism\",\"Reference proteome\",\"Repeat\",\"Tumor suppressor\",\"Ubl conjugation\"],\"features\":[{\"id\":\"IPR015525\",\"start\":2,\"end\":1709,\"description\":\"Breast cancer type 2 susceptibility protein\"},{\"id\":\"IPR015525\",\"start\":1,\"end\":3418,\"type\":\"chain\",\"description\":\"Breast cancer type 2 susceptibility protein\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"BRCA2\",\"ensemblGeneId\":\"ENSG00000139618\",\"ensemblTranscriptId\":\"ENST00000544455\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":352,\"cdsPosition\":125,\"codon\":\"tAt/tGt\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P51587\",\"position\":42,\"reference\":\"TYR\",\"alternate\":\"CYS\",\"uniprotVariantId\":\"VAR_020705\",\"functionalDescription\":\"In BC and ovarian cancer; unknown pathological significance; dbSNP:rs4987046.\",\"substitutionScores\":[{\"score\":0.11,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0.032,\"source\":\"polyphen\",\"description\":\"benign\"}],\"keywords\":[\"3D-structure\",\"Cell cycle\",\"Complete proteome\",\"Cytoplasm\",\"Cytoskeleton\",\"Disease mutation\",\"DNA damage\",\"DNA recombination\",\"DNA repair\",\"DNA-binding\",\"Fanconi anemia\",\"Nucleus\",\"Phosphoprotein\",\"Polymorphism\",\"Reference proteome\",\"Repeat\",\"Tumor suppressor\",\"Ubl conjugation\"],\"features\":[{\"id\":\"IPR015525\",\"start\":2,\"end\":1709,\"description\":\"Breast cancer type 2 susceptibility protein\"},{\"id\":\"IPR015525\",\"start\":1,\"end\":3418,\"type\":\"chain\",\"description\":\"Breast cancer type 2 susceptibility protein\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"BRCA2\",\"ensemblGeneId\":\"ENSG00000139618\",\"ensemblTranscriptId\":\"ENST00000530893\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":323,\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001623\",\"name\":\"5_prime_UTR_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);

        consequenceTypeResult =
                variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("19", 45411941, "T", "C"),
                        new QueryOptions());
        assertObjectListEquals("[{\"geneName\":\"TOMM40\",\"ensemblGeneId\":\"ENSG00000130204\",\"ensemblTranscriptId\":\"ENST00000252487\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_gene_variant\"}]},{\"geneName\":\"TOMM40\",\"ensemblGeneId\":\"ENSG00000130204\",\"ensemblTranscriptId\":\"ENST00000592434\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"basic\"],\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"downstream_gene_variant\"}]},{\"geneName\":\"APOE\",\"ensemblGeneId\":\"ENSG00000130203\",\"ensemblTranscriptId\":\"ENST00000252486\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"CCDS\",\"basic\"],\"cdnaPosition\":499,\"cdsPosition\":388,\"codon\":\"Tgc/Cgc\",\"proteinVariantAnnotation\":{\"uniprotAccession\":\"P02649\",\"position\":130,\"reference\":\"CYS\",\"alternate\":\"ARG\",\"uniprotVariantId\":\"VAR_000652\",\"functionalDescription\":\"In HLPP3; form E3**, form E4, form E4/3 and some forms E5-type; only form E3** is disease-linked; dbSNP:rs429358.\",\"substitutionScores\":[{\"score\":1,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0,\"source\":\"polyphen\",\"description\":\"benign\"}],\"keywords\":[\"3D-structure\",\"Alzheimer disease\",\"Amyloidosis\",\"Cholesterol metabolism\",\"Chylomicron\",\"Complete proteome\",\"Direct protein sequencing\",\"Disease mutation\",\"Glycation\",\"Glycoprotein\",\"HDL\",\"Heparin-binding\",\"Hyperlipidemia\",\"Lipid metabolism\",\"Lipid transport\",\"Neurodegeneration\",\"Oxidation\",\"Phosphoprotein\",\"Polymorphism\",\"Reference proteome\",\"Repeat\",\"Secreted\",\"Signal\",\"Steroid metabolism\",\"Sterol metabolism\",\"Transport\",\"VLDL\"],\"features\":[{\"start\":106,\"end\":141,\"type\":\"helix\"},{\"start\":80,\"end\":255,\"type\":\"region of interest\",\"description\":\"8 X 22 AA approximate tandem repeats\"},{\"start\":124,\"end\":145,\"type\":\"repeat\",\"description\":\"3\"},{\"id\":\"IPR000074\",\"start\":81,\"end\":292,\"description\":\"Apolipoprotein A/E\"},{\"id\":\"PRO_0000001987\",\"start\":19,\"end\":317,\"type\":\"chain\",\"description\":\"Apolipoprotein E\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"APOE\",\"ensemblGeneId\":\"ENSG00000130203\",\"ensemblTranscriptId\":\"ENST00000446996\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":477,\"cdsPosition\":388,\"codon\":\"Tgc/Cgc\",\"proteinVariantAnnotation\":{\"position\":130,\"reference\":\"CYS\",\"alternate\":\"ARG\",\"substitutionScores\":[{\"score\":1,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0,\"source\":\"polyphen\",\"description\":\"benign\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"APOE\",\"ensemblGeneId\":\"ENSG00000130203\",\"ensemblTranscriptId\":\"ENST00000485628\",\"strand\":\"+\",\"biotype\":\"retained_intron\",\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001632\",\"name\":\"2KB_downstream_gene_variant\"}]},{\"geneName\":\"APOE\",\"ensemblGeneId\":\"ENSG00000130203\",\"ensemblTranscriptId\":\"ENST00000434152\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":523,\"cdsPosition\":466,\"codon\":\"Tgc/Cgc\",\"proteinVariantAnnotation\":{\"position\":156,\"reference\":\"CYS\",\"alternate\":\"ARG\",\"substitutionScores\":[{\"score\":0.91,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0,\"source\":\"polyphen\",\"description\":\"benign\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"geneName\":\"APOE\",\"ensemblGeneId\":\"ENSG00000130203\",\"ensemblTranscriptId\":\"ENST00000425718\",\"strand\":\"+\",\"biotype\":\"protein_coding\",\"transcriptAnnotationFlags\":[\"mRNA_end_NF\",\"cds_end_NF\"],\"cdnaPosition\":653,\"cdsPosition\":388,\"codon\":\"Tgc/Cgc\",\"proteinVariantAnnotation\":{\"position\":130,\"reference\":\"CYS\",\"alternate\":\"ARG\",\"substitutionScores\":[{\"score\":1,\"source\":\"sift\",\"description\":\"tolerated\"},{\"score\":0,\"source\":\"polyphen\",\"description\":\"benign\"}]},\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001583\",\"name\":\"missense_variant\"}]},{\"sequenceOntologyTerms\":[{\"accession\":\"SO:0001566\",\"name\":\"regulatory_region_variant\"}]}]",
                consequenceTypeResult.getResult(), ConsequenceType.class);


////        QueryResult queryResult = variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("19", 45411941, "T", "C"), new QueryOptions());  // should return intergenic_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 6638139, "A", "T"), new QueryOptions());  // should return intergenic_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 108309064, StringUtils.repeat("N",2252), "-"), new QueryOptions());  // should return ENSG00000215002 ENST00000399415 -       transcript_ablation
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 124814698, StringUtils.repeat("N",1048), "-"), new QueryOptions());  // should return ENSG00000215002 ENST00000399415 -       transcript_ablation
////        variantAnnotationCalculator.getAllConsequenceTypesByVariantOld(new Variant("10", 327947, "A", "-"), new QueryOptions());  // should return
////        variantAnnotationCalculator.getAllConsequenceTypesByVariantOld(new Variant("10", 327947, "A", "-"), new QueryOptions());  // should return
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 327947, "A", "-"), new QueryOptions());  // should return
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 101786, "A", "-"), new QueryOptions());  // should return ENSG00000173876 ENST00000413237 -       intron_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 10103931, "A", "-"), new QueryOptions());  // should return ENSG00000224788 ENST00000429539 -       intron_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 10005684, "-", "AT"), new QueryOptions());  // should return intergenic_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 696869, "C", "-"), new QueryOptions());  // should not return NPE
////        variantAnnotationCalculator.getAllConsequenceTypesByVariantOld(new Variant("10", 52365874, "G", "A"), new QueryOptions());  // should not return 10      133761141       A       ENSG00000175470 ENST00000422256 -       missense_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 133761141, "G", "A"), new QueryOptions());  // should not return 10      133761141       A       ENSG00000175470 ENST00000422256 -       missense_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 12172775, "G", "A"), new QueryOptions());  // should return 10      12172775        A       ENSG00000265653 ENST00000584402 -       non_coding_transcript_exon_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 10993859, "G", "C"), new QueryOptions());  // should return 10      10993859        C       ENSG00000229240 ENST00000598573 -       splice_region_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 323246, "T", "C"), new QueryOptions());  // should not return NPE
////        variantAnnotationCalculator.getAllConsequenceTypesByVariantOld(new Variant("10", 323246, "T", "C"), new QueryOptions());  // should not return NPE
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 295047, "T", "G"), new QueryOptions());  // should return NPE
////        variantAnnotationCalculator.getAllConsequenceTypesByVariantOld(new Variant("10", 295047, "T", "G"), new QueryOptions());  // should return NPE
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 172663, "G", "A"), new QueryOptions());  // should return intergenic_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("2", 114340663, "GCTGGGCATCCT", "-"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("2", 114340663, "GCTGGGCATCCT", "ACTGGGCATCCT"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 220603289, "-", "GTGT"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 220603347, "CCTAGTA", "ACTACTA"), new QueryOptions());  // last triplet of the transcript (- strand) and last codifying codon of the transcript, should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 16555369, "-", "TG"), new QueryOptions());  // last triplet of the transcript (- strand) and last codifying codon of the transcript, should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 16555369, "T", "-"), new QueryOptions());  // last triplet of the transcript (- strand) and last codifying codon of the transcript, should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 70008, "-", "TG"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 167385325, "A", "-"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16287365, "C", "T"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("19", 45411941, "T", "C"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("5", 150407694, "G", "A"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("19", 20047783, "AAAAAA", "-"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("13", 28942717, "NNN", "-"), new QueryOptions());  // should return ENST00000541932 stop_retained
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("13", 45411941, "T", "C"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("9", 107366952, StringUtils.repeat("N",12577), "A"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("7", 23775220, "T", "A"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("5", 150407694, "G", "A"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("5", 150407693, "T", "G"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("4", 48896023, "G", "C"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 12837706, "-", "CC"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("19", 20047783, "-", "AAAAAA"), new QueryOptions());  // should return stop_gained
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 115828861, "C", "G"), new QueryOptions());  // should return
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("16", 32859177, "C", "T"), new QueryOptions());  // should return stop_lost
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 13481174, "NN", "-"), new QueryOptions());  // should return stop_lost
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 153600596, "-", "C"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 10041199, "A", "T"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 102269845, "C", "A"), new QueryOptions());  // should
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("7", 158384306, "TGTG", "-"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("11", 118898436, "N", "-"), new QueryOptions());  // should return intergenic_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 6638139, "-", "T"), new QueryOptions());  // should return intergenic_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 70612070, StringUtils.repeat("N",11725), "-"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 36587846, "-", "CT"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("13", 52718051, "-", "T"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 115412783, "-", "C"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 27793991, StringUtils.repeat("N",1907), "-"), new QueryOptions());  // should not return null
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 27436462, StringUtils.repeat("N",2), "-"), new QueryOptions());  // should not return intergenic_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("10", 6638139, "A", "T"), new QueryOptions());  // should not return intergenic_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 3745870, "C", "T"), new QueryOptions());  // should not return null
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 35656173, "C", "A"), new QueryOptions());  // should return initiator_codon_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 28071285, "C", "G"), new QueryOptions());  // should return initiator_codon_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 35656173, "C", "A"), new QueryOptions());  // should return synonymous_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 22274249, "-", "AGGAG"), new QueryOptions());  // should return downstream_gene_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 51042514, "-", "G"), new QueryOptions());  // should return downstream_gene_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 36587846, "-", "CT"), new QueryOptions());  // should
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 42537628, "T", "C"), new QueryOptions());  // should return downstream_gene_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 27283340, "-", "C"), new QueryOptions());  // should return splice_region_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 31478142, "-", "G"), new QueryOptions());  // should return downstream_gene_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 29684676, "G", "A"), new QueryOptions());  // should return downstream_gene_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 40806293, "-", "TGTG"), new QueryOptions());  // should return downstream_gene_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 39426437, StringUtils.repeat("N",20092), "-"), new QueryOptions());  // ¿should return 3_prime_UTR_variant? No if ENSEMBLs gtf was used
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 38069602, StringUtils.repeat("N",5799), "-"), new QueryOptions());  // should return 3_prime_UTR_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17054103, "A", "G"), new QueryOptions());  // should NOT return non_coding_transcript_exon_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 35661560, "A", "G"), new QueryOptions());  // should return synonymous_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 31368158, StringUtils.repeat("N",4), "-"), new QueryOptions());  // should return donor_variant, intron_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 36587124, "-", "TA"), new QueryOptions());  // should return stop_retained_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 30824659, "-", "A"), new QueryOptions());  // should return stop_retained_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 26951215, "T", "C"), new QueryOptions());  // should NOT return null pointer exception
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17602839, "G", "A"), new QueryOptions());  // should NOT return null pointer exception
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 20891503, "-", "CCTC"), new QueryOptions());  // should return missense_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 21991357, "T", "C"), new QueryOptions());  // should return missense_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 24717655, "C", "T"), new QueryOptions());  // should return missense_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 24314402, StringUtils.repeat("N",19399), "-"), new QueryOptions());  // should return 3prime_UTR_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 22007661, "G", "A"), new QueryOptions());  // should
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 23476261, "G", "A"), new QueryOptions());  // should
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 22517056, StringUtils.repeat("N",82585), "-"), new QueryOptions());  // should return 3prime_UTR_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 21388510, "A", "T"), new QueryOptions());  // should NOT return mature_miRNA_variant but non_coding_transcript_variant,non_coding_transcript_exon_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 22007634, "G", "A"), new QueryOptions());  // should NOT return mature_miRNA_variant but non_coding_transcript_variant,non_coding_transcript_exon_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 20891502, "-", "CCTC"), new QueryOptions());  // should return splice_region_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 18387495, "G", "A"), new QueryOptions());  // should NOT return incomplete_teminator_codon_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 19258045, StringUtils.repeat("N",27376), "-"), new QueryOptions());  // should return initiator_codon_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 18293502, "T", "C"), new QueryOptions());  // should return initiator_codon_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 18620375, StringUtils.repeat("N",9436), "-"), new QueryOptions());  // should return transcript_ablation
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 18997219, StringUtils.repeat("N",12521), "-"), new QueryOptions());  // should return transcript_ablation
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17449263, "G", "A"), new QueryOptions());  // should return
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 21982892, "C", "T"), new QueryOptions());  // should return a result
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16676212, "C", "T"), new QueryOptions());  // should include downstream_gene_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 22022872, "T", "C"), new QueryOptions());  // should not raise an error
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("2", 179633644, "G", "C"), new QueryOptions());  // should include
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16123409, "-", "A"), new QueryOptions());  // should include
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 51234118, "C", "G"), new QueryOptions());  // should include upstream_gene_variant
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 155159745, "G", "A"), new QueryOptions());  // should not raise error
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("2", 179621477, "C", "T"), new QueryOptions());  // should not raise error
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 20918922, "C", "T"), new QueryOptions());  // should not raise java.lang.StringIndexOutOfBoundsException
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 18628756, "A", "T"), new QueryOptions());  // should not raise java.lang.NumberFormatException
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17488995, "G", "A"), new QueryOptions());  // should not raise java.lang.NumberFormatException
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17280889, "G", "A"), new QueryOptions());  // should not raise java.lang.NumberFormatException
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16449075, "G", "A"), new QueryOptions());  // should not raise null exception
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16287784, "C", "T"), new QueryOptions());  // should not raise null exception
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16287365, "C", "T"), new QueryOptions());  // should not raise null exception
////          variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17468875, "C", "A"), new QueryOptions());  // missense_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17451081, "C", "T"), new QueryOptions());  // should not include stop_reained_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17468875, "C", "T"), new QueryOptions());  // synonymous_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17449263, "G", "A"), new QueryOptions());  // should not include stop_reained_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17449238, "T", "C"), new QueryOptions());  // should not include stop_codon
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17071673, "A", "G"), new QueryOptions());  // 3_prime_UTR_variant
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16151191, "G", "-"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16340551, "A", "G"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17039749, "C", "A"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16287365, "C", "T"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16101010, "TTA", "-"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 16062270, "G", "T"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 20918922, "C", "T"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17668822, "TCTCTACTAAAAATACAAAAAATTAGCCAGGCGTGGTGGCAGGTGCCTGTAGTACCAGCTACTTGGAAGGCTGAGGCAGGAGACTCTCTTGAACCTGGGAAGCCGAGGTTGCAGTGAGCTGGGCGACAGAGGGAGACTCCGTAAAAAAAAGAAAAAAAAAGAAGAAGAAGAAAAGAAAACAGGAAGGAAAGAAGAAAGAGAAACTAGAAATAATACATGTAAAGTGGCTGATTCTATTATCCTTGTTATTCCTTCTCCATGGGGCTGTTGTCAGGATTAAGTGAGATAGAGCACAGGAAAGGGCTCTGGAAACGCCTGTAGGCTCTAACCCTGAGGCATGGGCCTGTGGCCAGGAGCTCTCCCATTGACCACCTCCGCTGCCTCTGCTCGCATCCCGCAGGCTCACCTGTTTCTCCGGCGTGGAAGAAGTAAGGCAGCTTAACGCCATCCTTGGCGGGGATCATCAGAGCTTCCTTGTAGTCATGCAAGGAGTGGCCAGTGTCCTCATGCCCCACCTGCAGGACAGAGAGGGACAGGGAGGTGTCTGCAGGGCGCATGCCTCACTTGCTGATGGCGCGCCCTGGAGCCTGTGCACACCCTTCCTTGTACCCTGCCACCACTGCCGGGACCTTTGTCACACAGCCTTTTAAGAATGACCAGGAGCAGGCCAGGCGTGGTGGCTCACACCTGTAATCCCAGCACTTTGGGAGGCCGAGGCAGGCAGATCACGAAGTCAGGAGATCGAGACCATCCTGGCTAACACAGTGAAACCCCA", "-"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("22", 17668818, "C", "A"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("8", 408515, "GAA", ""), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("3", 367747, "C", "T"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("9", 214512, "C", "A"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("14", 19108198, "-", "GGTCTAGCATG"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("3L", 22024723, "G", "T"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("2L", 37541199, "G", "A"), new QueryOptions());
//
////
////        // Use local gene collection to test these
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 5, "GGTCTAGCATG", "-"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 1, "G", "A"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 5, "GGTCTAGCATGTTACATGAAG", "-"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 15, "GTTACATGAAG", "-"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 21, "T", "A"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 34, "-", "AAAT"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 42, "G", "A"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 75, "T", "A"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 75, "TCTAAGGCCTC", "-"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 25, "GATAGTTCCTA", "-"), new QueryOptions());
////        variantAnnotationCalculator.getAllConsequenceTypesByVariant(new Variant("1", 45, "GATAGGGTAC", "-"), new QueryOptions());
//
//
////        try {
////            br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get("/tmp/22.wgs.integrated_phase1_v3.20101123.snps_indels_sv.sites.vcf"))));
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//
        /**
         * Calculates annotation for vcf file variants, loads vep annotations, compares batches and writes results
         */
//        String DIROUT = "/tmp/";
////        String DIROUT = "/homes/fjlopez/tmp/";
//        List<String> VCFS = new ArrayList<>();
//////        VCFS.add("/tmp/test.vcf");
//        VCFS.add("/media/shared/tmp/ALL.chr22.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr10.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr11.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr12.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr13.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr14.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr15.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr16.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr17.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr18.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr19.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr1.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr20.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr21.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr22.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr2.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr3.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr4.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr5.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr6.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr7.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr8.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chr9.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
//////        VCFS.add("/nfs/production2/eva/release-2015-pag/1000g-phase1/vcf_accessioned/ALL.chrX.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned.vcf");
////
//        List<String> VEPFILENAMES = new ArrayList<>();
//////        VEPFILENAMES.add("/tmp/test.txt");
//        VEPFILENAMES.add("/media/shared/tmp/ALL.chr22.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr10.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr11.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr12.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr13.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr14.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr15.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr16.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr17.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr18.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr19.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr1.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr20.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr21.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr22.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr2.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr3.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr4.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr5.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr6.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr7.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr8.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chr9.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
//////        VEPFILENAMES.add("/nfs/production2/eva/VEP/Old/eva_output_by_study/release-2015-pag/Complete/1000g-phase1/vcf_accessioned/ALL.chrX.integrated_phase1_v3.20101123.snps_indels_svs.genotypes_accessioned_VEPprocessed.txt");
////
////
////
//        Set<AnnotationComparisonObject> uvaAnnotationSet = new HashSet<>();
//        Set<AnnotationComparisonObject> vepAnnotationSet = new HashSet<>();
//        int vepFileIndex = 0;
//        int nNonRegulatoryAnnotations = 0;
//        int nVariants = 0;
//        for (String vcfFilename : VCFS) {
//            System.out.println("Processing "+vcfFilename+" lines...");
//            VcfRawReader vcfReader = new VcfRawReader(vcfFilename);
//            File file = new File(VEPFILENAMES.get(vepFileIndex));
//            RandomAccessFile raf = new RandomAccessFile(file, "r");
//            if (vcfReader.open()) {
//                vcfReader.pre();
//                skipVepFileHeader(raf);
//                int nLines = countLines(vcfFilename);
//                int nReadVariants;
//                int lineCounter=0;
//                do {
//                    nReadVariants = getVcfAnnotationBatch(vcfReader, variantAnnotationCalculator, uvaAnnotationSet);
//                    nNonRegulatoryAnnotations += getVepAnnotationBatch(raf, nReadVariants, vepAnnotationSet);
//                    nVariants += nReadVariants;
//                    compareAndWrite(uvaAnnotationSet, vepAnnotationSet, lineCounter, nLines, nNonRegulatoryAnnotations,
//                            nVariants, DIROUT);
//                    lineCounter += nReadVariants;
//                    System.out.print(lineCounter+"/"+nLines+" - non-regulatory annotations: "+nNonRegulatoryAnnotations+"\r");
//                } while (nReadVariants > 0);
//                vcfReader.post();
//                vcfReader.close();
//                raf.close();
//            }
//            vepFileIndex++;
//        }
    }

    private <T> void assertObjectListEquals(String consequenceTypeJson, List<T> list,
                                            Class<T> clazz) {
        List goldenObjectList = jsonObjectMapper.convertValue(JSON.parse(consequenceTypeJson), List.class);
        assertEquals(goldenObjectList.size(), list.size());
        for (int i = 0; i < list.size(); i++) {
            assertEquals(list.get(i), jsonObjectMapper.convertValue(goldenObjectList.get(i), clazz));
        }
    }

    private void skipVepFileHeader(RandomAccessFile raf) throws IOException {
        String line;
        long pos;
        do {
            pos = raf.getFilePointer();
            line = raf.readLine();
        }while(line.startsWith("#"));
        raf.seek(pos);
    }

    private int getVcfAnnotationBatch(VcfRawReader vcfReader, VariantAnnotationCalculator variantAnnotationDBAdaptor,
                                      Set<AnnotationComparisonObject> uvaAnnotationSet) {
        QueryResult queryResult = null;
        String pos;
        String ref;
        String cellbaseRef;
        String alt;
        String cellbaseAlt;
        String SoNameToTest;

        List<VcfRecord> vcfRecordList = vcfReader.read(1000);
        int ensemblPos;
        int processedVariants = 0;

        for (VcfRecord vcfRecord : vcfRecordList) {
//            boolean isSnv = false;
            // Short deletion
            if (vcfRecord.getReference().length() > 1) {
                ref = vcfRecord.getReference().substring(1);
                cellbaseRef = vcfRecord.getReference().substring(1);
                alt = "-";
                cellbaseAlt = "";
                ensemblPos = vcfRecord.getPosition() + 1;
                int end = getEndFromInfoField(vcfRecord);
                if(end==-1) {
                    if (ref.length() > 1) {
                        pos = (vcfRecord.getPosition() + 1) + "-" + (vcfRecord.getPosition() + ref.length());
                    } else {
                        pos = Integer.toString(vcfRecord.getPosition() + 1);
                    }
                } else {
                    pos = (vcfRecord.getPosition() + 1) + "-" + end;
                }
                // Alternate length may be > 1 if it contains <DEL>
            } else if (vcfRecord.getAlternate().length() > 1) {
                // Large deletion
                if (vcfRecord.getAlternate().equals("<DEL>")) {
                    ensemblPos = vcfRecord.getPosition() + 1;
                    int end = getEndFromInfoField(vcfRecord);
                    pos = (vcfRecord.getPosition() + 1) + "-" + end;
                    ref = StringUtils.repeat("N", end - vcfRecord.getPosition());
                    cellbaseRef = StringUtils.repeat("N", end - vcfRecord.getPosition());
                    alt = "-";
                    cellbaseAlt = "";
                    // Short insertion
                } else {
                    ensemblPos = vcfRecord.getPosition() + 1;
                    ref = "-";
                    cellbaseRef = "";
                    alt = vcfRecord.getAlternate().substring(1);
                    cellbaseAlt = vcfRecord.getAlternate().substring(1);
                    pos = vcfRecord.getPosition() + "-" + (vcfRecord.getPosition() + 1);
                }
                // SNV
            } else {
                ref = vcfRecord.getReference();
                cellbaseRef = vcfRecord.getReference();
                alt = vcfRecord.getAlternate();
                cellbaseAlt = vcfRecord.getAlternate();
                ensemblPos = vcfRecord.getPosition();
                pos = Integer.toString(ensemblPos);
//                isSnv = true;
            }
            // TODO: Remove this if as refactoring implements consequence types for other variant types
//            if(isSnv) {
                processedVariants++;
                try {
                    queryResult = variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new Variant(vcfRecord.getChromosome(), ensemblPos, cellbaseRef, cellbaseAlt), new QueryOptions());
                } catch (Exception e) {
                    System.out.println("new Variant = " + new Variant(vcfRecord.getChromosome(), ensemblPos, cellbaseRef, cellbaseAlt));
                    e.printStackTrace();
                    System.exit(1);
                }

                int i;
                List<ConsequenceType> consequenceTypeList = (List<ConsequenceType>) queryResult.getResult();
                for (i = 0; i < consequenceTypeList.size(); i++) {
                    for (SequenceOntologyTerm soTerm : consequenceTypeList.get(i).getSequenceOntologyTerms()) {
                        if (soTerm.getName().equals("2KB_upstream_gene_variant")) {
                            SoNameToTest = "upstream_gene_variant";
                        } else if (soTerm.getName().equals("2KB_downstream_gene_variant")) {
                            SoNameToTest = "downstream_gene_variant";
                        } else {
                            SoNameToTest = soTerm.getName();
                        }
                        uvaAnnotationSet.add(new AnnotationComparisonObject(vcfRecord.getChromosome(), pos, alt,
                                consequenceTypeList.get(i).getEnsemblGeneId() == null ? "-" : consequenceTypeList.get(i).getEnsemblGeneId(),
                                consequenceTypeList.get(i).getEnsemblTranscriptId() == null ? "-" : consequenceTypeList.get(i).getEnsemblTranscriptId(),
                                consequenceTypeList.get(i).getBiotype() == null ? "-" : consequenceTypeList.get(i).getBiotype(),
                                SoNameToTest));
                    }
                }
            }
//        }
        return processedVariants;
//        return vcfRecordList.size();
    }

    private int getEndFromInfoField(VcfRecord vcfRecord) {
        String[] infoFields = vcfRecord.getInfo().split(";");
        int i = 0;
        while (i < infoFields.length && !infoFields[i].startsWith("END=")) {
            i++;
        }

        if(i<infoFields.length) {
            return Integer.parseInt(infoFields[i].split("=")[1]);
        } else {
            return -1;
        }
    }

    private int getVepAnnotationBatch(RandomAccessFile raf, int nVariantsToRead,
                                       Set<AnnotationComparisonObject> vepAnnotationSet) throws IOException {
        /**
         * Loads VEP annotation
         */
        String newLine;
        int nNonRegulatoryAnnotations = 0;
        int nReadVariants = 0;
        String previousChr = "";
        String previousPosition = "";
        String previousAlt = "";
        String alt;
        long filePointer=0;

        if(nVariantsToRead>0) {
            while (((newLine = raf.readLine()) != null) && nReadVariants <= nVariantsToRead) {
                String[] lineFields = newLine.split("\t");
                String[] coordinatesParts = lineFields[1].split(":");
                if (lineFields[2].equals("deletion")) {
                    alt = "-";
                } else {
                    alt = lineFields[2];
                }
                // TODO: Remove this if as refactoring implements consequence types for other variant types
//                if(!alt.equals("-") && coordinatesParts[1].split("-").length==1) {
                    if (!previousChr.equals(coordinatesParts[0]) || !previousPosition.equals(coordinatesParts[1]) ||
                            !previousAlt.equals(alt)) {
                        nReadVariants++;
                    }
                    if (nReadVariants <= nVariantsToRead) {
                        for (String SOname : lineFields[6].split(",")) {
                            if (SOname.equals("nc_transcript_variant")) {
                                SOname = "non_coding_transcript_variant";
                            }
                            if (!SOname.equals("regulatory_region_variant")) {
                                nNonRegulatoryAnnotations++;
                            }
                            vepAnnotationSet.add(new AnnotationComparisonObject(coordinatesParts[0], coordinatesParts[1], alt, lineFields[3],
                                    lineFields[4], SOname));
                        }
                        previousChr = coordinatesParts[0];
                        previousPosition = coordinatesParts[1];
                        previousAlt = alt;
                        filePointer = raf.getFilePointer();
                    }
//                }
            }

            raf.seek(filePointer);
        }

        return nNonRegulatoryAnnotations;
    }

    private void compareAndWrite(Set<AnnotationComparisonObject> uvaAnnotationSet,
                                 Set<AnnotationComparisonObject> vepAnnotationSet, int lineCounter, int nLines,
                                 int nNonRegulatoryAnnotations, int nVariants, String dirout) throws IOException {

        /**
         * Compare both annotation sets and get UVA specific annotations
         */
        BufferedWriter bw = Files.newBufferedWriter(Paths.get(dirout+"/uva.specific.txt"), Charset.defaultCharset());
        bw.write("#CHR\tPOS\tALT\tENSG\tENST\tBIOTYPE\tCT\n");
        Set<AnnotationComparisonObject> uvaAnnotationSetBck = new HashSet<>(uvaAnnotationSet);
        uvaAnnotationSet.removeAll(vepAnnotationSet);
        List<AnnotationComparisonObject> uvaSpecificAnnotationList = new ArrayList(uvaAnnotationSet);
        Collections.sort(uvaSpecificAnnotationList, new AnnotationComparisonObjectComparator());
        for(AnnotationComparisonObject comparisonObject : uvaSpecificAnnotationList) {
            if(!comparisonObject.getSOname().equals("regulatory_region_variant")) {
                bw.write(comparisonObject.toString());
            }
        }
        bw.close();

        /**
         * Compare both annotation sets and get VEP specific annotations
         */
        bw = Files.newBufferedWriter(Paths.get(dirout+"vep.specific.txt"), Charset.defaultCharset());
        bw.write("#CHR\tPOS\tALT\tENSG\tENST\tBIOTYPE\tCT\n");
        vepAnnotationSet.removeAll(uvaAnnotationSetBck);
        List<AnnotationComparisonObject> vepSpecificAnnotationList = new ArrayList<>(vepAnnotationSet);
        Collections.sort(vepSpecificAnnotationList, new AnnotationComparisonObjectComparator());
        for(AnnotationComparisonObject comparisonObject : vepSpecificAnnotationList) {
            bw.write(comparisonObject.toString());
        }
        bw.write("\n\n\n");
        bw.write(lineCounter+"/"+nLines+"\n");
        bw.write("# processed variants: "+nVariants+"\n");
        bw.write("# non-regulatory annotations: "+nNonRegulatoryAnnotations+"\n");

        bw.close();
    }

}
