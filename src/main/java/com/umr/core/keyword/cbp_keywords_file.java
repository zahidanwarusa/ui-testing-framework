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

            // 3. Fill Weight if empty
            LogUtil.info("Checking and filling Weight field");
            Boolean weightResult = fillWeightIfEmpty(js);
            LogUtil.info("Weight field result: " + weightResult);

            Thread.sleep(2000);

            // 4. Add A# if section is empty (important field)
            LogUtil.info("Checking A# section");
            if (isAnumberSectionEmpty(js)) {
                LogUtil.info("Adding A# - section appears empty");
                if (clickAddButtonSafe(js, "Add A#")) {
                    Thread.sleep(3000);
                    String aNumber = "123456789"; // Generate valid A# format
                    fillAnumber(js, aNumber);
                }
            } else {
                LogUtil.info("A# section already has data, skipping");
            }

            Thread.sleep(2000);

            // 5. Only add Race if it's completely missing (not just empty dropdown)
            if (shouldAddRaceField(js)) {
                LogUtil.info("Race field not found, adding it");
                if (clickAddButtonSafe(js, "Add Race")) {
                    Thread.sleep(3000);
                    selectFromSpecificNewDropdown(js, "Race", "A - ASIAN");
                }
            } else {
                LogUtil.info("Race field exists, skipping add");
            }

            Thread.sleep(2000);

            // 6. Only add Eye Color if completely missing
            if (shouldAddEyeColorField(js)) {
                LogUtil.info("Eye Color field not found, adding it");
                if (clickAddButtonSafe(js, "Add Eye Color")) {
                    Thread.sleep(3000);
                    selectFromSpecificNewDropdown(js, "Eye Color", "BG - BLUE/GREEN");
                }
            } else {
                LogUtil.info("Eye Color field exists, skipping add");
            }

            Thread.sleep(2000);

            // 7. Only add Hair Color if completely missing
            if (shouldAddHairColorField(js)) {
                LogUtil.info("Hair Color field not found, adding it");
                if (clickAddButtonSafe(js, "Add Hair Color")) {
                    Thread.sleep(3000);
                    selectFromSpecificNewDropdown(js, "Hair Color", "BA - BALD");
                }
            } else {
                LogUtil.info("Hair Color field exists, skipping add");
            }

            Thread.sleep(3000);

            // 8. Take final screenshot
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

    private boolean isAnumberSectionEmpty(JavascriptExecutor js) {
        try {
            Boolean isEmpty = (Boolean) js.executeScript(
                    "var aNumberSection = null;" +
                            "var headings = document.querySelectorAll('.panel-heading, h3, h4, span');" +
                            "for (var i = 0; i < headings.length; i++) {" +
                            "  if (headings[i].textContent && headings[i].textContent.includes('A#')) {" +
                            "    aNumberSection = headings[i].closest('.panel, div');" +
                            "    break;" +
                            "  }" +
                            "}" +
                            "if (aNumberSection) {" +
                            "  var inputs = aNumberSection.querySelectorAll('input');" +
                            "  for (var j = 0; j < inputs.length; j++) {" +
                            "    if (inputs[j].value && inputs[j].value !== '') {" +
                            "      return false;" +
                            "    }" +
                            "  }" +
                            "  return true;" +
                            "}" +
                            "return true;"
            );
            return isEmpty != null && isEmpty;
        } catch (Exception e) {
            LogUtil.error("Error checking A# section", e);
            return true; // Assume empty if error
        }
    }

    private boolean shouldAddRaceField(JavascriptExecutor js) {
        try {
            Boolean shouldAdd = (Boolean) js.executeScript(
                    "var labels = document.querySelectorAll('label, mat-label, span, b');" +
                            "for (var i = 0; i < labels.length; i++) {" +
                            "  if (labels[i].textContent && labels[i].textContent.includes('Race')) {" +
                            "    var container = labels[i].closest('.panel, .tecs-flex-container, div');" +
                            "    if (container) {" +
                            "      var selects = container.querySelectorAll('select, mat-select');" +
                            "      if (selects.length > 0) {" +
                            "        return false;" +
                            "      }" +
                            "    }" +
                            "  }" +
                            "}" +
                            "return true;"
            );
            return shouldAdd != null && shouldAdd;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean shouldAddEyeColorField(JavascriptExecutor js) {
        try {
            Boolean shouldAdd = (Boolean) js.executeScript(
                    "var labels = document.querySelectorAll('label, mat-label, span, b');" +
                            "for (var i = 0; i < labels.length; i++) {" +
                            "  if (labels[i].textContent && labels[i].textContent.includes('Eye Color')) {" +
                            "    return false;" +
                            "  }" +
                            "}" +
                            "return true;"
            );
            return shouldAdd != null && shouldAdd;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean shouldAddHairColorField(JavascriptExecutor js) {
        try {
            Boolean shouldAdd = (Boolean) js.executeScript(
                    "var labels = document.querySelectorAll('label, mat-label, span, b');" +
                            "for (var i = 0; i < labels.length; i++) {" +
                            "  if (labels[i].textContent && labels[i].textContent.includes('Hair Color')) {" +
                            "    return false;" +
                            "  }" +
                            "}" +
                            "return true;"
            );
            return shouldAdd != null && shouldAdd;
        } catch (Exception e) {
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

    private boolean selectFromSpecificNewDropdown(JavascriptExecutor js, String fieldContext, String optionText) {
        try {
            LogUtil.info("Selecting '" + optionText + "' for " + fieldContext + " field");
            Thread.sleep(1000);

            Boolean result = (Boolean) js.executeScript(
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
                            "      if (options[i].offsetParent !== null && options[i].textContent.includes(arguments[0])) {" +
                            "        options[i].click();" +
                            "        setTimeout(() => { document.body.click(); resolve(true); }, 500);" +
                            "        return;" +
                            "      }" +
                            "    }" +
                            "    resolve(false);" +
                            "  }, 2000);" +
                            "});", optionText
            );

            Thread.sleep(3000);
            return result != null && result;
        } catch (Exception e) {
            LogUtil.error("Error selecting from dropdown: " + optionText, e);
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

    private String generateFutureDate(int minDaysAhead, int maxDaysAhead) {
        Random random = new Random();
        LocalDate date = LocalDate.now()
                .plusDays(minDaysAhead + random.nextInt(maxDaysAhead - minDaysAhead));
        return date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
    }
}