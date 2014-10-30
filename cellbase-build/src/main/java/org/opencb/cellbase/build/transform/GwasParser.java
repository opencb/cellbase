package org.opencb.cellbase.build.transform;

import org.broad.tribble.readers.TabixReader;
import org.opencb.biodata.models.variant.clinical.Gwas;
import org.opencb.cellbase.build.serializers.CellBaseSerializer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
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
    private int malformedPValueRecords;
    private int malformedStartRecords;
    private int malformedRiskAlleleFrequencyRecords;
    private int malformedPValueMLogRecords;

    public GwasParser(CellBaseSerializer serializer, Path gwasFile, Path dbSnpTabixFilePath) {
        super(serializer);
        this.gwasFile = gwasFile;
        this.dbSnpTabixFilePath = dbSnpTabixFilePath;
        this.malformedPValueRecords = 0;
        this.malformedStartRecords = 0;
        this.malformedRiskAlleleFrequencyRecords = 0;
        this.malformedPValueMLogRecords = 0;
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
                     gwasLinesNotFoundInDbsnp = 0,
                     malformedGwasLines = 0;

                logger.info("Parsing gwas file ...");
                for (String line; (line = inputReader.readLine())!= null;) {
                    if (!line.isEmpty()) {
                        processedGwasLines++;
                        try {
                            Gwas gwasRecord = buildGwasObject(line.split("\t"));
                            if (gwasRecord.getChromosome() != null && gwasRecord.getStart() != null && getRefAndAltFromDbsnp(gwasRecord, dbsnpTabixReader)) {
                                addGwasRecordToVariantMap(variantMap, gwasRecord);
                            } else {
                                gwasLinesNotFoundInDbsnp++;
                            }
                        } catch (NumberFormatException e) {
                            malformedGwasLines++;
                        }
                    }
                }

                logger.info("Serializing parsed variants ...");
                for (Gwas gwasOutputRecord : variantMap.values()) {
                    serialize(gwasOutputRecord);
                }
                logger.info("Done");
                this.printSummary(processedGwasLines, gwasLinesNotFoundInDbsnp, malformedGwasLines, variantMap);


            } catch (NumberFormatException e) {
                logger.error("Malformed gwas line: " + e.getMessage());
			} catch (IOException e) {
                logger.error("Unable to parse " + gwasFile + " using dbSNP file " + dbSnpTabixFilePath + ": " + e.getMessage());
            }
		}
	}

    private Gwas buildGwasObject(String[] values) throws NumberFormatException {
        Gwas gwas = new Gwas();

        gwas.setRegion(values[10].trim());
        setGwasChromosome(values[11], gwas);

        try{
            gwas.setStart(Integer.parseInt(values[12]));
            gwas.setEnd(gwas.getStart());
        } catch (NumberFormatException e){
            malformedStartRecords++;
            throw e;
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
            if (!values[26].equals("NR")) {
                gwas.setRiskAlleleFrequency(Float.parseFloat(values[26]));
            }
        } catch (NumberFormatException e){
            malformedRiskAlleleFrequencyRecords++;
            throw e;
        }
        gwas.setCnv(values[33].trim());
        builgGwasStudy(values, gwas);

        return gwas;
    }

    private void builgGwasStudy(String[] values, Gwas gwas) {
        // Add the study values
        Gwas.GwasStudy study = gwas.new GwasStudy();
        study.setPubmedId(values[1].trim());
        study.setFirstAuthor(values[2].trim());
        study.setDate(values[3].trim());
        study.setJournal(values[4].trim());
        study.setLink(values[5].trim());
        study.setStudy(values[6].trim());
        study.setInitialSampleSize(values[8].trim());
        study.setReplicationSampleSize(values[9].trim());
        study.setPlatform(values[32].trim());
        buildGwasStudyTrait(values, study);
        gwas.addStudy(study);
    }

    private void buildGwasStudyTrait(String[] values, Gwas.GwasStudy study) {
        // Add the trait values
        Gwas.GwasStudy.GwasTrait trait = study.new GwasTrait();
        trait.setDiseaseTrait(values[7].trim());
        trait.setDateAddedToCatalog(values[0].trim());
        buildGwasStudyTraitTest(values, trait);
        study.addTrait(trait);
    }

    private void buildGwasStudyTraitTest(String[] values, Gwas.GwasStudy.GwasTrait trait) throws NumberFormatException {
        // Add the test values
        Gwas.GwasStudy.GwasTrait.GwasTest test = trait.new GwasTest();
        try {
            test.setpValue(Float.parseFloat(values[27]));
        } catch (NumberFormatException e){
            malformedPValueRecords++;
            throw e;
        }
        try {
            test.setpValueMlog(Float.parseFloat(values[28]));
        } catch (NumberFormatException e){
            malformedPValueMLogRecords++;
            throw e;
        }
        test.setpValueText(values[29].trim());
        test.setOrBeta(values[30].trim());
        test.setPercentCI(values[31].trim());
        trait.addTest(test);
    }

    private void setGwasChromosome(String chromosome, Gwas gwas) {
        if(!chromosome.isEmpty()){
            switch(chromosome) {
                case "23":
                    gwas.setChromosome("X");
                    break;
                case "24":
                    gwas.setChromosome("Y");
                    break;
                case "25":
                    gwas.setChromosome("MT");
                    break;
                default:
                    gwas.setChromosome(chromosome);
            }
        }
    }

    private void printSummary(long processedGwasLines, long gwasLinesNotFoundInDbsnp, long malformedGwasLines, Map<Variant, Gwas> variantMap) {
        NumberFormat formatter = NumberFormat.getInstance();
        logger.info("");
        logger.info("Summary");
        logger.info("=======");
        logger.info("Processed " + formatter.format(processedGwasLines) + " gwas lines");
        logger.info("Serialized " + formatter.format(variantMap.size()) + " variants");
        logger.info(formatter.format(gwasLinesNotFoundInDbsnp) + " gwas lines ignored because variant not found in dbsnp");
        logger.info(formatter.format(malformedGwasLines) + " gwas lines ignored because are malformed");
        logger.info("\t - " + formatter.format(malformedStartRecords) + " because 'start' is not a valid integer");
        logger.info("\t - " + formatter.format(malformedPValueRecords) + " because 'pValue' is not a valid float");
        logger.info("\t - " + formatter.format(malformedPValueMLogRecords) + " because 'pValueMlog' is not a valid float");
        logger.info("\t - " + formatter.format(malformedRiskAlleleFrequencyRecords) + " because 'risk allele frequency' is not a valid float");
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