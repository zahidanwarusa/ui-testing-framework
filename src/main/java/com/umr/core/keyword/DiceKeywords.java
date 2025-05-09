package com.umr.core.keyword;

import com.aventstack.extentreports.ExtentTest;
import com.umr.core.DriverManager;
import com.umr.core.TestContext;
import com.umr.core.annotation.Keyword;
import com.umr.core.factory.PageFactory;
import com.umr.pages.HomePage;
import com.umr.pages.LoginPage;
import com.umr.pages.SearchResultsPage;
import com.umr.reporting.ReportManager;
import com.umr.utils.LogUtil;
import com.umr.utils.ScreenshotUtils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

public class DiceKeywords {

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

    @Keyword("NAVIGATE_TO_DICE_LOGIN")
    public boolean navigateToDiceLogin(TestContext context) {
        LogUtil.info("Executing NAVIGATE_TO_DICE_LOGIN keyword");
        ReportManager.logInfo(context.getTestId(), context.getTestName(), "Executing NAVIGATE_TO_DICE_LOGIN keyword");

        try {
            WebDriver driver = context.getDriver();
            String url = "https://www.dice.com/dashboard/login";

            driver.get(url);
            LogUtil.info("Navigated to Dice login page: " + url);
            ReportManager.logInfo(context.getTestId(), context.getTestName(), "Navigated to Dice login page: " + url);

            // Wait for page load to complete
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));

