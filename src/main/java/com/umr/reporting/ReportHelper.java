package com.umr.reporting;

import com.umr.core.TestContext;
import com.umr.utils.ScreenshotUtils;

/**
 * Helper class to simplify reporting operations.
 */
public class ReportHelper {

    /**
     * Reports a keyword execution start.
     *
     * @param context The test context
     * @param keyword The keyword being executed
     */
    public static void reportKeywordStart(TestContext context, String keyword) {
        ReportManager.logInfo(context.getTestId(), context.getTestName(), "Executing keyword: " + keyword);
    }

    /**
     * Reports a keyword execution success.
     *
     * @param context The test context
     * @param keyword The keyword that was executed
     * @param message Additional success message (optional)
     */
    public static void reportKeywordSuccess(TestContext context, String keyword, String message) {
        String logMessage = "Keyword executed successfully: " + keyword;
        if (message != null && !message.isEmpty()) {
            logMessage += " - " + message;
        }
        ReportManager.logPass(context.getTestId(), context.getTestName(), logMessage);

        // Optionally take a screenshot
        String screenshotPath = ScreenshotUtils.takeScreenshot(keyword + "_Success");
        if (screenshotPath != null) {
            ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), screenshotPath, "Success Screenshot");
        }
    }

    /**
     * Reports a keyword execution failure.
     *
     * @param context The test context
     * @param keyword The keyword that failed
     * @param reason The failure reason
     */
    public static void reportKeywordFailure(TestContext context, String keyword, String reason) {
        String logMessage = "Keyword failed: " + keyword;
        if (reason != null && !reason.isEmpty()) {
            logMessage += " - Reason: " + reason;
        }
        ReportManager.logFail(context.getTestId(), context.getTestName(), logMessage);

        // Take a failure screenshot
        String screenshotPath = ScreenshotUtils.takeScreenshot(keyword + "_Failure");
        if (screenshotPath != null) {
            ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), screenshotPath, "Failure Screenshot");
        }
    }
}