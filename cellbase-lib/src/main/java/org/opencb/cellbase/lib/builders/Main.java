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

package org.opencb.cellbase.lib.builders;

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
