package org.opencb.cellbase.build.transform;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.broad.tribble.readers.TabixReader;
import org.opencb.biodata.models.variant.clinical.Gwas;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;

/**
 * @author Luis Miguel Cruz
 * @version 1.2.3
 * @since October 08, 2014 
 */
public class GwasParser {
    private CellBaseSerializer serializer;

	public GwasParser(CellBaseSerializer serializer) {
        this.serializer = serializer;
	}

	public void parseFile(Path gwasFile, Path dbSnpFilePath) {
		if (Files.exists(gwasFile) && Files.exists(dbSnpFilePath)) {
			try {
                BufferedReader inputReader = new BufferedReader(new FileReader(gwasFile.toFile()));
				// Read the header
				inputReader.readLine();

                Map<String, Gwas> variantMap = new HashMap<>();
                TabixReader dbsnpTabixReader = new TabixReader(dbSnpFilePath.toString());

                String line;
                while ((line = inputReader.readLine()) != null) {
                    if (!line.isEmpty()) {
                        Gwas gwasRecord = new Gwas(line.split("\t"));

                        if (gwasRecord.getChromosome() != null && gwasRecord.getStart() != null && getRefAndAltFromDbsnp(gwasRecord, dbsnpTabixReader)) {
                            addGwasRecordToVariantMap(variantMap, gwasRecord);
                        }
                    }
                }

                for (Gwas gwasOutputRecord : variantMap.values()) {
                    this.serializer.serializeObject(gwasOutputRecord);
                }

            } catch (ParseException e) {
                e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

    private boolean getRefAndAltFromDbsnp(Gwas gwasVO, TabixReader dbsnpTabixReader) throws IOException {
        TabixReader.Iterator dbsnpIterator = dbsnpTabixReader.query(gwasVO.getChromosome() + ":"
                + gwasVO.getStart() + "-" + gwasVO.getStart());
        String dbSnpRecord = dbsnpIterator.next();
        boolean found = false;

        while (dbSnpRecord != null && !found) {
            String[] fields = dbSnpRecord.split("\t");
            // ff the rs is the same in dbsnp that the found in gwas
            if (gwasVO.getSnps().equalsIgnoreCase(fields[2])) {
                gwasVO.setReference(fields[3]);
                gwasVO.setAlternate(fields[4]);
                found = true;
            }

            dbSnpRecord = dbsnpIterator.next();
        }
        return found;
    }

    private void addGwasRecordToVariantMap(Map<String, Gwas> variantMap, Gwas gwasRecord) {
        String ref = gwasRecord.getReference();
        // TODO: test split with one alternate
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
        // TODO: add studies is adding tests?
        variantMap.put(gwasKey, gwas);
    }
}