package org.opencb.cellbase.core.api.queries;

import java.util.ArrayList;
import java.util.List;

public class LogicalList extends ArrayList {
    private boolean and;

    public LogicalList() {}

    public LogicalList(List defaultList) {
        this(defaultList, false);
    }

    public LogicalList(List defaultList, boolean isAnd) {
        this.and = isAnd;
        this.addAll(defaultList);
    }

    public boolean isAnd() {
        return and;
    }

    public LogicalList setAnd(boolean and) {
        this.and = and;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LogicalList{");
        sb.append("and=").append(and);
        sb.append('}');
        return sb.toString();
    }
}
