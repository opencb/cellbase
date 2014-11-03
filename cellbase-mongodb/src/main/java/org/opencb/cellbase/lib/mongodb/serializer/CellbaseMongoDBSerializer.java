package org.opencb.cellbase.lib.mongodb.serializer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.*;
import com.mongodb.util.JSON;
import org.opencb.biodata.formats.protein.uniprot.v201311jaxb.Entry;
import org.opencb.biodata.models.core.Gene;
import org.opencb.biodata.models.core.GenomeSequenceChunk;
import org.opencb.biodata.models.protein.Interaction;
import org.opencb.biodata.models.variant.effect.VariantAnnotation;
import org.opencb.biodata.models.variation.Mutation;
import org.opencb.biodata.models.variation.Variation;
import org.opencb.biodata.models.variation.VariationPhenotypeAnnotation;
import org.opencb.cellbase.core.common.GenericFeature;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.cellbase.core.common.variation.ClinicalVariation;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;

/**
 * Created by parce on 10/31/14.
 */
public class CellbaseMongoDBSerializer extends CellBaseSerializer {

    private final String user;
    private final int port;
    private final String password;
    private final String host;
    private final String database;
    private MongoClient mongoClient;

    // TODO: quizas esta constante esta en otra clase
    private static final String CLINICAL_COLLECTION = "clinical";
    private DB db;
    private ObjectMapper jsonObjectMapper;
    private DBCollection clinicalCollection;

    public CellbaseMongoDBSerializer(String host, int port, String database, String user, String password) {
        super();
        this.user = user;
        this.port = port;
        this.database = database;
        this.password = password;
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

    public void serialize(ClinicalVariation variation) throws IOException {
        if (clinicalCollection == null) {
            createClinicalCollection();
        }
        clinicalCollection.insert((DBObject)JSON.parse(jsonObjectMapper.writeValueAsString(variation)));
    }

    private void createClinicalCollection() {
        clinicalCollection = db.getCollection(CLINICAL_COLLECTION);
        clinicalCollection.ensureIndex((DBObject) JSON.parse("{chromosome: 1, start: 1, end: 1, reference: 1, alternate: 1}"));
    }

    public void init() throws UnknownHostException {
        MongoCredential credential = MongoCredential.createMongoCRCredential(user, database, password.toCharArray());
        mongoClient = new MongoClient(new ServerAddress(host, port), Arrays.asList(credential));
        db = mongoClient.getDB(database);
        // TODO: mongo clients options, like write concern
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    }

    @Override
    public void close() {
        this.mongoClient.close();
    }
}
