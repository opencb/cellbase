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
import org.opencb.biodata.models.variant.effect.VariantAnnotation;
import org.opencb.biodata.models.variation.Mutation;
import org.opencb.biodata.models.variation.Variation;
import org.opencb.biodata.models.variation.VariationPhenotypeAnnotation;
import org.opencb.cellbase.core.common.ConservedRegionChunk;
import org.opencb.cellbase.core.common.GenericFeature;
import org.opencb.commons.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import java.util.zip.GZIPOutputStream;

/**
 * Created by imedina on 17/06/14.
 */
public class DefaultJsonSerializer extends CellBaseSerializer {

    private final boolean includeEmtpyValues;
    private Map<String, BufferedWriter> bufferedWriterMap;

    private BufferedWriter genomeSequenceBufferedWriter;
    private BufferedWriter variationPhenotypeAnnotationBufferedWriter;
    private BufferedWriter mutationBufferedWriter;
    private BufferedWriter ppiBufferedWriter;
    
    // variation and conservation data are too big to be stored in a single file, data is split in different files
    private Map<String, BufferedWriter> variationBufferedWriter;
    private Map<String, JsonGenerator> conservedRegionJsonWriters;
    
    private GZIPOutputStream gzipOutputStream;
    private ObjectMapper jsonObjectMapper;
    private ObjectWriter jsonObjectWriter;

    private Path outputFileName;
    private JsonGenerator generator;


    public DefaultJsonSerializer(Path outdirPath) throws IOException {
        this(outdirPath, true);
    }

    public DefaultJsonSerializer(Path outdirPath, boolean includeEmptyValues) throws IOException {
        this(outdirPath, null, includeEmptyValues);
    }

    public DefaultJsonSerializer(Path outdirPath, Path outputFileName) throws IOException {
        this(outdirPath, outputFileName, true);
    }

    public DefaultJsonSerializer(Path outdirPath, Path outputFileName, boolean includeEmptyValues) throws IOException {
        super(outdirPath);
        this.outputFileName = outputFileName;
        this.includeEmtpyValues = includeEmptyValues;
        init();
    }

    private void init() throws IOException {
        FileUtils.checkPath(outdirPath);

        bufferedWriterMap = new Hashtable<>(50);
        variationBufferedWriter = new HashMap<>(40);

        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        jsonObjectMapper.setPropertyNamingStrategy(new GeneNamingStrategy());

        jsonObjectWriter = jsonObjectMapper.writer();
//        PropertyNamingStrategy propertyNamingStrategy = new PropertyNamingStrategy() {
//            @Override
//            public String nameForField(MapperConfig<?> mapperConfig, AnnotatedField annotatedField, String s) {
//                return super.nameForField(mapperConfig, annotatedField, s);    //To change body of overridden methods use File | Settings | File Templates.
//            }
//        };
    }


    @Override
    public void serialize(GenomeSequenceChunk genomeSequenceChunk) {
        try {
            if(genomeSequenceBufferedWriter == null) {
                genomeSequenceBufferedWriter = Files.newBufferedWriter(outdirPath.resolve("genome_sequence.json"), Charset.defaultCharset());
            }
            genomeSequenceBufferedWriter.write(jsonObjectWriter.writeValueAsString(genomeSequenceChunk));
            genomeSequenceBufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        };
    }

