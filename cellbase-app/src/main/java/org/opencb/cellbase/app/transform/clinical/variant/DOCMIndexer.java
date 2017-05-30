package org.opencb.cellbase.app.transform.clinical.variant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.Germline;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.biodata.models.variant.avro.VariantTraitAssociation;
import org.opencb.commons.utils.FileUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fjlopez on 29/03/17.
 */
public class DOCMIndexer extends ClinicalIndexer {
    private final RocksDB rdb;
    private final Path docmFile;
    private final String assembly;

    public DOCMIndexer(Path docmFile, String assembly, RocksDB rdb) {
        super();
        this.rdb = rdb;
        this.assembly = assembly;
        this.docmFile = docmFile;
    }

    public void index() throws RocksDBException {
        logger.info("Parsing DOCM file ...");

        try {
            BufferedReader bufferedReader = FileUtils.newBufferedReader(docmFile);
            String line = bufferedReader.readLine();
            while (line != null) {
                Variant variant = parseVariant(line);
                if (variant != null) {
                    updateRocksDB(variant);
                    numberIndexedRecords++;
                }
                line = bufferedReader.readLine();
            }
            totalNumberRecords++;
            if (totalNumberRecords % 1000 == 0) {
                logger.info("{} records parsed", totalNumberRecords);
            }

        } catch (RocksDBException e) {
            logger.error("Error reading/writing from/to the RocksDB index while indexing Cosmic");
            throw e;
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            logger.info("Done");
//            this.printSummary();
        }

    }

    private void updateRocksDB(Variant variant) throws RocksDBException {
        int a = 1;
    }

    private Variant parseVariant(String line) throws IOException {
        Map<String, String> map = (HashMap<String, String>) new ObjectMapper().readValue(line, HashMap.class);
        if (assembly.equalsIgnoreCase((String) map.get("reference_version"))) {
            Variant variant = new Variant((String) map.get("chromosome"), Integer.valueOf((String) map.get("start")),
                    (String) map.get("reference"), (String) map.get("alternate"));

            List<Germline> germlineList = new ArrayList<Germline>();


            variant.setAnnotation(new VariantAnnotation());
            variant.getAnnotation().setVariantTraitAssociation(new VariantTraitAssociation());
            variant.getAnnotation().getVariantTraitAssociation().setGermline(germlineList);
            return variant;
        } else {
            return null;
        }
    }
}
