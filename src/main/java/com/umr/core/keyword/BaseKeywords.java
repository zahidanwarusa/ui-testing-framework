package com.umr.core.keyword;

import com.umr.core.DriverManager;
import com.umr.core.TestContext;
import com.umr.core.annotation.Keyword;
import com.umr.core.config.ConfigLoader;
import com.umr.core.factory.PageFactory;
import com.umr.reporting.ReportManager;
import com.umr.utils.LogUtil;
import com.umr.utils.ScreenshotUtils;

import org.openqa.selenium.WebDriver;

/**
 * Implements the basic keywords used in test flows.
 * Each keyword is annotated with @Keyword and takes a TestContext parameter.
 */
public class BaseKeywords {

    private final ConfigLoader config = ConfigLoader.getInstance();

    /**
     * Opens a browser session.
     *
     * @param context The test context
     * @return True if successful, false otherwise
     */
    @Keyword("OPEN_BROWSER")
    public boolean openBrowser(TestContext context) {
        LogUtil.info("Executing OPEN_BROWSER keyword");
        ReportManager.logInfo(context.getTestId(), context.getTestName(), "Executing OPEN_BROWSER keyword");

        try {
            WebDriver driver = DriverManager.initializeDriver();
            context.setDriver(driver);
            LogUtil.info("Browser opened successfully");
            ReportManager.logPass(context.getTestId(), context.getTestName(), "Browser opened successfully");
            return true;
        } catch (Exception e) {
            LogUtil.error("Failed to open browser", e);
            context.setTestFailed("Failed to open browser: " + e.getMessage());
            ReportManager.logFail(context.getTestId(), context.getTestName(), "Failed to open browser: " + e.getMessage());
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
        ReportManager.logInfo(context.getTestId(), context.getTestName(), "Executing CLOSE_BROWSER keyword");

        try {
            DriverManager.quitDriver();
            LogUtil.info("Browser closed successfully");
            ReportManager.logPass(context.getTestId(), context.getTestName(), "Browser closed successfully");
            return true;
        } catch (Exception e) {
            LogUtil.error("Failed to close browser", e);
            ReportManager.logFail(context.getTestId(), context.getTestName(), "Failed to close browser: " + e.getMessage());
            return false;
        }
    }

    /**
     * Navigates to a URL.
     *
     * @param context The test context
     * @return True if successful, false otherwise
     */
    @Keyword("NAVIGATE_TO")
    public boolean navigateTo(TestContext context) {
        LogUtil.info("Executing NAVIGATE_TO keyword");
        ReportManager.logInfo(context.getTestId(), context.getTestName(), "Executing NAVIGATE_TO keyword");

        try {
            String url = context.getTestDataAsString("URL");
            if (url == null || url.isEmpty()) {
                // If URL not provided, use base URL from config
                url = config.getBaseUrl();
                LogUtil.info("URL not provided in test data, using base URL: " + url);
                ReportManager.logInfo(context.getTestId(), context.getTestName(), "URL not provided in test data, using base URL: " + url);
            }

            WebDriver driver = context.getDriver();
            driver.get(url);
            LogUtil.info("Navigated to URL: " + url);
            ReportManager.logPass(context.getTestId(), context.getTestName(), "Navigated to URL: " + url);

            // Capture screenshot
            String screenshotPath = ScreenshotUtils.takeScreenshot("Navigate_To_" + url.replaceAll("[^a-zA-Z0-9]", "_"));
            if (screenshotPath != null) {
                ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), screenshotPath, "Navigation Screenshot");
            }

            return true;
        } catch (Exception e) {
            LogUtil.error("Failed to navigate to URL", e);
            context.setTestFailed("Failed to navigate to URL: " + e.getMessage());
            ReportManager.logFail(context.getTestId(), context.getTestName(), "Failed to navigate to URL: " + e.getMessage());
            return false;
        }
    }

    /**
     * Performs login action.
     *
     * @param context The test context
     * @return True if successful, false otherwise
     */
    @Keyword("LOGIN")
    public boolean login(TestContext context) {
        LogUtil.info("Executing LOGIN keyword");
        ReportManager.logInfo(context.getTestId(), context.getTestName(), "Executing LOGIN keyword");

        try {
            String username = context.getTestDataAsString("Username");
            String password = context.getTestDataAsString("Password");

            if (username == null || password == null) {
                LogUtil.error("Username or password not provided in test data");
                context.setTestFailed("Username or password not provided in test data");
                ReportManager.logFail(context.getTestId(), context.getTestName(), "Username or password not provided in test data");
                return false;
            }

            WebDriver driver = context.getDriver();
            LoginPage loginPage = new LoginPage(driver);

            // Use loginToDice instead of loginToApplication
            ReportManager.logInfo(context.getTestId(), context.getTestName(), "Attempting to login with username: " + username);
            boolean result = loginPage.loginToDice(username, password);

            if (!result) {
                LogUtil.error("Login failed");
                context.setTestFailed("Login failed");
                ReportManager.logFail(context.getTestId(), context.getTestName(), "Login failed");
                String screenshotPath = ScreenshotUtils.takeScreenshot("Login_Failed");
                if (screenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), screenshotPath, "Login Failure");
                }
            } else {
                LogUtil.info("Login successful");
                ReportManager.logPass(context.getTestId(), context.getTestName(), "Login successful");
                String screenshotPath = ScreenshotUtils.takeScreenshot("Login_Successful");
                if (screenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), screenshotPath, "Login Success");
                }
            }

            return result;
        } catch (Exception e) {
            LogUtil.error("Exception during login", e);
            context.setTestFailed("Exception during login: " + e.getMessage());
            ReportManager.logFail(context.getTestId(), context.getTestName(), "Exception during login: " + e.getMessage());
            String screenshotPath = ScreenshotUtils.takeScreenshot("Login_Exception");
            if (screenshotPath != null) {
                ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), screenshotPath, "Login Exception");
            }
            return false;
        }
    }

    /**
     * Clicks on an element.
     *
     * @param context The test context
     * @return True if successful, false otherwise
     */
    @Keyword("CLICK")
    public boolean click(TestContext context) {
        LogUtil.info("Executing CLICK keyword");
        ReportManager.logInfo(context.getTestId(), context.getTestName(), "Executing CLICK keyword");

        try {
            String pageName = context.getTestDataAsString("Page");
            String elementName = context.getTestDataAsString("Element");

            if (pageName == null || elementName == null) {
                LogUtil.error("Page or Element not provided in test data");
                context.setTestFailed("Page or Element not provided in test data");
                ReportManager.logFail(context.getTestId(), context.getTestName(), "Page or Element not provided in test data");
                return false;
            }

            WebDriver driver = context.getDriver();
            BasePage page = PageFactory.getPage(pageName, driver);

            ReportManager.logInfo(context.getTestId(), context.getTestName(),
                    "Clicking element: " + elementName + " on page: " + pageName);
            boolean result = page.clickElement(elementName);

            if (!result) {
                LogUtil.error("Failed to click element: " + elementName + " on page: " + pageName);
                context.setTestFailed("Failed to click element: " + elementName);
                ReportManager.logFail(context.getTestId(), context.getTestName(),
                        "Failed to click element: " + elementName + " on page: " + pageName);
                String screenshotPath = ScreenshotUtils.takeScreenshot("Click_Failed");
                if (screenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), screenshotPath, "Click Failure");
                }
            } else {
                LogUtil.info("Successfully clicked element: " + elementName + " on page: " + pageName);
                ReportManager.logPass(context.getTestId(), context.getTestName(),
                        "Successfully clicked element: " + elementName + " on page: " + pageName);
                String screenshotPath = ScreenshotUtils.takeScreenshot("Click_Success");
                if (screenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), screenshotPath, "Click Success");
                }
            }

            return result;
        } catch (Exception e) {
            LogUtil.error("Exception during click operation", e);
            context.setTestFailed("Exception during click operation: " + e.getMessage());
            ReportManager.logFail(context.getTestId(), context.getTestName(), "Exception during click operation: " + e.getMessage());
            String screenshotPath = ScreenshotUtils.takeScreenshot("Click_Exception");
            if (screenshotPath != null) {
                ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), screenshotPath, "Click Exception");
            }
            return false;
        }
    }

    /**
     * Types text into an element.
     *
     * @param context The test context
     * @return True if successful, false otherwise
     */
    @Keyword("TYPE")
    public boolean type(TestContext context) {
        LogUtil.info("Executing TYPE keyword");
        ReportManager.logInfo(context.getTestId(), context.getTestName(), "Executing TYPE keyword");

        try {
            String pageName = context.getTestDataAsString("Page");
            String elementName = context.getTestDataAsString("Element");
            String text = context.getTestDataAsString("Text");

            if (pageName == null || elementName == null || text == null) {
                LogUtil.error("Page, Element, or Text not provided in test data");
                context.setTestFailed("Page, Element, or Text not provided in test data");
                ReportManager.logFail(context.getTestId(), context.getTestName(), "Page, Element, or Text not provided in test data");
                return false;
            }

            WebDriver driver = context.getDriver();
            BasePage page = PageFactory.getPage(pageName, driver);

            ReportManager.logInfo(context.getTestId(), context.getTestName(),
                    "Typing text into element: " + elementName + " on page: " + pageName);
            boolean result = page.typeIntoElement(elementName, text);

            if (!result) {
                LogUtil.error("Failed to type text into element: " + elementName + " on page: " + pageName);
                context.setTestFailed("Failed to type text into element: " + elementName);
                ReportManager.logFail(context.getTestId(), context.getTestName(),
                        "Failed to type text into element: " + elementName + " on page: " + pageName);
                String screenshotPath = ScreenshotUtils.takeScreenshot("Type_Failed");
                if (screenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), screenshotPath, "Type Failure");
                }
            } else {
                LogUtil.info("Successfully typed text into element: " + elementName + " on page: " + pageName);
                ReportManager.logPass(context.getTestId(), context.getTestName(),
                        "Successfully typed text into element: " + elementName + " on page: " + pageName);
                String screenshotPath = ScreenshotUtils.takeScreenshot("Type_Success");
                if (screenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), screenshotPath, "Type Success");
                }
            }

            return result;
        } catch (Exception e) {
            LogUtil.error("Exception during type operation", e);
            context.setTestFailed("Exception during type operation: " + e.getMessage());
            ReportManager.logFail(context.getTestId(), context.getTestName(), "Exception during type operation: " + e.getMessage());
            String screenshotPath = ScreenshotUtils.takeScreenshot("Type_Exception");
            if (screenshotPath != null) {
                ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), screenshotPath, "Type Exception");
            }
            return false;
        }
    }

    /**
     * Verifies the state of an element.
     *
     * @param context The test context
     * @return True if verification passes, false otherwise
     */
    @Keyword("VERIFY_ELEMENT")
    public boolean verifyElement(TestContext context) {
        LogUtil.info("Executing VERIFY_ELEMENT keyword");
        ReportManager.logInfo(context.getTestId(), context.getTestName(), "Executing VERIFY_ELEMENT keyword");

        try {
            String pageName = context.getTestDataAsString("Page");
            String elementName = context.getTestDataAsString("Element");
            String expectedState = context.getTestDataAsString("State");

            if (pageName == null || elementName == null || expectedState == null) {
                LogUtil.error("Page, Element, or State not provided in test data");
                context.setTestFailed("Page, Element, or State not provided in test data");
                ReportManager.logFail(context.getTestId(), context.getTestName(), "Page, Element, or State not provided in test data");
                return false;
            }

            WebDriver driver = context.getDriver();
            BasePage page = PageFactory.getPage(pageName, driver);

            ReportManager.logInfo(context.getTestId(), context.getTestName(),
                    "Verifying element: " + elementName + " on page: " + pageName + " is in state: " + expectedState);
            boolean result = page.verifyElementState(elementName, expectedState);

            if (!result) {
                LogUtil.error("Element verification failed. Element: " + elementName +
                        " on page: " + pageName + " is not in state: " + expectedState);
                context.setTestFailed("Element verification failed: " + elementName);
                ReportManager.logFail(context.getTestId(), context.getTestName(),
                        "Element verification failed. Element: " + elementName + " on page: " + pageName + " is not in state: " + expectedState);
                String screenshotPath = ScreenshotUtils.takeScreenshot("Verify_Failed");
                if (screenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), screenshotPath, "Verification Failure");
                }
            } else {
                LogUtil.info("Element verification successful. Element: " + elementName +
                        " on page: " + pageName + " is in state: " + expectedState);
                ReportManager.logPass(context.getTestId(), context.getTestName(),
                        "Element verification successful. Element: " + elementName + " on page: " + pageName + " is in state: " + expectedState);
                String screenshotPath = ScreenshotUtils.takeScreenshot("Verify_Success");
                if (screenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), screenshotPath, "Verification Success");
                }
            }

            return result;
        } catch (Exception e) {
            LogUtil.error("Exception during element verification", e);
            context.setTestFailed("Exception during element verification: " + e.getMessage());
            ReportManager.logFail(context.getTestId(), context.getTestName(), "Exception during element verification: " + e.getMessage());
            String screenshotPath = ScreenshotUtils.takeScreenshot("Verify_Exception");
            if (screenshotPath != null) {
                ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), screenshotPath, "Verification Exception");
            }
            return false;
        }
    }

    /**
     * Waits for a specified number of seconds.
     *
     * @param context The test context
     * @return True if successful, false otherwise
     */
    @Keyword("WAIT")
    public boolean wait(TestContext context) {
        LogUtil.info("Executing WAIT keyword");
        ReportManager.logInfo(context.getTestId(), context.getTestName(), "Executing WAIT keyword");

        try {
            String secondsStr = context.getTestDataAsString("Seconds");

            if (secondsStr == null) {
                LogUtil.error("Seconds not provided in test data");
                context.setTestFailed("Seconds not provided in test data");
                ReportManager.logFail(context.getTestId(), context.getTestName(), "Seconds not provided in test data");
                return false;
            }

            int seconds = Integer.parseInt(secondsStr);
            LogUtil.info("Waiting for " + seconds + " seconds");
            ReportManager.logInfo(context.getTestId(), context.getTestName(), "Waiting for " + seconds + " seconds");

            Thread.sleep(seconds * 1000L);

            LogUtil.info("Wait completed successfully");
            ReportManager.logPass(context.getTestId(), context.getTestName(), "Wait completed successfully");
            return true;
        } catch (NumberFormatException e) {
            LogUtil.error("Invalid seconds value in test data", e);
            context.setTestFailed("Invalid seconds value in test data");
            ReportManager.logFail(context.getTestId(), context.getTestName(), "Invalid seconds value in test data");
            return false;
        } catch (InterruptedException e) {
            LogUtil.error("Wait interrupted", e);
            Thread.currentThread().interrupt();
            ReportManager.logFail(context.getTestId(), context.getTestName(), "Wait interrupted");
            return false;
        }
    }
}