/*
 * Copyright 2015 OpenCB
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

package org.opencb.cellbase.app.transform;

import org.opencb.cellbase.app.transform.utils.FileUtils;
import org.opencb.cellbase.core.common.GenomicPositionScore;
import org.opencb.cellbase.core.serializer.CellBaseSerializer;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by imedina on 06/11/15.
 */
public class CaddScoreParser extends CellBaseParser {

    private Path caddFilePath;

    private static int CHUNK_SIZE = 2000;

    public CaddScoreParser(Path caddFilePath, CellBaseSerializer serializer) {
        super(serializer);
        this.caddFilePath = caddFilePath;

        logger = LoggerFactory.getLogger(ConservationParser.class);
    }

    /* Example:
        ## CADD v1.3 (c) University of Washington and Hudson-Alpha Institute for Biotechnology 2013-2015. All rights reserved.
        #Chrom  Pos     Ref     Alt     RawScore        PHRED
        1       10001   T       A       0.337036        6.046
        1       10001   T       C       0.143254        4.073
        1       10001   T       G       0.202491        4.705
        1       10002   A       C       0.192576        4.601
        1       10002   A       G       0.178363        4.450
        1       10002   A       T       0.347401        6.143
    */
    @Override
    public void parse() throws Exception {
        FileUtils.checkPath(caddFilePath);

        BufferedReader bufferedReader = FileUtils.newBufferedReader(caddFilePath);

        List<Long> values = new ArrayList<>(CHUNK_SIZE);

        int start = 1;
        int end = 1999;
        int counter = 1;
        String line;
        String[] fields;
        short v1;
        long l = 0;
        int linecount = 1;
        int pos = 0;
        while((line = bufferedReader.readLine()) != null) {
                fields = line.split("\t");

            if ( linecount <= 3 ) {
                float a = Float.parseFloat(fields[4]);
                v1 = (short) (a * 10000);
                l |= (v1 << 16 * pos);
                pos ++;
            }
            else
            {
                linecount = 0;
                pos = 0;
                values.add(l);
                l = 0;
            }

            counter++;
            if (counter == CHUNK_SIZE ) {
                GenomicPositionScore genomicPositionScore = new GenomicPositionScore(fields[0], start, end, "Cadd", values);
                serializer.serialize(genomicPositionScore);
                start = end + 1;
                end += CHUNK_SIZE;

                counter = 0;
                values.clear();

            }
        bufferedReader.close();
    }
}
