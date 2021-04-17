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

package org.opencb.cellbase.lib.managers;


import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.models.core.Region;
import org.opencb.biodata.tools.feature.BigWigManager;
import org.opencb.biodata.tools.sequence.FastaIndex;
import org.opencb.cellbase.core.api.FileQuery;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.commons.datastore.core.Event;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileManager extends AbstractManager {

    public FileManager(CellBaseConfiguration configuration) throws CellBaseException {
        super("Homo sapiens", configuration);
    }

    public List<CellBaseDataResult<String>> search(FileQuery query) throws IOException {
        Path dataFile = new File(query.getFilePath()).toPath();
        if (!Files.exists(dataFile)) {
            throw new FileNotFoundException("Can't file file: " + query.getFilePath());
        }
        switch(query.getFileType()) {
            case "fasta":
                return getFasta(dataFile, query);
//            case "bigwig":
//                return getBigWig(dataFile, query);
            default:
                throw new InvalidParameterException("File type not found:" + query.getFileType());
        }
    }

    private List<CellBaseDataResult<String>> getBigWig(Path dataFile, FileQuery query) throws IOException {
        BigWigManager bigWigManager = new BigWigManager(dataFile);
        List<CellBaseDataResult<String>> results = new ArrayList<>();
        List<Region> regions = query.getRegions();
        for (Region region : regions) {
            double[] coverage = bigWigManager.query(region);
            String id = region.toString();
            int time = 0;
            List<Event> events = new ArrayList<>();
            int numResults = 1;
            long numMatches = 1;
            CellBaseDataResult<String> result = new CellBaseDataResult(id, time, events, numResults, Collections.singletonList(coverage),
                    numMatches);
            results.add(result);
        }
        return results;
    }

    private List<CellBaseDataResult<String>> getFasta(Path dataFile, FileQuery query) throws IOException {
        FastaIndex fastaIndex = new FastaIndex(dataFile);
        List<CellBaseDataResult<String>> results = new ArrayList<>();
        List<Region> regions = query.getRegions();
        for (Region region : regions) {
            String sequence = fastaIndex.query(region.getChromosome(), region.getStart(), region.getEnd());
            String id = region.toString();
            int time = 0;
            List<Event> events = new ArrayList<>();
            int numResults = 1;
            long numMatches = 1;
            if (StringUtils.isEmpty(sequence)) {
                numResults = 0;
                numMatches = 0;
            }
            CellBaseDataResult<String> result = new CellBaseDataResult<>(id, time, events, numResults, Collections.singletonList(sequence),
                    numMatches);
            results.add(result);
        }
        return results;
    }
}
