package org.opencb.cellbase.build.transform.serializers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.cellbase.core.common.core.Gene;
import org.opencb.cellbase.core.common.variation.Variation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 8/28/13
 * Time: 5:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class JsonSerializer implements Serializer {

    private File outdir;
    private Path outdirPath;

    private Map<String, BufferedWriter> bufferedWriterrMap;

    private ObjectMapper jsonObjectMapper;
    private ObjectWriter jsonObjectWriter;

    public JsonSerializer(File outdir) throws IOException {
        this.outdir = outdir;
        init();
    }

    private void init() throws IOException {
        if(outdir.exists() && outdir.isDirectory() && outdir.canWrite()) {
            outdirPath = outdir.toPath();
        }

        bufferedWriterrMap = new Hashtable<>(50);

        jsonObjectMapper = new ObjectMapper();
        jsonObjectWriter = jsonObjectMapper.writer();
    }


    @Override
    public void serialize(Gene gene) {
        try {
            if(bufferedWriterrMap.get("gene") == null) {
                bufferedWriterrMap.put("gene", Files.newBufferedWriter(outdirPath.resolve("gene.json"), Charset.defaultCharset()));
            }
            bufferedWriterrMap.get("gene").write(jsonObjectWriter.writeValueAsString(gene));
            bufferedWriterrMap.get("gene").newLine();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void serialize(Variation variation) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void close() {
        String id;
        Iterator<String> iter = bufferedWriterrMap.keySet().iterator();
        while(iter.hasNext()) {
            id = iter.next();
            if(bufferedWriterrMap.get(id) != null) {
                try {
                    bufferedWriterrMap.get(id).close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
