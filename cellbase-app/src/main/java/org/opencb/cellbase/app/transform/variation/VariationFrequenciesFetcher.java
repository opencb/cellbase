package org.opencb.cellbase.app.transform.variation;

import htsjdk.tribble.readers.TabixReader;
import org.opencb.biodata.models.variant.avro.PopulationFrequency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by parce on 09/12/15.
 */
public class VariationFrequenciesFetcher {

    private Logger logger;

    private TabixReader frequenciesTabixReader;

    private final Set<String> thousandGenomesPhase1MissedPopulations;
    private final Set<String> thousandGenomesPhase3MissedPopulations;

    private Pattern populationFrequnciesPattern;
    private static final String POPULATION_ID_GROUP = "popId";
    private static final String REFERENCE_FREQUENCY_GROUP = "ref";
    private static final String ALTERNATE_FREQUENCY_GROUP = "alt";

    private static final String THOUSAND_GENOMES_PHASE_1_STUDY = "1000GENOMES_phase_1";
    private static final String THOUSAND_GENOMES_PHASE_3_STUDY = "1000GENOMES_phase_3";
    private static final String ESP_6500_STUDY = "ESP_6500";
    private static final String EXAC_STUDY = "ExAC";
    private static final String THOUSAND_GENOMES_ALL_POPULATION = "ALL";
    private static final String THOUSAND_GENOMES_AMERICAN_POPULATION = "AMR";
    private static final String THOUSAND_GENOMES_ASIAN_POPULATION = "ASN";
    private static final String THOUSAND_GENOMES_AFRICAN_POPULATION = "AFR";
    private static final String THOUSAND_GENOMES_EUROPEAN_POPULATION = "EUR";
    private static final String THOUSAND_GENOMES_EASTASIAN_POPULATION = "EAS";
    private static final String THOUSAND_GENOMES_SOUTHASIAN_POPULATION = "SAS";
    private static final String ESP_EUROPEAN_AMERICAN_POPULATION = "European_American";
    private static final String ESP_AFRICAN_AMERICAN_POPULATION = "African_American";
    private static final String ESP_ALL_POPULATION = "All";
    private static final String EXAC_AFRICAN_POPULATION = "AFR";
    private static final String EXAC_LATINO_POPULATION = "AMR";
    private static final String EXAC_EAST_ASIAN_POPULATION = "EAS";
    private static final String EXAC_FINNISH_POPULATION = "FIN";
    private static final String EXAC_NON_FINNISH_EUROPEAN_POPULATION = "NFE";
    private static final String EXAC_SOUTH_ASIAN_POPULATION = "SAS";
    private static final String EXAC_OTHER_POPULATION = "OTH";
    private static final String EXAC_ALL_POPULATION = "ALL";

    public VariationFrequenciesFetcher(Path frequenciesFile) throws IOException {
        logger = LoggerFactory.getLogger(this.getClass());
        frequenciesTabixReader = new TabixReader(frequenciesFile.toString());
        thousandGenomesPhase1MissedPopulations = new HashSet<>();
        thousandGenomesPhase3MissedPopulations = new HashSet<>();
        populationFrequnciesPattern = Pattern.compile("(?<" + POPULATION_ID_GROUP + ">\\w+):(?<" + REFERENCE_FREQUENCY_GROUP
                + ">\\d+(.\\d)*),(?<" + ALTERNATE_FREQUENCY_GROUP + ">\\d+(.\\d)*)");
    }

    public List<PopulationFrequency> getPopulationFrequencies(String chromosome, int start, String referenceAllele,
                                                               String alternativeAllele) throws IOException {
        List<PopulationFrequency> populationFrequencies;
        String variationFrequenciesString = getVariationFrequenciesString(chromosome, start, referenceAllele, alternativeAllele);
        if (variationFrequenciesString != null) {
            populationFrequencies = parseVariationFrequenciesString(variationFrequenciesString, referenceAllele, alternativeAllele);
        } else {
            populationFrequencies = Collections.EMPTY_LIST;
        }
        return populationFrequencies;
    }

