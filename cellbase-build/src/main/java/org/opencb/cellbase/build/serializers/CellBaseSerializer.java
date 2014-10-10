package org.opencb.cellbase.build.serializers;

/**
 * @author Alejandro Alemán Ramos <aaleman@cipf.es>
 */
public interface CellBaseSerializer {
    boolean open();

    boolean serialize(Object elem);

    boolean close();
}
