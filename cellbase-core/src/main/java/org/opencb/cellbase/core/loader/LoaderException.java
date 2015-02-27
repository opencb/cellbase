package org.opencb.cellbase.core.loader;

/**
 * Created by parce on 27/02/15.
 */
public class LoaderException extends Exception {

    public LoaderException(Exception e) {
        super(e.getMessage());
    }
}
