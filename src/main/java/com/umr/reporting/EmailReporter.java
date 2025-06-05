package com.umr.reporting;

import com.umr.core.config.ConfigLoader;
import com.umr.utils.LogUtil;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * EmailReporter handles sending customized email reports with test execution results.
 * Supports custom subjects, bodies, test statistics, and JIRA ticket information.
 */
public class EmailReporter {

    private static final ConfigLoader config = ConfigLoader.getInstance();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String smtpHost;
    private String smtpPort;
    private String emailUsername;
    private String emailPassword;
    private boolean enableTLS;
    private List<String> recipients;

    // Test execution statistics
    private int totalTests = 0;
    private int passedTests = 0;
    private int failedTests = 0;
    private int skippedTests = 0;
    private String executionStartTime;
    private String executionEndTime;
    private List<TestResult> testResults = new ArrayList<>();

    public EmailReporter() {
        loadEmailConfiguration();
        this.executionStartTime = DATE_FORMAT.format(new Date());
    }

    private void loadEmailConfiguration() {
        this.smtpHost = config.getProperty("email.smtp.host", "smtp.gmail.com");
        this.smtpPort = config.getProperty("email.smtp.port", "587");
        this.emailUsername = config.getProperty("email.username", "");
        this.emailPassword = config.getProperty("email.password", "");
        this.enableTLS = config.getBooleanProperty("email.enable.tls", true);

        String recipientList = config.getProperty("email.recipients", "");
        this.recipients = Arrays.asList(recipientList.split(","));
    }

    /**
     * Adds a test result to the email report.
     */
    public void addTestResult(String testId, String testName, String status, String duration,
                              String jiraTicket, String tecsId, String failureReason) {
        TestResult result = new TestResult();
        result.testId = testId;
        result.testName = testName;
        result.status = status;
        result.duration = duration;
        result.jiraTicket = jiraTicket != null ? jiraTicket : "N/A";
        result.tecsId = tecsId != null ? tecsId : "N/A";
        result.failureReason = failureReason;
        result.timestamp = DATE_FORMAT.format(new Date());

        testResults.add(result);
        totalTests++;

        switch (status.toUpperCase()) {
            case "PASS":
            case "PASSED":
                passedTests++;
                break;
            case "FAIL":
            case "FAILED":
                failedTests++;
                break;
            case "SKIP":
            case "SKIPPED":
                skippedTests++;
                break;
        }
    }

    /**
     * Sends email report with custom subject and body, including HTML report attachment.
     */
    public boolean sendEmailReport(String customSubject, String customBodyHeader) {
        try {
            this.executionEndTime = DATE_FORMAT.format(new Date());

            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.auth", "true");
            if (enableTLS) {
                props.put("mail.smtp.starttls.enable", "true");
            }

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(emailUsername, emailPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailUsername));

            // Add recipients
            InternetAddress[] addresses = new InternetAddress[recipients.size()];
            for (int i = 0; i < recipients.size(); i++) {
                addresses[i] = new InternetAddress(recipients.get(i).trim());
            }
            message.setRecipients(Message.RecipientType.TO, addresses);

            // Set subject
            String subject = customSubject != null ? customSubject : generateDefaultSubject();
            message.setSubject(subject);

            // Create multipart message for text + attachments
            Multipart multipart = new MimeMultipart();

            // Add email body
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            String emailBody = generateEmailBody(customBodyHeader);
            messageBodyPart.setContent(emailBody, "text/html");
            multipart.addBodyPart(messageBodyPart);

            // Add HTML report attachment
            String htmlReportPath = findLatestHTMLReport();
            if (htmlReportPath != null) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(htmlReportPath);
                attachmentPart.setFileName("CBP_Test_Report_" +
                        new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".html");
                multipart.addBodyPart(attachmentPart);
                LogUtil.info("HTML report attached: " + htmlReportPath);
            } else {
                LogUtil.warn("HTML report file not found for attachment");
            }

            // Add screenshots ZIP if available
            String screenshotsZip = createScreenshotsZip();
            if (screenshotsZip != null) {
                MimeBodyPart screenshotsPart = new MimeBodyPart();
                screenshotsPart.attachFile(screenshotsZip);
                screenshotsPart.setFileName("CBP_Test_Screenshots_" +
                        new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".zip");
                multipart.addBodyPart(screenshotsPart);
                LogUtil.info("Screenshots ZIP attached: " + screenshotsZip);
            }

            // Set the complete message content
            message.setContent(multipart);

            // Send email
            Transport.send(message);

