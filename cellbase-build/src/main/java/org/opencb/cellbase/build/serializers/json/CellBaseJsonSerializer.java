package org.opencb.cellbase.build.serializers.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.cellbase.build.serializers.CellBaseSerializer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 * @param <T> Type of data to serialize
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
public class CellBaseJsonSerializer implements CellBaseSerializer {

    private Path file;

    protected JsonFactory factory;
    protected ObjectMapper jsonObjectMapper;
    protected JsonGenerator generator;
    private OutputStream stream;

    public CellBaseJsonSerializer(Path file) {
        this.file = file;
        this.factory = new JsonFactory();
        this.jsonObjectMapper = new ObjectMapper(this.factory);
    }

    @Override
    public boolean open() {
        try {
            stream = new GZIPOutputStream(new FileOutputStream(Paths.get(file.toAbsolutePath().toString() + ".json.gz").toFile()));
            generator = factory.createGenerator(stream);
        } catch (IOException ex) {
            Logger.getLogger(CellBaseJsonSerializer.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    @Override
    public boolean serialize(Object elem) {
        try {
            generator.writeObject(elem);
            generator.writeRaw('\n');
        } catch (IOException ex) {
            Logger.getLogger(CellBaseJsonSerializer.class.getName()).log(Level.SEVERE, elem.toString(), ex);
            return false;
        }

        return true;
    }

    @Override
    public boolean close() {
        try {
            stream.flush();
            generator.flush();
            generator.close();
        } catch (IOException ex) {
            Logger.getLogger(CellBaseJsonSerializer.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

}
