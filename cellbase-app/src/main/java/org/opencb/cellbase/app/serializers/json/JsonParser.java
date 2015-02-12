package org.opencb.cellbase.app.serializers.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.cellbase.app.serializers.CellBaseFileSerializer;

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
public class JsonParser implements CellBaseFileSerializer {

    private final Path outdir;
    private final String fileName;
    private final HashMap<String, BufferedWriter> bufferedWriters;

    private boolean serializeEmptyValues;
    private ObjectWriter jsonObjectWriter;

    public JsonParser(Path outdir) {
        this(outdir, null);
    }

    public JsonParser(Path outdir, String baseFileName) {
        this(outdir, baseFileName, false);
    }

    public JsonParser(Path outdir, String baseFileName, boolean serializeEmptyValues) {
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
        jsonObjectWriter = jsonObjectMapper.writer();
    }

    public void serialize(Object elem, String filename) {
        try {
            if(bufferedWriters.get(filename) == null) {
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

    public Object deserialize(String line) {
        // TODO: implement
        // TODO: receive class object?
        return null;
    }
}
