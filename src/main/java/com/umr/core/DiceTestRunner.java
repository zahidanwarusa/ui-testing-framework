package com.umr.core;

import com.umr.core.keyword.DiceKeywords;
import com.umr.reporting.ReportManager;
import com.umr.utils.ExcelReader;
import com.umr.utils.LogUtil;
import com.umr.utils.ScreenshotUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DiceTestRunner {

    public static void main(String[] args) {
        LogUtil.info("Starting Dice.com Test Execution");

        try {
            // Initialize reporting
            ReportManager.initializeReport();

            // Get active test cases from TestRunner
            List<Map<String, String>> activeTests = ExcelReader.getActiveTestCases();

            if (activeTests.isEmpty()) {
                LogUtil.warn("No active tests found in TestRunner. Execution complete.");
                return;
            }

            LogUtil.info("Found " + activeTests.size() + " active test(s)");

            // Create keyword executor with DiceKeywords
            KeywordExecutor keywordExecutor = new KeywordExecutor(new DiceKeywords());

            // Execute each test
            for (Map<String, String> test : activeTests) {
                executeTest(test, keywordExecutor);

                // Reset driver state completely after each test
                DriverManager.resetDriver();
            }

            LogUtil.info("Test Execution Completed");

            // Finalize the report
            ReportManager.finalizeReport();
        } catch (Exception e) {
            LogUtil.error("Error during test execution", e);
            // Make sure to finalize report even if there's an exception
            ReportManager.finalizeReport();
        }
    }
    private static void executeTest(Map<String, String> test, KeywordExecutor keywordExecutor) {
        String testId = test.get("TestID");
        String testName = test.get("TestName");
        String description = test.get("Description");

        LogUtil.startTest(testId, testName);
        TestContext context = new TestContext(testId, testName);

        // Create test in report
        ReportManager.createTest(testId, testName, description != null ? description : testName);

        try {
            // Load test data
            Map<String, String> testData = ExcelReader.getTestData(testId);
            ReportManager.logInfo(testId, testName, "Test data loaded for test: " + testId);

            // Add test data to context
            for (Map.Entry<String, String> entry : testData.entrySet()) {
                context.addTestData(entry.getKey(), entry.getValue());
            }

            // Load keywords
            List<String> keywords = ExcelReader.getKeywordsForTest(testId);
            ReportManager.logInfo(testId, testName, "Found " + keywords.size() + " keywords to execute");

            if (keywords.isEmpty()) {
                LogUtil.error("No keywords found for test ID: " + testId);
                ReportManager.logFail(testId, testName, "No keywords found for test ID: " + testId);
                LogUtil.endTest(testId, testName, "FAIL");
                ReportManager.markTestAsFailed(testId, testName, "No keywords found for test ID: " + testId);
                return;
            }

            LogUtil.info("Executing test with " + keywords.size() + " keywords");

            // Execute each keyword in sequence
            for (String keyword : keywords) {
                if (keyword == null || keyword.trim().isEmpty()) {
                    continue; // Skip empty keywords
                }

                LogUtil.info("Executing keyword: " + keyword);
                ReportManager.logInfo(testId, testName, "Executing keyword: " + keyword);

                boolean result = keywordExecutor.executeKeyword(keyword, context);

                if (result) {
                    ReportManager.logPass(testId, testName, "Keyword executed successfully: " + keyword);
                } else {
                    LogUtil.error("Test failed during keyword: " + keyword);
                    String failureReason = context.getFailureReason();
                    ReportManager.logFail(testId, testName, "Keyword failed: " + keyword +
                            (failureReason != null ? " - Reason: " + failureReason : ""));

                    // Take and attach failure screenshot
                    String screenshotPath = ScreenshotUtils.takeFailureScreenshot(testId, testName, context.getFailureReason());
                    if (screenshotPath != null) {
                        ReportManager.attachScreenshot(testId, testName, screenshotPath, "Failure Screenshot");
                    }
                    break;
                }
            }

            // Log test result
            String result = context.isTestPassed() ? "PASS" : "FAIL";
            LogUtil.endTest(testId, testName, result);

            if (context.isTestPassed()) {
                ReportManager.markTestAsPassed(testId, testName, "Test executed successfully");
            } else {
                ReportManager.markTestAsFailed(testId, testName, "Test failed: " + context.getFailureReason());
            }

        } catch (Exception e) {
            LogUtil.error("Error executing test: " + testName, e);
            LogUtil.endTest(testId, testName, "FAIL");
            ReportManager.logFail(testId, testName, "Exception during test execution: " + e.getMessage());
            ReportManager.markTestAsFailed(testId, testName, "Exception: " + e.getMessage());
        } finally {
            // Clean up resources
            context.cleanup();
        }
    }

    private static void verifyDirectories() {
        // Check if reports directory exists
        File reportsDir = new File("./reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
            LogUtil.info("Created reports directory");
        } else {
            LogUtil.info("Reports directory exists");
        }

        // Check if screenshots directory exists
        File screenshotsDir = new File("./screenshots");
        if (!screenshotsDir.exists()) {
            screenshotsDir.mkdirs();
            LogUtil.info("Created screenshots directory");
        } else {
            LogUtil.info("Screenshots directory exists");
        }

        // Check write permissions
        try {
            File testFile = new File("./reports/test.txt");
            boolean created = testFile.createNewFile();
            if (created) {
                LogUtil.info("Write permissions confirmed for reports directory");
                testFile.delete();
            } else {
                LogUtil.warn("Could not create test file in reports directory");
            }

            testFile = new File("./screenshots/test.txt");
            created = testFile.createNewFile();
            if (created) {
                LogUtil.info("Write permissions confirmed for screenshots directory");
                testFile.delete();
            } else {
                LogUtil.warn("Could not create test file in screenshots directory");
            }
        } catch (IOException e) {
            LogUtil.error("Error checking write permissions", e);
            throw new RuntimeException(e);
        }
    }
}