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
            LogUtil.info("Filling 1-Day Lookout form - Analyzing existing data first");

            // Wait for form to load
            Thread.sleep(10000);

            // First, let's analyze what's already filled
            analyzeExistingFormData(js);

            // 1. Fill Remarks (Required field - always empty)
            LogUtil.info("Filling remarks field (required)");
            String remarks = "Automated 1-Day Lookout - Created at " + System.currentTimeMillis() + " - Subject flagged for review per automated screening protocols";
            Boolean remarksResult = fillRemarksField(js, remarks);
            LogUtil.info("Remarks filled: " + remarksResult);

            Thread.sleep(2000);

            // 2. Fill Primary End Date if empty (Required field)
            LogUtil.info("Checking and filling Primary End Date");
            String endDate = generateFutureDate(1, 30);
            Boolean endDateResult = fillPrimaryEndDate(js, endDate);
            LogUtil.info("Primary End Date result: " + endDateResult);

            Thread.sleep(2000);

            // 3. Fill Height dropdown (Physical Descriptions)
            LogUtil.info("Filling Height dropdown");
            Boolean heightResult = fillHeightDropdown(js);
            LogUtil.info("Height dropdown result: " + heightResult);

            Thread.sleep(2000);

            // 4. Fill Weight if empty
            LogUtil.info("Checking and filling Weight field");
            Boolean weightResult = fillWeightIfEmpty(js);
            LogUtil.info("Weight field result: " + weightResult);

            Thread.sleep(2000);

            // 5. Add and fill Race
            LogUtil.info("Adding Race field");
            if (clickAddButtonSafe(js, "Add Race")) {
                Thread.sleep(4000); // More time for field to appear
                Boolean raceResult = selectFromNewlyAddedDropdown(js, "Race", "A - ASIAN");
                LogUtil.info("Race selection result: " + raceResult);
            }

            Thread.sleep(2000);

            // 6. Add and fill Eye Color
            LogUtil.info("Adding Eye Color field");
            if (clickAddButtonSafe(js, "Add Eye Color")) {
                Thread.sleep(4000); // More time for field to appear
                Boolean eyeResult = selectFromNewlyAddedDropdown(js, "Eye Color", "BG - BLUE/GREEN");
                LogUtil.info("Eye Color selection result: " + eyeResult);
            }

            Thread.sleep(2000);

            // 7. Add and fill Hair Color
            LogUtil.info("Adding Hair Color field");
            if (clickAddButtonSafe(js, "Add Hair Color")) {
                Thread.sleep(4000); // More time for field to appear
                Boolean hairResult = selectFromNewlyAddedDropdown(js, "Hair Color", "BA - BALD");
                LogUtil.info("Hair Color selection result: " + hairResult);
            }

            Thread.sleep(2000);

            // 8. Add and fill A#
            LogUtil.info("Adding A# field");
            if (clickAddButtonSafe(js, "Add A#")) {
                Thread.sleep(3000);
                String aNumber = "123456789";
                Boolean aNumberResult = fillAnumber(js, aNumber);
                LogUtil.info("A# field result: " + aNumberResult);
            }

            Thread.sleep(2000);

            // 9. Add and fill Passport if needed
            LogUtil.info("Checking and adding Passport information");
            if (shouldAddPassport(js)) {
                LogUtil.info("Adding Passport field");
                if (clickAddButtonSafe(js, "Add Passport")) {
                    Thread.sleep(4000);
                    Boolean passportResult = fillPassportInfo(js);
                    LogUtil.info("Passport filling result: " + passportResult);
                }
            } else {
                LogUtil.info("Passport section exists, filling missing data");
                Boolean passportResult = fillExistingPassportInfo(js);
                LogUtil.info("Existing passport filling result: " + passportResult);
            }

            Thread.sleep(2000);

            // 10. Add and fill Driver's License
            LogUtil.info("Adding Driver's License field");
            if (clickAddButtonSafe(js, "Add Driver's License")) {
                Thread.sleep(4000);
                Boolean licenseResult = fillDriversLicense(js);
                LogUtil.info("Driver's License result: " + licenseResult);
            }

            Thread.sleep(3000);

            // 11. Take final screenshot
            String finalScreenshotPath = ScreenshotUtils.takeScreenshot("1Day_Lookout_Form_Filled");
            if (finalScreenshotPath != null) {
                ReportManager.attachScreenshot(context.getTestId(), context.getTestName(),
                        finalScreenshotPath, "Form Filled - Ready for Submission");
            }

            LogUtil.info("1-Day Lookout form filling completed successfully");
            return true;

        } catch (Exception e) {
            LogUtil.error("Error filling 1-Day Lookout form", e);
            return false;
        }
    }

    // === NEW ROBUST HELPER METHODS BASED ON FORMFILLER PATTERNS ===

    private void analyzeExistingFormData(JavascriptExecutor js) {
        try {
            LogUtil.info("=== ANALYZING EXISTING FORM DATA ===");

            // Check what's already filled
            String analysis = (String) js.executeScript(
                    "var analysis = [];" +
                            "analysis.push('=== FORM ANALYSIS ===');" +

                            // Check Sex field
                            "var sexSelect = document.querySelector('select, mat-select');" +
                            "if (sexSelect && sexSelect.textContent.includes('FEMALE')) {" +
                            "  analysis.push('Sex: Already filled (FEMALE)');" +
                            "} else if (sexSelect && sexSelect.textContent.includes('MALE')) {" +
                            "  analysis.push('Sex: Already filled (MALE)');" +
                            "} else {" +
                            "  analysis.push('Sex: Not found or empty');" +
                            "}" +

                            // Check Height
                            "var heightSelects = document.querySelectorAll('select, mat-select');" +
                            "var heightFound = false;" +
                            "for (var h = 0; h < heightSelects.length; h++) {" +
                            "  if (heightSelects[h].textContent && (heightSelects[h].textContent.includes('Height') || heightSelects[h].textContent.includes('Select Height'))) {" +
                            "    analysis.push('Height: Found dropdown');" +
                            "    heightFound = true;" +
                            "    break;" +
                            "  }" +
                            "}" +
                            "if (!heightFound) analysis.push('Height: Dropdown not found');" +

                            // Check Citizenship
                            "var citizenshipElements = document.querySelectorAll('*');" +
                            "var foundCitizenship = false;" +
                            "for (var i = 0; i < citizenshipElements.length; i++) {" +
                            "  if (citizenshipElements[i].textContent && citizenshipElements[i].textContent.includes('USA')) {" +
                            "    analysis.push('Citizenship: Already filled (USA)');" +
                            "    foundCitizenship = true;" +
                            "    break;" +
                            "  }" +
                            "}" +
                            "if (!foundCitizenship) analysis.push('Citizenship: Not found');" +

                            // Check Passport
                            "var passportSection = null;" +
                            "var passportHeadings = document.querySelectorAll('.panel-heading, h3, h4, span');" +
                            "for (var p = 0; p < passportHeadings.length; p++) {" +
                            "  if (passportHeadings[p].textContent && passportHeadings[p].textContent.includes('Passport')) {" +
                            "    passportSection = passportHeadings[p].closest('.panel, div');" +
                            "    break;" +
                            "  }" +
                            "}" +
                            "if (passportSection) {" +
                            "  var passportInputs = passportSection.querySelectorAll('input[type=\"text\"]');" +
                            "  var passportSelects = passportSection.querySelectorAll('select, mat-select');" +
                            "  if (passportInputs.length > 0 || passportSelects.length > 0) {" +
                            "    analysis.push('Passport: Section exists - will check/fill missing data');" +
                            "  } else {" +
                            "    analysis.push('Passport: Section empty - will add new passport');" +
                            "  }" +
                            "} else {" +
                            "  analysis.push('Passport: Section not found - will add new passport');" +
                            "}" +

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
            // Look for Primary End Date specifically
            return (Boolean) js.executeScript(
                    "var found = false;" +
                            "var labels = document.querySelectorAll('b, label, span');" +
                            "for (var i = 0; i < labels.length; i++) {" +
                            "  if (labels[i].textContent && labels[i].textContent.includes('Primary End Date')) {" +
                            "    var container = labels[i].closest('div');" +
                            "    if (container) {" +
                            "      var dateInput = container.querySelector('input[mask=\"00/00/0000\"]');" +
                            "      if (dateInput && dateInput.value === '') {" +
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

    private Boolean fillHeightDropdown(JavascriptExecutor js) {
        try {
            LogUtil.info("Looking for Height dropdown in Physical Descriptions");
            return (Boolean) js.executeScript(
                    "return new Promise((resolve) => {" +
                            "  var labels = document.querySelectorAll('label, span, b');" +
                            "  var heightContainer = null;" +
                            "  for (var i = 0; i < labels.length; i++) {" +
                            "    if (labels[i].textContent && labels[i].textContent.includes('Height')) {" +
                            "      heightContainer = labels[i].closest('div');" +
                            "      break;" +
                            "    }" +
                            "  }" +
                            "  if (!heightContainer) {" +
                            "    var selects = document.querySelectorAll('select, mat-select');" +
                            "    for (var j = 0; j < selects.length; j++) {" +
                            "      if (selects[j].textContent && selects[j].textContent.includes('Select Height')) {" +
                            "        heightContainer = selects[j];" +
                            "        break;" +
                            "      }" +
                            "    }" +
                            "  }" +
                            "  if (!heightContainer) { resolve(false); return; }" +
                            "  var heightSelect = heightContainer.querySelector ? heightContainer.querySelector('select, mat-select') : heightContainer;" +
                            "  if (!heightSelect) { resolve(false); return; }" +
                            "  var trigger = heightSelect.querySelector('.mat-select-trigger');" +
                            "  if (trigger) { trigger.click(); } else { heightSelect.click(); }" +
                            "  setTimeout(() => {" +
                            "    var options = document.querySelectorAll('mat-option, option');" +
                            "    for (var k = 0; k < options.length; k++) {" +
                            "      var optionText = options[k].textContent.trim();" +
                            "      if (optionText.includes('5\\'') && optionText.includes('8') && options[k].offsetParent !== null) {" +
                            "        options[k].click();" +
                            "        setTimeout(() => { document.body.click(); resolve(true); }, 500);" +
                            "        return;" +
                            "      }" +
                            "    }" +
                            "    for (var l = 0; l < options.length; l++) {" +
                            "      var optText = options[l].textContent.trim();" +
                            "      if ((optText.includes('5\\'') || optText.includes('6\\'')) && options[l].offsetParent !== null) {" +
                            "        options[l].click();" +
                            "        setTimeout(() => { document.body.click(); resolve(true); }, 500);" +
                            "        return;" +
                            "      }" +
                            "    }" +
                            "    resolve(false);" +
                            "  }, 3000);" +
                            "});"
            );
        } catch (Exception e) {
            LogUtil.error("Error filling height dropdown", e);
            return false;
        }
    }

    private Boolean fillWeightIfEmpty(JavascriptExecutor js) {
        try {
            return (Boolean) js.executeScript(
                    "var weightInput = document.querySelector('input[mask=\"0*\"][maxlength=\"4\"]');" +
                            "if (weightInput && weightInput.value === '') {" +
                            "  weightInput.focus();" +
                            "  weightInput.value = '150';" +
                            "  weightInput.dispatchEvent(new Event('input', {bubbles: true}));" +
                            "  weightInput.dispatchEvent(new Event('change', {bubbles: true}));" +
                            "  weightInput.blur();" +
                            "  return true;" +
                            "}" +
                            "return false;"
            );
        } catch (Exception e) {
            LogUtil.error("Error filling weight", e);
            return false;
        }
    }

    private boolean clickAddButtonSafe(JavascriptExecutor js, String buttonText) {
        try {
            LogUtil.info("Attempting to click: " + buttonText);
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

    private boolean selectFromNewlyAddedDropdown(JavascriptExecutor js, String fieldType, String optionText) {
        try {
            LogUtil.info("Selecting '" + optionText + "' from newly added " + fieldType + " dropdown");

            Boolean result = (Boolean) js.executeScript(
                    "return new Promise((resolve) => {" +
                            "  setTimeout(() => {" +
                            "    var labels = document.querySelectorAll('label, mat-label, span');" +
                            "    var targetDropdown = null;" +
                            "    for (var i = 0; i < labels.length; i++) {" +
                            "      if (labels[i].textContent && labels[i].textContent.includes(arguments[0])) {" +
                            "        var container = labels[i].closest('div, mat-form-field, .tecs-flex-container');" +
                            "        if (container) {" +
                            "          var dropdown = container.querySelector('mat-select:not([aria-disabled=\"true\"])');" +
                            "          if (dropdown) {" +
                            "            targetDropdown = dropdown;" +
                            "            break;" +
                            "          }" +
                            "        }" +
                            "      }" +
                            "    }" +
                            "    if (!targetDropdown) {" +
                            "      var allDropdowns = document.querySelectorAll('mat-select:not([aria-disabled=\"true\"])');" +
                            "      if (allDropdowns.length > 0) {" +
                            "        targetDropdown = allDropdowns[allDropdowns.length - 1];" +
                            "      }" +
                            "    }" +
                            "    if (!targetDropdown) { resolve(false); return; }" +
                            "    var trigger = targetDropdown.querySelector('.mat-select-trigger');" +
                            "    if (!trigger) { resolve(false); return; }" +
                            "    trigger.click();" +
                            "    setTimeout(() => {" +
                            "      var options = document.querySelectorAll('mat-option:not(.mat-option-disabled)');" +
                            "      for (var j = 0; j < options.length; j++) {" +
                            "        if (options[j].offsetParent !== null && options[j].textContent.includes(arguments[1])) {" +
                            "          options[j].click();" +
                            "          setTimeout(() => { document.body.click(); resolve(true); }, 500);" +
                            "          return;" +
                            "        }" +
                            "      }" +
                            "      resolve(false);" +
                            "    }, 3000);" +
                            "  }, 1000);" +
                            "});", fieldType, optionText
            );

            Thread.sleep(4000);
            return result != null && result;
        } catch (Exception e) {
            LogUtil.error("Error selecting from " + fieldType + " dropdown: " + optionText, e);
            return false;
        }
    }

    private boolean fillAnumber(JavascriptExecutor js, String aNumber) {
        try {
            LogUtil.info("Filling A# field with: " + aNumber);
            Thread.sleep(2000);

            Boolean result = (Boolean) js.executeScript(
                    "var inputs = document.querySelectorAll('input[mask=\"0*\"], input[maxlength=\"9\"]');" +
                            "for (var i = inputs.length - 1; i >= 0; i--) {" +
                            "  var input = inputs[i];" +
                            "  var rect = input.getBoundingClientRect();" +
                            "  if (rect.width > 0 && rect.height > 0 && input.value === '') {" +
                            "    input.focus();" +
                            "    input.value = arguments[0];" +
                            "    input.dispatchEvent(new Event('input', {bubbles: true}));" +
                            "    input.dispatchEvent(new Event('change', {bubbles: true}));" +
                            "    input.blur();" +
                            "    return true;" +
                            "  }" +
                            "}" +
                            "return false;", aNumber
            );

            return result != null && result;
        } catch (Exception e) {
            LogUtil.error("Error filling A# field", e);
            return false;
        }
    }

    private boolean fillDriversLicense(JavascriptExecutor js) {
        try {
            LogUtil.info("Filling Driver's License information");
            Thread.sleep(2000);

            // Fill license number
            Boolean licenseNumberResult = (Boolean) js.executeScript(
                    "var inputs = document.querySelectorAll('input[maxlength=\"20\"]');" +
                            "for (var i = inputs.length - 1; i >= 0; i--) {" +
                            "  var input = inputs[i];" +
                            "  var rect = input.getBoundingClientRect();" +
                            "  if (rect.width > 0 && rect.height > 0 && input.value === '') {" +
                            "    input.focus();" +
                            "    input.value = 'DL' + Math.floor(Math.random() * 1000000);" +
                            "    input.dispatchEvent(new Event('input', {bubbles: true}));" +
                            "    input.dispatchEvent(new Event('change', {bubbles: true}));" +
                            "    input.blur();" +
                            "    return true;" +
                            "  }" +
                            "}" +
                            "return false;"
            );

            Thread.sleep(2000);

            // Select state
            Boolean stateResult = (Boolean) js.executeScript(
                    "return new Promise((resolve) => {" +
                            "  var selects = document.querySelectorAll('mat-select:not([aria-disabled=\"true\"])');" +
                            "  if (selects.length === 0) { resolve(false); return; }" +
                            "  var newest = selects[selects.length - 1];" +
                            "  var trigger = newest.querySelector('.mat-select-trigger');" +
                            "  if (!trigger) { resolve(false); return; }" +
                            "  trigger.click();" +
                            "  setTimeout(() => {" +
                            "    var options = document.querySelectorAll('mat-option:not(.mat-option-disabled)');" +
                            "    for (var i = 0; i < options.length; i++) {" +
                            "      var optText = options[i].textContent.trim();" +
                            "      if (options[i].offsetParent !== null && (optText.includes('VA') || optText.includes('DC') || optText.includes('MD'))) {" +
                            "        options[i].click();" +
                            "        setTimeout(() => { document.body.click(); resolve(true); }, 500);" +
                            "        return;" +
                            "      }" +
                            "    }" +
                            "    if (options.length > 5) {" +
                            "      options[5].click();" +
                            "      setTimeout(() => { document.body.click(); resolve(true); }, 500);" +
                            "    } else { resolve(false); }" +
                            "  }, 3000);" +
                            "});"
            );

            LogUtil.info("Driver's License - Number: " + licenseNumberResult + ", State: " + stateResult);
            return licenseNumberResult != null && licenseNumberResult;

        } catch (Exception e) {
            LogUtil.error("Error filling driver's license", e);
            return false;
        }
    }

    private boolean shouldAddPassport(JavascriptExecutor js) {
        try {
            Boolean shouldAdd = (Boolean) js.executeScript(
                    "var passportSection = null;" +
                            "var headings = document.querySelectorAll('.panel-heading, h3, h4, span, label');" +
                            "for (var i = 0; i < headings.length; i++) {" +
                            "  if (headings[i].textContent && headings[i].textContent.includes('Passport')) {" +
                            "    passportSection = headings[i].closest('.panel, div');" +
                            "    break;" +
                            "  }" +
                            "}" +
                            "if (passportSection) {" +
                            "  var inputs = passportSection.querySelectorAll('input[type=\"text\"]');" +
                            "  var selects = passportSection.querySelectorAll('select, mat-select');" +
                            "  if (inputs.length === 0 && selects.length === 0) {" +
                            "    return true;" +
                            "  }" +
                            "  return false;" +
                            "}" +
                            "return true;"
            );
            return shouldAdd != null && shouldAdd;
        } catch (Exception e) {
            LogUtil.error("Error checking passport section", e);
            return true; // If error, assume we should add
        }
    }

    private boolean fillPassportInfo(JavascriptExecutor js) {
        try {
            LogUtil.info("Filling new passport information");
            Thread.sleep(2000);

            // Step 1: Select passport type
            LogUtil.info("Selecting passport type");
            Boolean typeResult = selectFromNewlyAddedDropdown(js, "Type", "R - Regular");
            Thread.sleep(2000);

            // Step 2: Fill passport number
            LogUtil.info("Filling passport number");
            String passportNumber = "PP" + (100000000 + new Random().nextInt(900000000));
            Boolean numberResult = (Boolean) js.executeScript(
                    "var inputs = document.querySelectorAll('input[maxlength=\"20\"]');" +
                            "for (var i = inputs.length - 1; i >= 0; i--) {" +
                            "  var input = inputs[i];" +
                            "  var rect = input.getBoundingClientRect();" +
                            "  if (rect.width > 0 && rect.height > 0 && input.value === '') {" +
                            "    input.focus();" +
                            "    input.value = arguments[0];" +
                            "    input.dispatchEvent(new Event('input', {bubbles: true}));" +
                            "    input.dispatchEvent(new Event('change', {bubbles: true}));" +
                            "    input.blur();" +
                            "    return true;" +
                            "  }" +
                            "}" +
                            "return false;", passportNumber
            );
            Thread.sleep(2000);

            // Step 3: Select passport country
            LogUtil.info("Selecting passport country");
            Boolean countryResult = selectFromNewlyAddedDropdown(js, "Country", "USA - UNITED STATES");
            Thread.sleep(2000);

            // Step 4: Fill issue date
            LogUtil.info("Filling passport issue date");
            String issueDate = generatePastDate(365, 3650); // 1-10 years ago
            Boolean issueDateResult = fillNewestDateInput(js, issueDate);
            Thread.sleep(2000);

            // Step 5: Fill expiry date
            LogUtil.info("Filling passport expiry date");
            String expiryDate = generateFutureDate(365, 3650); // 1-10 years from now
            Boolean expiryDateResult = fillNewestDateInput(js, expiryDate);

            LogUtil.info("Passport filling - Type: " + typeResult + ", Number: " + numberResult +
                    ", Country: " + countryResult + ", Issue: " + issueDateResult + ", Expiry: " + expiryDateResult);

            return numberResult != null && numberResult;

        } catch (Exception e) {
            LogUtil.error("Error filling passport info", e);
            return false;
        }
    }

    private boolean fillExistingPassportInfo(JavascriptExecutor js) {
        try {
            LogUtil.info("Filling missing data in existing passport section");
            Thread.sleep(2000);

            // Check and fill passport number if empty
            Boolean numberResult = (Boolean) js.executeScript(
                    "var passportSection = null;" +
                            "var headings = document.querySelectorAll('.panel-heading, h3, h4, span');" +
                            "for (var i = 0; i < headings.length; i++) {" +
                            "  if (headings[i].textContent && headings[i].textContent.includes('Passport')) {" +
                            "    passportSection = headings[i].closest('.panel, div');" +
                            "    break;" +
                            "  }" +
                            "}" +
                            "if (passportSection) {" +
                            "  var numberInputs = passportSection.querySelectorAll('input[maxlength=\"20\"]');" +
                            "  for (var j = 0; j < numberInputs.length; j++) {" +
                            "    if (numberInputs[j].value === '') {" +
                            "      numberInputs[j].focus();" +
                            "      numberInputs[j].value = 'PP' + Math.floor(Math.random() * 1000000000);" +
                            "      numberInputs[j].dispatchEvent(new Event('input', {bubbles: true}));" +
                            "      numberInputs[j].dispatchEvent(new Event('change', {bubbles: true}));" +
                            "      numberInputs[j].blur();" +
                            "      return true;" +
                            "    }" +
                            "  }" +
                            "}" +
                            "return false;"
            );

            Thread.sleep(2000);

            // Check and fill passport dates if empty
            Boolean datesResult = (Boolean) js.executeScript(
                    "var passportSection = null;" +
                            "var headings = document.querySelectorAll('.panel-heading, h3, h4, span');" +
                            "for (var i = 0; i < headings.length; i++) {" +
                            "  if (headings[i].textContent && headings[i].textContent.includes('Passport')) {" +
                            "    passportSection = headings[i].closest('.panel, div');" +
                            "    break;" +
                            "  }" +
                            "}" +
                            "if (passportSection) {" +
                            "  var dateInputs = passportSection.querySelectorAll('input[mask=\"00/00/0000\"]');" +
                            "  var filled = false;" +
                            "  for (var k = 0; k < dateInputs.length; k++) {" +
                            "    if (dateInputs[k].value === '') {" +
                            "      var isIssueDate = k === 0 || (dateInputs[k].previousElementSibling && dateInputs[k].previousElementSibling.textContent.includes('Issue'));" +
                            "      var dateValue = isIssueDate ? arguments[0] : arguments[1];" +
                            "      dateInputs[k].focus();" +
                            "      dateInputs[k].value = dateValue;" +
                            "      dateInputs[k].dispatchEvent(new Event('input', {bubbles: true}));" +
                            "      dateInputs[k].dispatchEvent(new Event('change', {bubbles: true}));" +
                            "      dateInputs[k].blur();" +
                            "      filled = true;" +
                            "    }" +
                            "  }" +
                            "  return filled;" +
                            "}" +
                            "return false;",
                    generatePastDate(365, 3650),
                    generateFutureDate(365, 3650)
            );

            LogUtil.info("Existing passport filling - Number: " + numberResult + ", Dates: " + datesResult);
            return numberResult != null && numberResult;

        } catch (Exception e) {
            LogUtil.error("Error filling existing passport info", e);
            return false;
        }
    }

    private boolean fillNewestDateInput(JavascriptExecutor js, String date) {
        try {
            return (Boolean) js.executeScript(
                    "var dateInputs = document.querySelectorAll('input[mask=\"00/00/0000\"]');" +
                            "for (var i = dateInputs.length - 1; i >= 0; i--) {" +
                            "  var input = dateInputs[i];" +
                            "  var rect = input.getBoundingClientRect();" +
                            "  if (rect.width > 0 && rect.height > 0 && input.value === '') {" +
                            "    input.focus();" +
                            "    input.value = arguments[0];" +
                            "    input.dispatchEvent(new Event('input', {bubbles: true}));" +
                            "    input.dispatchEvent(new Event('change', {bubbles: true}));" +
                            "    input.blur();" +
                            "    return true;" +
                            "  }" +
                            "}" +
                            "return false;", date
            );
        } catch (Exception e) {
            LogUtil.error("Error filling date input", e);
            return false;
        }
    }

    private String generatePastDate(int minDaysAgo, int maxDaysAgo) {
        Random random = new Random();
        LocalDate date = LocalDate.now()
                .minusDays(minDaysAgo + random.nextInt(maxDaysAgo - minDaysAgo));
        return date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }

    private String generateFutureDate(int minDaysAhead, int maxDaysAhead) {
        Random random = new Random();
        LocalDate date = LocalDate.now()
                .plusDays(minDaysAhead + random.nextInt(maxDaysAhead - minDaysAhead));
        return date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }
}