    @Override
    public void serialize(Gene gene) {
        try {
            if(bufferedWriterMap.get("gene") == null) {
                bufferedWriterMap.put("gene", Files.newBufferedWriter(outdirPath.resolve("gene.json"), Charset.defaultCharset()));
            }
            bufferedWriterMap.get("gene").write(jsonObjectWriter.writeValueAsString(gene));
            bufferedWriterMap.get("gene").newLine();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(Entry protein) {
        try {
            if(bufferedWriterMap.get("protein") == null) {
                bufferedWriterMap.put("protein", Files.newBufferedWriter(outdirPath.resolve("protein.json"), Charset.defaultCharset()));
            }
            bufferedWriterMap.get("protein").write(jsonObjectWriter.writeValueAsString(protein));
            bufferedWriterMap.get("protein").newLine();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(Variation variation) {
        try {
            if(variationBufferedWriter.get(variation.getChromosome()) == null) {
                variationBufferedWriter.put(variation.getChromosome(), Files.newBufferedWriter(outdirPath.resolve("variation_chr" + variation.getChromosome() + ".json"), Charset.defaultCharset()));
            }
            variationBufferedWriter.get(variation.getChromosome()).write(jsonObjectWriter.writeValueAsString(variation));
            variationBufferedWriter.get(variation.getChromosome()).newLine();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(VariantAnnotation variantAnnotation) {
        try {
            if(variationBufferedWriter.get(variantAnnotation.getChromosome()) == null) {
                variationBufferedWriter.put(variantAnnotation.getChromosome(), Files.newBufferedWriter(outdirPath.resolve("variant_effect_chr" + variantAnnotation.getChromosome() + ".json"), Charset.defaultCharset()));
            }
            variationBufferedWriter.get(variantAnnotation.getChromosome()).write(jsonObjectWriter.writeValueAsString(variantAnnotation));
            variationBufferedWriter.get(variantAnnotation.getChromosome()).newLine();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(VariationPhenotypeAnnotation variationPhenotypeAnnotation) {
        try {
            if(variationPhenotypeAnnotationBufferedWriter == null) {
                variationPhenotypeAnnotationBufferedWriter = Files.newBufferedWriter(outdirPath.resolve("variation_phenotype_annotation.json"), Charset.defaultCharset());
            }
            variationPhenotypeAnnotationBufferedWriter.write(jsonObjectWriter.writeValueAsString(variationPhenotypeAnnotation));
            variationPhenotypeAnnotationBufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        };
    }

    @Override
    public void serialize(Mutation mutation) {
        try {
            if(mutationBufferedWriter == null) {
                mutationBufferedWriter = Files.newBufferedWriter(outdirPath.resolve("mutation.json"), Charset.defaultCharset());
            }
            mutationBufferedWriter.write(jsonObjectWriter.writeValueAsString(mutation));
            mutationBufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        };
    }

    @Override
    public void serialize(Interaction interaction) {
        try {
            if(ppiBufferedWriter == null) {
                ppiBufferedWriter = Files.newBufferedWriter(outdirPath.resolve("protein_protein_interaction.json"), Charset.defaultCharset());
            }
            ppiBufferedWriter.write(jsonObjectWriter.writeValueAsString(interaction));
            ppiBufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        };
    }

    @Override
    public void serialize(GenericFeature genericFeature) {
        try {
            if(bufferedWriterMap.get("regulatory") == null) {
                bufferedWriterMap.put("regulatory", Files.newBufferedWriter(outdirPath.resolve("regulatory_region.json"), Charset.defaultCharset()));
            }
            bufferedWriterMap.get("regulatory").write(jsonObjectWriter.writeValueAsString(genericFeature));
            bufferedWriterMap.get("regulatory").newLine();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void serialize(ConservedRegionChunk conservedRegionChunk) {
        try {
            if (conservedRegionJsonWriters.get(conservedRegionChunk.getChromosome()) == null) {
                JsonFactory conservedRegionJsonFactory = new JsonFactory();
                GZIPOutputStream gzipOutputStream =
                        new GZIPOutputStream(new FileOutputStream(outdirPath.resolve("conservation_" + conservedRegionChunk.getChromosome() + ".json.gz").toAbsolutePath().toString()));
                JsonGenerator generator = conservedRegionJsonFactory.createGenerator(gzipOutputStream);
                conservedRegionJsonWriters.put(conservedRegionChunk.getChromosome(), generator);
            }
            conservedRegionJsonWriters.get(conservedRegionChunk.getChromosome()).writeObject(conservedRegionChunk);
            conservedRegionJsonWriters.get(conservedRegionChunk.getChromosome()).writeRaw('\n');
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
                if (!includeEmtpyValues) {
                    jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
                }
                gzipOutputStream = new GZIPOutputStream(new FileOutputStream(outdirPath.resolve(outputFileName).toAbsolutePath().toString() + ".json.gz"));
                generator = jsonFactory.createGenerator(gzipOutputStream);
            }
            generator.writeObject(elem);
            generator.writeRaw('\n');
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void close() {
        String id;
        try {

            closeBufferedWriter(genomeSequenceBufferedWriter);
            closeBufferedWriter(variationPhenotypeAnnotationBufferedWriter);
            closeBufferedWriter(mutationBufferedWriter);
            closeBufferedWriter(ppiBufferedWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Iterator<String> iter = bufferedWriterMap.keySet().iterator();
        while(iter.hasNext()) {
            id = iter.next();
            if(bufferedWriterMap.get(id) != null) {
                try {
                    bufferedWriterMap.get(id).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        iter = variationBufferedWriter.keySet().iterator();
        while(iter.hasNext()) {
            id = iter.next();
            if(variationBufferedWriter.get(id) != null) {
                try {
                    variationBufferedWriter.get(id).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

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

    private void closeConservationWriters() {
        if (conservedRegionJsonWriters != null) {
            try {
                for (JsonGenerator conservationWriter : conservedRegionJsonWriters.values()) {
                    conservationWriter.flush();
                    conservationWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeBufferedWriter(BufferedWriter bufferedWriter) throws IOException {
        if(bufferedWriter != null) {
            bufferedWriter.close();
        }
    }
}
