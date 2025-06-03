package com.umr.reporting;

import com.umr.core.config.ConfigLoader;
import com.umr.utils.LogUtil;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

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
     * Sends email report with custom subject and body.
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
            
            // Create email body
            String emailBody = generateEmailBody(customBodyHeader);
            message.setContent(emailBody, "text/html");
            
            // Send email
            Transport.send(message);
            
            LogUtil.info("Email report sent successfully to: " + String.join(", ", recipients));
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
}