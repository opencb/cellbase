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

import org.opencb.cellbase.core.api.key.ApiKeyJwtPayload;
import org.opencb.cellbase.core.api.key.ApiKeyStats;
import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.lib.impl.core.MetaMongoDBAdaptor;
import org.opencb.cellbase.lib.iterator.CellBaseIterator;
import org.opencb.commons.monitor.DatastoreStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaManager extends AbstractManager {

    private final MetaMongoDBAdaptor metaDBAdaptor;

    public MetaManager(CellBaseConfiguration configuration) throws CellBaseException {
        super("Homo sapiens", null, configuration);
        this.metaDBAdaptor = dbAdaptorFactory.getMetaDBAdaptor();
    }

    @Deprecated
    public CellBaseDataResult getVersions(String species, String assembly) {

        return metaDBAdaptor.getAll();
    }

    public CellBaseDataResult getVersions() {

        return metaDBAdaptor.getAll();
    }

    public String getMaintenanceFlagFile() {
        return configuration.getMaintenanceFlagFile();
    }

    public String getMaintainerContact() {
        return configuration.getMaintainerContact();
    }

    public Map<String, DatastoreStatus> getDatabaseStatus(String species, String assembly) {
        return this.mongoDBManager.getDatabaseStatus(species, assembly);
    }

    public CellBaseDataResult<ApiKeyStats> getApiKeyStats(String apiKey, String date) {

        return metaDBAdaptor.getApiKeyStats(apiKey, date);
    }

    public CellBaseDataResult<ApiKeyStats> getApiKeyStats(List<String> apiKeys, String startDate, String endDate) {

        return metaDBAdaptor.getApiKeyStats(apiKeys, startDate, endDate);
    }

    public CellBaseDataResult<ApiKeyStats> getApiKeys() {
        long dbTimeStart = System.currentTimeMillis();

        CellBaseIterator<ApiKeyStats> iterator = metaDBAdaptor.apiKeyStatsIterator();
        System.out.println("API Key Stats:");
        Map<String, ApiKeyStats> apiKeyStatsMap = new HashMap<>();
        while (iterator.hasNext()) {
            ApiKeyStats apiKeyStats = iterator.next();
            if (!apiKeyStatsMap.containsKey(apiKeyStats.getApiKey())) {
                apiKeyStatsMap.put(apiKeyStats.getApiKey(), apiKeyStats);
            } else {
                ApiKeyStats updatedApiKey = apiKeyStatsMap.get(apiKeyStats.getApiKey());
                updatedApiKey.setNumAnnotatedVariants(updatedApiKey.getNumAnnotatedVariants() + apiKeyStats.getNumAnnotatedVariants());
                updatedApiKey.setNumQueries(updatedApiKey.getNumQueries() + apiKeyStats.getNumQueries());
                updatedApiKey.setDuration(updatedApiKey.getDuration() + apiKeyStats.getDuration());
                updatedApiKey.setOutputBytes(updatedApiKey.getOutputBytes() + apiKeyStats.getOutputBytes());
            }
        }

        // Return the aggregated results
        int dbTime = (int) (System.currentTimeMillis() - dbTimeStart);
        return new CellBaseDataResult<>("ApiKeys", dbTime, null, apiKeyStatsMap.size(), new ArrayList<>(apiKeyStatsMap.values()),
                apiKeyStatsMap.size());
    }

    public void checkQuota(String apiKey, ApiKeyJwtPayload payload) throws CellBaseException {
        String date = getApiKeyStatsDate();


        CellBaseDataResult<ApiKeyStats> quotaResult = metaDBAdaptor.getApiKeyStats(apiKey, date);

        // If no stats exist yet for this period, treat all counters as 0 (incApiKeyStats will upsert on first write)
        long numQueries = (quotaResult.getNumResults() == 0) ? 0 : quotaResult.first().getNumQueries();
        long numAnnotatedVariants = (quotaResult.getNumResults() == 0) ? 0 : quotaResult.first().getNumAnnotatedVariants();
        long outputBytes = (quotaResult.getNumResults() == 0) ? 0 : quotaResult.first().getOutputBytes();
        if (numQueries >= payload.getQuota().getMaxNumQueries()) {
            throw new CellBaseException("Maximum query limit reached: Your current API key has a quota of "
                    + payload.getQuota().getMaxNumQueries() + " queries; currently " + numQueries + " queries");
        }
        if (payload.getQuota().getMaxNumAnnotatedVariants() > 0
                && numAnnotatedVariants >= payload.getQuota().getMaxNumAnnotatedVariants()) {
            throw new CellBaseException("Maximum annotated variants limit reached: Your current API key has a quota of "
                    + payload.getQuota().getMaxNumAnnotatedVariants() + " annotated variants; currently " + numAnnotatedVariants
                    + " annotated variants");
        }
        if (payload.getQuota().getMaxOutputBytes() > 0 && outputBytes >= payload.getQuota().getMaxOutputBytes()) {
            throw new CellBaseException("Maximum output bytes limit reached: Your current API key has a quota of "
                    + payload.getQuota().getMaxOutputBytes() + " bytes; currently used " + outputBytes + " bytes");
        }
    }

    public CellBaseDataResult incApiKeyStats(String apiKey, long incNumQueries, long incNumAnnotatedVariants, long incDuration,
                                             long incOutputBytes) {
        String date = getApiKeyStatsDate();


        return metaDBAdaptor.incApiKeyStats(apiKey, date, incNumQueries, incNumAnnotatedVariants, incDuration, incOutputBytes);
    }

    private String getApiKeyStatsDate() {
        // Get the current year and month as yyyymm, e.g.:202309
        LocalDate currentDate = LocalDate.now();
        return currentDate.getYear() + String.format("%02d", currentDate.getMonthValue());
    }
}
