package org.opencb.cellbase.app.transform.clinical.variant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.map.HashedMap;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.avro.*;
import org.opencb.cellbase.app.cli.EtlCommons;
import org.opencb.cellbase.core.variant.annotation.VariantAnnotationUtils;
import org.opencb.commons.utils.FileUtils;
import org.opencb.commons.utils.StringUtils;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.opencb.biodata.models.variant.protobuf.EvidenceEntryProto.FeatureTypes.gene;

/**
 * Created by fjlopez on 29/03/17.
 */
public class DOCMIndexer extends ClinicalIndexer {
    private static final String REFERENCE_VERSION = "reference_version";
    private static final String CHROMOSOME = "chromosome";
    private static final String START = "start";
    private static final String REFERENCE = "reference";
    private static final String VARIANT = "variant";
    private static final String DISEASES = "diseases";
    private static final String DISEASE = "disease";
    private static final String SOURCE_PUBMED_ID = "source_pubmed_id";
    private static final String PMID = "PMID:";
    private static final String URL_PREFIX = "http://docm.genome.wustl.edu/variants/";
    private static final String HGVS = "hgvs";
    private static final String GENE = "gene";
    private static final String TRANSCRIPT = "transcript";
    private static final String ENST = "ENST";
    private static final String SYMBOL = "symbol";
    private static final String TAGS = "tags";
    private static final String TAGS_IN_SOURCE_FILE = "tags_in_source_file";
    private static final String META = "meta";
    private static final String DRUG_INTERACTION_DATA = "Drug Interaction Data";
    private static final String FIELDS = "fields";
    private static final String ROWS = "rows";
    private static final String THERAPEUTIC_CONTEXT = "Therapeutic Context";
    private static final String PATHWAY = "Pathway";
    private static final String EFFECT = "Effect";
    private static final String ASSOCIATION = "Association";
    private static final String STATUS = "Status";
    private static final String EVIDENCE = "Evidence";
    private static final String SOURCE = "Source";
    private static final String NAME = "name";
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

    private void updateRocksDB(Variant variant) throws RocksDBException, IOException {
        byte[] key = VariantAnnotationUtils.buildVariantId(variant.getChromosome(), variant.getStart(),
                variant.getReference(), variant.getAlternate()).getBytes();
        VariantAnnotation variantAnnotation = getVariantAnnotation(key);
//        List<EvidenceEntry> evidenceEntryList = getVariantAnnotation(key);

        // Add EvidenceEntry objects
        variantAnnotation.getTraitAssociation().addAll(variant.getAnnotation().getTraitAssociation());

        // Check if drug info is available
        if (variant.getAnnotation().getDrugs() != null && !variant.getAnnotation().getDrugs().isEmpty()) {
            // Drug info is stored at the VariantAnnotation root
            if (variantAnnotation.getDrugs() == null) {
                variantAnnotation.setDrugs(variant.getAnnotation().getDrugs());
            } else {
                variantAnnotation.getDrugs().addAll(variant.getAnnotation().getDrugs());
            }

        }

        rdb.put(key, jsonObjectWriter.writeValueAsBytes(variantAnnotation));
    }

    private Variant parseVariant(String line) throws IOException {
        Map<String, Object> map = (HashMap<String, Object>) new ObjectMapper().readValue(line, HashMap.class);
        if (assembly.equalsIgnoreCase((String) map.get(REFERENCE_VERSION))) {
            Variant variant = new Variant((String) map.get(CHROMOSOME), (Integer) map.get(START),
                    (String) map.get(REFERENCE), (String) map.get(VARIANT));

            VariantAnnotation variantAnnotation = parseVariantAnnotation(map);
            variant.setAnnotation(variantAnnotation);

            return variant;
        } else {
            return null;
        }
    }

