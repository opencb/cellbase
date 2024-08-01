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

import org.opencb.biodata.formats.variant.io.VariantReader;
import org.opencb.biodata.models.variant.Variant;
import org.opencb.biodata.models.variant.VariantFileMetadata;
import org.opencb.biodata.models.variant.metadata.VariantStudyMetadata;
import org.opencb.biodata.tools.variant.VariantNormalizer;
import org.opencb.biodata.tools.variant.VariantVcfHtsjdkReader;
import org.opencb.cellbase.core.serializer.CellBaseFileSerializer;
import org.opencb.cellbase.core.serializer.CellBaseJsonFileSerializer;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;

import static org.opencb.cellbase.lib.EtlCommons.ENSEMBL_DATA;
import static org.opencb.cellbase.lib.EtlCommons.HOMO_SAPIENS;

/**
 * Created by jtarraga on 01/08/24
 */
public class VariationBuilder extends AbstractBuilder {

    private Path downloadPath;
    private String species;

    public static final String VARIATION_CHR_PREFIX = "variation_chr";

    public VariationBuilder(Path downloadPath, String species, CellBaseFileSerializer fileSerializer) {
        super(fileSerializer);

        this.downloadPath = downloadPath;
        this.species = species;
    }

    @Override
    public void parse() throws IOException {
        if (!species.equalsIgnoreCase(HOMO_SAPIENS)) {
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
                VariantStudyMetadata variantStudyMetadata = new VariantFileMetadata(vcfPath.getFileName().toString(), "")
                        .toVariantStudyMetadata(ENSEMBL_DATA);
                VariantReader variantVcfReader = new VariantVcfHtsjdkReader(vcfPath, variantStudyMetadata,
                        new VariantNormalizer(normalizerConfig));

                // Write variant to the JSON files according to the chromosome
                Iterator<Variant> iterator = variantVcfReader.iterator();
                while (iterator.hasNext()) {
                    Variant variant = iterator.next();
                    fileSerializer.serialize(variant, VARIATION_CHR_PREFIX + variant.getChromosome());
                }
                variantVcfReader.close();
            }
        }

        fileSerializer.close();
    }
}
