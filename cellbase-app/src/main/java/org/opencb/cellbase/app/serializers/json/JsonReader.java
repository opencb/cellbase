package org.opencb.cellbase.app.serializers.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import org.opencb.commons.io.DataReader;
import org.opencb.commons.io.DataWriter;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 * @param <T> Type of data to read
 */
@Deprecated
public class JsonReader<T> implements DataReader<T> {

    private static final int BATCH_SIZE = 2000;
    
    private Path file;
    private Class<T> clazz;

    protected JsonFactory factory;
    protected ObjectMapper jsonObjectMapper;
    private JsonParser parser;
    private InputStream stream;
    
    private DataWriter<T> serializer;
    
    public JsonReader(Path file, Class<T> clazz) {
        this(file, clazz, null);
    }
    
    public JsonReader(Path file, Class<T> clazz, DataWriter<T> serializer) {
        this.file = file;
        this.clazz = clazz;
        
        this.factory = new JsonFactory();
        this.jsonObjectMapper = new ObjectMapper(this.factory);
        
        this.serializer = serializer;
    }
    
    @Override
    public boolean open() {
        try {
            Files.exists(file);

            if (file.toFile().getName().endsWith(".gz")) {
                this.stream = new GZIPInputStream(new FileInputStream(file.toAbsolutePath().toFile()));
            } else {
                this.stream = new FileInputStream(file.toAbsolutePath().toFile());
            }

            if (serializer != null) {
                serializer.open();
            }
        } catch (IOException ex) {
            Logger.getLogger(JsonReader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean pre() {
        try {
            parser = factory.createParser(stream);
            
            if (serializer != null) {
                serializer.pre();
            }
        } catch (IOException ex) {
            Logger.getLogger(JsonReader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }

    public boolean parse() {
        List<T> batch;
        while((batch = read(BATCH_SIZE)) != null && !batch.isEmpty()) {
            if (serializer != null) {
                if (!serializer.write(batch)) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    @Override
    public List<T> read() {
        try {
            List<T> listRecords = new ArrayList<>(1);
            if (parser.nextToken() != null) {
                T variant = parser.readValueAs(clazz);
                listRecords.add(variant);
                return listRecords;
            }
        } catch (IOException ex) {
            Logger.getLogger(JsonReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    @Override
    public List<T> read(int batchSize) {
        List<T> listRecords = new ArrayList<>(batchSize);
        
        try {
            for (int i = 0; i < batchSize && parser.nextToken() != null; i++) {
                T variant = parser.readValueAs(clazz);
                listRecords.add(variant);
            }
        } catch (IOException ex) {
            Logger.getLogger(JsonReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        return listRecords;
    }
    
    @Override
    public boolean post() {
        return true;
    }

    @Override
    public boolean close() {
        try {
            parser.close();
        } catch (IOException ex) {
            Logger.getLogger(JsonReader.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }

}
