package com.umr.utils;

import com.umr.core.DriverManager;
import com.umr.core.config.ConfigLoader;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility class for capturing and managing screenshots during test execution.
 */
public class ScreenshotUtils {
    private static final ConfigLoader config = ConfigLoader.getInstance();
    private static final String SCREENSHOT_DIR = config.getScreenshotsDirectory();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

    static {
        // Ensure the screenshot directory exists
        createScreenshotDirectory();
    }

    /**
     * Creates the screenshot directory if it doesn't exist.
     */
    private static void createScreenshotDirectory() {
        try {
            Path path = Paths.get(SCREENSHOT_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                LogUtil.info("Created screenshot directory: " + SCREENSHOT_DIR);
            }
        } catch (IOException e) {
            LogUtil.error("Failed to create screenshot directory: " + SCREENSHOT_DIR, e);
        }
    }

    /**
     * Takes a screenshot of the current browser window.
     *
     * @param fileName Base name for the screenshot file
     * @return The full path to the saved screenshot file, or null if the operation failed
     */
    public static String takeScreenshot(String fileName) {
        try {
            WebDriver driver = DriverManager.getDriver();
            if (driver == null) {
                LogUtil.error("Cannot take screenshot - driver is null");
                return null;
            }
            return takeScreenshot(driver, fileName);
        } catch (Exception e) {
            LogUtil.error("Failed to take screenshot: " + fileName, e);
            return null;
        }
    }

    /**
     * Takes a screenshot of the specified browser window.
     *
     * @param driver The WebDriver instance to capture
     * @param fileName Base name for the screenshot file
     * @return The full path to the saved screenshot file, or null if the operation failed
     */
    public static String takeScreenshot(WebDriver driver, String fileName) {
        if (driver == null) {
            LogUtil.error("Cannot take screenshot - driver is null");
            return null;
        }

        // Clean the filename to remove invalid characters
        fileName = sanitizeFileName(fileName);

        try {
            // Add timestamp to ensure unique filenames
            String timestamp = DATE_FORMAT.format(new Date());
            String screenshotFileName = fileName + "_" + timestamp + ".png";
            String filePath = SCREENSHOT_DIR + File.separator + screenshotFileName;
            File destFile = new File(filePath);

            // Take the screenshot
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshot, destFile);

            LogUtil.info("Screenshot saved: " + filePath);

            // Return the full path for internal use
            return filePath;
        } catch (Exception e) {
            LogUtil.error("Failed to take screenshot: " + fileName, e);
            return null;
        }
    }

    /**
     * Gets the relative path for a screenshot, for use in reports.
     *
     * @param fullPath The full screenshot path
     * @return The relative path for use in reports
     */
    public static String getRelativePathForReport(String fullPath) {
        if (fullPath == null) {
            return null;
        }

        String screenshotsRelativePath = config.getScreenshotsRelativePath();
        String fileName = new File(fullPath).getName();
        return screenshotsRelativePath + "/" + fileName;
    }

    /**
     * Takes a screenshot for attaching to test reports.
     *
     * @param testId The test ID
     * @param testName The test name
     * @param screenshotName The name for the screenshot
     * @return The path to the screenshot file, relative to report location
     */
    public static String takeScreenshotForReport(String testId, String testName, String screenshotName) {
        String fullPath = takeScreenshot(screenshotName);
        if (fullPath != null) {
            return getRelativePathForReport(fullPath);
        }
        return null;
    }

    /**
     * Takes a screenshot on test failure.
     *
     * @param testId The test ID
     * @param testName The test name
     * @param failureReason The reason for the test failure
     * @return The full path to the saved screenshot file, or null if the operation failed
     */
    public static String takeFailureScreenshot(String testId, String testName, String failureReason) {
        try {
            WebDriver driver = DriverManager.getDriver();
            if (driver == null) {
                LogUtil.error("Cannot take failure screenshot - driver is null");
                return null;
            }

            String fileName = "FAILURE_" + testId + "_" + sanitizeFileName(testName);
            return takeScreenshot(driver, fileName);
        } catch (Exception e) {
            LogUtil.error("Failed to take failure screenshot", e);
            return null;
        }
    }

    /**
     * Sanitizes a filename by removing invalid characters.
     *
     * @param fileName The original filename
     * @return The sanitized filename
     */
    private static String sanitizeFileName(String fileName) {
        // Replace invalid characters with underscores
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}