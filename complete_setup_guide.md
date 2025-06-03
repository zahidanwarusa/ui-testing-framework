# 🎯 Complete Setup Guide - TECS ID Capture & Email Reporting

## 🆕 NEW FEATURES ADDED:

### ✅ TECS ID Capture
- **Automatically submits** the 1-day lookout form
- **Captures TECS ID** from the submission page (e.g., "XYZ121312")
- **Takes screenshots** with TECS ID highlighted
- **Adds TECS ID to reports** with proper logging

### ✅ Email Reporting
- **Customizable email subject and body**
- **Test execution statistics** (total, passed, failed, skipped)
- **JIRA ticket tracking** and summaries
- **HTML formatted emails** with tables and styling
- **Automatic email sending** after test completion

---

## 📋 SETUP CHECKLIST:

### 1. **Update Your Excel Files**

#### TestRunner.xlsx - Add JiraTicket column:
```
TestID | TestName           | Description              | Execute | JiraTicket
CBP001 | CBP Login Test     | Test CBP login          | Y       | CBP-1234
CBP002 | Create 1-Day Lookout | Complete workflow      | Y       | CBP-1235
```

### 2. **Configure Email Settings**

#### Update `framework.properties`:
```properties
# Email Configuration
send.email.report=true
email.smtp.host=smtp.gmail.com
email.smtp.port=587
email.enable.tls=true
email.username=your-automation-email@gmail.com
email.password=your-app-password
email.recipients=qa-team@company.com,manager@company.com
```

#### 🔧 Gmail Setup (if using Gmail):
1. **Enable 2-Factor Authentication** on your Gmail account
2. **Generate App Password**: 
   - Go to Google Account settings
   - Security → 2-Step Verification → App passwords
   - Generate password for "Mail"
3. **Use App Password** in framework.properties (not your regular password)

### 3. **Update Maven Dependencies**

Your `pom.xml` now includes JavaMail:
```xml
<dependency>
  <groupId>com.sun.mail</groupId>
  <artifactId>javax.mail</artifactId>
  <version>1.6.2</version>
</dependency>
```

### 4. **Add New Files to Project**

- ✅ **EmailReporter.java** → `src/main/java/com/umr/reporting/`
- ✅ **Updated CBPKeywords.java** → Updated with TECS ID capture
- ✅ **Updated CBPTestRunner.java** → Updated with email integration

---

## 🚀 HOW TO RUN:

### **Same as before, but now with enhanced features:**

1. **Run CBPTestRunner.main()**
2. **Watch the automation:**
   - Login to CBP
   - Navigate to traveler page
   - Create and fill 1-day lookout
   - **NEW:** Submit form and capture TECS ID
3. **Check outputs:**
   - HTML report with TECS ID information
   - Screenshots with TECS ID highlighted
   - **NEW:** Email report sent automatically

---

## 📊 EMAIL REPORT FEATURES:

### **Executive Summary:**
- Total tests run
- Pass/fail counts
- Success rate percentage
- Execution time

### **Detailed Results Table:**
| Test ID | Test Name | Status | Duration | JIRA Ticket | TECS ID | Timestamp | Failure Reason |
|---------|-----------|--------|----------|-------------|---------|-----------|----------------|
| CBP001  | Login     | PASSED | 45s      | CBP-1234    | XYZ121312 | 2024-01-15 14:30 | |
| CBP002  | Lookout   | PASSED | 2m 15s   | CBP-1235    | ABC456789 | 2024-01-15 14:32 | |

### **JIRA Summary:**
- Groups tests by JIRA ticket
- Shows success rate per ticket
- Helps track feature completion

---

## 🎯 WHAT HAPPENS NOW:

### **Enhanced 1-Day Lookout Workflow:**
1. ✅ Fill remarks, height, weight
2. ✅ Add race, eye color, hair color
3. ✅ Add passport information
4. ✅ Add A# and driver's license
5. 🆕 **Submit form automatically**
6. 🆕 **Capture TECS ID (e.g., XYZ121312)**
7. 🆕 **Highlight TECS ID with yellow background**
8. 🆕 **Take screenshot of TECS ID**
9. 🆕 **Add TECS ID to test reports**

### **After All Tests Complete:**
1. 🆕 **Generate execution statistics**
2. 🆕 **Create HTML email report**
3. 🆕 **Send email to configured recipients**
4. 🆕 **Include JIRA ticket summaries**

---

## 📧 SAMPLE EMAIL OUTPUT:

**Subject:** `CBP Automation Results - 2/2 Passed (ALL PASSED) - 2024-01-15 14:30`

**Content:**
- 📊 Executive Summary with statistics
- 📋 Detailed test results table
- 🎫 JIRA ticket groupings and success rates
- 📸 Links to screenshots and reports

---

## 🔧 CUSTOMIZATION OPTIONS:

### **Custom Email Subject:**
```java
// Automatically generated based on results
"CBP Automation Results - X/Y Passed (STATUS) - DATE"
```

### **Custom Email Body Header:**
```java
// Automatically includes:
- Execution date and time
- Total execution duration
- Environment information
```

### **JIRA Integration:**
- Add JIRA tickets to Excel TestRunner
- Automatic grouping in email reports
- Success rate tracking per ticket

---

## 🎉 YOU'RE READY!

Just run your tests as usual, and you'll now get:
- ✅ **TECS ID capture and reporting**
- ✅ **Professional email reports**
- ✅ **JIRA ticket tracking**
- ✅ **Enhanced screenshots and logging**

The framework handles everything automatically! 🚀