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
                    .setActiveByDefault(true)
                    .setDate(sdf.format(new Date()));
            releaseDBAdaptor.insert(lastRelease);
        } else {
            // Check if collections are empty, a new data release will be created only if collections is not empty
            if (MapUtils.isNotEmpty(lastRelease.getCollections())) {
                // Increment the release number, only if the previous release has collections
                lastRelease.setRelease(lastRelease.getRelease() + 1)
                        .setActiveByDefault(false)
                        .setDate(sdf.format(new Date()));
                // Write it to the database
                releaseDBAdaptor.insert(lastRelease);
            } else {
                lastRelease = null;
            }
        }
        return lastRelease;
    }

    public DataRelease get(int release) {
        CellBaseDataResult<DataRelease> result = releaseDBAdaptor.getAll();
        if (CollectionUtils.isNotEmpty(result.getResults())) {
            for (DataRelease dataRelease : result.getResults()) {
                if (dataRelease.getRelease() == release) {
                    return dataRelease;
                }
            }
        }
        return null;
    }

    public DataRelease getDefault() {
        CellBaseDataResult<DataRelease> result = releaseDBAdaptor.getAll();
        if (CollectionUtils.isNotEmpty(result.getResults())) {
            for (DataRelease dataRelease : result.getResults()) {
                if (dataRelease.isActiveByDefault()) {
                    return dataRelease;
                }
            }
        }
        return null;
    }

    public void update(int release, String collection, String data, List<Path> dataSourcePaths) {
        DataRelease currDataRelease = get(release);
        if (currDataRelease != null) {
            // Update collections
            currDataRelease.getCollections().put(collection, CellBaseDBAdaptor.buildCollectionName(collection, release));

            // Check sources
            if (StringUtils.isNotEmpty(data) && CollectionUtils.isNotEmpty(dataSourcePaths)) {
                List<DataReleaseSource> newSources = new ArrayList<>();
                // First, remove previous sources for the data loaded
                if (CollectionUtils.isNotEmpty(currDataRelease.getSources())) {
                    newSources.addAll(currDataRelease.getSources());
                }
                // Second, add new sources
                ObjectMapper jsonObjectMapper = new ObjectMapper();
                ObjectReader jsonObjectReader = jsonObjectMapper.readerFor(DataReleaseSource.class);

                for (Path dataSourcePath : dataSourcePaths) {
                    if (dataSourcePath.toFile().exists()) {
                        try {
                            DataReleaseSource dataReleaseSource = jsonObjectReader.readValue(dataSourcePath.toFile());

                            boolean found = false;
                            for (DataReleaseSource source : currDataRelease.getSources()) {
                                if (StringUtils.isNotEmpty(dataReleaseSource.getData())
                                        && dataReleaseSource.getData().equals(source.getData())
                                        && StringUtils.isNotEmpty(dataReleaseSource.getName())
                                        && dataReleaseSource.getName().equals(source.getName())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                newSources.add(dataReleaseSource);
                            }
                        } catch (IOException e) {
                            logger.warn("Something wrong happened when reading data release source " + dataSourcePath + ". "
                                    + e.getMessage());
                        }
                    }
                }

                if (CollectionUtils.isNotEmpty(newSources)) {
                    currDataRelease.setSources(newSources);
                }
            }

            // Update data release in the database
            update(currDataRelease);
        }
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

    public DataRelease activeByDefault(int release) throws JsonProcessingException {
        // Gel all releases and check if the input release exists
        DataRelease prevActive = null;
        DataRelease newActive = null;
        CellBaseDataResult<DataRelease> releaseResult = getReleases();
        if (CollectionUtils.isEmpty(releaseResult.getResults())) {
            // Nothing to do, maybe exception or warning
            return null;
        }
        for (DataRelease dataRelease : releaseResult.getResults()) {
            if (dataRelease.isActiveByDefault()) {
                prevActive = dataRelease;
            } else if (dataRelease.getRelease() == release) {
                newActive = dataRelease;
            }
        }
        if (prevActive != null && newActive != null && newActive.getRelease() == prevActive.getRelease()) {
            // Nothing to do
            return newActive;
        }

        // Change active by default
        if (prevActive != null) {
            prevActive.setActiveByDefault(false);
            releaseDBAdaptor.update(prevActive.getRelease(), "activeByDefault", prevActive.isActiveByDefault());
        }
        if (newActive != null) {
            newActive.setActiveByDefault(true);
            releaseDBAdaptor.update(newActive.getRelease(), "activeByDefault", newActive.isActiveByDefault());
        }
        return newActive;
    }

    public String getMaintenanceFlagFile() {
        return configuration.getMaintenanceFlagFile();
    }

    public String getMaintainerContact() {
        return configuration.getMaintainerContact();
    }

}
