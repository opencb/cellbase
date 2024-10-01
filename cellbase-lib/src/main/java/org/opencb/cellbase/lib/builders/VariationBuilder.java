/*
 * Copyright 2015-2020 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.lib.builders;

import org.apache.commons.collections4.MapUtils;
import org.opencb.biodata.formats.variant.io.VariantReader;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantFileMetadata;
import org.opencb.biodata.models.variant.avro.AdditionalAttribute;
import org.opencb.biodata.models.variant.avro.VariantAnnotation;
import org.opencb.biodata.models.variant.avro.Xref;
import org.opencb.biodata.models.variant.metadata.VariantStudyMetadata;
import org.opencb.biodata.tools.variant.VariantNormalizer;
import org.opencb.biodata.tools.variant.VariantVcfHtsjdkReader;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.config.DownloadProperties;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.opencb.cellbase.lib.EtlCommons.DBSNP_DATA;
import static org.opencb.cellbase.lib.EtlCommons.HOMO_SAPIENS;

/**
 * Created by jtarraga on 01/08/24.
 */
public class VariationBuilder extends AbstractBuilder {

    private Path downloadPath;
    private String species;

    private DbSnpBuilder dbSnpBuilder;

    public static final String VARIATION_CHR_PREFIX = "variation_chr";
    public static final String VCF_ID_KEY = "VCF_ID";
    public static final String EVA_PREFIX = "EVA_";
    public static final String RS_PREFIX = "rs";

    private static final String VARIANTS_PARSED_LOG_MESSAGE = "{} variants parsed";

    public static final Map<String, String> SV_VALUES_MAP;

    static {
        Map<String, String> tempMap = new HashMap<>();
        tempMap.put("<complex_structural_alteration>", "<CS>");
        tempMap.put("<copy_number_loss>", "<CNL>");
        tempMap.put("<copy_number_gain>", "<CNG>");
        tempMap.put("<copy_number_variation>", "<CNV>");
        tempMap.put("<deletion>", "<DEL>");
        tempMap.put("<duplication>", "<DUP>");
        tempMap.put("<insertion>", "<INS>");
        tempMap.put("<inversion>", "<INV>");
        tempMap.put("<mobile_element_insertion>", "<INS:ME>");
        tempMap.put("<tandem_duplication>", "<DUP:TANDEM>");
        SV_VALUES_MAP = Collections.unmodifiableMap(tempMap);
    }

    public VariationBuilder(Path downloadPath, String species, CellBaseFileSerializer fileSerializer, CellBaseConfiguration configuration) {
        super(fileSerializer);

        this.downloadPath = downloadPath;
        this.species = species;

        // dbSNP
        DownloadProperties.URLProperties dbSnpUrlProperties = configuration.getDownload().getDbSNP();
        dbSnpBuilder = new DbSnpBuilder(downloadPath.resolve(DBSNP_DATA), dbSnpUrlProperties, fileSerializer);
    }

    @Override
    public void parse() throws Exception {
        if (species.equalsIgnoreCase(HOMO_SAPIENS)) {
            // Parsing dbSNP data
            dbSnpBuilder.parse();
        } else {
            // Parsing VCF files
            parseVcf();
        }
    }

    private void parseVcf() throws IOException {
        VariantNormalizer.VariantNormalizerConfig normalizerConfig = new VariantNormalizer.VariantNormalizerConfig()
                .setReuseVariants(true)
                .setNormalizeAlleles(true)
                .setDecomposeMNVs(false);

        CellBaseJsonFileSerializer fileSerializer = (CellBaseJsonFileSerializer) this.serializer;

        // Usually we expect two VCF files prefixed by the species scientific name
        // e.g., for 'Mus musculus' the VCF files are 'mus_musculus.vcf.gz' and 'mus_musculus_structural_variations.vcf.gz'
        String prefix = species.toLowerCase(Locale.ROOT).replace(" ", "_");

        try (DirectoryStream<Path> vcfPaths = Files.newDirectoryStream(downloadPath,
                entry -> entry.getFileName().toString().startsWith(prefix))) {
            for (Path vcfPath : vcfPaths) {
                logger.info(PARSING_LOG_MESSAGE, vcfPath);

                VariantStudyMetadata variantStudyMetadata = new VariantFileMetadata(vcfPath.getFileName().toString(),
                        vcfPath.toAbsolutePath().toString()).toVariantStudyMetadata("");
                VariantReader variantVcfReader = new VariantVcfHtsjdkReader(vcfPath, variantStudyMetadata,
                        new VariantNormalizer(normalizerConfig));

                // Write variant to the JSON files according to the chromosome
                int count = 0;
                Iterator<Variant> iterator = variantVcfReader.iterator();
                while (iterator.hasNext()) {
                    Variant variant = iterator.next();
                    // Convert alternate for structural variants
                    if (SV_VALUES_MAP.containsKey(variant.getAlternate())) {
                        variant.setAlternate(SV_VALUES_MAP.get(variant.getAlternate()));
                    }
                    // Set variant ID (after converting the alternate)
                    variant.setId(variant.toString());
                    // Set variant annotation: chrom, start, end, ref, alt, xrefs and additional attributes
                    VariantAnnotation variantAnnotation = new VariantAnnotation();
                    variantAnnotation.setChromosome(variant.getChromosome());
                    variantAnnotation.setStart(variant.getStart());
                    variantAnnotation.setEnd(variant.getEnd());
                    variantAnnotation.setReference(variant.getReference());
                    variantAnnotation.setAlternate(variant.getAlternate());
                    try {
                        Xref xref = null;
                        Map<String, String> attributes = new HashMap<>();
                        Map<String, String> data = variant.getStudies().get(0).getFiles().get(0).getData();
                        for (Map.Entry<String, String> entry : data.entrySet()) {
                            if (entry.getKey().startsWith(EVA_PREFIX)) {
                                if (xref == null && data.containsKey(VCF_ID_KEY) && data.get(VCF_ID_KEY).startsWith(RS_PREFIX)) {
                                    xref = new Xref(data.get(VCF_ID_KEY), entry.getKey());
                                }
                            } else if (!entry.getKey().equals(VCF_ID_KEY)) {
                                attributes.put(entry.getKey(), entry.getValue());
                            }
                        }
                        if (xref != null) {
                            variantAnnotation.setXrefs(Collections.singletonList(xref));
                        }
                        if (MapUtils.isNotEmpty(attributes)) {
                            AdditionalAttribute additionalAttribute = new AdditionalAttribute(attributes);
                            Map<String, AdditionalAttribute> additionalAttributeMap = new HashMap<>();
                            additionalAttributeMap.put(vcfPath.getFileName().toString(), additionalAttribute);
                            variantAnnotation.setAdditionalAttributes(additionalAttributeMap);
                        }
                    } catch (Exception e) {
                        logger.warn("Error setting annotation for variant {}: {}", variant.getId(), Arrays.toString(e.getStackTrace()));
                    }
                    if (variantAnnotation != null) {
                        variant.setAnnotation(variantAnnotation);
                    }
                    variant.setAnnotation(variantAnnotation);

                    // Remove study info
                    variant.setStudies(null);

                    // Serialize
                    fileSerializer.serialize(variant, VARIATION_CHR_PREFIX + variant.getChromosome());
                    if (++count % 1000000 == 0) {
                        logger.info(VARIANTS_PARSED_LOG_MESSAGE, count);
                    }
                }
                variantVcfReader.close();

                logger.info(VARIANTS_PARSED_LOG_MESSAGE, count);
                logger.info(PARSING_DONE_LOG_MESSAGE);
            }
        }

        fileSerializer.close();
    }
}
