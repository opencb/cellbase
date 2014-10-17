package org.opencb.cellbase.build.transform;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.broad.tribble.readers.TabixReader;
import org.opencb.biodata.models.variant.clinical.Gwas;
import org.opencb.cellbase.build.serializers.CellBaseSerializer;

/**
 * @author Luis Miguel Cruz
 * @version 1.2.3
 * @since October 08, 2014 
 */
public class GwasParser extends CellBaseParser {

    private final Path gwasFile;
    private final Path dbSnpTabixFilePath;

    public GwasParser(CellBaseSerializer serializer, Path gwasFile, Path dbSnpTabixFilePath) {
        super(serializer);
        this.gwasFile = gwasFile;
        this.dbSnpTabixFilePath = dbSnpTabixFilePath;
    }

	public void parse() {
		if (Files.exists(gwasFile) && Files.exists(dbSnpTabixFilePath)) {
			try {
                logger.info("Opening gwas file " + gwasFile + " ...");
                BufferedReader inputReader = new BufferedReader(new FileReader(gwasFile.toFile()));

				// read the header
                logger.info("Ignoring gwas file header line ...");
				inputReader.readLine();

                Map<String, Gwas> variantMap = new HashMap<>();
                logger.info("Opening dbSNP tabix file " + dbSnpTabixFilePath + " ...");
                TabixReader dbsnpTabixReader = new TabixReader(dbSnpTabixFilePath.toString());

                long processedGwasLines = 0,
                     ignoredGwasLines = 0;

                logger.info("Parsing gwas file ...");
                for (String line; (line = inputReader.readLine())!= null;) {
                    if (!line.isEmpty()) {
                        processedGwasLines++;
                        Gwas gwasRecord = new Gwas(line.split("\t"));

                        if (gwasRecord.getChromosome() != null && gwasRecord.getStart() != null && getRefAndAltFromDbsnp(gwasRecord, dbsnpTabixReader)) {
                            addGwasRecordToVariantMap(variantMap, gwasRecord);
                        } else {
                            ignoredGwasLines++;
                        }
                    }
                }

                logger.info("Serializing parsed variants ...");
                for (Gwas gwasOutputRecord : variantMap.values()) {
                    serialize(gwasOutputRecord);
                }
                logger.info("Done");
                this.printSummary(processedGwasLines, ignoredGwasLines, variantMap);


            } catch (ParseException e) {
                logger.error("Malformed gwas line: " + e.getMessage());
			} catch (IOException e) {
                logger.error("Unable to parse " + gwasFile + " using dbSNP file " + dbSnpTabixFilePath + ": " + e.getMessage());
            }
		}
	}

    private void printSummary(long processedGwasLines, long ignoredGwasLines, Map<String, Gwas> variantMap) {
        logger.info("");
        logger.info("Summary");
        logger.info("=======");
        logger.info("Processed " + processedGwasLines + " gwas lines");
        logger.info(ignoredGwasLines + " gwas lines ignored because variant not found in dbsnp");
        logger.info("Serialized " + variantMap.size() + " variants");
    }

    private boolean getRefAndAltFromDbsnp(Gwas gwasVO, TabixReader dbsnpTabixReader)  {
        boolean found = false;
        TabixReader.Iterator dbsnpIterator = dbsnpTabixReader.query(gwasVO.getChromosome() + ":" +
                gwasVO.getStart() + "-" + gwasVO.getStart());
        try {
            String dbSnpRecord = dbsnpIterator.next();

            while (dbSnpRecord != null && !found) {
                String[] fields = dbSnpRecord.split("\t");
                // ff the rs is the same in dbsnp and gwas
                if (gwasVO.getSnps().equalsIgnoreCase(fields[2])) {
                    gwasVO.setReference(fields[3]);
                    gwasVO.setAlternate(fields[4]);
                    found = true;
                }

                dbSnpRecord = dbsnpIterator.next();
            }
        } catch (IOException e) {
            logger.warn("Position " + gwasVO.getChromosome() + ":" + gwasVO.getStart() + " not found in dbSNP");
        }
        return found;
    }

    private void addGwasRecordToVariantMap(Map<String, Gwas> variantMap, Gwas gwasRecord) {
        String ref = gwasRecord.getReference();
        for (String alternate : gwasRecord.getAlternate().split(",")) {
            String variantKey = gwasRecord.getChromosome() + "::" + gwasRecord.getStart() + "::" + ref + "::" + alternate;
            if (variantMap.containsKey(variantKey)) {
                updateGwasEntry(variantMap, gwasRecord, variantKey);
            } else {
                gwasRecord.setAlternate(alternate);
                variantMap.put(variantKey, gwasRecord);
            }
        }
    }

    private void updateGwasEntry(Map<String, Gwas> variantMap, Gwas gwasVO, String gwasKey) {
        Gwas gwas = variantMap.get(gwasKey);
        gwas.addStudies(gwasVO.getStudies());
        variantMap.put(gwasKey, gwas);
    }
}