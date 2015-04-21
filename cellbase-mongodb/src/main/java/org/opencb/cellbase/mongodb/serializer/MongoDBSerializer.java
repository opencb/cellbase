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

package org.opencb.cellbase.mongodb.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.DBObject;

import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variation.Variation;
import org.opencb.cellbase.core.serializer.DefaultJsonSerializer;
import org.opencb.cellbase.mongodb.loader.converters.GeneConverter;
import org.opencb.cellbase.mongodb.loader.converters.VariantEffectConverter;
import org.opencb.cellbase.mongodb.loader.converters.VariationConverter;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPOutputStream;


/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 8/28/13
 * Time: 5:41 PM
 * To change this template use File | Settings | File Templates.
 */
@Deprecated
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
                Path outputFilePath = outdirPath.resolve("variation_chr" + variation.getChromosome() + ".json.gz");
                variationWriters.put(variation.getChromosome(), new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputFilePath.toFile())))));
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
