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

package org.opencb.cellbase.core.api;

import org.opencb.cellbase.core.api.query.AbstractQuery;
import org.opencb.cellbase.core.api.query.QueryException;
import org.opencb.cellbase.core.api.query.QueryParameter;

import java.util.List;
import java.util.Map;

public class PolygenicScoreQuery extends AbstractQuery {

    @QueryParameter(id = "id")
    private List<String> ids;

    @QueryParameter(id = "name")
    private List<String> names;

    @QueryParameter(id = "source", allowedValues = {"PGS Catalog"})
    private List<String> sources;

    public PolygenicScoreQuery() {
    }

    public PolygenicScoreQuery(Map<String, String> params) throws QueryException {
        super(params);

        objectMapper.readerForUpdating(this);
        objectMapper.readerFor(PolygenicScoreQuery.class);
        objectWriter = objectMapper.writerFor(PolygenicScoreQuery.class);
    }

    @Override
    protected void validateQuery() throws QueryException {
        // Nothing to to
        return;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PolygenicScoreQuery{");
        sb.append("ids=").append(ids);
        sb.append(", names=").append(names);
        sb.append(", sources=").append(sources);
        sb.append(", limit=").append(limit);
        sb.append(", skip=").append(skip);
        sb.append(", count=").append(count);
        sb.append(", sort='").append(sort).append('\'');
        sb.append(", order=").append(order);
        sb.append(", facet='").append(facet).append('\'');
        sb.append(", includes=").append(includes);
        sb.append(", excludes=").append(excludes);
        sb.append('}');
        return sb.toString();
    }

    public List<String> getIds() {
        return ids;
    }

    public PolygenicScoreQuery setIds(List<String> ids) {
        this.ids = ids;
        return this;
    }

    public List<String> getNames() {
        return names;
    }

    public PolygenicScoreQuery setNames(List<String> names) {
        this.names = names;
        return this;
    }

    public List<String> getSources() {
        return sources;
    }

    public PolygenicScoreQuery setSources(List<String> sources) {
        this.sources = sources;
        return this;
    }
}
