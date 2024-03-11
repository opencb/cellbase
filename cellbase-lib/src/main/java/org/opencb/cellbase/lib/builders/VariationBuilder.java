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

import org.opencb.biodata.models.core.Snp;
import org.opencb.biodata.models.core.SnpAnnotation;
import org.opencb.biodata.models.variant.avro.PopulationFrequency;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.opencb.commons.utils.FileUtils;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by imedina on 06/11/15.
 */
public class VariationBuilder extends CellBaseBuilder {

    private Path dbSnpFilePath;

    private static final int CHUNK_SIZE = 1000;
    private static final int DECIMAL_RESOLUTION = 100;

    public VariationBuilder(Path dbSnpFilePath, CellBaseSerializer serializer) {
        super(serializer);
        this.dbSnpFilePath = dbSnpFilePath;

        logger = LoggerFactory.getLogger(VariationBuilder.class);
    }

    /* Example:
        ## dbSNP 156
        #CHROM          POS     ID              REF     ALT     QUAL    FILTER  INFO
        NC_000001.11    926003  rs1329301928    C       A,T     .       .       RS=1329301928;dbSNPBuildID=151;SSR=0;
            GENEINFO=SAMD11:148398|LOC107985728:107985728;VC=SNV;NSM;R5;GNO;
            FREQ=Estonian:0.9998,0.0002232,.|TOMMO:0.9999,.,0.0001062|dbGaP_PopFreq:0.9999,5.4e-05,0;
            CLNVI=.,.,;CLNORIGIN=.,.,1;CLNSIG=.,.,0;CLNDISDB=.,.,MedGen:CN517202;CLNDN=.,.,not_provided;CLNREVSTAT=.,.,single;
            CLNACC=.,.,RCV001929748.1;CLNHGVS=NC_000001.11:g.926003=,NC_000001.11:g.926003C>A,NC_000001.11:g.926003C>T
        NC_000001.11    925952  rs1640863258    G       A       .       .       RS=1640863258;SSR=0;
            GENEINFO=SAMD11:148398|LOC107985728:107985728;VC=SNV;NSM;R5;CLNVI=.,;CLNORIGIN=.,1;CLNSIG=.,0;CLNDISDB=.,MedGen:CN517202;
            CLNDN=.,not_provided;CLNREVSTAT=.,single;CLNACC=.,RCV001318826.4;CLNHGVS=NC_000001.11:g.925952=,NC_000001.11:g.925952G>A
        NC_000001.11    925953  rs1349221494    G       A,T     .       .       RS=1349221494;dbSNPBuildID=151;SSR=0;
            GENEINFO=SAMD11:148398|LOC107985728:107985728;VC=SNV;SYN;R5;GNO;
            FREQ=GnomAD:1,1.426e-05,.|GnomAD_exomes:1,.,4.008e-06|TOPMED:1,3.778e-06,.|dbGaP_PopFreq:1,0,3.124e-05
        NC_000001.11    925956  rs1342334044    C       T       .       .       RS=1342334044;dbSNPBuildID=155;SSR=0;
            GENEINFO=SAMD11:148398|LOC107985728:107985728;VC=SNV;SYN;R5;GNO;
            FREQ=TOPMED:1,1.133e-05|dbGaP_PopFreq:1,0;
            CLNVI=.,;CLNORIGIN=.,1;CLNSIG=.,3;CLNDISDB=.,MedGen:CN517202;CLNDN=.,not_provided;CLNREVSTAT=.,single;CLNACC=.,RCV002170030.3;
            CLNHGVS=NC_000001.11:g.925956=,NC_000001.11:g.925956C>T
    */
    @Override
    public void parse() throws Exception {
        FileUtils.checkPath(dbSnpFilePath);

        BufferedReader bufferedReader = FileUtils.newBufferedReader(dbSnpFilePath);

        String line;
        String[] fields;

        String currentChromosome = null;
        String chromosome = null;
        int position;
        String id;
        String ref;
        String[] alt;
        String info;
        Map<String, String> infoMap;
        List<String> flags;

        while ((line = bufferedReader.readLine()) != null) {
            if (!line.startsWith("#")) {
                fields = line.split("\t");

                // this only happens the first time, when we start reading the file
                if (chromosome == null) {
                    logger.info("Parsing chr {} ", fields[0]);
                    currentChromosome = fields[0];
                    chromosome = fields[0].split("\\.")[0];
                }

                position = Integer.parseInt(fields[1]);
                id = fields[2];
                ref = fields[3];
                alt = fields[4].split(",");
                info = fields[7];

                String[] infoFields = info.split(";");
                flags = new ArrayList<>();
                SnpAnnotation snpAnnotation = new SnpAnnotation();
                for (String infoField : infoFields) {
                    String[] infoKeyValue = infoField.split("=");
                    switch (infoKeyValue[0]) {
                        case "GENEINFO":
                            snpAnnotation.setGene(infoKeyValue[1].split(":")[0]);
                            break;
                        case "FREQ":
                            String[] studies = infoKeyValue[1].split("\\|");
                            List<PopulationFrequency> populationFrequencies = new ArrayList<>();
                            for (String study : studies) {
                                String[] freqFields = study.split("[:,]");
                                for (int i = 0; i < alt.length; i++) {
                                    if (!freqFields[i + 2].equals(".")) {
                                        PopulationFrequency populationFrequency = new PopulationFrequency();
                                        populationFrequency.setStudy(freqFields[0]);
                                        System.out.println(freqFields[i + 1]);
                                        populationFrequency.setRefAlleleFreq(Float.parseFloat(freqFields[i + 1]));
                                        // freqFields[1] is the 'ref' allele ALT freq
                                        populationFrequency.setAltAllele(alt[i]);
                                        populationFrequency.setAltAlleleFreq(Float.parseFloat(freqFields[i + 2]));
                                        populationFrequencies.add(populationFrequency);
                                    }
                                }
                            }
                            snpAnnotation.setPopulationFrequencies(populationFrequencies);
                            break;
                        default:
                            if (infoKeyValue.length == 1) {
                                flags.add(infoKeyValue[0]);
                            }
                    }
                }
                snpAnnotation.setFlags(flags);

                if (!currentChromosome.equals(fields[0])) {
                    logger.info("Parsing chr {} ", fields[0]);

                }

                Snp snp = new Snp(id, chromosome, position, ref, Arrays.asList(alt), "SNV", "dbSNP", "156", snpAnnotation);
                serializer.serialize(snp);
            }
        }

        serializer.close();
        bufferedReader.close();
        logger.info("Parsing finished.");
    }
}
