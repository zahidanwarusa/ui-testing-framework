package com.umr.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for logging across the framework.
 * Provides consistent logging methods and formats.
 */
public class LogUtil {
    private static final Logger logger = LogManager.getLogger("com.umr");

    /**
     * Private constructor to prevent instantiation.
     */
    private LogUtil() {
        // Utility class should not be instantiated
    }

    /**
     * Logs a debug message.
     *
     * @param message The message to log
     */
    public static void debug(String message) {
        logger.debug(message);
    }

    /**
     * Logs an info message.
     *
     * @param message The message to log
     */
    public static void info(String message) {
        logger.info(message);
    }

    /**
     * Logs a warning message.
     *
     * @param message The message to log
     */
    public static void warn(String message) {
        logger.warn(message);
    }

    /**
     * Logs a warning message with an exception.
     *
     * @param message The message to log
     * @param throwable The exception to log
     */
    public static void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    /**
     * Logs an error message.
     *
     * @param message The message to log
     */
    public static void error(String message) {
        logger.error(message);
    }

    /**
     * Logs an error message with an exception.
     *
     * @param message The message to log
     * @param throwable The exception to log
     */
    public static void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    /**
     * Logs the start of a test case.
     *
     * @param testId The test ID
     * @param testName The test name
     */
    public static void startTest(String testId, String testName) {
        logger.info("======================================================");
        logger.info("STARTING TEST: {} - {}", testId, testName);
        logger.info("======================================================");
    }

    /**
     * Logs the end of a test case with result.
     *
     * @param testId The test ID
     * @param testName The test name
     * @param result The test result (PASS/FAIL)
     */
    public static void endTest(String testId, String testName, String result) {
        logger.info("======================================================");
        logger.info("TEST COMPLETED: {} - {} with result: {}", testId, testName, result);
        logger.info("======================================================");
    }

    /**
     * Logs the execution of a keyword.
     *
     * @param keyword The keyword being executed
     */
    public static void keyword(String keyword) {
        logger.info("Executing keyword: {}", keyword);
    }

    /**
     * Logs the result of a keyword execution.
     *
     * @param keyword The keyword
     * @param result True if successful, false otherwise
     */
    public static void keywordResult(String keyword, boolean result) {
        if (result) {
            logger.info("Keyword executed successfully: {}", keyword);
        } else {
            logger.error("Keyword execution failed: {}", keyword);
        }
    }
}