package com.umr.core.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigLoader is responsible for loading and providing access to framework configuration properties.
 * It implements the Singleton pattern to ensure a single instance is used throughout the framework.
 */
public class ConfigLoader {
    private static final Logger logger = LogManager.getLogger(ConfigLoader.class);
    private static final String CONFIG_FILE = "src/main/resources/config/framework.properties";
    private static volatile ConfigLoader instance;
    private Properties properties;

    /**
     * Private constructor to prevent instantiation outside of this class.
     * Loads properties from the configuration file.
     */
    private ConfigLoader() {
        properties = new Properties();
        try (InputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
            logger.info("Configuration loaded successfully from: {}", CONFIG_FILE);

            // Create necessary directories based on config
            createRequiredDirectories();

        } catch (IOException e) {
            logger.error("Failed to load configuration from: {}", CONFIG_FILE, e);
            throw new RuntimeException("Failed to load framework configuration", e);
        }
    }

    /**
     * Creates directories required by the framework.
     */
    private void createRequiredDirectories() {
        // Create reports directory
        String reportsDir = getProperty("reports.dir", "./reports");
        File reportsDirFile = new File(reportsDir);
        if (!reportsDirFile.exists()) {
            boolean created = reportsDirFile.mkdirs();
            if (created) {
                logger.info("Created reports directory: {}", reportsDir);
            } else {
                logger.warn("Failed to create reports directory: {}", reportsDir);
            }
        }

        // Create screenshots directory
        String screenshotsDir = getProperty("screenshots.dir", "./reports/screenshots");
        File screenshotsDirFile = new File(screenshotsDir);
        if (!screenshotsDirFile.exists()) {
            boolean created = screenshotsDirFile.mkdirs();
            if (created) {
                logger.info("Created screenshots directory: {}", screenshotsDir);
            } else {
                logger.warn("Failed to create screenshots directory: {}", screenshotsDir);
            }
        }
    }

    /**
     * Gets the singleton instance of ConfigLoader.
     *
     * @return The ConfigLoader instance
     */
    public static ConfigLoader getInstance() {
        if (instance == null) {
            synchronized (ConfigLoader.class) {
                if (instance == null) {
                    instance = new ConfigLoader();
                }
            }
        }
        return instance;
    }

    /**
     * Gets a property value by its key.
     *
     * @param key The property key
     * @return The property value or null if the key doesn't exist
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Gets a property value by its key, returning a default value if the key doesn't exist.
     *
     * @param key The property key
     * @param defaultValue The default value to return if the key doesn't exist
     * @return The property value or the default value
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Gets an integer property value by its key.
     *
     * @param key The property key
     * @param defaultValue The default value to return if the key doesn't exist or cannot be parsed
     * @return The property value as an integer or the default value
     */
    public int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse integer property: {}. Using default value: {}", key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Gets a boolean property value by its key.
     *
     * @param key The property key
     * @param defaultValue The default value to return if the key doesn't exist
     * @return The property value as a boolean or the default value
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
    }

    /**
     * Gets the base URL for the specified environment from the configuration.
     *
     * @return The base URL for the current environment
     */
    public String getBaseUrl() {
        String environment = getProperty("environment", "QA");
        String baseUrlKey = "base.url." + environment;
        String baseUrl = getProperty(baseUrlKey);

        if (baseUrl == null) {
            logger.warn("Base URL not found for environment: {}. Using QA environment as fallback.", environment);
            baseUrl = getProperty("base.url.QA");
        }

        return baseUrl;
    }

    /**
     * Gets the reports directory path.
     *
     * @return The reports directory path
     */
    public String getReportsDirectory() {
        return getProperty("reports.dir", "./reports");
    }

    /**
     * Gets the screenshots directory path.
     *
     * @return The screenshots directory path
     */
    public String getScreenshotsDirectory() {
        return getProperty("screenshots.dir", "./reports/screenshots");
    }

    /**
     * Gets the screenshots path relative to the reports directory.
     *
     * @return The relative path for screenshots
     */
    public String getScreenshotsRelativePath() {
        return getProperty("screenshots.relative.path", "screenshots");
    }

    /**
     * Gets the report title.
     *
     * @return The report title
     */
    public String getReportTitle() {
        return getProperty("report.title", "UI Test Automation Report");
    }

    /**
     * Gets the report name.
     *
     * @return The report name
     */
    public String getReportName() {
        return getProperty("report.name", "UI Test Execution Report");
    }
}