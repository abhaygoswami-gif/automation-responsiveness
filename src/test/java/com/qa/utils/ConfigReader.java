package com.qa.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public final class ConfigReader {
    private static final Properties PROPERTIES = new Properties();

    static {
        try (FileInputStream input = new FileInputStream("config.properties")) {
            PROPERTIES.load(input);
        } catch (IOException e) {
            // Defaults are used if config.properties is unavailable.
        }
    }

    private ConfigReader() {}

    public static String get(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.trim().isEmpty()) {
            return systemValue.trim();
        }
        return PROPERTIES.getProperty(key, defaultValue).trim();
    }
}