    private VariantAnnotation parseVariantAnnotation(Map<String, Object> map) {
        // The list diseases in map.get(DISEASES) may contain multiple elements for the same disease but with different
        // pubmed ids
        Map<String, EvidenceEntry> evidenceEntryMap = new HashedMap(((List) map.get(DISEASES)).size());
        for (Map diseaseMap : ((List<Map>) map.get(DISEASES))) {
            EvidenceEntry evidenceEntry;
            // An object with current disease string has already been parsed for this variant
            if (evidenceEntryMap.containsKey(diseaseMap.get(DISEASE))) {
                evidenceEntry = evidenceEntryMap.get(DISEASE);
                evidenceEntry.getBibliography().add(PMID +  (String) diseaseMap.get(SOURCE_PUBMED_ID));
            } else {
                EvidenceSource evidenceSource = new EvidenceSource(EtlCommons.DOCM_DATA, null, null);
                HeritableTrait heritableTrait = new HeritableTrait((String) diseaseMap.get(DISEASE), null);

                List<GenomicFeature> genomicFeatureList = getGenomicFeature(map);

                VariantClassification variantClassification = getVariantClassification((List<String>) diseaseMap.get(TAGS));

                Property property = new Property(null, TAGS_IN_SOURCE_FILE,
                        String.join(",", (List<String>) diseaseMap.get(TAGS)));

                evidenceEntry = new EvidenceEntry(evidenceSource, null, null, URL_PREFIX + (String) map.get(HGVS),
                        null, null, null, Collections.singletonList(heritableTrait), genomicFeatureList,
                        variantClassification, null, null, null, null, null, null, null,
                        Collections.singletonList(property),
                        Collections.singletonList(PMID +  String.valueOf(diseaseMap.get(SOURCE_PUBMED_ID))));

                evidenceEntryMap.put((String) diseaseMap.get(DISEASE), evidenceEntry);
            }
        }

        List<Drug> drugList = null;
        if (map.containsKey(META)) {
            drugList = new ArrayList<>(((List<Map>) map.get(META)).size());
            for (Map metaMap : ((List<Map>) map.get(META))) {
                if (metaMap.containsKey(DRUG_INTERACTION_DATA)
                        && ((Map) metaMap.get(DRUG_INTERACTION_DATA)).containsKey(FIELDS)
                        && ((Map) metaMap.get(DRUG_INTERACTION_DATA)).containsKey(ROWS)) {
                    List<String> fields = (List<String>) ((Map) metaMap.get(DRUG_INTERACTION_DATA)).get(FIELDS);

                    if (fields.size() > 7) {
                        logger.warn("More fields than expected found within Drug Interaction info. Please, check:");
                        for (String field : fields) {
                            logger.warn("{}", field);
                        }
                    }

                    for (List<String> drugInfoList : (List<List<String>>) ((Map) metaMap.get(DRUG_INTERACTION_DATA)).get(ROWS)) {
                        Drug drug = new Drug();
                        int idx = fields.indexOf(THERAPEUTIC_CONTEXT);
                        if (idx > -1) {
                            drug.setTherapeuticContext(drugInfoList.get(idx));
                        }
                        idx = fields.indexOf(PATHWAY);
                        if (idx > -1) {
                            drug.setPathway(drugInfoList.get(idx));
                        }
                        idx = fields.indexOf(EFFECT);
                        if (idx > -1) {
                            drug.setEffect(drugInfoList.get(idx));
                        }
                        idx = fields.indexOf(ASSOCIATION);
                        if (idx > -1) {
                            drug.setAssociation(drugInfoList.get(idx));
                        }
                        idx = fields.indexOf(STATUS);
                        if (idx > -1) {
                            drug.setStatus(drugInfoList.get(idx));
                        }
                        idx = fields.indexOf(EVIDENCE);
                        if (idx > -1) {
                            drug.setEvidence(drugInfoList.get(idx));
                        }
                        idx = fields.indexOf(SOURCE);
                        if (idx > -1) {
                            drug.setBibliography(Collections.singletonList(PMID + drugInfoList.get(idx)));
                        }
                        drugList.add(drug);
                    }
                } else {
                    logger.warn("Meta field found but no drug interaction data");
                    logger.warn("Variant: {}:{}:{}:{}", map.get("chromosome"), map.get("start"), map.get("reference"),
                            map.get("alternate"));
                }

            }
        }

        VariantAnnotation variantAnnotation = new VariantAnnotation();
        variantAnnotation.setDrugs(drugList);
        variantAnnotation.setTraitAssociation(evidenceEntryMap
                .keySet()
                .stream()
                .map((diseaseName) -> evidenceEntryMap.get(diseaseName))
                .collect(Collectors.toList()));

        return variantAnnotation;

    }

    private List<GenomicFeature> getGenomicFeature(Map<String, Object> map) {
        List<GenomicFeature> genomicFeatureList = new ArrayList<>();
        if (map.containsKey(GENE)) {
            genomicFeatureList.add(createGeneGenomicFeature((String) map.get(GENE)));
        }
        if (map.containsKey(TRANSCRIPT)) {
            String symbol = (String) ((Map) map.get(TRANSCRIPT)).get(NAME);
            if (symbol.startsWith(ENST)) {
                genomicFeatureList.add(new GenomicFeature(FeatureTypes.transcript, symbol, null));
            } else {
                Map<String, String> transcriptMap = new HashMap<>(1);
                map.put(SYMBOL, gene);
                genomicFeatureList.add(new GenomicFeature(FeatureTypes.transcript, null, transcriptMap));
            }
        }
        return genomicFeatureList;
    }
}
