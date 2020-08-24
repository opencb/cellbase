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
import java.util.List;

/**
 * An array list that also has an associate boolean. Used by AbstractQuery. If TRUE, then the objects in the list
 * will be added to the query with AND. By default, the values are added with an OR.
 *
 * @param <T> Type of object in list
 */
public class LogicalList<T> extends ArrayList<T> {
    private boolean and;

    public LogicalList() {}

    public LogicalList(List<T> defaultList) {
        this(defaultList, false);
    }

    public LogicalList(List<T> defaultList, boolean isAnd) {
        this.and = isAnd;
        this.addAll(defaultList);
    }

    public boolean isAnd() {
        return and;
    }

    public LogicalList<T> setAnd(boolean and) {
        this.and = and;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LogicalList{");
        sb.append("and=").append(and);
        sb.append("defaultList=").append(this.toString());
        sb.append('}');
        return sb.toString();
    }
}
