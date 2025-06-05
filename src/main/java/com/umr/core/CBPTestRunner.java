package com.umr.core;

import com.umr.core.keyword.CBPKeywords;
import com.umr.reporting.EmailReporter;
import com.umr.reporting.ReportManager;
import com.umr.utils.ExcelReader;
import com.umr.utils.LogUtil;
import com.umr.utils.ScreenshotUtils;

import java.util.List;
import java.util.Map;

/**
 * Test runner specifically for CBP (Customs and Border Protection) automation tests.
 * Uses CBPKeywords for test execution and includes email reporting functionality.
 */
public class CBPTestRunner {

    private static EmailReporter emailReporter;

    public static void main(String[] args) {
        LogUtil.info("Starting CBP Test Execution");
        long startTime = System.currentTimeMillis();

        // Initialize email reporter
        emailReporter = new EmailReporter();

        try {
            // Initialize reporting
            ReportManager.initializeReport();

            // Get active test cases from TestRunner
            List<Map<String, String>> activeTests = ExcelReader.getActiveTestCases();

            if (activeTests.isEmpty()) {
                LogUtil.warn("No active tests found in TestRunner. Execution complete.");
                return;
            }

            LogUtil.info("Found " + activeTests.size() + " active CBP test(s)");

            // Create keyword executor with CBPKeywords
            KeywordExecutor keywordExecutor = new KeywordExecutor(new CBPKeywords());

            // Execute each test
            for (Map<String, String> test : activeTests) {
                executeTest(test, keywordExecutor);

                // Reset driver state completely after each test
                DriverManager.resetDriver();
            }

            LogUtil.info("CBP Test Execution Completed");

            // Finalize the report
            ReportManager.finalizeReport();

            // Calculate execution time
            long endTime = System.currentTimeMillis();
            long executionTime = (endTime - startTime) / 1000; // in seconds

            // Send email report
            sendEmailReport(executionTime);

        } catch (Exception e) {
            LogUtil.error("Error during CBP test execution", e);
            // Make sure to finalize report even if there's an exception
            ReportManager.finalizeReport();

            // Send email report even on failure
            sendEmailReport(0);
        }
    }

    private static void executeTest(Map<String, String> test, KeywordExecutor keywordExecutor) {
        String testId = test.get("TestID");
        String testName = test.get("TestName");
        String description = test.get("Description");
        String jiraTicket = test.get("JiraTicket"); // Get JIRA ticket from Excel

        long testStartTime = System.currentTimeMillis();

        LogUtil.startTest(testId, testName);
        TestContext context = new TestContext(testId, testName);

        // Create test in report
        ReportManager.createTest(testId, testName, description != null ? description : testName);

        String testStatus = "UNKNOWN";
        String failureReason = null;
        String tecsId = null;

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
                testStatus = "FAILED";
                failureReason = "No keywords found for test ID: " + testId;
                return;
            }

            LogUtil.info("Executing CBP test with " + keywords.size() + " keywords");

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
                    failureReason = context.getFailureReason();
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

            // Get TECS ID if available
            tecsId = context.getFromContextAsString("TECS_ID");

            // Log test result
            testStatus = context.isTestPassed() ? "PASSED" : "FAILED";
            LogUtil.endTest(testId, testName, testStatus);

            if (context.isTestPassed()) {
                ReportManager.markTestAsPassed(testId, testName, "CBP test executed successfully");
            } else {
                ReportManager.markTestAsFailed(testId, testName, "CBP test failed: " + context.getFailureReason());
                if (failureReason == null) {
                    failureReason = context.getFailureReason();
                }
            }

        } catch (Exception e) {
            LogUtil.error("Error executing CBP test: " + testName, e);
            LogUtil.endTest(testId, testName, "FAILED");
            ReportManager.logFail(testId, testName, "Exception during CBP test execution: " + e.getMessage());
            ReportManager.markTestAsFailed(testId, testName, "Exception: " + e.getMessage());
            testStatus = "FAILED";
            failureReason = "Exception: " + e.getMessage();
        } finally {
            // Calculate test duration
            long testEndTime = System.currentTimeMillis();
            long testDuration = (testEndTime - testStartTime) / 1000; // in seconds
            String durationString = formatDuration(testDuration);

            // Add test result to email reporter
            emailReporter.addTestResult(testId, testName, testStatus, durationString, jiraTicket, tecsId, failureReason);

            // Clean up resources
            context.cleanup();
        }
    }

    private static void sendEmailReport(long executionTimeSeconds) {
        try {
            LogUtil.info("Preparing to send email report");

            // Create custom subject
            String customSubject = String.format("CBP Automation Results - %d/%d Passed (%s) - %s",
                    emailReporter.getPassedTests(),
                    emailReporter.getTotalTests(),
                    emailReporter.getFailedTests() > 0 ? "FAILURES DETECTED" : "ALL PASSED",
                    new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new java.util.Date()));

            // Create custom header
            String customHeader = String.format(
                    "🚨 CBP Test Automation Execution Summary<br>" +
                            "📅 Execution Date: %s<br>" +
                            "⏱️ Total Execution Time: %s<br>" +
                            "🎯 Test Environment: CBP UAT Environment",
                    new java.text.SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' HH:mm:ss").format(new java.util.Date()),
                    formatDuration(executionTimeSeconds)
            );

            // Send email report
            boolean emailSent = emailReporter.sendEmailReport(customSubject, customHeader);

            if (emailSent) {
                LogUtil.info("Email report sent successfully");
            } else {
                LogUtil.warn("Failed to send email report");
            }

        } catch (Exception e) {
            LogUtil.error("Error sending email report", e);
        }
    }

    private static String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + " seconds";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + " minutes " + remainingSeconds + " seconds";
        } else {
            long hours = seconds / 3600;
            long remainingMinutes = (seconds % 3600) / 60;
            return hours + " hours " + remainingMinutes + " minutes";
        }
    }
}