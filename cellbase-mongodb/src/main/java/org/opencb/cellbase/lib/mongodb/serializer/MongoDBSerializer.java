package org.opencb.cellbase.lib.mongodb.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.DBObject;

import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variation.Variation;
import org.opencb.cellbase.core.serializer.DefaultJsonSerializer;
import org.opencb.cellbase.lib.mongodb.serializer.converters.GeneConverter;
import org.opencb.cellbase.lib.mongodb.serializer.converters.VariantEffectConverter;
import org.opencb.cellbase.lib.mongodb.serializer.converters.VariationConverter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 8/28/13
 * Time: 5:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class MongoDBSerializer extends DefaultJsonSerializer {

    private VariantEffectConverter variantEffectConverter;
    private GeneConverter geneConverter;
    private VariationConverter variationConverter;

    public MongoDBSerializer(Path path) throws IOException {
        super(path);
    }

    protected void init() throws IOException {
        super.init();
        variantEffectConverter = new VariantEffectConverter();
        geneConverter = new GeneConverter();
        variationConverter = new VariationConverter();
    }

    @Override
    public void serialize(Gene gene) {
        try {
            if(writers.get("gene") == null) {
                writers.put("gene", Files.newBufferedWriter(outdirPath.resolve("gene.json"), Charset.defaultCharset()));
            }
            DBObject mongoDbSchema = geneConverter.convertToStorageSchema(gene);
            writers.get("gene").write(jsonObjectWriter.writeValueAsString(mongoDbSchema));
            writers.get("gene").newLine();
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
                variationWriters.put(variation.getChromosome(), Files.newBufferedWriter(outdirPath.resolve("variation_chr" + variation.getChromosome() + ".json"), Charset.defaultCharset()));
            }
            DBObject mongoDbDchema = variationConverter.convertToStorageSchema(variation);
            variationWriters.get(variation.getChromosome()).write(jsonObjectWriter.writeValueAsString(mongoDbDchema));
            variationWriters.get(variation.getChromosome()).newLine();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
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
            DBObject mongoDbSchema = variantEffectConverter.convertToStorageSchema(variantAnnotation);
            variationWriters.get(variantAnnotation.getChromosome()).write(variantAnnotation.getChromosome()+"\t"+variantAnnotation.getStart()+"\t"+jsonObjectWriter.writeValueAsString(mongoDbSchema));
            variationWriters.get(variantAnnotation.getChromosome()).newLine();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
