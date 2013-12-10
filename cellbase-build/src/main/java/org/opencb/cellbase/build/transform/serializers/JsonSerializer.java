package org.opencb.cellbase.build.transform.serializers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.opencb.cellbase.core.common.GenericFeatureChunk;
import org.opencb.cellbase.core.common.core.Gene;
import org.opencb.cellbase.core.common.core.GenomeSequenceChunk;
import org.opencb.cellbase.core.common.ppi.Interaction;
import org.opencb.cellbase.core.common.variation.Mutation;
import org.opencb.cellbase.core.common.variation.Variation;
import org.opencb.commons.bioformats.protein.uniprot.v201311jaxb.Entry;

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
@Deprecated
public class JsonSerializer implements CellbaseSerializer {

    private File file;
    private Path outdirPath;
    private Path outfilePath;

    private Map<String, BufferedWriter> bufferedWriterMap;

    private BufferedWriter genomeSequenceBufferedWriter;

    private ObjectMapper jsonObjectMapper;
    private ObjectWriter jsonObjectWriter;

    private int chunkSize = 2000;

    public JsonSerializer(File file) throws IOException {
        this.file = file;
        init();
    }

    private void init() throws IOException {
        if(file.exists() && file.isDirectory() && file.canWrite()) {
            outdirPath = file.toPath();
        }else {
            outfilePath = file.toPath();
        }

        bufferedWriterMap = new Hashtable<>(50);

        jsonObjectMapper = new ObjectMapper();
        jsonObjectWriter = jsonObjectMapper.writer();
    }


    @Override
    public void serialize(GenomeSequenceChunk genomeSequenceChunk) {
        try {
            if(genomeSequenceBufferedWriter == null) {
                genomeSequenceBufferedWriter = Files.newBufferedWriter(outfilePath, Charset.defaultCharset());
            }
            genomeSequenceBufferedWriter.write(jsonObjectWriter.writeValueAsString(genomeSequenceChunk));
            genomeSequenceBufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        };
    }

    @Override
    public void serialize(Mutation mutation) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void serialize(Interaction interaction) {
        //To change body of implemented methods use File | Settings | File Templates.
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void serialize(Entry gene) {

    }

    @Override
    public void serialize(Variation variation) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void serialize(GenericFeatureChunk genericFeatureChunk) {
        //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public void close() {
        String id;
        try {
            genomeSequenceBufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        ;

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
    }
}
