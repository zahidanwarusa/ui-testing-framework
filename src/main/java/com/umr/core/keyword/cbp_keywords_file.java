package com.umr.core.keyword;

import com.umr.core.DriverManager;
import com.umr.core.TestContext;
import com.umr.core.annotation.Keyword;
import com.umr.reporting.ReportManager;
import com.umr.utils.LogUtil;
import com.umr.utils.ScreenshotUtils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * CBP (Customs and Border Protection) specific keywords for automation testing.
 * Contains keywords for CBP login, traveler selection, and 1-day lookout creation.
 */
public class CBPKeywords {

    @Keyword("CBP_LOGIN")
    public boolean cbpLogin(TestContext context) {
        LogUtil.info("Executing CBP_LOGIN keyword");
        ReportManager.logInfo(context.getTestId(), context.getTestName(), "Executing CBP_LOGIN keyword");

        try {
            WebDriver driver = context.getDriver();
            String url = "https://tf-sat.cbp.dhs.gov/pax/LoginPage";

            // Navigate to CBP login page
            LogUtil.info("Navigating to CBP login page: " + url);
            driver.get(url);
            ReportManager.logInfo(context.getTestId(), context.getTestName(), "Navigated to CBP login page: " + url);

            // Wait for page to load completely
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));

            // Take screenshot after navigation
            String initialScreenshotPath = ScreenshotUtils.takeScreenshot("CBP_Login_Page_Initial");
            if (initialScreenshotPath != null) {
                ReportManager.attachScreenshot(context.getTestId(), context.getTestName(),
                        initialScreenshotPath, "CBP Login Page Loaded");
            }

            // Wait for and click the CBP Users login button
            By loginButtonLocator = By.id("login-kerberos-btn");

