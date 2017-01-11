package org.opencb.cellbase.app.transform;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by fjlopez on 10/01/17.
 */
public class Main {
    public static void main(String[] args) {
        org.apache.log4j.Logger rootLogger = LogManager.getRootLogger();
        ConsoleAppender stderr = (ConsoleAppender) rootLogger.getAppender("stderr");
        stderr.setThreshold(Level.toLevel("warn"));

        Logger logger = LoggerFactory.getLogger(Main.class);

        logger.debug("Hello world debug");
        logger.info("Hello world info");
        logger.warn("Hello world warn");
        logger.error("Hello world error");

    }
}
