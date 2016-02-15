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

package org.opencb.cellbase.app.loaders.mongodb;

//import com.mongodb.*;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.opencb.biodata.models.variant.annotation.VariantAnnotation;
//import org.opencb.biodata.models.variant.annotation.VariantEffect;
//
//import java.net.UnknownHostException;
//import java.util.LinkedList;
//import java.util.List;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;

/**
 *
 * @author Cristina Yenyxe Gonzalez Garcia <cyenyxe@ebi.ac.uk>
 */
//@Ignore
public class VariantEffectMongoDBLoaderTest {
    /*
    private static String host = "localhost";
    private static int port = 27017;
    private static String dbName = "VariantEffectMongoDBLoader_test";
    private static String collectionName = "variant_effect";
    
    private static VariantEffectMongoDBLoader loader;
    
    private static MongoClient client;
    private static DB db;
    
    @BeforeClass
    public static void setUpClass() throws UnknownHostException {
        // Initialize native Mongo client
        client = new MongoClient("localhost", 27017);
        db = client.getDB("VariantEffectMongoDBLoader_test");
        
        // Initialize our loader
        loader = new VariantEffectMongoDBLoader(host, port, dbName, collectionName);
        loader.open();
        loader.pre();
    }
    
    @AfterClass
    public static void tearDownClass() {
        loader.post();
        loader.close();
        
        db.getCollection(collectionName).drop();
        client.close();
    }

    @Test
    public void testWrite_mainFields() {
//        VariantEffect effect1 = new VariantEffect("1", 1234, 1234, "A");
//        VariantEffect effect2 = new VariantEffect("1", 1235, 1235, "C");
        VariantAnnotation effect1 = new VariantAnnotation("1", 1234, 1234, "A");
        VariantAnnotation effect2 = new VariantAnnotation("1", 1235, 1235, "C");

        List<VariantAnnotation> batch = new LinkedList<>();
        batch.add(effect1);
        batch.add(effect2);
        boolean writeResult = loader.write(batch);
        assertTrue(writeResult);
        
        DBCursor cursor = db.getCollection(collectionName).find(new Document("chr", "1")).sort(new Document("start", 1));
        assertEquals(2, cursor.count());
        
        Document obj1 = cursor.next();
        assertEquals("1", obj1.get("chr"));
        assertEquals(1234, obj1.get("start"));
        assertEquals(1234, obj1.get("end"));
        assertEquals("A", obj1.get("ref"));
        assertEquals(0, ((BasicDBList) obj1.get("ct")).size());
        assertEquals(8, ((Document) obj1.get("freqs")).keySet().size());
        assertEquals(5, ((Document) obj1.get("regulatory")).keySet().size());
        assertEquals(2, ((Document) obj1.get("scores")).keySet().size());
        
        Document obj2 = cursor.next();
        assertEquals("1", obj2.get("chr"));
        assertEquals(1235, obj2.get("start"));
        assertEquals(1235, obj2.get("end"));
        assertEquals("C", obj2.get("ref"));
        assertEquals(0, ((BasicDBList) obj2.get("ct")).size());
        assertEquals(8, ((Document) obj2.get("freqs")).keySet().size());
        assertEquals(5, ((Document) obj2.get("regulatory")).keySet().size());
        assertEquals(2, ((Document) obj2.get("scores")).keySet().size());
    }

    @Test
    public void testWrite_consequenceTypes() {
//        VariantEffect effect1 = new VariantEffect("2", 1234, 1234, "A");
        VariantEffect effect1 = new VariantEffect("2", 1234, "A", "C");

//        ConsequenceType ct_C1 = new ConsequenceType("C");
        effect1.setAminoacidChange("gAg/gCg");
        effect1.setFeatureStrand("+");
        effect1.setGeneId("BRCA2");
        effect1.setCanonical(true);
//        ConsequenceType ct_C2 = new ConsequenceType("C");
//        ct_C2.setAminoacidChange("gAg/gCg");
//        ConsequenceType ct_G1 = new ConsequenceType("G");
//        ct_G1.setAminoacidChange("gAg/gGg");
//        ConsequenceType ct_d1 = new ConsequenceType("-");
//        ct_d1.setAminoacidChange("gAg/g-g");
//
//        effect1..addConsequenceType("C", ct_C1);
//        effect1.addConsequenceType("C", ct_C2);
//        effect1.addConsequenceType("G", ct_G1);
//        effect1.addConsequenceType("-", ct_d1);
//
//        List<VariantEffect> batch = new LinkedList<>();
//        batch.add(effect1);
//        boolean writeResult = loader.write(batch);
//        assertTrue(writeResult);
//
//        DBCursor cursor = db.getCollection(collectionName).find(new Document("chr", "2")).sort(new Document("start", 1));
//        assertEquals(1, cursor.count());
//
//        Document obj1 = cursor.next();
//        assertEquals("2", obj1.get("chr"));
//        assertEquals(1234, obj1.get("start"));
//        assertEquals(1234, obj1.get("end"));
//        assertEquals("A", obj1.get("ref"));
//        assertEquals(3, ((BasicDBList) obj1.get("ct")).size());
//        assertEquals(8, ((Document) obj1.get("freqs")).keySet().size());
//        assertEquals(5, ((Document) obj1.get("regulatory")).keySet().size());
//        assertEquals(2, ((Document) obj1.get("scores")).keySet().size());
//
//        BasicDBList cts = (BasicDBList) obj1.get("ct");
//        Iterator<Object> iter = cts.listIterator();
//        while (iter.hasNext()) {
//            Document obj = (Document) iter.next();
//            assertEquals(2, obj.keySet().size());
//            Document val = (Document) obj.get("val");
//
//            switch (obj.get("alt").toString()) {
//                case "C":
//                    assertEquals(2, ((BasicDBList) val).size());
//                    Document o = (Document) ((BasicDBList) val).get(0);
//                    assertEquals("gAg/gCg", o.get("aaChange"));
//                    break;
//                case "G":
//                    assertEquals(1, ((BasicDBList) val).size());
//                    o = (Document) ((BasicDBList) val).get(0);
//                    assertEquals("gAg/gGg", o.get("aaChange"));
//                    break;
//                case "-":
//                    assertEquals(1, ((BasicDBList) val).size());
//                    o = (Document) ((BasicDBList) val).get(0);
//                    assertEquals("gAg/g-g", o.get("aaChange"));
//                    break;
//            }
//        }
    }
    
    @Test
    public void testWrite_frequencies() {
//        VariantEffect effect1 = new VariantEffect("3", 1234, 1234, "A");
//        VariantEffect effect2 = new VariantEffect("3", 1235, 1235, "C");
//
//        effect1.setFrequencies(new Frequencies("A", 0.01f, 0.02f, 0.03f, 0.04f, 0.05f, 0.06f, 0.07f));
//        effect2.setFrequencies(new Frequencies("C", 0.01f, 0.02f, 0.03f, 0.04f, 0.05f, 0.06f, 0.07f));
//
//        List<VariantEffect> batch = new LinkedList<>();
//        batch.add(effect1);
//        batch.add(effect2);
//        boolean writeResult = loader.write(batch);
//        assertTrue(writeResult);
//
//        DBCursor cursor = db.getCollection(collectionName).find(new Document("chr", "3")).sort(new Document("start", 1));
//        assertEquals(2, cursor.count());
//
//        Document obj1 = cursor.next();
//        assertEquals("3", obj1.get("chr"));
//        assertEquals(1234, obj1.get("start"));
//        assertEquals(1234, obj1.get("end"));
//        assertEquals("A", obj1.get("ref"));
//        assertEquals(0, ((BasicDBList) obj1.get("ct")).size());
//        assertEquals(8, ((Document) obj1.get("freqs")).keySet().size());
//        assertEquals(5, ((Document) obj1.get("regulatory")).keySet().size());
//        assertEquals(2, ((Document) obj1.get("scores")).keySet().size());
//
//        Document freqs1 = (Document) obj1.get("freqs");
//        assertEquals("A", freqs1.get("mafAllele"));
//        assertEquals(0.01, (double) freqs1.get("gmaf"), 1e-6);
//        assertEquals(0.02, (double) freqs1.get("afrMaf"), 1e-6);
//        assertEquals(0.03, (double) freqs1.get("amrMaf"), 1e-6);
//        assertEquals(0.04, (double) freqs1.get("asnMaf"), 1e-6);
//        assertEquals(0.05, (double) freqs1.get("eurMaf"), 1e-6);
//        assertEquals(0.06, (double) freqs1.get("afrAmrMaf"), 1e-6);
//        assertEquals(0.07, (double) freqs1.get("eurAmrMaf"), 1e-6);
//
//        Document obj2 = cursor.next();
//        assertEquals("3", obj2.get("chr"));
//        assertEquals(1235, obj2.get("start"));
//        assertEquals(1235, obj2.get("end"));
//        assertEquals("C", obj2.get("ref"));
//        assertEquals(0, ((BasicDBList) obj2.get("ct")).size());
//        assertEquals(8, ((Document) obj2.get("freqs")).keySet().size());
//        assertEquals(5, ((Document) obj2.get("regulatory")).keySet().size());
//        assertEquals(2, ((Document) obj2.get("scores")).keySet().size());
//
//        Document freqs2 = (Document) obj2.get("freqs");
//        assertEquals("C", freqs2.get("mafAllele"));
//        assertEquals(0.01, (double) freqs2.get("gmaf"), 1e-6);
//        assertEquals(0.02, (double) freqs2.get("afrMaf"), 1e-6);
//        assertEquals(0.03, (double) freqs2.get("amrMaf"), 1e-6);
//        assertEquals(0.04, (double) freqs2.get("asnMaf"), 1e-6);
//        assertEquals(0.05, (double) freqs2.get("eurMaf"), 1e-6);
//        assertEquals(0.06, (double) freqs2.get("afrAmrMaf"), 1e-6);
//        assertEquals(0.07, (double) freqs2.get("eurAmrMaf"), 1e-6);
    }
    
    @Test
    public void testWrite_proteinSubstitutionScores() {
//        VariantEffect effect1 = new VariantEffect("4", 1234, 1234, "A");
//        VariantEffect effect2 = new VariantEffect("4", 1235, 1235, "C");
//
//        effect1.setProteinSubstitutionScores(new ProteinSubstitutionScores(0.01f, 0.02f,
//                ProteinSubstitutionScores.PolyphenEffect.BENIGN, ProteinSubstitutionScores.SiftEffect.TOLERATED));
//        effect2.setProteinSubstitutionScores(new ProteinSubstitutionScores(0.05f, 0.06f,
//                ProteinSubstitutionScores.PolyphenEffect.PROBABLY_DAMAGING, ProteinSubstitutionScores.SiftEffect.DELETERIOUS));
//
//        List<VariantEffect> batch = new LinkedList<>();
//        batch.add(effect1);
//        batch.add(effect2);
//        boolean writeResult = loader.write(batch);
//        assertTrue(writeResult);
//
//        DBCursor cursor = db.getCollection(collectionName).find(new Document("chr", "4")).sort(new Document("start", 1));
//        assertEquals(2, cursor.count());
//
//        Document obj1 = cursor.next();
//        assertEquals("4", obj1.get("chr"));
//        assertEquals(1234, obj1.get("start"));
//        assertEquals(1234, obj1.get("end"));
//        assertEquals("A", obj1.get("ref"));
//        assertEquals(0, ((BasicDBList) obj1.get("ct")).size());
//        assertEquals(8, ((Document) obj1.get("freqs")).keySet().size());
//        assertEquals(5, ((Document) obj1.get("regulatory")).keySet().size());
//        assertEquals(4, ((Document) obj1.get("scores")).keySet().size());
//
//        Document scores1 = (Document) obj1.get("scores");
//        assertEquals(0.01, (double) scores1.get("polyScore"), 1e-6);
//        assertEquals(0.02, (double) scores1.get("siftScore"), 1e-6);
//        assertEquals(ProteinSubstitutionScores.PolyphenEffect.BENIGN.name(), scores1.get("polyEff").toString());
//        assertEquals(ProteinSubstitutionScores.SiftEffect.TOLERATED.name(), scores1.get("siftEff").toString());
//
//        Document obj2 = cursor.next();
//        assertEquals("4", obj2.get("chr"));
//        assertEquals(1235, obj2.get("start"));
//        assertEquals(1235, obj2.get("end"));
//        assertEquals("C", obj2.get("ref"));
//        assertEquals(0, ((BasicDBList) obj2.get("ct")).size());
//        assertEquals(8, ((Document) obj2.get("freqs")).keySet().size());
//        assertEquals(5, ((Document) obj2.get("regulatory")).keySet().size());
//        assertEquals(4, ((Document) obj2.get("scores")).keySet().size());
//
//        Document scores2 = (Document) obj2.get("scores");
//        assertEquals(0.05, (double) scores2.get("polyScore"), 1e-6);
//        assertEquals(0.06, (double) scores2.get("siftScore"), 1e-6);
//        assertEquals(ProteinSubstitutionScores.PolyphenEffect.PROBABLY_DAMAGING.name(), scores2.get("polyEff").toString());
//        assertEquals(ProteinSubstitutionScores.SiftEffect.DELETERIOUS.name(), scores2.get("siftEff").toString());
    }
    
    @Test
    public void testWrite_regulatoryEffect() {
//        VariantEffect effect1 = new VariantEffect("5", 1234, 1234, "A");
//        VariantEffect effect2 = new VariantEffect("5", 1235, 1235, "C");
//
//        effect1.setRegulatoryEffect(new RegulatoryEffect("motif1", 1234, 0.01f, true, "celltype1"));
//        effect2.setRegulatoryEffect(new RegulatoryEffect("motif2", 1235, 0.02f, false, "celltype2"));
//
//        List<VariantEffect> batch = new LinkedList<>();
//        batch.add(effect1);
//        batch.add(effect2);
//        boolean writeResult = loader.write(batch);
//        assertTrue(writeResult);
//
//        DBCursor cursor = db.getCollection(collectionName).find(new Document("chr", "5")).sort(new Document("start", 1));
//        assertEquals(2, cursor.count());
//
//        Document obj1 = cursor.next();
//        assertEquals("5", obj1.get("chr"));
//        assertEquals(1234, obj1.get("start"));
//        assertEquals(1234, obj1.get("end"));
//        assertEquals("A", obj1.get("ref"));
//        assertEquals(0, ((BasicDBList) obj1.get("ct")).size());
//        assertEquals(8, ((Document) obj1.get("freqs")).keySet().size());
//        assertEquals(5, ((Document) obj1.get("regulatory")).keySet().size());
//        assertEquals(2, ((Document) obj1.get("scores")).keySet().size());
//
//        Document regulatory1 = (Document) obj1.get("regulatory");
//        assertEquals("motif1", regulatory1.get("motifName"));
//        assertEquals(1234, regulatory1.get("motifPos"));
//        assertEquals(0.01, (double) regulatory1.get("motifScoreChange"), 1e-6);
//        assertTrue((boolean) regulatory1.get("highInfoPos"));
//        assertEquals("celltype1", regulatory1.get("cellType"));
//
//        Document obj2 = cursor.next();
//        assertEquals("5", obj2.get("chr"));
//        assertEquals(1235, obj2.get("start"));
//        assertEquals(1235, obj2.get("end"));
//        assertEquals("C", obj2.get("ref"));
//        assertEquals(0, ((BasicDBList) obj2.get("ct")).size());
//        assertEquals(8, ((Document) obj2.get("freqs")).keySet().size());
//        assertEquals(5, ((Document) obj2.get("regulatory")).keySet().size());
//        assertEquals(2, ((Document) obj2.get("scores")).keySet().size());
//
//        Document regulatory2 = (Document) obj2.get("regulatory");
//        assertEquals("motif2", regulatory2.get("motifName"));
//        assertEquals(1235, regulatory2.get("motifPos"));
//        assertEquals(0.02, (double) regulatory2.get("motifScoreChange"), 1e-6);
//        assertFalse((boolean) regulatory2.get("highInfoPos"));
//        assertEquals("celltype2", regulatory2.get("cellType"));
    }
    */
}
