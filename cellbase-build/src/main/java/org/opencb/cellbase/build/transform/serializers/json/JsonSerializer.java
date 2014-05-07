package org.opencb.cellbase.build.transform.serializers.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import org.opencb.commons.io.DataWriter;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class JsonSerializer<T> implements DataWriter<T> {

    private Path outdir;
    private Path file;
    
    protected JsonFactory factory;
    protected ObjectMapper jsonObjectMapper;
    protected JsonGenerator generator;
    private OutputStream stream;
    
    public JsonSerializer(Path outdir, Path file) {
        this.outdir = outdir;
        this.file = file;
        this.factory = new JsonFactory();
        this.jsonObjectMapper = new ObjectMapper(this.factory);
    }
    
    @Override
    public boolean open() {
        try {
            stream = new GZIPOutputStream(new FileOutputStream(
                    Paths.get(outdir.toString(), file.getFileName().toString()).toAbsolutePath().toString() + ".json.gz"));
        } catch (IOException ex) {
            Logger.getLogger(JsonSerializer.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean pre() {
        try {
            generator = factory.createGenerator(stream);
        } catch (IOException ex) {
            Logger.getLogger(JsonSerializer.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }

    @Override
    public boolean write(T elem) {
        try {
            generator.writeObject(elem);
            generator.writeRaw('\n');
        } catch (IOException ex) {
            Logger.getLogger(JsonSerializer.class.getName()).log(Level.SEVERE, elem.toString(), ex);
            return false;
        }
        
        return true;
    }

    @Override
    public boolean write(List<T> batch) {
        for (T elem : batch) {
            try {
                generator.writeObject(elem);
                generator.writeRaw('\n');
            } catch (IOException ex) {
                Logger.getLogger(JsonSerializer.class.getName()).log(Level.SEVERE, elem.toString(), ex);
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public boolean post() {
        try {
            stream.flush();
            generator.flush();
        } catch (IOException ex) {
            Logger.getLogger(JsonSerializer.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean close() {
        try {
            generator.close();
        } catch (IOException ex) {
            Logger.getLogger(JsonSerializer.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

}
