package org.opencb.cellbase.build.transform;

import org.broad.tribble.readers.TabixReader;
import org.opencb.biodata.models.variant.clinical.Gwas;
import org.opencb.cellbase.build.serializers.CellBaseSerializer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

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

                Map<Variant, Gwas> variantMap = new TreeMap<>();
                logger.info("Opening dbSNP tabix file " + dbSnpTabixFilePath + " ...");
                TabixReader dbsnpTabixReader = new TabixReader(dbSnpTabixFilePath.toString());

                long processedGwasLines = 0,
                     ignoredGwasLines = 0;

                logger.info("Parsing gwas file ...");
                for (String line; (line = inputReader.readLine())!= null;) {
                    if (!line.isEmpty()) {
                        processedGwasLines++;
                        Gwas gwasRecord = buildGwasObject(line.split("\t"));

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


            } catch (NumberFormatException e) {
                logger.error("Malformed gwas line: " + e.getMessage());
			} catch (IOException e) {
                logger.error("Unable to parse " + gwasFile + " using dbSNP file " + dbSnpTabixFilePath + ": " + e.getMessage());
            }
		}
	}

    private Gwas buildGwasObject(String[] values) {
        Gwas gwas = new Gwas();

        gwas.setRegion(values[10].trim());
        if(!values[11].isEmpty()){
            if(values[11].equalsIgnoreCase("23")){
                gwas.setChromosome("X");
            } else if(values[11].equalsIgnoreCase("24")) {
                gwas.setChromosome("Y");
            } else if(values[11].equalsIgnoreCase("25")) {
                gwas.setChromosome("MT");
            } else {
                gwas.setChromosome(values[11]);
            }
        }

        try{
            gwas.setStart(Integer.parseInt(values[12]));
            gwas.setEnd(gwas.getStart());;
        } catch (NumberFormatException e){
            logger.error("Malformed gwas line in chromosome position (it is not a valid number): "+values[12]);
        }

        gwas.setReportedGenes(values[13].trim());
        gwas.setMappedGene(values[14].trim());
        gwas.setUpstreamGeneId(values[15].trim());
        gwas.setDownstreamGeneId(values[16].trim());
        gwas.setSnpGeneIds(values[17].trim());
        gwas.setUpstreamGeneDistance(values[18].trim());
        gwas.setDownstreamGeneDistance(values[19].trim());
        gwas.setStrongestSNPRiskAllele(values[20].trim());
        gwas.setSnps(values[21].trim());
        gwas.setMerged(values[22].trim());
        gwas.setSnpIdCurrent(values[23].trim());
        gwas.setContext(values[24].trim());
        gwas.setIntergenic(values[25].trim());
        try {
            gwas.setRiskAlleleFrequency(Float.parseFloat(values[26]));
        } catch (NumberFormatException e){
            logger.warn("Malformed gwas line in Risk Allele Frequency (it is not a valid number): "+values[26]);
        }
        gwas.setCnv(values[33].trim());

        // Add the study values
        gwas.getStudies().get(0).setPubmedId(values[1].trim());
        gwas.getStudies().get(0).setFirstAuthor(values[2].trim());
        gwas.getStudies().get(0).setDate(values[3].trim());
        gwas.getStudies().get(0).setJournal(values[4].trim());
        gwas.getStudies().get(0).setLink(values[5].trim());
        gwas.getStudies().get(0).setStudy(values[6].trim());
        gwas.getStudies().get(0).setInitialSampleSize(values[8].trim());
        gwas.getStudies().get(0).setReplicationSampleSize(values[9].trim());
        gwas.getStudies().get(0).setPlatform(values[32].trim());

        // Add the tray values
        gwas.getStudies().get(0).getTraits().get(0).setDiseaseTrait(values[7].trim());
        gwas.getStudies().get(0).getTraits().get(0).setDateAddedToCatalog(values[0].trim());

        // Add the test values
        try {
            gwas.getStudies().get(0).getTraits().get(0).getTests().get(0).setpValue(Float.parseFloat(values[27]));
        } catch (NumberFormatException e){
            logger.warn("Malformed gwas line in P Value (it is not a valid number): "+values[27]);
        }
        try {
            gwas.getStudies().get(0).getTraits().get(0).getTests().get(0).setpValueMlog(Float.parseFloat(values[28]));
        } catch (NumberFormatException e){
            logger.warn("Malformed gwas line in P Value Mlog (it is not a valid number): "+values[28]);
        }
        gwas.getStudies().get(0).getTraits().get(0).getTests().get(0).setpValueText(values[29].trim());
        gwas.getStudies().get(0).getTraits().get(0).getTests().get(0).setOrBeta(values[30].trim());
        gwas.getStudies().get(0).getTraits().get(0).getTests().get(0).setPercentCI(values[31].trim());

        return gwas;
    }

    private void printSummary(long processedGwasLines, long ignoredGwasLines, Map<Variant, Gwas> variantMap) {
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

    private void addGwasRecordToVariantMap(Map<Variant, Gwas> variantMap, Gwas gwasRecord) {
        String[] alternates = gwasRecord.getAlternate().split(",");
        for (int i=0; i < alternates.length; i++) {
            String alternate = alternates[i];
            Variant variantKey =
                    new Variant(gwasRecord.getChromosome(), gwasRecord.getStart(), gwasRecord.getReference(), alternate);
            if (variantMap.containsKey(variantKey)) {
                updateGwasEntry(variantMap, gwasRecord, variantKey);
            } else {
                // if a gwas record has several alternatives, it has to be cloned to avoid side effects (set gwasRecord
                // alternative would update the previous instance of gwas record saved in the 'variantMap')
                gwasRecord = cloneGwasRecordIfNecessary(gwasRecord, i);
                gwasRecord.setAlternate(alternate);
                variantMap.put(variantKey, gwasRecord);
            }
        }
    }

    private Gwas cloneGwasRecordIfNecessary(Gwas gwasRecord, int i) {
        if (i > 0) {
            gwasRecord = new Gwas(gwasRecord);
        }
        return gwasRecord;
    }

    private void updateGwasEntry(Map<Variant, Gwas> variantMap, Gwas gwasVO, Variant gwasKey) {
        Gwas gwas = variantMap.get(gwasKey);
        gwas.addStudies(gwasVO.getStudies());
        variantMap.put(gwasKey, gwas);
    }

    class Variant implements Comparable<Variant> {
        private String chr;
        private Integer start;
        private String ref;
        private String alt;

        Variant(String chr, Integer start, String ref, String alt) {
            this.ref = ref;
            this.chr = chr;
            this.start = start;
            this.alt = alt;
        }

        public int compareTo(Variant anotherVariant) {
            if (!chr.equals(anotherVariant.chr)) {
                return chr.compareTo(anotherVariant.chr);
            } else {
                if (!start.equals(anotherVariant.start)) {
                    return start.compareTo(anotherVariant.start);
                } else {
                    if (!ref.equals(anotherVariant.ref)) {
                        return ref.compareTo(anotherVariant.ref);
                    } else {
                        return alt.compareTo(anotherVariant.alt);
                    }
                }
            }
        }
    }
}