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
            LogUtil.info("Filling 1-Day Lookout form");
            
            // Wait for form to load
            Thread.sleep(10000);
            
            // 1. Fill Remarks (Required field)
            LogUtil.info("Filling remarks field");
            String remarks = "Automated 1-Day Lookout - Created at " + System.currentTimeMillis() + " - Subject requires review";
            Boolean remarksResult = (Boolean) js.executeScript(
                "var textarea = document.querySelector('textarea[maxlength=\"3000\"]');" +
                "if (textarea) {" +
                "  textarea.focus();" +
                "  textarea.value = arguments[0];" +
                "  textarea.dispatchEvent(new Event('input', {bubbles: true}));" +
                "  textarea.dispatchEvent(new Event('change', {bubbles: true}));" +
                "  textarea.blur();" +
                "  return true;" +
                "}" +
                "return false;", remarks
            );
            LogUtil.info("Remarks filled: " + remarksResult);

            Thread.sleep(2000);

            // 2. Fill Primary End Date (Required field)
            LogUtil.info("Filling primary end date");
            String endDate = generateFutureDate(1, 30); // 1-30 days from now
            Boolean endDateResult = (Boolean) js.executeScript(
                "var dateInputs = document.querySelectorAll('input[mask=\"00/00/0000\"]');" +
                "for (var i = 0; i < dateInputs.length; i++) {" +
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
                "return false;", endDate
            );
            LogUtil.info("End date filled: " + endDateResult);

            Thread.sleep(2000);

            // 3. Add and fill Sex if not already present
            if (!isFieldAlreadyFilled(js, "Sex")) {
                LogUtil.info("Adding Sex field");
                if (clickAddButton(js, "Add Sex")) {
                    Thread.sleep(3000);
                    selectFromNewestDropdown(js, "M - MALE");
                }
            }

            // 4. Add and fill Race if not already present
            if (!isFieldAlreadyFilled(js, "Race")) {
                LogUtil.info("Adding Race field");
                if (clickAddButton(js, "Add Race")) {
                    Thread.sleep(3000);
                    selectFromNewestDropdown(js, "A - ASIAN");
                }
            }

            // 5. Add and fill Eye Color if not already present
            if (!isFieldAlreadyFilled(js, "Eye Color")) {
                LogUtil.info("Adding Eye Color field");
                if (clickAddButton(js, "Add Eye Color")) {
                    Thread.sleep(3000);
                    selectFromNewestDropdown(js, "BG - BLUE/GREEN");
                }
            }

            // 6. Add and fill Hair Color if not already present
            if (!isFieldAlreadyFilled(js, "Hair Color")) {
                LogUtil.info("Adding Hair Color field");
                if (clickAddButton(js, "Add Hair Color")) {
                    Thread.sleep(3000);
                    selectFromNewestDropdown(js, "BA - BALD");
                }
            }

            // 7. Fill Weight if empty
            LogUtil.info("Filling weight field");
            Boolean weightResult = (Boolean) js.executeScript(
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

            Thread.sleep(2000);

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

    private boolean isFieldAlreadyFilled(JavascriptExecutor js, String fieldName) {
        try {
            Boolean result = (Boolean) js.executeScript(
                "var labels = document.querySelectorAll('label, mat-label, span');" +
                "for (var i = 0; i < labels.length; i++) {" +
                "  if (labels[i].textContent.includes('" + fieldName + "')) {" +
                "    var container = labels[i].closest('.panel, .tecs-flex-container, mat-form-field');" +
                "    if (container) {" +
                "      var selects = container.querySelectorAll('select, mat-select');" +
                "      var inputs = container.querySelectorAll('input');" +
                "      for (var j = 0; j < selects.length; j++) {" +
                "        if (selects[j].value && selects[j].value !== '') return true;" +
                "      }" +
                "      for (var k = 0; k < inputs.length; k++) {" +
                "        if (inputs[k].value && inputs[k].value !== '') return true;" +
                "      }" +
                "    }" +
                "  }" +
                "}" +
                "return false;"
            );
            return result != null && result;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean clickAddButton(JavascriptExecutor js, String buttonText) {
        try {
            Boolean result = (Boolean) js.executeScript(
                "var buttons = document.querySelectorAll('button');" +
                "for (var i = 0; i < buttons.length; i++) {" +
                "  if (buttons[i].textContent.includes('" + buttonText + "')) {" +
                "    buttons[i].scrollIntoView({behavior: 'smooth', block: 'center'});" +
                "    setTimeout(() => buttons[i].click(), 500);" +
                "    return true;" +
                "  }" +
                "}" +
                "return false;"
            );
            return result != null && result;
        } catch (Exception e) {
            LogUtil.error("Error clicking add button: " + buttonText, e);
            return false;
        }
    }

    private boolean selectFromNewestDropdown(JavascriptExecutor js, String optionText) {
        try {
            Thread.sleep(1000);
            Boolean result = (Boolean) js.executeScript(
                "var selects = document.querySelectorAll('mat-select:not([aria-disabled=\"true\"])');" +
                "if (selects.length === 0) return false;" +
                "var newest = selects[selects.length - 1];" +
                "var trigger = newest.querySelector('.mat-select-trigger');" +
                "if (!trigger) return false;" +
                "trigger.click();" +
                "setTimeout(() => {" +
                "  var options = document.querySelectorAll('mat-option');" +
                "  for (var i = 0; i < options.length; i++) {" +
                "    if (options[i].offsetParent !== null && options[i].textContent.includes('" + optionText + "')) {" +
                "      options[i].click();" +
                "      setTimeout(() => document.body.click(), 300);" +
                "      return;" +
                "    }" +
                "  }" +
                "}, 2000);" +
                "return true;"
            );
            Thread.sleep(3000);
            return result != null && result;
        } catch (Exception e) {
            LogUtil.error("Error selecting from dropdown: " + optionText, e);
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