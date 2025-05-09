package com.umr.core.keyword;

import com.umr.core.DriverManager;
import com.umr.core.TestContext;
import com.umr.core.annotation.Keyword;
import com.umr.utils.LogUtil;
import com.umr.utils.ScreenshotUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Application-specific keywords for business-level test actions.
 * Each keyword handles complete business functions instead of granular UI actions.
 */
public class ApplicationKeywords {

    /**
     * Opens a browser session.
     *
     * @param context The test context
     * @return True if successful, false otherwise
     */
    @Keyword("OPEN_BROWSER")
    public boolean openBrowser(TestContext context) {
        LogUtil.info("Executing OPEN_BROWSER keyword");

        try {
            WebDriver driver = DriverManager.initializeDriver();
            context.setDriver(driver);
            LogUtil.info("Browser opened successfully");
            return true;
        } catch (Exception e) {
            LogUtil.error("Failed to open browser", e);
            context.setTestFailed("Failed to open browser: " + e.getMessage());
            return false;
        }
    }

    /**
     * Closes the browser session.
     *
     * @param context The test context
     * @return True if successful, false otherwise
     */
    @Keyword("CLOSE_BROWSER")
    public boolean closeBrowser(TestContext context) {
        LogUtil.info("Executing CLOSE_BROWSER keyword");

        try {
            DriverManager.quitDriver();
            LogUtil.info("Browser closed successfully");
            return true;
        } catch (Exception e) {
            LogUtil.error("Failed to close browser", e);
            return false;
        }
    }

