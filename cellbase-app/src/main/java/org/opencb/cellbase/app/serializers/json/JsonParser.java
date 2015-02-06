package org.opencb.cellbase.app.serializers.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
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
public class JsonParser {

    private final Path outdir;
    private final String fileName;
    private final HashMap<String, BufferedWriter> bufferedWriters;
    private JsonGenerator generator;

    private boolean serializeEmptyValues;
    private ObjectWriter jsonObjectWriter;

    public JsonParser(Path outdir) {
        this(outdir, null);
    }

    public JsonParser(Path outdir, String fileName) {
        this(outdir, fileName, true);
    }

    public JsonParser(Path outdir, String fileName, boolean serializeEmptyValues) {
        this.outdir = outdir;
        this.fileName = fileName;
        this.serializeEmptyValues = serializeEmptyValues;
        this.bufferedWriters = new HashMap<>();
        init();
    }

    public void serialize(Object object) {
        this.serialize(object, this.fileName);
    }

    private void init() {
         ObjectMapper jsonObjectMapper = new ObjectMapper();
        if (!serializeEmptyValues) {
            jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
        jsonObjectWriter = jsonObjectMapper.writer();
    }

    public void serialize(Object elem, String filename) {
        try {
            if(bufferedWriters.get(filename) == null) {
                // TODO: get complete filename o just basefilename?
                Path outputFilePath = outdir.resolve(filename + ".json.gz");
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(Files.newOutputStream(outputFilePath))));
                bufferedWriters.put(filename, bw);
            }
            bufferedWriters.get(filename).write(jsonObjectWriter.writeValueAsString(elem));
            bufferedWriters.get(filename).newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
