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

package org.opencb.cellbase.core.serializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.biodata.formats.protein.uniprot.v201311jaxb.Entry;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.GenomeSequenceChunk;
import org.opencb.biodata.models.protein.Interaction;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variation.Mutation;
import org.opencb.biodata.models.variation.Variation;
import org.opencb.biodata.models.variation.VariationPhenotypeAnnotation;
import org.opencb.cellbase.core.common.ConservedRegionChunk;
import org.opencb.cellbase.core.common.GenericFeature;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Created by imedina on 17/06/14.
 */
@Deprecated
public class DefaultJsonSerializer extends CellBaseSerializerOld {

    protected Map<String, BufferedWriter> writers;

    // variation and conservation data are too big to be stored in a single file, data is split in different files
    protected Map<String, BufferedWriter> variationWriters;
    private Map<String, BufferedWriter> conservedRegionJsonWriters;

    private ObjectMapper jsonObjectMapper;
    protected ObjectWriter jsonObjectWriter;
    private JsonGenerator generator;

    public DefaultJsonSerializer(Path outdirPath) throws IOException {
        this(outdirPath, null);
    }

    public DefaultJsonSerializer(Path outdirPath, Path outputFileName) throws IOException {
        super(outdirPath);
        this.outputFileName = outputFileName;
        init();
    }

    protected void init() throws IOException {
        FileUtils.checkPath(outdirPath);

        writers = new Hashtable<>(50);
        variationWriters = new HashMap<>(40);
        conservedRegionJsonWriters = new HashMap<>(40);

        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jsonObjectWriter = jsonObjectMapper.writer();
    }


    @Override
    public void serialize(GenomeSequenceChunk genomeSequenceChunk) {
        try {
            if(writers.get("genome_sequence") == null) {
                writers.put("genome_sequence", Files.newBufferedWriter(outdirPath.resolve("genome_sequence.json"), Charset.defaultCharset()));
            }
            writers.get("genome_sequence").write(jsonObjectWriter.writeValueAsString(genomeSequenceChunk));
            writers.get("genome_sequence").newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(Gene gene) {
        try {
            if(writers.get("gene") == null) {
                writers.put("gene", Files.newBufferedWriter(outdirPath.resolve("gene.json"), Charset.defaultCharset()));
            }
            writers.get("gene").write(jsonObjectWriter.writeValueAsString(gene));
            writers.get("gene").newLine();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(Entry protein) {
        try {
            if(writers.get("protein") == null) {
                writers.put("protein", Files.newBufferedWriter(outdirPath.resolve("protein.json"), Charset.defaultCharset()));
            }
            writers.get("protein").write(jsonObjectWriter.writeValueAsString(protein));
            writers.get("protein").newLine();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(Variation variation) {
        try {
            if(variationWriters.get(variation.getChromosome()) == null) {
                Path outputFilePath = outdirPath.resolve("variation_chr" + variation.getChromosome() + ".json.gz");
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(outputFilePath))));
                variationWriters.put(variation.getChromosome(), bw);
            }
            variationWriters.get(variation.getChromosome()).write(jsonObjectWriter.writeValueAsString(variation));
            variationWriters.get(variation.getChromosome()).newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(VariantAnnotation variantAnnotation) {
        try {
            if(variationWriters.get(variantAnnotation.getChromosome()) == null) {
                variationWriters.put(variantAnnotation.getChromosome(), Files.newBufferedWriter(outdirPath.resolve("variant_effect_chr" + variantAnnotation.getChromosome() + ".json"), Charset.defaultCharset()));
            }
            variationWriters.get(variantAnnotation.getChromosome()).write(jsonObjectWriter.writeValueAsString(variantAnnotation));
            variationWriters.get(variantAnnotation.getChromosome()).newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(VariationPhenotypeAnnotation variationPhenotypeAnnotation) {
        try {
            if(writers.get("variationPhenotype") == null) {
                writers.put("variationPhenotype", Files.newBufferedWriter(outdirPath.resolve("variation_phenotype_annotation.json"), Charset.defaultCharset()));
            }
            writers.get("variationPhenotype").write(jsonObjectWriter.writeValueAsString(variationPhenotypeAnnotation));
            writers.get("variationPhenotype").newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(Mutation mutation) {
        try {
            if(writers.get("mutation") == null) {
                writers.put("mutation", Files.newBufferedWriter(outdirPath.resolve("mutation.json"), Charset.defaultCharset()));
            }
            writers.get("mutation").write(jsonObjectWriter.writeValueAsString(mutation));
            writers.get("mutation").newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(Interaction interaction) {
        try {
            if(writers.get("ppi") == null) {
                writers.put("ppi", Files.newBufferedWriter(outdirPath.resolve("protein_protein_interaction.json"), Charset.defaultCharset()));
            }
            writers.get("ppi").write(jsonObjectWriter.writeValueAsString(interaction));
            writers.get("ppi").newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(GenericFeature genericFeature) {
        try {
            if(writers.get("regulatory") == null) {
                writers.put("regulatory", Files.newBufferedWriter(outdirPath.resolve("regulatory_region.json"), Charset.defaultCharset()));
            }
            writers.get("regulatory").write(jsonObjectWriter.writeValueAsString(genericFeature));
            writers.get("regulatory").newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(ConservedRegionChunk conservedRegionChunk) {
        try {
            if (conservedRegionJsonWriters.get(conservedRegionChunk.getChromosome()) == null) {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(outdirPath.resolve("conservation_" + conservedRegionChunk.getChromosome() + ".json.gz")))));

//                JsonFactory conservedRegionJsonFactory = new JsonFactory();
//                GZIPOutputStream gzOutputStream =
//                        new GZIPOutputStream(new FileOutputStream(outdirPath.resolve("conservation_" + conservedRegionChunk.getChromosome() + ".json.gz").toAbsolutePath().toString()));
//                JsonGenerator generator = conservedRegionJsonFactory.createGenerator(gzOutputStream);
//                conservedRegionJsonWriters.put(conservedRegionChunk.getChromosome(), generator);
                conservedRegionJsonWriters.put(conservedRegionChunk.getChromosome(), bw);
            }
            conservedRegionJsonWriters.get(conservedRegionChunk.getChromosome()).write(jsonObjectWriter.writeValueAsString(conservedRegionChunk));
            conservedRegionJsonWriters.get(conservedRegionChunk.getChromosome()).newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(Object elem) {
        try {
            if (generator == null) {
                JsonFactory jsonFactory = new JsonFactory();
                jsonObjectMapper = new ObjectMapper(jsonFactory);
                if (!serializeEmptyValues) {
                    jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                }
                GZIPOutputStream gzOutputStream = new GZIPOutputStream(new FileOutputStream(outdirPath.resolve(outputFileName).toAbsolutePath().toString() + ".gz"));
                generator = jsonFactory.createGenerator(gzOutputStream);
            }
            generator.writeObject(elem);
            generator.writeRaw('\n');
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void close() {
        closeBufferedWriters();
        closeVariationWriters();

        if (generator != null) {
            try {
                generator.flush();
                generator.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        closeConservationWriters();
    }

    private void closeBufferedWriters() {
        for (BufferedWriter bw : writers.values()) {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeVariationWriters() {
        for (BufferedWriter bw : variationWriters.values()) {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConservationWriters() {
        if (conservedRegionJsonWriters != null) {
            for (BufferedWriter bw : conservedRegionJsonWriters.values()) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            try {
//                for (JsonGenerator conservationWriter : conservedRegionJsonWriters.values()) {
//                    conservationWriter.flush();
//                    conservationWriter.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }
}