            LogUtil.info("Waiting for CBP Users login button to be clickable");
            WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(loginButtonLocator));

            LogUtil.info("Clicking CBP Users login button");
            loginButton.click();
            ReportManager.logInfo(context.getTestId(), context.getTestName(), "Clicked CBP Users login button");

            // Take screenshot after clicking login
            String clickScreenshotPath = ScreenshotUtils.takeScreenshot("CBP_Login_Button_Clicked");
            if (clickScreenshotPath != null) {
                ReportManager.attachScreenshot(context.getTestId(), context.getTestName(),
                        clickScreenshotPath, "Login Button Clicked");
            }

            // Wait a moment for any redirect or authentication process to begin
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            LogUtil.info("CBP login process initiated successfully");
            ReportManager.logPass(context.getTestId(), context.getTestName(), "CBP login process initiated successfully");

            return true;

        } catch (Exception e) {
            LogUtil.error("Failed to perform CBP login", e);
            context.setTestFailed("Failed to perform CBP login: " + e.getMessage());
            ReportManager.logFail(context.getTestId(), context.getTestName(), "Failed to perform CBP login: " + e.getMessage());

            // Take failure screenshot
            String failureScreenshotPath = ScreenshotUtils.takeScreenshot("CBP_Login_Failed");
            if (failureScreenshotPath != null) {
                ReportManager.attachScreenshot(context.getTestId(), context.getTestName(),
                        failureScreenshotPath, "CBP Login Failure");
            }

            return false;
        }
    }

    @Keyword("CREATE_AND_FILL_1DAY_LOOKOUT")
    public boolean createAndFill1DayLookout(TestContext context) {
        LogUtil.info("Executing CREATE_AND_FILL_1DAY_LOOKOUT keyword");
        ReportManager.logInfo(context.getTestId(), context.getTestName(), "Executing CREATE_AND_FILL_1DAY_LOOKOUT keyword");

        try {
            WebDriver driver = context.getDriver();
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            String url = "https://tf-sat.cbp.dhs.gov/uv/hotlists/ntc/traveler";

            // Step 1: Navigate to traveler page
            LogUtil.info("Navigating to traveler page: " + url);
            driver.get(url);
            ReportManager.logInfo(context.getTestId(), context.getTestName(), "Navigated to traveler page: " + url);

            // Wait for page to load completely
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));

            Thread.sleep(5000);

            // Step 2: Set filters (Departure Time - NOW and Not Reviewed)
            LogUtil.info("Setting filters: Departure Time - NOW and Not Reviewed");

            // Set Departure Time filter
            Boolean departureTimeSet = (Boolean) js.executeScript(
                    "var departureCheckbox = document.querySelector('input[name=\"scheduledDepartureDttm\"][value=\"departureTime\"]');" +
                            "if (departureCheckbox && !departureCheckbox.checked) {" +
                            "  var checkboxDiv = departureCheckbox.closest('.p-checkbox');" +
                            "  if (checkboxDiv) checkboxDiv.click();" +
                            "  return true;" +
                            "}" +
                            "return departureCheckbox && departureCheckbox.checked;"
            );

            // Set Not Reviewed filter
            Boolean notReviewedSet = (Boolean) js.executeScript(
                    "var notReviewedCheckbox = document.querySelector('input[name=\"eventStatusFilter\"][value=\"NOT REVIEWED\"]');" +
                            "if (notReviewedCheckbox && !notReviewedCheckbox.checked) {" +
                            "  var checkboxDiv = notReviewedCheckbox.closest('.p-checkbox');" +
                            "  if (checkboxDiv) checkboxDiv.click();" +
                            "  return true;" +
                            "}" +
                            "return notReviewedCheckbox && notReviewedCheckbox.checked;"
            );

            LogUtil.info("Filters set - Departure Time: " + departureTimeSet + ", Not Reviewed: " + notReviewedSet);
            Thread.sleep(3000);

            // Step 3: Click on the first traveler row
            LogUtil.info("Clicking on first traveler row");

            WebElement travelerRow = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("tr.highlightable.row-bold")));

            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", travelerRow);
            Thread.sleep(1000);
            travelerRow.click();

            LogUtil.info("Selected first traveler");
            Thread.sleep(5000);

            // Step 4: Click on action dropdown
            LogUtil.info("Opening action dropdown");

            WebElement actionDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button.mat-menu-trigger[aria-haspopup='menu']")));

            js.executeScript("arguments[0].click();", actionDropdown);
            Thread.sleep(2000);

            // Step 5: Click "Create 1-Day Lookout"
            LogUtil.info("Clicking Create 1-Day Lookout");

            WebElement create1DayButton = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@mat-menu-item]//span[contains(text(), 'Create 1-Day Lookout')]")));

            // Store original window handle before clicking
            String originalWindow = driver.getWindowHandle();
            LogUtil.info("Original window handle: " + originalWindow);

            create1DayButton.click();
            LogUtil.info("Clicked Create 1-Day Lookout button");

            // Step 6: Switch to popup window
            Thread.sleep(5000);

            boolean windowSwitched = false;
            if (driver.getWindowHandles().size() > 1) {
                LogUtil.info("New window detected! Switching to popup...");
                for (String windowHandle : driver.getWindowHandles()) {
                    if (!windowHandle.equals(originalWindow)) {
                        driver.switchTo().window(windowHandle);
                        LogUtil.info("Switched to popup window: " + windowHandle);
                        windowSwitched = true;
                        break;
                    }
                }
            }

            if (!windowSwitched) {
                LogUtil.info("No new window found, continuing on same window");
            }

            Thread.sleep(5000);
            LogUtil.info("Current URL after window switch: " + driver.getCurrentUrl());

            // Take screenshot of the 1-day lookout form
            String formScreenshotPath = ScreenshotUtils.takeScreenshot("1Day_Lookout_Form_Opened");
            if (formScreenshotPath != null) {
                ReportManager.attachScreenshot(context.getTestId(), context.getTestName(),
                        formScreenshotPath, "1-Day Lookout Form");
            }

            // Step 7: Fill the 1-Day Lookout form
            LogUtil.info("Starting to fill 1-Day Lookout form");
            boolean formFilled = fill1DayLookoutForm(driver, js, wait, context);

            if (formFilled) {
                LogUtil.info("Successfully filled 1-Day Lookout form");
                ReportManager.logPass(context.getTestId(), context.getTestName(),
                        "Successfully created and filled 1-Day Lookout");
                return true;
            } else {
                LogUtil.error("Failed to fill 1-Day Lookout form");
                context.setTestFailed("Failed to fill 1-Day Lookout form");
                return false;
            }

        } catch (Exception e) {
            LogUtil.error("Failed to create and fill 1-day lookout", e);
            context.setTestFailed("Failed to create and fill 1-day lookout: " + e.getMessage());
            ReportManager.logFail(context.getTestId(), context.getTestName(),
                    "Failed to create and fill 1-day lookout: " + e.getMessage());

            String failureScreenshotPath = ScreenshotUtils.takeScreenshot("1Day_Lookout_Failed");
            if (failureScreenshotPath != null) {
                ReportManager.attachScreenshot(context.getTestId(), context.getTestName(),
                        failureScreenshotPath, "1-Day Lookout Failure");
            }

            return false;
        }
    }

    private boolean fill1DayLookoutForm(WebDriver driver, JavascriptExecutor js, WebDriverWait wait, TestContext context) {
        try {
            LogUtil.info("Filling 1-Day Lookout form - Complete workflow");

            // Wait for form to load
            Thread.sleep(10000);

            // First, let's analyze what's already filled
            analyzeExistingFormData(js);

            // 1. Fill Remarks (Required field - always empty)
            LogUtil.info("1. Filling remarks field (required)");
            String remarks = "Automated 1-Day Lookout - Created at " + System.currentTimeMillis() + " - Subject flagged for review per automated screening protocols";
            Boolean remarksResult = fillRemarksField(js, remarks);
            LogUtil.info("Remarks filled: " + remarksResult);
            Thread.sleep(2000);

            // 2. Fill Primary End Date if empty (Required field)
            LogUtil.info("2. Checking and filling Primary End Date");
            String endDate = generateFutureDate(1, 30);
            Boolean endDateResult = fillPrimaryEndDate(js, endDate);
            LogUtil.info("Primary End Date result: " + endDateResult);
            Thread.sleep(2000);

            // 3. Fill Height dropdown (Physical Description)
            LogUtil.info("3. Filling Height dropdown");
            Boolean heightResult = fillHeightDropdown(js, "5'8\"");
            LogUtil.info("Height filled: " + heightResult);
            Thread.sleep(2000);

            // 4. Fill Weight field
            LogUtil.info("4. Filling Weight field");
            Boolean weightResult = fillWeightField(js, "170");
            LogUtil.info("Weight filled: " + weightResult);
            Thread.sleep(2000);

            // 5. Add and fill Race (if button exists)
            LogUtil.info("5. Adding and filling Race");
            if (clickAddButtonSafe(js, "Add Race")) {
                Thread.sleep(4000);
                Boolean raceResult = selectFromLatestDropdown(js, "A - ASIAN");
                LogUtil.info("Race selection result: " + raceResult);
            }
            Thread.sleep(2000);

            // 6. Add and fill Eye Color (if button exists)
            LogUtil.info("6. Adding and filling Eye Color");
            if (clickAddButtonSafe(js, "Add Eye Color")) {
                Thread.sleep(4000);
                Boolean eyeResult = selectFromLatestDropdown(js, "BG - BLUE/GREEN");
                LogUtil.info("Eye Color selection result: " + eyeResult);
            }
            Thread.sleep(2000);

            // 7. Add and fill Hair Color (if button exists)
            LogUtil.info("7. Adding and filling Hair Color");
            if (clickAddButtonSafe(js, "Add Hair Color")) {
                Thread.sleep(4000);
                Boolean hairResult = selectFromLatestDropdown(js, "BA - BALD");
                LogUtil.info("Hair Color selection result: " + hairResult);
            }
            Thread.sleep(2000);

            // 8. Add and fill A# if section is empty
            LogUtil.info("8. Adding A# field");
            if (clickAddButtonSafe(js, "Add A#")) {
                Thread.sleep(3000);
                String aNumber = "123456789";
                Boolean aNumberResult = fillAnumber(js, aNumber);
                LogUtil.info("A# filled: " + aNumberResult);
            }
            Thread.sleep(2000);

            // 9. Add and fill Driver's License
            LogUtil.info("9. Adding Driver's License");
            if (clickAddButtonSafe(js, "Add Driver's License")) {
                Thread.sleep(4000);
                Boolean licenseResult = fillDriversLicense(js);
                LogUtil.info("Driver's License filled: " + licenseResult);
            }
            Thread.sleep(2000);

            // 10. Add SSN if available
            LogUtil.info("10. Adding SSN if available");
            if (clickAddButtonSafe(js, "Add SSN")) {
                Thread.sleep(3000);
                Boolean ssnResult = fillSSN(js, "123-45-6789");
                LogUtil.info("SSN filled: " + ssnResult);
            }
            Thread.sleep(2000);

            // 11. Take final screenshot
            String finalScreenshotPath = ScreenshotUtils.takeScreenshot("1Day_Lookout_Form_Filled_Complete");
            if (finalScreenshotPath != null) {
                ReportManager.attachScreenshot(context.getTestId(), context.getTestName(),
                        finalScreenshotPath, "Complete Form Filled - Ready for Submission");
            }

            LogUtil.info("1-Day Lookout form filling completed successfully - All fields processed");
            return true;

        } catch (Exception e) {
            LogUtil.error("Error filling 1-Day Lookout form", e);
            return false;
        }
    }

    // === FORM ANALYSIS AND BASIC FIELD FILLING METHODS ===

    private void analyzeExistingFormData(JavascriptExecutor js) {
        try {
            LogUtil.info("=== ANALYZING EXISTING FORM DATA ===");

            String analysis = (String) js.executeScript(
                    "var analysis = [];" +
                            "analysis.push('=== FORM ANALYSIS ===');" +

                            // Check Sex field
                            "var sexElements = document.querySelectorAll('*');" +
                            "var foundSex = false;" +
                            "for (var i = 0; i < sexElements.length; i++) {" +
                            "  if (sexElements[i].textContent && sexElements[i].textContent.includes('F - FEMALE')) {" +
                            "    analysis.push('Sex: Already filled (F - FEMALE)');" +
                            "    foundSex = true;" +
                            "    break;" +
                            "  } else if (sexElements[i].textContent && sexElements[i].textContent.includes('M - MALE')) {" +
                            "    analysis.push('Sex: Already filled (M - MALE)');" +
                            "    foundSex = true;" +
                            "    break;" +
                            "  }" +
                            "}" +
                            "if (!foundSex) analysis.push('Sex: Not found or empty');" +

                            // Check Citizenship
                            "var citizenshipElements = document.querySelectorAll('*');" +
                            "var foundCitizenship = false;" +
                            "for (var j = 0; j < citizenshipElements.length; j++) {" +
                            "  if (citizenshipElements[j].textContent && citizenshipElements[j].textContent.includes('USA - UNITED STATES')) {" +
                            "    analysis.push('Citizenship: Already filled (USA)');" +
                            "    foundCitizenship = true;" +
                            "    break;" +
                            "  }" +
                            "}" +
                            "if (!foundCitizenship) analysis.push('Citizenship: Not found');" +

                            // Check Passport
                            "var passportElements = document.querySelectorAll('*');" +
                            "var foundPassport = false;" +
                            "for (var k = 0; k < passportElements.length; k++) {" +
                            "  if (passportElements[k].textContent && passportElements[k].textContent.includes('R - Regular')) {" +
                            "    analysis.push('Passport: Already filled (R - Regular)');" +
                            "    foundPassport = true;" +
                            "    break;" +
                            "  }" +
                            "}" +
                            "if (!foundPassport) analysis.push('Passport: Not found');" +

                            // Check Remarks
                            "var textarea = document.querySelector('textarea[maxlength=\"3000\"]');" +
                            "if (textarea) {" +
                            "  if (textarea.value && textarea.value.length > 0) {" +
                            "    analysis.push('Remarks: Already filled (' + textarea.value.length + ' chars)');" +
                            "  } else {" +
                            "    analysis.push('Remarks: Empty - NEEDS FILLING');" +
                            "  }" +
                            "} else {" +
                            "  analysis.push('Remarks: Textarea not found');" +
                            "}" +

                            "return analysis.join('\\n');"
            );

            LogUtil.info("Form Analysis Result:\n" + analysis);

        } catch (Exception e) {
            LogUtil.error("Error analyzing form data", e);
        }
    }

    private Boolean fillRemarksField(JavascriptExecutor js, String remarks) {
        try {
            return (Boolean) js.executeScript(
                    "var textarea = document.querySelector('textarea[maxlength=\"3000\"]');" +
                            "if (textarea) {" +
                            "  textarea.scrollIntoView({behavior: 'smooth', block: 'center'});" +
                            "  textarea.focus();" +
                            "  textarea.value = arguments[0];" +
                            "  textarea.dispatchEvent(new Event('input', {bubbles: true}));" +
                            "  textarea.dispatchEvent(new Event('change', {bubbles: true}));" +
                            "  textarea.blur();" +
                            "  return true;" +
                            "}" +
                            "return false;", remarks
            );
        } catch (Exception e) {
            LogUtil.error("Error filling remarks", e);
            return false;
        }
    }

    private Boolean fillPrimaryEndDate(JavascriptExecutor js, String endDate) {
        try {
            return (Boolean) js.executeScript(
                    "var found = false;" +
                            "var labels = document.querySelectorAll('b, label, span');" +
                            "for (var i = 0; i < labels.length; i++) {" +
                            "  if (labels[i].textContent && labels[i].textContent.includes('Primary End Date')) {" +
                            "    var container = labels[i].closest('div');" +
                            "    if (container) {" +
                            "      var dateInput = container.querySelector('input[mask=\"00/00/0000\"]');" +
                            "      if (dateInput) {" +
                            "        dateInput.focus();" +
                            "        dateInput.value = arguments[0];" +
                            "        dateInput.dispatchEvent(new Event('input', {bubbles: true}));" +
                            "        dateInput.dispatchEvent(new Event('change', {bubbles: true}));" +
                            "        dateInput.blur();" +
                            "        found = true;" +
                            "        break;" +
                            "      }" +
                            "    }" +
                            "  }" +
                            "}" +
                            "return found;", endDate
            );
        } catch (Exception e) {
            LogUtil.error("Error filling primary end date", e);
            return false;
        }
    }

    // === PHYSICAL DESCRIPTION AND ADDITIONAL FIELD METHODS ===

    private Boolean fillHeightDropdown(JavascriptExecutor js, String height) {
        try {
            LogUtil.info("Filling height dropdown with: " + height);
            return (Boolean) js.executeScript(
                    "var labels = document.querySelectorAll('label, b, span');" +
                            "for (var i = 0; i < labels.length; i++) {" +
                            "  if (labels[i].textContent && labels[i].textContent.includes('Height')) {" +
                            "    var container = labels[i].closest('div');" +
                            "    if (container) {" +
                            "      var heightSelect = container.querySelector('select, mat-select');" +
                            "      if (heightSelect) {" +
                            "        heightSelect.scrollIntoView({behavior: 'smooth', block: 'center'});" +
                            "        var trigger = heightSelect.querySelector('.mat-select-trigger');" +
                            "        if (trigger) {" +
                            "          trigger.click();" +
                            "          setTimeout(() => {" +
                            "            var options = document.querySelectorAll('mat-option');" +
                            "            for (var j = 0; j < options.length; j++) {" +
                            "              if (options[j].offsetParent !== null && options[j].textContent.includes('5\\'8')) {" +
                            "                options[j].click();" +
                            "                setTimeout(() => document.body.click(), 500);" +
                            "                return;" +
                            "              }" +
                            "            }" +
                            "          }, 2000);" +
                            "          return true;" +
                            "        }" +
                            "      }" +
                            "    }" +
                            "  }" +
                            "}" +
                            "return false;"
            );
        } catch (Exception e) {
            LogUtil.error("Error filling height", e);
            return false;
        }
    }

    private Boolean fillWeightField(JavascriptExecutor js, String weight) {
        try {
            LogUtil.info("Filling weight field with: " + weight);
            return (Boolean) js.executeScript(
                    "var weightInput = document.querySelector('input[mask=\"0*\"][maxlength=\"4\"]');" +
                            "if (weightInput) {" +
                            "  weightInput.scrollIntoView({behavior: 'smooth', block: 'center'});" +
                            "  weightInput.focus();" +
                            "  weightInput.value = arguments[0];" +
                            "  weightInput.dispatchEvent(new Event('input', {bubbles: true}));" +
                            "  weightInput.dispatchEvent(new Event('change', {bubbles: true}));" +
                            "  weightInput.blur();" +
                            "  return true;" +
                            "}" +
                            "return false;", weight
            );
        } catch (Exception e) {
            LogUtil.error("Error filling weight", e);
            return false;
        }
    }

    private boolean selectFromLatestDropdown(JavascriptExecutor js, String optionText) {
        try {
            LogUtil.info("Selecting '" + optionText + "' from latest dropdown");

            // Close any open dropdowns first
            js.executeScript("document.body.click();");
            Thread.sleep(1000);

            Boolean result = (Boolean) js.executeScript(
                    "return new Promise((resolve) => {" +
                            "  var selects = Array.from(document.querySelectorAll('mat-select:not([aria-disabled=\"true\"])'));" +
                            "  var visibleSelects = selects.filter(s => {" +
                            "    var rect = s.getBoundingClientRect();" +
                            "    return rect.width > 0 && rect.height > 0;" +
                            "  });" +
                            "  if (visibleSelects.length === 0) { resolve(false); return; }" +
                            "  var newest = visibleSelects[visibleSelects.length - 1];" +
                            "  newest.scrollIntoView({behavior: 'smooth', block: 'center'});" +
                            "  var trigger = newest.querySelector('.mat-select-trigger');" +
                            "  if (!trigger) { resolve(false); return; }" +
                            "  trigger.click();" +
                            "  setTimeout(() => {" +
                            "    var options = Array.from(document.querySelectorAll('mat-option:not(.mat-option-disabled)'));" +
                            "    var visibleOptions = options.filter(o => o.offsetParent !== null);" +
                            "    console.log('Found ' + visibleOptions.length + ' visible options');" +
                            "    for (var i = 0; i < visibleOptions.length; i++) {" +
                            "      console.log('Option ' + i + ': ' + visibleOptions[i].textContent);" +
                            "      if (visibleOptions[i].textContent.includes(arguments[0])) {" +
                            "        visibleOptions[i].click();" +
                            "        setTimeout(() => { document.body.click(); resolve(true); }, 500);" +
                            "        return;" +
                            "      }" +
                            "    }" +
                            "    // If exact match not found, try partial match" +
                            "    for (var j = 0; j < visibleOptions.length; j++) {" +
                            "      var optText = arguments[0].split(' - ')[0];" +
                            "      if (visibleOptions[j].textContent.includes(optText)) {" +
                            "        visibleOptions[j].click();" +
                            "        setTimeout(() => { document.body.click(); resolve(true); }, 500);" +
                            "        return;" +
                            "      }" +
                            "    }" +
                            "    // If still no match, select first available option" +
                            "    if (visibleOptions.length > 0) {" +
                            "      visibleOptions[0].click();" +
                            "      setTimeout(() => { document.body.click(); resolve(true); }, 500);" +
                            "    } else {" +
                            "      resolve(false);" +
                            "    }" +
                            "  }, 3000);" +
                            "});", optionText
            );

            Thread.sleep(3000);
            return result != null && result;
        } catch (Exception e) {
            LogUtil.error("Error selecting from latest dropdown: " + optionText, e);
            return false;
        }
    }

    private boolean fillDriversLicense(JavascriptExecutor js) {
        try {
            LogUtil.info("Filling Driver's License information");

            // Fill license number
            Boolean licenseNumberResult = (Boolean) js.executeScript(
                    "var labels = document.querySelectorAll('label, b, span');" +
                            "for (var i = 0; i < labels.length; i++) {" +
                            "  if (labels[i].textContent && labels[i].textContent.includes('License #')) {" +
                            "    var container = labels[i].closest('div');" +
                            "    if (container) {" +
                            "      var input = container.querySelector('input[maxlength=\"20\"]');" +
                            "      if (input && input.value === '') {" +
                            "        input.focus();" +
                            "        input.value = 'DL123456789';" +
                            "        input.dispatchEvent(new Event('input', {bubbles: true}));" +
                            "        input.dispatchEvent(new Event('change', {bubbles: true}));" +
                            "        input.blur();" +
                            "        return true;" +
                            "      }" +
                            "    }" +
                            "  }" +
                            "}" +
                            "return false;"
            );

            Thread.sleep(2000);

            // Select state
            Boolean stateResult = (Boolean) js.executeScript(
                    "var labels = document.querySelectorAll('label, b, span');" +
                            "for (var i = 0; i < labels.length; i++) {" +
                            "  if (labels[i].textContent && labels[i].textContent.includes('State')) {" +
                            "    var container = labels[i].closest('div');" +
                            "    if (container) {" +
                            "      var stateSelect = container.querySelector('select, mat-select');" +
                            "      if (stateSelect) {" +
                            "        var trigger = stateSelect.querySelector('.mat-select-trigger');" +
                            "        if (trigger) {" +
                            "          trigger.click();" +
                            "          setTimeout(() => {" +
                            "            var options = document.querySelectorAll('mat-option');" +
                            "            for (var j = 0; j < options.length; j++) {" +
                            "              if (options[j].offsetParent !== null && (options[j].textContent.includes('VA') || options[j].textContent.includes('Virginia'))) {" +
                            "                options[j].click();" +
                            "                setTimeout(() => document.body.click(), 500);" +
                            "                return;" +
                            "              }" +
                            "            }" +
                            "            // If VA not found, select first available state" +
                            "            if (options.length > 1) {" +
                            "              options[1].click();" +
                            "              setTimeout(() => document.body.click(), 500);" +
                            "            }" +
                            "          }, 2000);" +
                            "          return true;" +
                            "        }" +
                            "      }" +
                            "    }" +
                            "  }" +
                            "}" +
                            "return false;"
            );

            LogUtil.info("Driver's License - Number: " + licenseNumberResult + ", State: " + stateResult);
            return licenseNumberResult != null && licenseNumberResult;

        } catch (Exception e) {
            LogUtil.error("Error filling driver's license", e);
            return false;
        }
    }

    private boolean fillSSN(JavascriptExecutor js, String ssn) {
        try {
            LogUtil.info("Filling SSN with: " + ssn);
            return (Boolean) js.executeScript(
                    "var inputs = document.querySelectorAll('input[mask=\"000-00-0000\"]');" +
                            "if (inputs.length > 0) {" +
                            "  var ssnInput = inputs[inputs.length - 1];" +
                            "  ssnInput.focus();" +
                            "  ssnInput.value = arguments[0];" +
                            "  ssnInput.dispatchEvent(new Event('input', {bubbles: true}));" +
                            "  ssnInput.dispatchEvent(new Event('change', {bubbles: true}));" +
                            "  ssnInput.blur();" +
                            "  return true;" +
                            "}" +
                            "return false;", ssn
            );
        } catch (Exception e) {
            LogUtil.error("Error filling SSN", e);
            return false;
        }
    }

    private boolean clickAddButtonSafe(JavascriptExecutor js, String buttonText) {
        try {
            LogUtil.info("Attempting to click: " + buttonText);

            // Close any open dropdowns first
            js.executeScript("document.body.click();");
            Thread.sleep(500);

            Boolean result = (Boolean) js.executeScript(
                    "var buttons = document.querySelectorAll('button.add-button, button.mat-raised-button, button');" +
                            "for (var i = 0; i < buttons.length; i++) {" +
                            "  if (buttons[i].textContent && buttons[i].textContent.includes(arguments[0])) {" +
                            "    buttons[i].scrollIntoView({behavior: 'smooth', block: 'center'});" +
                            "    buttons[i].click();" +
                            "    return true;" +
                            "  }" +
                            "}" +
                            "return false;", buttonText
            );
            return result != null && result;
        } catch (Exception e) {
            LogUtil.error("Error clicking add button: " + buttonText, e);
            return false;
        }
    }

    private boolean fillAnumber(JavascriptExecutor js, String aNumber) {
        try {
            LogUtil.info("Filling A# field with: " + aNumber);
            Thread.sleep(2000);

            Boolean result = (Boolean) js.executeScript(
                    "var inputs = document.querySelectorAll('input');" +
                            "var aNumberInputs = [];" +
                            "for (var i = 0; i < inputs.length; i++) {" +
                            "  var input = inputs[i];" +
                            "  if ((input.getAttribute('mask') === '0*' && input.getAttribute('maxlength') === '9') ||" +
                            "      (input.getAttribute('maxlength') === '9' && input.type === 'text')) {" +
                            "    var rect = input.getBoundingClientRect();" +
                            "    if (rect.width > 0 && rect.height > 0) {" +
                            "      aNumberInputs.push(input);" +
                            "    }" +
                            "  }" +
                            "}" +
                            "if (aNumberInputs.length > 0) {" +
                            "  var newest = aNumberInputs[aNumberInputs.length - 1];" +
                            "  newest.focus();" +
                            "  newest.value = arguments[0];" +
                            "  newest.dispatchEvent(new Event('input', {bubbles: true}));" +
                            "  newest.dispatchEvent(new Event('change', {bubbles: true}));" +
                            "  newest.blur();" +
                            "  return true;" +
                            "}" +
                            "return false;", aNumber
            );

            return result != null && result;
        } catch (Exception e) {
            LogUtil.error("Error filling A# field", e);
            return false;
        }
    }

    private String generateFutureDate(int minDaysAhead, int maxDaysAhead) {
        Random random = new Random();
        LocalDate date = LocalDate.now()
                .plusDays(minDaysAhead + random.nextInt(maxDaysAhead - minDaysAhead));
        return date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }
}
}