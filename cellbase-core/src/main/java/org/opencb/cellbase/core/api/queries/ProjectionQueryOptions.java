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

    List<String> excludes;
    List<String> includes;

    public ProjectionQueryOptions() {
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public ProjectionQueryOptions setExcludes(List<String> excludes) {
        this.excludes = excludes;
        return this;
    }

    public ProjectionQueryOptions addExcludes(String excludes) {
        if (this.excludes == null) {
            this.excludes = new ArrayList<>();
        }
        this.excludes.addAll(Arrays.asList(excludes.split(",")));
        return this;
    }

    public List<String> getIncludes() {
        return includes;
    }

    public ProjectionQueryOptions setIncludes(List<String> includes) {
        this.includes = includes;
        return this;
    }

    @Override
    public String toString() {
        return "ProjectionQueryOptions{" +
                "excludes=" + excludes +
                ", includes=" + includes +
                '}';
    }
}
