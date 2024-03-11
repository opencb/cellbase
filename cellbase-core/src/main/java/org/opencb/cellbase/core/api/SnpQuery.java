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

public class SnpQuery extends AbstractQuery {

    @QueryParameter(id = "id")
    private List<String> ids;
    @QueryParameter(id = "chromosome")
    private String chromosome;
    @QueryParameter(id = "position")
    private String position;
    @QueryParameter(id = "reference")
    private String reference;

    public SnpQuery() {
    }

    public SnpQuery(Map<String, String> params) throws QueryException {
        super(params);
    }

    @Override
    protected void validateQuery() {
        // nothing to validate
        return;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SnpQuery{");
        sb.append("ids=").append(ids);
        sb.append(", chromosome='").append(chromosome).append('\'');
        sb.append(", position='").append(position).append('\'');
        sb.append(", reference='").append(reference).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public List<String> getIds() {
        return ids;
    }

    public SnpQuery setIds(List<String> ids) {
        this.ids = ids;
        return this;
    }

    public String getChromosome() {
        return chromosome;
    }

    public SnpQuery setChromosome(String chromosome) {
        this.chromosome = chromosome;
        return this;
    }

    public String getPosition() {
        return position;
    }

    public SnpQuery setPosition(String position) {
        this.position = position;
        return this;
    }

    public String getReference() {
        return reference;
    }

    public SnpQuery setReference(String reference) {
        this.reference = reference;
        return this;
    }
}
