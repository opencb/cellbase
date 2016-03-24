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
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

/**
 * Created by parce on 2/6/15.
 */
public class CellBaseJsonFileSerializer implements CellBaseFileSerializer {

    private final Path outdir;
    private final String fileName;
    private final HashMap<String, BufferedWriter> bufferedWriters;

    private boolean serializeEmptyValues;
    private ObjectWriter jsonObjectWriter;
    private ObjectWriter clinvarJsonObjectWriter;

    public CellBaseJsonFileSerializer(Path outdir) {
        this(outdir, null);
    }

    public CellBaseJsonFileSerializer(Path outdir, String baseFileName) {
        this(outdir, baseFileName, false);
    }

    public CellBaseJsonFileSerializer(Path outdir, String baseFileName, boolean serializeEmptyValues) {
        this.outdir = outdir;
        this.fileName = baseFileName;
        this.serializeEmptyValues = serializeEmptyValues;
        this.bufferedWriters = new HashMap<>();
        init();
    }

    public void serialize(Object object) {
        this.serialize(object, this.fileName);
    }

    @Override
    public void close() throws IOException {
        for (BufferedWriter bw : bufferedWriters.values()) {
            bw.close();
        }

    }

    private void init() {
        ObjectMapper jsonObjectMapper = new ObjectMapper();
        if (!serializeEmptyValues) {
            jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        }
        clinvarJsonObjectWriter = jsonObjectMapper.writer();
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectWriter = jsonObjectMapper.writer();
    }

    public void serialize(Object elem, String filename) {
        try {
            if (bufferedWriters.get(filename) == null) {
                Path outputFilePath = outdir.resolve(filename + ".json.gz");
                BufferedWriter bw = new BufferedWriter(
                        new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(outputFilePath))));
                bufferedWriters.put(filename, bw);
            }
            // ClinVar objects require their own jsonObjectWriter since the property REQUIRE_SETTERS_FOR_GETTERS cannot
            // be activated for getting ClinVar objects properly serialized
            // clinvarSet.referenceClinVarAssertion.measureSet.measure.xref records which contain rs ids are not
            // serialized otherwise
            if (filename.equals("clinvar")) {
                bufferedWriters.get(filename).write(clinvarJsonObjectWriter.writeValueAsString(elem));
            } else {
                bufferedWriters.get(filename).write(jsonObjectWriter.writeValueAsString(elem));
            }
            bufferedWriters.get(filename).newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Object deserialize(String line) {
        // TODO: implement
        // TODO: receive class object?
        return null;
    }
}
