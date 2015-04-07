package org.opencb.cellbase.mongodb.serializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.opencb.biodata.formats.protein.uniprot.v201311jaxb.Entry;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.GenomeSequenceChunk;
import org.opencb.biodata.models.protein.Interaction;
import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
import org.opencb.biodata.models.variation.Mutation;
import org.opencb.biodata.models.variation.Variation;
import org.opencb.biodata.models.variation.VariationPhenotypeAnnotation;
import org.opencb.cellbase.core.common.ConservedRegionChunk;
import org.opencb.cellbase.core.common.GenericFeature;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.mongodb.model.ClinicalVariation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Created by parce on 10/31/14.
 */
public class CellBaseMongoDBSerializer extends CellBaseSerializer {

    private final String user;
    private final int port;
    private final char[] password;
    private final String host;
    private final String database;
    private MongoClient mongoClient;

    // TODO: quizas esta constante esta en otra clase
    private static final String CLINICAL_COLLECTION = "clinical";
    private DB db;
    private ObjectMapper jsonObjectMapper;
    private DBCollection clinicalCollection;

    public CellBaseMongoDBSerializer(String host, int port, String database, String user, String password) {
        super();
        this.user = user;
        this.port = port;
        this.database = database;
        if (password != null) {
            this.password = password.toCharArray();
        } else {
            this.password = null;
        }
        this.host = host;
    }

    @Override
    public void serialize(Gene gene) {}

    @Override
    public void serialize(Entry protein) {}

    @Override
    public void serialize(Variation variation) {}

    @Override
    public void serialize(VariantAnnotation variantAnnotation) {}

    @Override
    public void serialize(GenericFeature genericFeature) {}

    @Override
    public void serialize(GenomeSequenceChunk genomeSequenceChunk) {}

    @Override
    public void serialize(VariationPhenotypeAnnotation variationPhenotypeAnnotation) {}

    @Override
    public void serialize(Mutation mutation) {}

    @Override
    public void serialize(Interaction interaction) {}

    @Override
    public void serialize(ConservedRegionChunk conservedRegionChunk) {}

    @Override
    public void serialize(Object object) {}

    public void serialize(ClinicalVariation variation) throws IOException {
        if (clinicalCollection == null) {
            createClinicalCollection();
        }
        clinicalCollection.insert((DBObject)JSON.parse(jsonObjectMapper.writeValueAsString(variation)));
    }

    private void createClinicalCollection() {
        clinicalCollection = db.getCollection(CLINICAL_COLLECTION);
        DBObject index = new BasicDBObject("chromosome", 1).append("start", 1).append("end", 1).append("reference", 1).append("alternate", 1);
        clinicalCollection.ensureIndex(index, new BasicDBObject("unique", true));
    }

    public void init() throws UnknownHostException {
        ServerAddress serverAddres = new ServerAddress(host, port);
        if (this.user != null) {
            mongoClient = new MongoClient(serverAddres, Arrays.asList(MongoCredential.createMongoCRCredential(user, database, password)));
        } else {
            mongoClient = new MongoClient(serverAddres);
        }
        db = mongoClient.getDB(database);
        // TODO: mongo clients options, like write concern
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @Override
    public void close() {
        this.mongoClient.close();
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }
}
