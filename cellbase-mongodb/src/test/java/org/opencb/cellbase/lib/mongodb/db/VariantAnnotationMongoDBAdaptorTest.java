package org.opencb.cellbase.lib.mongodb.db;


import org.junit.Test;
import org.opencb.biodata.formats.variant.vcf4.VcfRecord;
import org.opencb.biodata.formats.variant.vcf4.io.VariantVcfReader;
import org.opencb.biodata.formats.variant.vcf4.io.VcfRawReader;
import org.opencb.biodata.models.variation.GenomicVariant;
import org.opencb.cellbase.core.common.core.CellbaseConfiguration;
import org.opencb.cellbase.core.lib.DBAdaptorFactory;
import org.opencb.cellbase.core.lib.api.variation.VariantAnnotationDBAdaptor;
import org.opencb.cellbase.core.lib.dbquery.QueryOptions;
import org.opencb.cellbase.core.lib.dbquery.QueryResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class VariantAnnotationMongoDBAdaptorTest {


    @Test
    public void testGetAllConsequenceTypesByVariant() {

        CellbaseConfiguration config = new CellbaseConfiguration();

        config.addSpeciesConnection("hsapiens", "GRCh37", "mongodb-hxvm-var-001.ebi.ac.uk", "hsapiens_cb_v3", 27017, "mongo", "biouser",
                "B10p@ss", 10, 10);
//        config.addSpeciesConnection("hsapiens", "GRCh37", "localhost", "test", 27017, "mongo", "", "", 10, 10);

        config.addSpeciesAlias("hsapiens", "hsapiens");

        DBAdaptorFactory dbAdaptorFactory = new MongoDBAdaptorFactory(config);

        VariantAnnotationDBAdaptor variantAnnotationDBAdaptor = dbAdaptorFactory.getGenomicVariantAnnotationDBAdaptor("hsapiens", "GRCh37");

        String line = null;

        BufferedReader br = null;

        QueryResult queryResult;

        // Use ebi cellbase to test these
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("14", 19108198, "-", "GGTCTAGCATG"), new QueryOptions());
//
//        // Use local gene collection to test these
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 5, "GGTCTAGCATG", "-"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 1, "G", "A"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 5, "GGTCTAGCATGTTACATGAAG", "-"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 15, "GTTACATGAAG", "-"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 21, "T", "A"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 34, "-", "AAAT"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 42, "G", "A"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 75, "T", "A"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 75, "TCTAAGGCCTC", "-"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 25, "GATAGTTCCTA", "-"), new QueryOptions());
//        variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant("1", 45, "GATAGGGTAC", "-"), new QueryOptions());


//        try {
//            br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get("/tmp/22.wgs.integrated_phase1_v3.20101123.snps_indels_sv.sites.vcf"))));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        VcfRawReader vcfReader = new VcfRawReader("/tmp/22.wgs.integrated_phase1_v3.20101123.snps_indels_sv.sites.vcf");
        if(vcfReader.open()) {
            List<VcfRecord> vcfRecordList= vcfReader.read(1000);
            while(vcfRecordList.size()>0) {
                for(VcfRecord vcfRecord : vcfRecordList) {
                    queryResult = variantAnnotationDBAdaptor.getAllConsequenceTypesByVariant(new GenomicVariant(vcfRecord.getChromosome(), vcfRecord.getPosition(),
                            vcfRecord.getReference(), vcfRecord.getAlternate()), new QueryOptions());
                }
                vcfRecordList = vcfReader.read(1000);
            }
        }
    }
}