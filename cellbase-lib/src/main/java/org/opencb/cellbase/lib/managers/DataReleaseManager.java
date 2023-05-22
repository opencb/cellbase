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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.core.common.GitRepositoryState;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.models.DataRelease;
import org.opencb.cellbase.core.models.DataReleaseSource;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.CellBaseDBAdaptor;
import org.opencb.cellbase.lib.impl.core.ReleaseMongoDBAdaptor;

import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class DataReleaseManager extends AbstractManager {
    private ReleaseMongoDBAdaptor releaseDBAdaptor;

    public DataReleaseManager(String databaseName, CellBaseConfiguration configuration) throws CellBaseException {
        super(databaseName, configuration);

        init();
    }

    public DataReleaseManager(String species, String assembly, CellBaseConfiguration configuration) throws CellBaseException {
        super(species, assembly, configuration);

        init();
    }

    private void init() {
        releaseDBAdaptor = dbAdaptorFactory.getReleaseDBAdaptor();
    }

    public CellBaseDataResult<DataRelease> getReleases() {
        return releaseDBAdaptor.getAll();
    }

    public DataRelease createRelease() throws JsonProcessingException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        // If collection release does not exist, it has to be created from zero (release 1), otherwise the biggest release
        // will be used to increment the release number and then create the new document release with the current date
        DataRelease lastRelease = null;
        CellBaseDataResult<DataRelease> releaseResult = getReleases();
        if (CollectionUtils.isNotEmpty(releaseResult.getResults())) {
            for (DataRelease dataRelease : releaseResult.getResults()) {
                if (lastRelease == null || dataRelease.getRelease() > lastRelease.getRelease()) {
                    lastRelease = dataRelease;
                }
            }
        }

        // Is it the first release?
        if (lastRelease == null) {
            // Create the first release, collections and sources are empty
            lastRelease = new DataRelease()
                    .setRelease(1)
                    .setDate(sdf.format(new Date()));
            releaseDBAdaptor.insert(lastRelease);
        } else {
            // Check if collections are empty, a new data release will be created only if collections is not empty
            if (MapUtils.isNotEmpty(lastRelease.getCollections())) {
                // Increment the release number, only if the previous release has collections
                lastRelease.setRelease(lastRelease.getRelease() + 1)
                        .setActiveByDefaultIn(new ArrayList<>())
                        .setDate(sdf.format(new Date()));
                // Write it to the database
                releaseDBAdaptor.insert(lastRelease);
            } else {
                lastRelease = null;
            }
        }
        return lastRelease;
    }

    public DataRelease get(int release) throws CellBaseException {
        CellBaseDataResult<DataRelease> result = releaseDBAdaptor.getAll();
        if (CollectionUtils.isNotEmpty(result.getResults())) {
            for (DataRelease dataRelease : result.getResults()) {
                if (dataRelease.getRelease() == release) {
                    return dataRelease;
                }
            }
        }
        throw new CellBaseException("Data release '" + release + "' does not exist.");
    }

    public DataRelease getDefault(String cellBaseVersion) throws CellBaseException {
        CellBaseDataResult<DataRelease> result = releaseDBAdaptor.getAll();
        if (CollectionUtils.isNotEmpty(result.getResults())) {
            for (DataRelease dataRelease : result.getResults()) {
                if (dataRelease.getActiveByDefaultIn().contains(cellBaseVersion)) {
                    return dataRelease;
                }
            }
        }
        throw new CellBaseException("No data release found for CellBase " + cellBaseVersion);
    }

    public DataRelease update(int release, List<String> versions) throws CellBaseException {
        return releaseDBAdaptor.update(release, versions).first();
    }

    public DataRelease update(int release, String collection, String data, List<Path> dataSourcePaths)
            throws CellBaseException {
        DataRelease currDataRelease = get(release);
        if (currDataRelease != null) {
            // Update collections
            currDataRelease.getCollections().put(collection, CellBaseDBAdaptor.buildCollectionName(collection, release));

            // Check sources
            if (StringUtils.isNotEmpty(data) && CollectionUtils.isNotEmpty(dataSourcePaths)) {
                List<DataReleaseSource> newSources = new ArrayList<>();

                // First, add new data sources
                Set<String> sourceSet = new HashSet<>();
                ObjectMapper jsonObjectMapper = new ObjectMapper();
                ObjectReader jsonObjectReader = jsonObjectMapper.readerFor(DataReleaseSource.class);
                for (Path dataSourcePath : dataSourcePaths) {
                    if (dataSourcePath.toFile().exists()) {
                        try {
                            DataReleaseSource dataReleaseSource = jsonObjectReader.readValue(dataSourcePath.toFile());
                            newSources.add(dataReleaseSource);
                            sourceSet.add(dataReleaseSource.getData() + "__" + dataReleaseSource.getName());
                        } catch (IOException e) {
                            logger.warn("Something wrong happened when reading data release source " + dataSourcePath + ". "
                                    + e.getMessage());
                        }
                    }
                }

                // Second, add previous data sources if necessary (to avoid duplicated sources)
                for (DataReleaseSource source : currDataRelease.getSources()) {
                    String key = source.getData() + "__" + source.getName();
                    if (!sourceSet.contains(key)) {
                        newSources.add(source);
                    }
                }

                if (CollectionUtils.isNotEmpty(newSources)) {
                    currDataRelease.setSources(newSources);
                }
            }

            // Update data release in the database
            update(currDataRelease);

            return currDataRelease;
        }
        throw new CellBaseException("Data release '" + release + "' does not exist.");
    }

    public void update(DataRelease dataRelase) {
        if (MapUtils.isNotEmpty(dataRelase.getCollections())) {
            releaseDBAdaptor.update(dataRelase.getRelease(), "collections", dataRelase.getCollections());
        }

        if (CollectionUtils.isNotEmpty(dataRelase.getSources())) {
            // TODO: use native functions
            List<Map<String, Object>> tmp = new ArrayList<>();
            for (DataReleaseSource source : dataRelase.getSources()) {
                Map<String, Object> map = new HashMap<>();
                if (StringUtils.isNotEmpty(source.getData())) {
                    map.put("data", source.getData());
                }
                if (StringUtils.isNotEmpty(source.getName())) {
                    map.put("name", source.getName());
                }
                if (StringUtils.isNotEmpty(source.getVersion())) {
                    map.put("version", source.getVersion());
                }
                if (CollectionUtils.isNotEmpty(source.getUrl())) {
                    map.put("url", source.getUrl());
                }
                if (StringUtils.isNotEmpty(source.getDate())) {
                    map.put("date", source.getDate());
                }
                tmp.add(map);
            }
            releaseDBAdaptor.update(dataRelase.getRelease(), "sources", tmp);
        }
    }

    public String getMaintenanceFlagFile() {
        return configuration.getMaintenanceFlagFile();
    }

    public String getMaintainerContact() {
        return configuration.getMaintainerContact();
    }

    public int checkDataRelease(int inRelease) throws CellBaseException {
        int outRelease = inRelease;
        if (outRelease < 0) {
            throw new CellBaseException("Invalid data release " + outRelease + ". Data release must be greater or equal to 0");
        }
        if (outRelease == 0) {
            String[] split = GitRepositoryState.get().getBuildVersion().split("[.-]");
            String version = "v" + split[0] + "." + split[1];
            outRelease = getDefault(version).getRelease();
            logger.info("Using data release 0: it means to take default data release '" + outRelease + "' for CellBase version '"
                    + version + "'");
            return outRelease;
        }

        List<DataRelease> dataReleases = getReleases().getResults();
        for (DataRelease dataRelease : dataReleases) {
            if (outRelease == dataRelease.getRelease()) {
                return outRelease;
            }
        }

        throw new CellBaseException("Invalid data release " + outRelease + ". Valid data releases are: "
                + StringUtils.join(dataReleases.stream().map(dr -> dr.getRelease()).collect(Collectors.toList()), ",") + ". And use 0 to"
                + " use the default data release.");
    }
}
