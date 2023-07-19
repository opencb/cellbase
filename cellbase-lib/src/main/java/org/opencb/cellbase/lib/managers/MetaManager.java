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

import org.opencb.cellbase.core.config.CellBaseConfiguration;
import org.opencb.cellbase.core.exception.CellBaseException;
import org.opencb.cellbase.core.result.CellBaseDataResult;
import org.opencb.cellbase.core.token.Quota;
import org.opencb.cellbase.lib.impl.core.MetaMongoDBAdaptor;
import org.opencb.commons.monitor.DatastoreStatus;

import java.time.LocalDate;
import java.util.Map;

@Deprecated
public class MetaManager extends AbstractManager {

    public MetaManager(CellBaseConfiguration configuration) throws CellBaseException {
        super("Homo sapiens", null, configuration);
    }

    @Deprecated
    public CellBaseDataResult getVersions(String species, String assembly) {
        MetaMongoDBAdaptor metaDBAdaptor = dbAdaptorFactory.getMetaDBAdaptor();
        return metaDBAdaptor.getAll();
    }

    public CellBaseDataResult getVersions() {
        MetaMongoDBAdaptor metaDBAdaptor = dbAdaptorFactory.getMetaDBAdaptor();
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

    public CellBaseDataResult<Quota> checkAndIncNumQueries(String token, long maxNumQueries) throws CellBaseException {
        long numQueries;
        // Get the current year and month as yyyymm, e.g.:202309
        LocalDate currentDate = LocalDate.now();
        String date = currentDate.getYear() + String.format("%02d", currentDate.getMonthValue());
        MetaMongoDBAdaptor metaDBAdaptor = dbAdaptorFactory.getMetaDBAdaptor();
        CellBaseDataResult<Quota> quotaResult = metaDBAdaptor.getQuota(token, date);
        System.out.println("quotaResult.numResutls = " + quotaResult.getNumResults());
        if (quotaResult.getNumResults() == 0) {
            metaDBAdaptor.initQuota(token, date);
            numQueries = 0;
        } else {
            numQueries = quotaResult.first().getNumQueries();
        }
        System.out.println("numQueries = " + numQueries + ", maxNumQueries = " + maxNumQueries + ", date = " + date + ", token = " + token);
        if (numQueries >= maxNumQueries) {
            throw new CellBaseException("Exceeded the maximum number of queries");
        }
        return metaDBAdaptor.update(token, date, numQueries + 1);
    }
}
