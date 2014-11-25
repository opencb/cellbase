package org.opencb.cellbase.lib.mongodb.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import com.mongodb.MongoInternalException;
import org.opencb.cellbase.core.common.clinical.ClinvarPublicSet;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.cellbase.core.common.clinical.Cosmic;
import org.opencb.cellbase.core.common.clinical.Gwas;
import org.opencb.cellbase.lib.mongodb.model.ClinicalVariation;
import org.opencb.cellbase.lib.mongodb.serializer.CellbaseMongoDBSerializer;

import java.io.*;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by parce on 10/31/14.
 */
public class ClinicalMongoDBLoader extends MongoDBLoader {

    private Path clinicalJsonFilesDir;
    private Path clinvarJsonFile;
    private Path gwasJsonFile;
    private Path cosmicJsonFile;
    private Map<Variant, ClinicalVariation> variantMap;


    public ClinicalMongoDBLoader(CellbaseMongoDBSerializer serializer, Path clinicalJsonFilesDir) {
        super(serializer);
        this.clinicalJsonFilesDir = clinicalJsonFilesDir;
        this.variantMap = new HashMap<>();
    }

    @Override
    public void load() {
        if (checkClinicalFiles()) {
            logger.info("Creating MongoDB Serializer ...");
            try {
                logger.info("Initializing serializer ...");
                serializer.init();
                logger.info("done");
                parseJsonFiles();
                serializeParsedVariants();
            } catch (UnknownHostException e) {
                logger.error("Unable to connect to host " + serializer.getHost() + ":" + serializer.getPort() + "\n" + e.getMessage());
            } catch (FileNotFoundException e) {
                logger.error("File not found: " + e.getMessage());
            } catch (IOException e) {
                logger.error("Exception parsing input file: " + e.getMessage());
            } finally {
                logger.info("Closing serializer ...");
                serializer.close();
                logger.info("Done");
            }
        }
    }

    private void serializeParsedVariants() {
        logger.info("Serializing variants ...");
        try {
            for (ClinicalVariation variant : variantMap.values()) {
                try {
                    serializer.serialize(variant);
                } catch (MongoInternalException e) {
                    logger.error("Error serializing variant " + variant.getVariantString() + ":\n" + e.getMessage());
                }
            }
            logger.info("done");
        } catch (IOException e) {
            logger.error("Error serializing variant: " + e.getMessage());
        } catch (MongoException.DuplicateKey e) {
            logger.error("Error serializing variant, duplicated key:\n" + e.getMessage());
        }
    }

    private void parseJsonFiles() throws IOException {
        parseClinvar();
        parseCosmic();
        parseGwas();
    }

    private void parseClinvar() throws IOException {
        logger.info("Parsing clinvar ...");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(clinvarJsonFile.toFile()))))) {
            ObjectMapper jsonMapper = new ObjectMapper();
            for (String line; (line = br.readLine()) != null; ) {
                ClinvarPublicSet clinvarSet = jsonMapper.readValue(line, ClinvarPublicSet.class);
                Variant variant =
                        new Variant(clinvarSet.getChromosome(), clinvarSet.getStart(), clinvarSet.getEnd(), clinvarSet.getReference(), clinvarSet.getAlternate());
                ClinicalVariation clinicalVariation = variantMap.get(variant);
                if (clinicalVariation != null) {
                    clinicalVariation.addClinvar(clinvarSet);
                } else {
                    clinicalVariation = new ClinicalVariation(clinvarSet);
                    variantMap.put(variant, clinicalVariation);
                }
            }
            logger.info("done");
        } catch (IOException e) {
            logger.error("Error parsing clinvar Json file: " + e.getMessage());
            throw e;
        }
    }

    private void parseCosmic() throws IOException {
        logger.info("Parsing cosmic ...");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(cosmicJsonFile.toFile()))))) {
            ObjectMapper jsonMapper = new ObjectMapper();
            for (String line; (line = br.readLine()) != null; ) {
                Cosmic cosmic = jsonMapper.readValue(line, Cosmic.class);
                Variant variant =
                        new Variant(cosmic.getChromosome(), cosmic.getStart(), cosmic.getEnd(), cosmic.getReference(), cosmic.getAlternate());
                ClinicalVariation clinicalVariation = variantMap.get(variant);
                if (clinicalVariation != null) {
                    clinicalVariation.addCosmic(cosmic);
                } else {
                    clinicalVariation = new ClinicalVariation(cosmic);
                    variantMap.put(variant, clinicalVariation);
                }
            }
            logger.info("done");
        } catch (IOException e) {
            logger.error("Error parsing cosmic Json file: " + e.getMessage());
            throw e;
        }
    }

    private void parseGwas() throws IOException {
        logger.info("Parsing gwas ...");
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(gwasJsonFile.toFile()))))) {
            ObjectMapper jsonMapper = new ObjectMapper();
            for (String line; (line = br.readLine()) != null; ) {
                Gwas gwas = jsonMapper.readValue(line, Gwas.class);
                Variant variant =
                        new Variant(gwas.getChromosome(), gwas.getStart(), gwas.getEnd(), gwas.getReference(), gwas.getAlternate());
                ClinicalVariation clinicalVariation = variantMap.get(variant);
                if (clinicalVariation != null) {
                    clinicalVariation.addGwas(gwas);
                } else {
                    clinicalVariation = new ClinicalVariation(gwas);
                    variantMap.put(variant, clinicalVariation);
                }
            }
            logger.info("done");
        } catch (IOException e) {
            logger.error("Error parsing gwas Json file: " + e.getMessage());
            throw e;
        }
    }

    private boolean checkClinicalFiles() {
        logger.info("Checking input files ...");
        clinvarJsonFile = clinicalJsonFilesDir.resolve("clinvar.json.gz");
        if (!clinvarJsonFile.toFile().exists()) {
            logger.error("Clinvar json file " + clinvarJsonFile + " doesn't exist");
            return false;
        }
        cosmicJsonFile = clinicalJsonFilesDir.resolve("cosmic.json.gz");
        if (!cosmicJsonFile.toFile().exists()) {
            logger.error("Cosmic json file " + cosmicJsonFile + " doesn't exist");
            return false;
        }
        gwasJsonFile = clinicalJsonFilesDir.resolve("gwas.json.gz");
        if (!gwasJsonFile.toFile().exists()) {
            logger.error("Gwas json file " + gwasJsonFile + " doesn't exist");
            return false;
        }
        logger.info("Done");
        return true;
    }
}