    private String getVariationFrequenciesString(String chromosome, int start, String reference, String alternate) throws IOException {
        try {
            if (frequenciesTabixReader != null) {
                TabixReader.Iterator frequenciesFileIterator = frequenciesTabixReader.query(chromosome, start - 1, start);
                if (frequenciesFileIterator != null) {
                    String variationFrequenciesLine = frequenciesFileIterator.next();
                    while (variationFrequenciesLine != null) {
                        String[] variationFrequenciesFields = variationFrequenciesLine.split("\t");
                        if (Integer.valueOf(variationFrequenciesFields[1]) == start && variationFrequenciesFields[3].equals(reference)
                                && variationFrequenciesFields[4].equals(alternate)) {
                            return variationFrequenciesFields[6];
                        }
                        variationFrequenciesLine = frequenciesFileIterator.next();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error getting variation {}:{} {}/{} frequencies: {}", chromosome, start, reference, alternate, e.getMessage());
        }
        return null;
    }

    private List<PopulationFrequency> parseVariationFrequenciesString(String variationFrequenciesString, String referenceAllele,
                                                                      String alternativeAllele) {
        List<PopulationFrequency> frequencies = new ArrayList<>();
        for (String populationFrequency : variationFrequenciesString.split(";")) {
            frequencies.add(parsePopulationFrequency(populationFrequency, referenceAllele, alternativeAllele));
        }

        thousandGenomesPhase1MissedPopulations.add(THOUSAND_GENOMES_AFRICAN_POPULATION);
        thousandGenomesPhase1MissedPopulations.add(THOUSAND_GENOMES_AMERICAN_POPULATION);
        thousandGenomesPhase1MissedPopulations.add(THOUSAND_GENOMES_EUROPEAN_POPULATION);
        thousandGenomesPhase1MissedPopulations.add(THOUSAND_GENOMES_ASIAN_POPULATION);
        frequencies = addMissedPopulations(frequencies, thousandGenomesPhase1MissedPopulations,
                THOUSAND_GENOMES_PHASE_1_STUDY, THOUSAND_GENOMES_ALL_POPULATION);

        thousandGenomesPhase3MissedPopulations.add(THOUSAND_GENOMES_AFRICAN_POPULATION);
        thousandGenomesPhase3MissedPopulations.add(THOUSAND_GENOMES_AMERICAN_POPULATION);
        thousandGenomesPhase3MissedPopulations.add(THOUSAND_GENOMES_EUROPEAN_POPULATION);
        thousandGenomesPhase3MissedPopulations.add(THOUSAND_GENOMES_EASTASIAN_POPULATION);
        thousandGenomesPhase3MissedPopulations.add(THOUSAND_GENOMES_SOUTHASIAN_POPULATION);
        frequencies = addMissedPopulations(frequencies, thousandGenomesPhase3MissedPopulations,
                THOUSAND_GENOMES_PHASE_3_STUDY, THOUSAND_GENOMES_ALL_POPULATION);

        return frequencies;
    }

    private PopulationFrequency parsePopulationFrequency(String frequencyString, String referenceAllele, String alternativeAllele) {
        PopulationFrequency populationFrequency = null;
        Matcher m = populationFrequnciesPattern.matcher(frequencyString);

        if (m.matches()) {
            String populationName;
            String study = "";
            String population = m.group(POPULATION_ID_GROUP);
            switch (population) {
                case "1000G_PHASE_1_AF":
                    study = THOUSAND_GENOMES_PHASE_1_STUDY;
                    populationName = THOUSAND_GENOMES_ALL_POPULATION;
                    break;
                case "1000G_PHASE_1_AMR_AF":
                    study = THOUSAND_GENOMES_PHASE_1_STUDY;
                    populationName = THOUSAND_GENOMES_AMERICAN_POPULATION;
                    break;
                case "1000G_PHASE_1_ASN_AF":
                    study = THOUSAND_GENOMES_PHASE_1_STUDY;
                    populationName = THOUSAND_GENOMES_ASIAN_POPULATION;
                    break;
                case "1000G_PHASE_1_AFR_AF":
                    study = THOUSAND_GENOMES_PHASE_1_STUDY;
                    populationName = THOUSAND_GENOMES_AFRICAN_POPULATION;
                    break;
                case "1000G_PHASE_1_EUR_AF":
                    study = THOUSAND_GENOMES_PHASE_1_STUDY;
                    populationName = THOUSAND_GENOMES_EUROPEAN_POPULATION;
                    break;
                case "1000G_PHASE_3_AF":
                    study = THOUSAND_GENOMES_PHASE_3_STUDY;
                    populationName = THOUSAND_GENOMES_ALL_POPULATION;
                    break;
                case "1000G_PHASE_3_AMR_AF":
                    study = THOUSAND_GENOMES_PHASE_3_STUDY;
                    populationName = THOUSAND_GENOMES_AMERICAN_POPULATION;
                    break;
                case "1000G_PHASE_3_AFR_AF":
                    study = THOUSAND_GENOMES_PHASE_3_STUDY;
                    populationName = THOUSAND_GENOMES_AFRICAN_POPULATION;
                    break;
                case "1000G_PHASE_3_EUR_AF":
                    study = THOUSAND_GENOMES_PHASE_3_STUDY;
                    populationName = THOUSAND_GENOMES_EUROPEAN_POPULATION;
                    break;
                case "1000G_PHASE_3_EAS_AF":
                    study = THOUSAND_GENOMES_PHASE_3_STUDY;
                    populationName = THOUSAND_GENOMES_EASTASIAN_POPULATION;
                    break;
                case "1000G_PHASE_3_SAS_AF":
                    study = THOUSAND_GENOMES_PHASE_3_STUDY;
                    populationName = THOUSAND_GENOMES_SOUTHASIAN_POPULATION;
                    break;
                case "ESP_6500_EA_AF":
                    study = ESP_6500_STUDY;
                    populationName = ESP_EUROPEAN_AMERICAN_POPULATION;
                    break;
                case "ESP_6500_AA_AF":
                    study = ESP_6500_STUDY;
                    populationName = ESP_AFRICAN_AMERICAN_POPULATION;
                    break;
                case "ESP_6500_ALL_AF":
                    study = ESP_6500_STUDY;
                    populationName = ESP_ALL_POPULATION;
                    break;
                case "EXAC_AFR_AF":
                    study = EXAC_STUDY;
                    populationName = EXAC_AFRICAN_POPULATION;
                    break;
                case "EXAC_AMR_AF":
                    study = EXAC_STUDY;
                    populationName = EXAC_LATINO_POPULATION;
                    break;
                case "EXAC_EAS_AF":
                    study = EXAC_STUDY;
                    populationName = EXAC_EAST_ASIAN_POPULATION;
                    break;
                case "EXAC_FIN_AF":
                    study = EXAC_STUDY;
                    populationName = EXAC_FINNISH_POPULATION;
                    break;
                case "EXAC_NFE_AF":
                    study = EXAC_STUDY;
                    populationName = EXAC_NON_FINNISH_EUROPEAN_POPULATION;
                    break;
                case "EXAC_SAS_AF":
                    study = EXAC_STUDY;
                    populationName = EXAC_SOUTH_ASIAN_POPULATION;
                    break;
                case "EXAC_OTH_AF":
                    study = EXAC_STUDY;
                    populationName = EXAC_OTHER_POPULATION;
                    break;
                case "EXAC_ALL_AF":
                    study = EXAC_STUDY;
                    populationName = EXAC_ALL_POPULATION;
                    break;
                default:
                    populationName = population;
            }
            Float referenceFrequency = Float.parseFloat(m.group(REFERENCE_FREQUENCY_GROUP));
            Float alternativeFrequency = Float.parseFloat(m.group(ALTERNATE_FREQUENCY_GROUP));

            populationFrequency = new PopulationFrequency(study, populationName, populationName, referenceAllele, alternativeAllele,
                    referenceFrequency, alternativeFrequency, null, null, null);
        }

        return populationFrequency;
    }

    private List<PopulationFrequency> addMissedPopulations(List<PopulationFrequency> frequencies,
                                                           Set<String> missedPopulations, String study,
                                                           String allPopulation) {
        int thousandGenomesPopulationsNumber = missedPopulations.size();

        String refAllele = null;
        String altAllele = null;
        for (PopulationFrequency frequency : frequencies) {
            if (frequency != null && frequency.getStudy() != null && frequency.getStudy().equals(study)) {
                if (frequency.getPopulation().equals(allPopulation)) {
                    refAllele = frequency.getRefAllele();
                    altAllele = frequency.getAltAllele();
                }
                missedPopulations.remove(frequency.getPopulation());
            }
        }

        // if the variation has some superpopulation frequency, but not all, add the missed superpopulations with 1 as ref allele proportion
        if (!missedPopulations.isEmpty() && missedPopulations.size() != thousandGenomesPopulationsNumber) {
            for (String population : missedPopulations) {
                frequencies.add(new PopulationFrequency(study, population, population, refAllele, altAllele, 1f, 0f, null, null, null));
            }
        }

        return frequencies;
    }
}