            LogUtil.info("Email report sent successfully with attachments to: " + String.join(", ", recipients));
            return true;

        } catch (Exception e) {
            LogUtil.error("Failed to send email report", e);
            return false;
        }
    }

    /**
     * Sends email report with default subject and body.
     */
    public boolean sendEmailReport() {
        return sendEmailReport(null, null);
    }

    private String generateDefaultSubject() {
        String status = failedTests > 0 ? "FAILED" : "PASSED";
        return String.format("CBP Test Execution Report - %s (%d/%d Passed) - %s",
                status, passedTests, totalTests,
                new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
    }

    private String generateEmailBody(String customHeader) {
        StringBuilder body = new StringBuilder();

        body.append("<html><head><style>");
        body.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        body.append("table { border-collapse: collapse; width: 100%; margin: 20px 0; }");
        body.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        body.append("th { background-color: #f2f2f2; }");
        body.append(".passed { color: green; font-weight: bold; }");
        body.append(".failed { color: red; font-weight: bold; }");
        body.append(".skipped { color: orange; font-weight: bold; }");
        body.append(".summary { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0; }");
        body.append(".header { background-color: #e6f3ff; padding: 15px; border-radius: 5px; margin: 20px 0; }");
        body.append("</style></head><body>");

        // Custom header
        if (customHeader != null && !customHeader.isEmpty()) {
            body.append("<div class='header'>");
            body.append("<h2>").append(customHeader).append("</h2>");
            body.append("</div>");
        } else {
            body.append("<div class='header'>");
            body.append("<h2>CBP Automation Test Execution Report</h2>");
            body.append("</div>");
        }

        // Executive Summary
        body.append("<div class='summary'>");
        body.append("<h3>📊 Executive Summary</h3>");
        body.append("<p><strong>Execution Period:</strong> ").append(executionStartTime).append(" to ").append(executionEndTime).append("</p>");
        body.append("<p><strong>Total Tests:</strong> ").append(totalTests).append("</p>");
        body.append("<p><strong>✅ Passed:</strong> <span class='passed'>").append(passedTests).append("</span></p>");
        body.append("<p><strong>❌ Failed:</strong> <span class='failed'>").append(failedTests).append("</span></p>");
        body.append("<p><strong>⚠️ Skipped:</strong> <span class='skipped'>").append(skippedTests).append("</span></p>");

        // Success rate
        double successRate = totalTests > 0 ? (double) passedTests / totalTests * 100 : 0;
        body.append("<p><strong>Success Rate:</strong> ").append(String.format("%.1f%%", successRate)).append("</p>");
        body.append("</div>");

        // Detailed Test Results
        body.append("<h3>📋 Detailed Test Results</h3>");
        body.append("<table>");
        body.append("<tr>");
        body.append("<th>Test ID</th>");
        body.append("<th>Test Name</th>");
        body.append("<th>Status</th>");
        body.append("<th>Duration</th>");
        body.append("<th>JIRA Ticket</th>");
        body.append("<th>TECS ID</th>");
        body.append("<th>Timestamp</th>");
        body.append("<th>Failure Reason</th>");
        body.append("</tr>");

        for (TestResult result : testResults) {
            body.append("<tr>");
            body.append("<td>").append(result.testId).append("</td>");
            body.append("<td>").append(result.testName).append("</td>");

            String statusClass = result.status.toLowerCase();
            if (statusClass.contains("pass")) statusClass = "passed";
            else if (statusClass.contains("fail")) statusClass = "failed";
            else if (statusClass.contains("skip")) statusClass = "skipped";

            body.append("<td class='").append(statusClass).append("'>").append(result.status).append("</td>");
            body.append("<td>").append(result.duration).append("</td>");
            body.append("<td>").append(result.jiraTicket).append("</td>");
            body.append("<td>").append(result.tecsId).append("</td>");
            body.append("<td>").append(result.timestamp).append("</td>");
            body.append("<td>").append(result.failureReason != null ? result.failureReason : "").append("</td>");
            body.append("</tr>");
        }

        body.append("</table>");

        // JIRA Summary
        body.append(generateJiraSummary());

        // Footer
        body.append("<br><hr>");
        body.append("<h3>📎 Attachments Included</h3>");
        body.append("<ul>");
        body.append("<li><strong>📄 Full HTML Report:</strong> Complete ExtentReports HTML file with detailed test execution results, screenshots, and logs</li>");
        body.append("<li><strong>📸 Screenshots ZIP:</strong> All screenshots captured during test execution, including TECS ID captures and failure screenshots</li>");
        body.append("</ul>");
        body.append("<p><em>💡 <strong>Tip:</strong> Open the HTML report in your browser for interactive viewing with expandable test steps and embedded screenshots.</em></p>");
        body.append("<br><hr>");
        body.append("<p><em>This report was generated automatically by the CBP Test Automation Framework.</em></p>");
        body.append("<p><em>For questions or issues, please contact the QA team.</em></p>");

        body.append("</body></html>");

        return body.toString();
    }

    private String generateJiraSummary() {
        StringBuilder summary = new StringBuilder();
        Map<String, List<TestResult>> jiraGroups = new HashMap<>();

        // Group tests by JIRA ticket
        for (TestResult result : testResults) {
            if (!"N/A".equals(result.jiraTicket)) {
                jiraGroups.computeIfAbsent(result.jiraTicket, k -> new ArrayList<>()).add(result);
            }
        }

        if (!jiraGroups.isEmpty()) {
            summary.append("<h3>🎫 JIRA Ticket Summary</h3>");
            summary.append("<table>");
            summary.append("<tr><th>JIRA Ticket</th><th>Total Tests</th><th>Passed</th><th>Failed</th><th>Success Rate</th></tr>");

            for (Map.Entry<String, List<TestResult>> entry : jiraGroups.entrySet()) {
                String jiraTicket = entry.getKey();
                List<TestResult> results = entry.getValue();

                int totalForJira = results.size();
                int passedForJira = (int) results.stream().filter(r -> r.status.toLowerCase().contains("pass")).count();
                int failedForJira = (int) results.stream().filter(r -> r.status.toLowerCase().contains("fail")).count();
                double successRateForJira = totalForJira > 0 ? (double) passedForJira / totalForJira * 100 : 0;

                summary.append("<tr>");
                summary.append("<td>").append(jiraTicket).append("</td>");
                summary.append("<td>").append(totalForJira).append("</td>");
                summary.append("<td class='passed'>").append(passedForJira).append("</td>");
                summary.append("<td class='failed'>").append(failedForJira).append("</td>");
                summary.append("<td>").append(String.format("%.1f%%", successRateForJira)).append("</td>");
                summary.append("</tr>");
            }

            summary.append("</table>");
        }

        return summary.toString();
    }

    // Inner class for test results
    private static class TestResult {
        String testId;
        String testName;
        String status;
        String duration;
        String jiraTicket;
        String tecsId;
        String failureReason;
        String timestamp;
    }

    // Getter methods for test statistics
    public int getTotalTests() { return totalTests; }
    public int getPassedTests() { return passedTests; }
    public int getFailedTests() { return failedTests; }
    public int getSkippedTests() { return skippedTests; }

    /**
     * Finds the latest HTML report file in the reports directory.
     */
    private String findLatestHTMLReport() {
        try {
            String reportsDir = config.getProperty("reports.dir", "./reports");
            File reportsFolder = new File(reportsDir);

            if (!reportsFolder.exists()) {
                LogUtil.warn("Reports directory not found: " + reportsDir);
                return null;
            }

            File[] htmlFiles = reportsFolder.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".html") && name.startsWith("TestReport_"));

            if (htmlFiles == null || htmlFiles.length == 0) {
                LogUtil.warn("No HTML report files found in: " + reportsDir);
                return null;
            }

            // Find the most recent file
            File latestFile = htmlFiles[0];
            for (File file : htmlFiles) {
                if (file.lastModified() > latestFile.lastModified()) {
                    latestFile = file;
                }
            }

            LogUtil.info("Found HTML report for attachment: " + latestFile.getAbsolutePath());
            return latestFile.getAbsolutePath();

        } catch (Exception e) {
            LogUtil.error("Error finding HTML report file", e);
            return null;
        }
    }

    /**
     * Creates a ZIP file containing all screenshots from the current test run.
     */
    private String createScreenshotsZip() {
        try {
            String screenshotsDir = config.getProperty("screenshots.dir", "./reports/screenshots");
            File screenshotsFolder = new File(screenshotsDir);

            if (!screenshotsFolder.exists()) {
                LogUtil.warn("Screenshots directory not found: " + screenshotsDir);
                return null;
            }

            File[] screenshotFiles = screenshotsFolder.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));

            if (screenshotFiles == null || screenshotFiles.length == 0) {
                LogUtil.warn("No screenshot files found in: " + screenshotsDir);
                return null;
            }

            // Create ZIP file
            String zipFileName = screenshotsDir + File.separator + "CBP_Screenshots_" +
                    new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".zip";

            try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(
                    new java.io.FileOutputStream(zipFileName))) {

                for (File file : screenshotFiles) {
                    // Only include files from today's execution (last 24 hours)
                    long dayInMillis = 24 * 60 * 60 * 1000;
                    if (System.currentTimeMillis() - file.lastModified() < dayInMillis) {

                        java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(file.getName());
                        zos.putNextEntry(entry);

                        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = fis.read(buffer)) > 0) {
                                zos.write(buffer, 0, length);
                            }
                        }
                        zos.closeEntry();
                    }
                }
            }

            LogUtil.info("Created screenshots ZIP for attachment: " + zipFileName);
            return zipFileName;

        } catch (Exception e) {
            LogUtil.error("Error creating screenshots ZIP file", e);
            return null;
        }
    }
}