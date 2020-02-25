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

package org.opencb.cellbase.core.api.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProjectionQueryOptions {

    protected List<String> includes;
    protected List<String> excludes;

    public ProjectionQueryOptions() {
    }

    public ProjectionQueryOptions(List<String> includes, List<String> excludes) {
        this.includes = includes;
        this.excludes = excludes;
    }

    public ProjectionQueryOptions addExcludes(String excludes) {
        if (this.excludes == null) {
            this.excludes = new ArrayList<>();
        }
        this.excludes.addAll(Arrays.asList(excludes.split(",")));
        return this;
    }

    public ProjectionQueryOptions addExcludes(List<String> excludes) {
        if (this.excludes == null) {
            this.excludes = new ArrayList<>();
        }
        this.excludes.addAll(excludes);
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProjectionQueryOptions{");
        sb.append("includes=").append(includes);
        sb.append(", excludes=").append(excludes);
        sb.append('}');
        return sb.toString();
    }

    public List<String> getIncludes() {
        return includes;
    }

    public ProjectionQueryOptions setIncludes(List<String> includes) {
        this.includes = includes;
        return this;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public ProjectionQueryOptions setExcludes(List<String> excludes) {
        this.excludes = excludes;
        return this;
    }
}
