package org.opencb.cellbase.lib.impl;

import org.bson.Document;
import org.junit.Test;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.api.ClinicalDBAdaptor;
import org.opencb.cellbase.lib.GenericMongoDBAdaptorTest;
import org.opencb.commons.datastore.core.Query;
import org.opencb.commons.datastore.core.QueryOptions;
import org.opencb.commons.datastore.core.QueryResult;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by fjlopez on 24/03/17.
 */
public class ClinicalMongoDBAdaptorTest extends GenericMongoDBAdaptorTest {
    @Test
    public void nativeGet() throws Exception {
        ClinicalDBAdaptor clinicalDBAdaptor = dbAdaptorFactory.getClinicalDBAdaptor("hsapiens", "GRCh37");
        QueryOptions queryOptions1 = new QueryOptions();

        Query query1 = new Query();
        query1.put("phenotypeDisease", "alzheimer");
        queryOptions1.add("limit", 30);
        queryOptions1.add("include", "annotation.variantTraitAssociation.germline.accession,"
                + "annotation.variantTraitAssociation.somatic.accession");
        QueryResult<Variant> queryResult1 = clinicalDBAdaptor.get(query1, queryOptions1);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(79, queryResult1.getNumTotalResults());
        assertEquals(30, queryResult1.getNumResults());
        // FIXME: commented to enable compiling for priesgo. Must be uncommented and fixed
//        boolean found = false;
//        int i = 0;
//        while (i < queryResult1.getNumResults() && !found) {
//            int j = 0;
//            while (j < queryResult1.getResult().get(i).getAnnotation().getVariantTraitAssociation().getGermline().size()
//                    && !found) {
//                found = queryResult1.getResult().get(i).getAnnotation().getVariantTraitAssociation().getGermline()
//                        .get(j).getAccession().equals("RCV000019769");
//                j++;
//            }
//            i++;
//        }
//        assertEquals(found, true);

        Query query2 = new Query();
        query2.put("phenotypeDisease", "myelofibrosis");
        QueryOptions queryOptions2 = new QueryOptions();
        queryOptions2.add("limit", 30);
        queryOptions2.add("include", "annotation.variantTraitAssociation.germline.accession,"
                + "annotation.variantTraitAssociation.somatic.accession");
        QueryResult queryResult2 = clinicalDBAdaptor.nativeGet(query2, queryOptions2);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(693, queryResult2.getNumTotalResults());
        assertEquals(30, queryResult2.getNumResults());

        Query query4 = new Query();
        query4.put("region", new Region("2", 170360030, 170362030));
        QueryOptions queryOptions4 = new QueryOptions();
        queryOptions4.add("limit", 30);
        queryOptions4.add("include", "annotation.variantTraitAssociation.germline.accession,"
                + "annotation.variantTraitAssociation.somatic.accession");
        QueryResult<Variant> queryResult4 = clinicalDBAdaptor.get(query4, queryOptions4);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(8, queryResult4.getNumTotalResults());
        // FIXME: commented to enable compiling for priesgo. Must be uncommented and fixed
//        found = false;
//        i = 0;
//        while (i < queryResult4.getNumResults() && !found) {
//            int j = 0;
//            while (j < queryResult4.getResult().get(i).getAnnotation().getVariantTraitAssociation().getSomatic().size()
//                    && !found) {
//                found = queryResult4.getResult().get(i).getAnnotation().getVariantTraitAssociation().getSomatic()
//                        .get(j).getAccession().equals("COSM4624460");
//                j++;
//            }
//            i++;
//        }
//        assertEquals(found, true);
//
//        found = false;
//        i = 0;
//        while (i < queryResult4.getNumResults() && !found) {
//            int j = 0;
//            while (j < queryResult4.getResult().get(i).getAnnotation().getVariantTraitAssociation().getGermline().size()
//                    && !found) {
//                found = queryResult4.getResult().get(i).getAnnotation().getVariantTraitAssociation().getGermline()
//                        .get(j).getAccession().equals("RCV000171500");
//                j++;
//            }
//            i++;
//        }
//        assertEquals(found, true);

        Query query5 = new Query();
        query5.put("clinicalSignificance", "Likely pathogenic");
        QueryOptions queryOptions5 = new QueryOptions();
        queryOptions5.add("limit", 30);
        QueryResult queryResult5 = clinicalDBAdaptor.nativeGet(query5, queryOptions5);
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(9019, queryResult5.getNumTotalResults());

        Query query6 = new Query();
        query6.put("feature", "APOE");
        QueryOptions queryOptions6 = new QueryOptions();
        queryOptions6.add("limit", 30);
        queryOptions6.put("sort", "chromosome,start");
        queryOptions6.put("include", "chromosome,start,annotation.variantTraitAssociation.somatic.geneNames");
        QueryResult queryResult6 = clinicalDBAdaptor.nativeGet(query6, queryOptions6);
        // Check sorted output
        int previousStart = -1;
        for (Document document : (List<Document>) queryResult6.getResult()) {
            assertTrue(previousStart < document.getInteger("start"));
        }
        // WARNING: these values may change from one ClinVar version to another
        assertEquals(91, queryResult6.getNumTotalResults());

        queryOptions6.remove("sort");
        query6.put("source", "clinvar");
        QueryResult queryResult7 = clinicalDBAdaptor.nativeGet(query6, queryOptions6);
        assertEquals(33, queryResult7.getNumTotalResults());

        query6.put("source", "cosmic");
        QueryResult<Variant> queryResult8 = clinicalDBAdaptor.get(query6, queryOptions6);
        assertEquals(61, queryResult8.getNumTotalResults());
        // FIXME: commented to enable compiling for priesgo. Must be uncommented and fixed
//        assertThat(queryResult8.getResult().get(29).getAnnotation().getVariantTraitAssociation().getSomatic().get(0).getGeneNames(),
//                CoreMatchers.hasItem("APOE"));
//
//        Query query7 = new Query();
//        query7.put("accession","COSM306824");
//        query7.put("source", "cosmic");
//        QueryOptions options = new QueryOptions();
//        QueryResult<Variant> queryResult9 = clinicalDBAdaptor.get(query7, options);
//        assertNotNull("Should return the queryResult of id=COSM306824", queryResult9.getResult());
//        assertThat(queryResult9.getResult().get(0).getAnnotation().getVariantTraitAssociation().getSomatic().get(0).getGeneNames(),
//                CoreMatchers.hasItem("FMN2"));


    }

}