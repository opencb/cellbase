package org.opencb.cellbase.build.serializers;

/**
 * @author Alejandro Alemán Ramos <aaleman@cipf.es>
 */
@Deprecated
public interface CellBaseSerializer {
    boolean open();

    boolean serialize(Object elem);

    boolean close();
}