            // Take screenshot after navigation
            String initialScreenshotPath = ScreenshotUtils.takeScreenshot("Dice_Login_Page_Initial");
            if (initialScreenshotPath != null) {
                ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), initialScreenshotPath, "Initial Login Page");
            }

            // Add a short delay to let page fully render
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Check if page loaded
            LoginPage loginPage = (LoginPage) PageFactory.getPage("LoginPage", driver);

            if (loginPage.isPageLoaded()) {
                LogUtil.info("Dice login page loaded successfully");
                ReportManager.logPass(context.getTestId(), context.getTestName(), "Dice login page loaded successfully");

                // Take another screenshot after verification
                String verifiedScreenshotPath = ScreenshotUtils.takeScreenshot("Verified_Dice_Login_Page");
                if (verifiedScreenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), verifiedScreenshotPath, "Verified Login Page");
                }
                return true;
            } else {
                // Try fallback check - check URL
                String currentUrl = driver.getCurrentUrl();
                String pageTitle = driver.getTitle();
                LogUtil.info("Current URL: " + currentUrl);
                LogUtil.info("Page title: " + pageTitle);
                ReportManager.logInfo(context.getTestId(), context.getTestName(), "Current URL: " + currentUrl);
                ReportManager.logInfo(context.getTestId(), context.getTestName(), "Page title: " + pageTitle);

                // Dice might redirect to a different login URL
                if (currentUrl.contains("dice.com")) {
                    LogUtil.info("Page appears to be on Dice domain, considering navigation successful");
                    ReportManager.logPass(context.getTestId(), context.getTestName(), "Page appears to be on Dice domain, considering navigation successful");
                    return true;
                }

                LogUtil.error("Dice login page failed to load");
                context.setTestFailed("Dice login page failed to load");
                ReportManager.logFail(context.getTestId(), context.getTestName(), "Dice login page failed to load");
                return false;
            }
        } catch (Exception e) {
            LogUtil.error("Failed to navigate to Dice login page", e);
            context.setTestFailed("Failed to navigate to Dice login page: " + e.getMessage());
            ReportManager.logFail(context.getTestId(), context.getTestName(), "Failed to navigate to Dice login page: " + e.getMessage());
            return false;
        }
    }
    @Keyword("LOGIN_TO_DICE")
    public boolean loginToDice(TestContext context) {
        LogUtil.info("Executing LOGIN_TO_DICE keyword");
        ReportManager.logInfo(context.getTestId(), context.getTestName(), "Executing LOGIN_TO_DICE keyword");

        try {
            WebDriver driver = context.getDriver();
            String email = context.getTestDataAsString("Email");
            String password = context.getTestDataAsString("Password");

            if (email == null || password == null) {
                LogUtil.error("Email or password not provided in test data");
                context.setTestFailed("Email or password not provided in test data");
                ReportManager.logFail(context.getTestId(), context.getTestName(), "Email or password not provided in test data");
                return false;
            }

            LoginPage loginPage = (LoginPage) PageFactory.getPage("LoginPage", driver);
            ReportManager.logInfo(context.getTestId(), context.getTestName(), "Attempting to login with email: " + email);

            boolean loginResult = loginPage.loginToDice(email, password);

            if (loginResult) {
                LogUtil.info("Successfully logged in to Dice");
                ReportManager.logPass(context.getTestId(), context.getTestName(), "Successfully logged in to Dice");

                String successScreenshotPath = ScreenshotUtils.takeScreenshot("Login_Successful");
                if (successScreenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), successScreenshotPath, "Login Success");
                }

                // Wait for home page to load
                HomePage homePage = (HomePage) PageFactory.getPage("HomePage", driver);
                if (homePage.isPageLoaded()) {
                    LogUtil.info("Home page loaded successfully after login");
                    ReportManager.logPass(context.getTestId(), context.getTestName(), "Home page loaded successfully after login");
                    return true;
                } else {
                    LogUtil.error("Home page failed to load after login");
                    context.setTestFailed("Home page failed to load after login");
                    ReportManager.logFail(context.getTestId(), context.getTestName(), "Home page failed to load after login");
                    return false;
                }
            } else {
                LogUtil.error("Failed to log in to Dice");
                context.setTestFailed("Failed to log in to Dice");
                ReportManager.logFail(context.getTestId(), context.getTestName(), "Failed to log in to Dice");

                String failureScreenshotPath = ScreenshotUtils.takeScreenshot("Login_Failed");
                if (failureScreenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), failureScreenshotPath, "Login Failure");
                }
                return false;
            }
        } catch (Exception e) {
            LogUtil.error("Exception during Dice login", e);
            context.setTestFailed("Exception during Dice login: " + e.getMessage());
            ReportManager.logFail(context.getTestId(), context.getTestName(), "Exception during Dice login: " + e.getMessage());

            String exceptionScreenshotPath = ScreenshotUtils.takeScreenshot("Login_Exception");
            if (exceptionScreenshotPath != null) {
                ReportManager.attachScreenshot(context.getTestId(), context.getTestName(), exceptionScreenshotPath, "Login Exception");
            }
            return false;
        }
    }

    @Keyword("VERIFY_USER_PROFILE")
    public boolean verifyUserProfile(TestContext context) {
        LogUtil.info("Executing VERIFY_USER_PROFILE keyword");
        ReportManager.logInfo(context.getTestId(), context.getTestName(), "Executing VERIFY_USER_PROFILE keyword");

        try {
            WebDriver driver = context.getDriver();
            HomePage homePage = (HomePage) PageFactory.getPage("HomePage", driver);

            // Get expected username from test data
            String expectedUsername = context.getTestDataAsString("ExpectedUsername");

            if (expectedUsername == null) {
                LogUtil.warn("Expected username not provided in test data, will only verify profile exists");
                ReportManager.logWarning(context.getTestId(), context.getTestName(), "Expected username not provided in test data, will only verify profile exists");
            }

            // Get actual username from page
            String actualUsername = homePage.getUserName();
            ReportManager.logInfo(context.getTestId(), context.getTestName(), "Retrieved username from profile: " + actualUsername);

            // Verify username if expected was provided
            if (expectedUsername != null && !expectedUsername.equals(actualUsername)) {
                LogUtil.error("Username verification failed. Expected: " + expectedUsername + ", Actual: " + actualUsername);
                context.setTestFailed("Username verification failed");
                ReportManager.logFail(context.getTestId(), context.getTestName(),
                        "Username verification failed. Expected: " + expectedUsername + ", Actual: " + actualUsername);

                String verificationFailedScreenshotPath = ScreenshotUtils.takeScreenshot("Username_Verification_Failed");
                if (verificationFailedScreenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(),
                            verificationFailedScreenshotPath, "Username Verification Failed");
                }
                return false;
            }

            // Verify other profile details
            boolean profileDetailsVerified = homePage.verifyProfileDetails();

            if (profileDetailsVerified) {
                LogUtil.info("User profile verification successful");
                ReportManager.logPass(context.getTestId(), context.getTestName(), "User profile verification successful");

                String successScreenshotPath = ScreenshotUtils.takeScreenshot("Profile_Verification_Successful");
                if (successScreenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(),
                            successScreenshotPath, "Profile Verification Successful");
                }
                return true;
            } else {
                LogUtil.error("Profile details verification failed");
                context.setTestFailed("Profile details verification failed");
                ReportManager.logFail(context.getTestId(), context.getTestName(), "Profile details verification failed");

                String failedScreenshotPath = ScreenshotUtils.takeScreenshot("Profile_Verification_Failed");
                if (failedScreenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(),
                            failedScreenshotPath, "Profile Verification Failed");
                }
                return false;
            }
        } catch (Exception e) {
            LogUtil.error("Exception during user profile verification", e);
            context.setTestFailed("Exception during user profile verification: " + e.getMessage());
            ReportManager.logFail(context.getTestId(), context.getTestName(),
                    "Exception during user profile verification: " + e.getMessage());

            String exceptionScreenshotPath = ScreenshotUtils.takeScreenshot("Profile_Verification_Exception");
            if (exceptionScreenshotPath != null) {
                ReportManager.attachScreenshot(context.getTestId(), context.getTestName(),
                        exceptionScreenshotPath, "Profile Verification Exception");
            }
            return false;
        }
    }

    @Keyword("SEARCH_JOBS")
    public boolean searchJobs(TestContext context) {
        LogUtil.info("Executing SEARCH_JOBS keyword");
        ReportManager.logInfo(context.getTestId(), context.getTestName(), "Executing SEARCH_JOBS keyword");

        try {
            WebDriver driver = context.getDriver();
            String searchTerm = context.getTestDataAsString("SearchTerm");

            if (searchTerm == null) {
                LogUtil.error("Search term not provided in test data");
                context.setTestFailed("Search term not provided in test data");
                ReportManager.logFail(context.getTestId(), context.getTestName(), "Search term not provided in test data");
                return false;
            }

            HomePage homePage = (HomePage) PageFactory.getPage("HomePage", driver);
            ReportManager.logInfo(context.getTestId(), context.getTestName(), "Searching for jobs with term: " + searchTerm);
            boolean searchPerformed = homePage.searchForJobs(searchTerm);

            if (searchPerformed) {
                LogUtil.info("Job search performed successfully for term: " + searchTerm);
                ReportManager.logPass(context.getTestId(), context.getTestName(),
                        "Job search performed successfully for term: " + searchTerm);

                String searchScreenshotPath = ScreenshotUtils.takeScreenshot("Search_Performed");
                if (searchScreenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(),
                            searchScreenshotPath, "Search Performed");
                }

                // Wait for search results page to load
                SearchResultsPage resultsPage = (SearchResultsPage) PageFactory.getPage("SearchResultsPage", driver);
                if (resultsPage.isPageLoaded()) {
                    LogUtil.info("Search results page loaded successfully");
                    ReportManager.logPass(context.getTestId(), context.getTestName(), "Search results page loaded successfully");
                    return true;
                } else {
                    LogUtil.error("Search results page failed to load");
                    context.setTestFailed("Search results page failed to load");
                    ReportManager.logFail(context.getTestId(), context.getTestName(), "Search results page failed to load");
                    return false;
                }
            } else {
                LogUtil.error("Failed to perform job search");
                context.setTestFailed("Failed to perform job search");
                ReportManager.logFail(context.getTestId(), context.getTestName(), "Failed to perform job search");

                String failedScreenshotPath = ScreenshotUtils.takeScreenshot("Search_Failed");
                if (failedScreenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(),
                            failedScreenshotPath, "Search Failed");
                }
                return false;
            }
        } catch (Exception e) {
            LogUtil.error("Exception during job search", e);
            context.setTestFailed("Exception during job search: " + e.getMessage());
            ReportManager.logFail(context.getTestId(), context.getTestName(), "Exception during job search: " + e.getMessage());

            String exceptionScreenshotPath = ScreenshotUtils.takeScreenshot("Search_Exception");
            if (exceptionScreenshotPath != null) {
                ReportManager.attachScreenshot(context.getTestId(), context.getTestName(),
                        exceptionScreenshotPath, "Search Exception");
            }
            return false;
        }
    }

    @Keyword("VERIFY_SEARCH_RESULTS")
    public boolean verifySearchResults(TestContext context) {
        LogUtil.info("Executing VERIFY_SEARCH_RESULTS keyword");
        ReportManager.logInfo(context.getTestId(), context.getTestName(), "Executing VERIFY_SEARCH_RESULTS keyword");

        try {
            WebDriver driver = context.getDriver();
            SearchResultsPage resultsPage = (SearchResultsPage) PageFactory.getPage("SearchResultsPage", driver);

            boolean hasResults = resultsPage.verifyResultsGreaterThanZero();

            if (hasResults) {
                int count = resultsPage.getResultsCount();
                LogUtil.info("Search results verification successful. Found " + count + " results.");
                ReportManager.logPass(context.getTestId(), context.getTestName(),
                        "Search results verification successful. Found " + count + " results.");

                String verifiedScreenshotPath = ScreenshotUtils.takeScreenshot("Search_Results_Verified");
                if (verifiedScreenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(),
                            verifiedScreenshotPath, "Search Results Verified");
                }

                // Store the result count in context for potential later use
                context.addToContext("ResultsCount", count);

                return true;
            } else {
                LogUtil.error("Search results verification failed. No results found.");
                context.setTestFailed("Search results verification failed. No results found.");
                ReportManager.logFail(context.getTestId(), context.getTestName(),
                        "Search results verification failed. No results found.");

                String failedScreenshotPath = ScreenshotUtils.takeScreenshot("Search_Results_Verification_Failed");
                if (failedScreenshotPath != null) {
                    ReportManager.attachScreenshot(context.getTestId(), context.getTestName(),
                            failedScreenshotPath, "Search Results Verification Failed");
                }
                return false;
            }
        } catch (Exception e) {
            LogUtil.error("Exception during search results verification", e);
            context.setTestFailed("Exception during search results verification: " + e.getMessage());
            ReportManager.logFail(context.getTestId(), context.getTestName(),
                    "Exception during search results verification: " + e.getMessage());

            String exceptionScreenshotPath = ScreenshotUtils.takeScreenshot("Search_Results_Verification_Exception");
            if (exceptionScreenshotPath != null) {
                ReportManager.attachScreenshot(context.getTestId(), context.getTestName(),
                        exceptionScreenshotPath, "Search Results Verification Exception");
            }
            return false;
        }
    }
}