    /**
     * Performs login to the application.
     * Handles URL navigation and complete login process.
     *
     * @param context The test context
     * @return True if login is successful, false otherwise
     */
    @Keyword("LOGIN")
    public boolean login(TestContext context) {
        LogUtil.info("Executing LOGIN keyword");
        WebDriver driver = context.getDriver();

        try {
            // Get test data
            String url = context.getTestDataAsString("URL");
            String username = context.getTestDataAsString("Username");
            String password = context.getTestDataAsString("Password");

            if (url == null || username == null || password == null) {
                LogUtil.error("Missing required test data for LOGIN: URL, Username, or Password");
                context.setTestFailed("Missing required test data for LOGIN");
                return false;
            }

            // Navigate to URL
            LogUtil.info("Navigating to: " + url);
            driver.get(url);

            // Wait for page to load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));

            // Perform login
            LogUtil.info("Attempting to login with username: " + username);

            // Find and interact with login elements
            WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
            WebElement passwordField = driver.findElement(By.id("password"));
            WebElement loginButton = driver.findElement(By.id("loginButton"));

            // Enter credentials and click login
            usernameField.clear();
            usernameField.sendKeys(username);

            passwordField.clear();
            passwordField.sendKeys(password);

            loginButton.click();

            // Wait for login to complete (wait for dashboard element or error message)
            try {
                // Check for successful login - wait for dashboard element
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(By.className("dashboard-header")),
                        ExpectedConditions.visibilityOfElementLocated(By.className("welcome-message"))
                ));

                // Check if error message is displayed
                if (driver.findElements(By.className("error-message")).size() > 0) {
                    String errorText = driver.findElement(By.className("error-message")).getText();
                    LogUtil.error("Login failed. Error message: " + errorText);
                    context.setTestFailed("Login failed: " + errorText);
                    ScreenshotUtils.takeScreenshot("Login_Failed");
                    return false;
                }

                LogUtil.info("Login successful");
                ScreenshotUtils.takeScreenshot("Login_Successful");
                return true;
            } catch (Exception e) {
                LogUtil.error("Login verification failed", e);
                context.setTestFailed("Login verification failed: " + e.getMessage());
                ScreenshotUtils.takeScreenshot("Login_Verification_Failed");
                return false;
            }
        } catch (Exception e) {
            LogUtil.error("Login process failed", e);
            context.setTestFailed("Login process failed: " + e.getMessage());
            ScreenshotUtils.takeScreenshot("Login_Process_Failed");
            return false;
        }
    }

    /**
     * Validates that the dashboard page is displayed correctly.
     * Verifies expected elements and content.
     *
     * @param context The test context
     * @return True if validation succeeds, false otherwise
     */
    @Keyword("VALIDATE_DASHBOARD")
    public boolean validateDashboard(TestContext context) {
        LogUtil.info("Executing VALIDATE_DASHBOARD keyword");
        WebDriver driver = context.getDriver();

        try {
            // Get expected validation data
            String expectedValue = context.getTestDataAsString("ExpectedValue");

            if (expectedValue == null) {
                LogUtil.error("Missing ExpectedValue in test data for VALIDATE_DASHBOARD");
                context.setTestFailed("Missing ExpectedValue for dashboard validation");
                return false;
            }

            // Wait for dashboard elements
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
            WebElement welcomeMessage = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.className("welcome-message"))
            );

            // Verify welcome message contains expected text
            String actualMessage = welcomeMessage.getText();
            boolean isValid = actualMessage.contains(expectedValue);

            if (isValid) {
                LogUtil.info("Dashboard validation successful. Found text: " + actualMessage);
                ScreenshotUtils.takeScreenshot("Dashboard_Valid");
                return true;
            } else {
                LogUtil.error("Dashboard validation failed. Expected: " + expectedValue + ", Actual: " + actualMessage);
                context.setTestFailed("Dashboard validation failed. Expected: " + expectedValue + ", Actual: " + actualMessage);
                ScreenshotUtils.takeScreenshot("Dashboard_Invalid");
                return false;
            }
        } catch (Exception e) {
            LogUtil.error("Dashboard validation failed", e);
            context.setTestFailed("Dashboard validation failed: " + e.getMessage());
            ScreenshotUtils.takeScreenshot("Dashboard_Validation_Failed");
            return false;
        }
    }

    /**
     * Navigates to a specific section or module in the application.
     * Uses Param1 from test data to determine which section to navigate to.
     *
     * @param context The test context
     * @return True if navigation succeeds, false otherwise
     */
    @Keyword("NAVIGATE_TO_SECTION")
    public boolean navigateToSection(TestContext context) {
        LogUtil.info("Executing NAVIGATE_TO_SECTION keyword");
        WebDriver driver = context.getDriver();

        try {
            // Get section name from test data
            String sectionName = context.getTestDataAsString("Param1");

            if (sectionName == null) {
                LogUtil.error("Missing Param1 (section name) in test data for NAVIGATE_TO_SECTION");
                context.setTestFailed("Missing section name for navigation");
                return false;
            }

            LogUtil.info("Navigating to section: " + sectionName);

            // Find and click on the section link/button
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            // Try different approaches to find the section
            try {
                // Try by link text
                WebElement sectionLink = wait.until(ExpectedConditions.elementToBeClickable(
                        By.linkText(sectionName)
                ));
                sectionLink.click();
            } catch (Exception e1) {
                try {
                    // Try by partial link text
                    WebElement sectionLink = wait.until(ExpectedConditions.elementToBeClickable(
                            By.partialLinkText(sectionName)
                    ));
                    sectionLink.click();
                } catch (Exception e2) {
                    try {
                        // Try by CSS containing text
                        String xpathExpression = "//*[contains(text(),'" + sectionName + "')]";
                        WebElement sectionElement = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath(xpathExpression)
                        ));
                        sectionElement.click();
                    } catch (Exception e3) {
                        LogUtil.error("Could not find section: " + sectionName);
                        context.setTestFailed("Could not find section: " + sectionName);
                        ScreenshotUtils.takeScreenshot("Section_Not_Found");
                        return false;
                    }
                }
            }

            // Wait for page to load after navigation
            wait.until(d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));

            LogUtil.info("Successfully navigated to section: " + sectionName);
            ScreenshotUtils.takeScreenshot("Section_Navigation_Success");
            return true;
        } catch (Exception e) {
            LogUtil.error("Failed to navigate to section", e);
            context.setTestFailed("Failed to navigate to section: " + e.getMessage());
            ScreenshotUtils.takeScreenshot("Section_Navigation_Failed");
            return false;
        }
    }
}