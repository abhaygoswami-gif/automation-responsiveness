package com.qa.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class TestLogger {
    private static final Logger LOGGER = LogManager.getLogger(TestLogger.class);
    private TestLogger() {}

    public static void info(String message) { LOGGER.info(message); }
    public static void warn(String message) { LOGGER.warn(message); }
    public static void error(String message) { LOGGER.error(message); }
}
