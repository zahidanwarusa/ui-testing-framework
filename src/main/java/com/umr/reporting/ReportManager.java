package com.umr.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.umr.core.config.ConfigLoader;
import com.umr.utils.LogUtil;
import com.umr.utils.ScreenshotUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages test reporting using ExtentReports.
 * Provides methods for creating and updating test reports.
 */
public class ReportManager {
    private static ExtentReports extentReports;
    private static final Map<String, ExtentTest> testMap = new HashMap<>();
    private static final ConfigLoader config = ConfigLoader.getInstance();
    private static final String REPORT_DIRECTORY = config.getReportsDirectory();

    /**
     * Initializes the ExtentReports instance.
     * Should be called once at the beginning of test execution.
     */
    public static synchronized void initializeReport() {
        if (extentReports == null) {
            // Create report directory if it doesn't exist
            File reportDir = new File(REPORT_DIRECTORY);
            if (!reportDir.exists()) {
                reportDir.mkdirs();
            }

            // Create timestamp for report name
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String reportFileName = "TestReport_" + timestamp + ".html";
            String reportPath = REPORT_DIRECTORY + File.separator + reportFileName;

            // Setup ExtentReports
            extentReports = new ExtentReports();
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);

            // Configure report appearance
            sparkReporter.config().setDocumentTitle(config.getReportTitle());
            sparkReporter.config().setReportName(config.getReportName());
            sparkReporter.config().setTheme(Theme.STANDARD);
            sparkReporter.config().setTimeStampFormat("EEEE, MMMM dd, yyyy, hh:mm a '('zzz')'");
            sparkReporter.config().setEncoding("UTF-8");

            // Attach reporter to ExtentReports
            extentReports.attachReporter(sparkReporter);

            // Set system info
            extentReports.setSystemInfo("OS", System.getProperty("os.name"));
            extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
            extentReports.setSystemInfo("Browser", config.getProperty("browser", "Chrome"));
            extentReports.setSystemInfo("Environment", config.getProperty("environment", "QA"));
            extentReports.setSystemInfo("Base URL", config.getBaseUrl());

            LogUtil.info("ExtentReports initialized: " + reportPath);
        }
    }

    /**
     * Creates a new test in the report.
     *
     * @param testId The unique test identifier
     * @param testName The test name
     * @param description The test description
     * @return The created ExtentTest instance
     */
    public static synchronized ExtentTest createTest(String testId, String testName, String description) {
        if (extentReports == null) {
            initializeReport();
        }

        String testKey = testId + "_" + testName;
        ExtentTest test = extentReports.createTest(testName, description);
        test.assignCategory("Dice Tests");
        test.assignAuthor("Automation Framework");
        testMap.put(testKey, test);

        LogUtil.info("Created test in report: " + testName);
        return test;
    }

    /**
     * Gets an existing test from the report.
     *
     * @param testId The unique test identifier
     * @param testName The test name
     * @return The ExtentTest instance or null if not found
     */
    public static synchronized ExtentTest getTest(String testId, String testName) {
        String testKey = testId + "_" + testName;
        return testMap.get(testKey);
    }

    /**
     * Logs information to the test report.
     *
     * @param testId The unique test identifier
     * @param testName The test name
     * @param details The information to log
     */
    public static synchronized void logInfo(String testId, String testName, String details) {
        ExtentTest test = getTest(testId, testName);
        if (test != null) {
            test.info(details);
        }
    }

    /**
     * Logs a passed step to the test report.
     *
     * @param testId The unique test identifier
     * @param testName The test name
     * @param details The information to log
     */
    public static synchronized void logPass(String testId, String testName, String details) {
        ExtentTest test = getTest(testId, testName);
        if (test != null) {
            test.pass(details);
        }
    }

    /**
     * Logs a failed step to the test report.
     *
     * @param testId The unique test identifier
     * @param testName The test name
     * @param details The information to log
     */
    public static synchronized void logFail(String testId, String testName, String details) {
        ExtentTest test = getTest(testId, testName);
        if (test != null) {
            test.fail(details);
        }
    }

    /**
     * Logs a skipped step to the test report.
     *
     * @param testId The unique test identifier
     * @param testName The test name
     * @param details The information to log
     */
    public static synchronized void logSkip(String testId, String testName, String details) {
        ExtentTest test = getTest(testId, testName);
        if (test != null) {
            test.skip(details);
        }
    }

    /**
     * Logs a warning to the test report.
     *
     * @param testId The unique test identifier
     * @param testName The test name
     * @param details The information to log
     */
    public static synchronized void logWarning(String testId, String testName, String details) {
        ExtentTest test = getTest(testId, testName);
        if (test != null) {
            test.warning(details);
        }
    }

    /**
     * Attaches a screenshot to the test report.
     *
     * @param testId The unique test identifier
     * @param testName The test name
     * @param screenshotPath The path to the screenshot file
     * @param title The title for the screenshot
     */
    public static synchronized void attachScreenshot(String testId, String testName, String screenshotPath, String title) {
        try {
            ExtentTest test = getTest(testId, testName);
            if (test != null && screenshotPath != null) {
                // Create a File object to ensure the screenshot exists
                File screenshotFile = new File(screenshotPath);
                if (screenshotFile.exists()) {
                    LogUtil.info("Attaching screenshot to report: " + screenshotPath);

                    // Get the relative path for the report
                    String reportRelativePath = ScreenshotUtils.getRelativePathForReport(screenshotPath);

                    test.addScreenCaptureFromPath(reportRelativePath, title);
                    LogUtil.info("Screenshot attached to report with path: " + reportRelativePath);
                } else {
                    LogUtil.error("Screenshot file not found: " + screenshotPath);
                }
            }
        } catch (Exception e) {
            LogUtil.error("Failed to attach screenshot to report: " + e.getMessage(), e);
        }
    }

    /**
     * Marks a test as passed in the report.
     *
     * @param testId The unique test identifier
     * @param testName The test name
     * @param details The pass details
     */
    public static synchronized void markTestAsPassed(String testId, String testName, String details) {
        ExtentTest test = getTest(testId, testName);
        if (test != null) {
            test.pass(details);
        }
    }

    /**
     * Marks a test as failed in the report.
     *
     * @param testId The unique test identifier
     * @param testName The test name
     * @param details The failure details
     */
    public static synchronized void markTestAsFailed(String testId, String testName, String details) {
        ExtentTest test = getTest(testId, testName);
        if (test != null) {
            test.fail(details);
        }
    }

    /**
     * Marks a test as skipped in the report.
     *
     * @param testId The unique test identifier
     * @param testName The test name
     * @param details The skip details
     */
    public static synchronized void markTestAsSkipped(String testId, String testName, String details) {
        ExtentTest test = getTest(testId, testName);
        if (test != null) {
            test.skip(details);
        }
    }

    /**
     * Finalizes the report and writes it to disk.
     * Should be called once at the end of test execution.
     */
    public static synchronized void finalizeReport() {
        if (extentReports != null) {
            extentReports.flush();
            LogUtil.info("ExtentReports finalized and written to disk");
        }
    }
}