package com.umr.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.umr.utils.LogUtil;

import java.time.Duration;

public class LoginPage extends BasePage {

    // More flexible selectors for Dice login page
    private static final By EMAIL_INPUT = By.xpath("//input[@type='email' or @id='email' or contains(@name,'email')]");
    private static final By CONTINUE_BUTTON = By.xpath("//button[contains(@type,'submit') or contains(text(),'Continue') or contains(@data-testid,'sign-in')]");

    // More flexible password selectors
    private static final By PASSWORD_INPUT = By.xpath("//input[@type='password' or @id='password' or contains(@name,'password')]");
    private static final By SIGN_IN_BUTTON = By.xpath("//button[contains(@type,'submit') or contains(text(),'Sign In') or contains(text(),'Log In')]");

    // Error message locators
    private static final By ERROR_MESSAGE = By.cssSelector(".error-message, .alert-error, [role='alert']");

    // General login form locator as a fallback
    private static final By LOGIN_FORM = By.xpath("//form[contains(@class,'login') or contains(@id,'login')]");
    private static final By ANY_LOGIN_ELEMENT = By.xpath("//h1[contains(text(),'Sign In') or contains(text(),'Log In')] | //div[contains(@class,'login')]");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isPageLoaded() {
        try {
            // Add fallback checks with more general selectors
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

            // Try to wait for any login-related element
            try {
                wait.until(ExpectedConditions.or(
                        ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT),
                        ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT),
                        ExpectedConditions.visibilityOfElementLocated(LOGIN_FORM),
                        ExpectedConditions.visibilityOfElementLocated(ANY_LOGIN_ELEMENT)
                ));

                LogUtil.info("Login page detected with at least one matching element");
                return true;
            } catch (Exception e) {
                LogUtil.warn("Could not find standard login elements, checking page title or URL as fallback");

                // Last resort - check if URL or title contains login-related terms
                boolean urlContainsLogin = driver.getCurrentUrl().toLowerCase().contains("login");
                boolean titleContainsLogin = driver.getTitle().toLowerCase().contains("sign in") ||
                        driver.getTitle().toLowerCase().contains("log in");

                if (urlContainsLogin || titleContainsLogin) {
                    LogUtil.info("Login page detected based on URL or title");
                    return true;
                }

                LogUtil.error("Could not verify login page by any method");
                return false;
            }
        } catch (Exception e) {
            LogUtil.error("Error checking if login page is loaded", e);
            return false;
        }
    }

    @Override
    protected By getLocatorForElement(String elementName) {
        switch (elementName.toLowerCase()) {
            case "email":
                return EMAIL_INPUT;
            case "continue":
                return CONTINUE_BUTTON;
            case "password":
                return PASSWORD_INPUT;
            case "signin":
                return SIGN_IN_BUTTON;
            case "errormessage":
                return ERROR_MESSAGE;
            case "loginform":
                return LOGIN_FORM;
            default:
                LogUtil.warn("No locator defined for element: " + elementName);
                return null;
        }
    }

    public boolean enterEmail(String email) {
        LogUtil.info("Entering email: " + email);
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT));

            elementUtils.type(EMAIL_INPUT, email);
            return true;
        } catch (Exception e) {
            LogUtil.error("Failed to enter email", e);
            return false;
        }
    }

    public boolean clickContinue() {
        LogUtil.info("Clicking Continue button");
        try {
            // Try to find and click continue button
            if (!elementUtils.isDisplayed(CONTINUE_BUTTON)) {
                LogUtil.warn("Continue button not visible, checking if already on password page");
                // Check if we're already on password page
                if (elementUtils.isDisplayed(PASSWORD_INPUT)) {
                    LogUtil.info("Password field already visible, skipping continue button");
                    return true;
                }
                LogUtil.error("Neither continue button nor password field found");
                return false;
            }

            elementUtils.click(CONTINUE_BUTTON);

            // Wait for password field to be visible, indicating successful transition
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            try {
                // Try to wait for the password field
                wait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT));
                LogUtil.info("Password field is now visible");
                return true;
            } catch (Exception e) {
                // If password field isn't visible, perhaps we're already on a page with both email and password
                LogUtil.warn("Password field not found after clicking continue. Checking if already on combined login page");
                return elementUtils.isDisplayed(PASSWORD_INPUT);
            }
        } catch (Exception e) {
            LogUtil.error("Failed to click Continue button", e);
            return false;
        }
    }

    public boolean enterPassword(String password) {
        LogUtil.info("Entering password");
        try {
            // Check if password field is visible
            if (!elementUtils.isDisplayed(PASSWORD_INPUT)) {
                LogUtil.error("Password field not visible");
                return false;
            }

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT));

            elementUtils.type(PASSWORD_INPUT, password);
            return true;
        } catch (Exception e) {
            LogUtil.error("Failed to enter password", e);
            return false;
        }
    }

    public boolean clickSignIn() {
        LogUtil.info("Clicking Sign In button");
        try {
            // Check if sign in button is visible
            if (!elementUtils.isDisplayed(SIGN_IN_BUTTON)) {
                LogUtil.error("Sign In button not visible");
                return false;
            }

            elementUtils.click(SIGN_IN_BUTTON);
            return true;
        } catch (Exception e) {
            LogUtil.error("Failed to click Sign In button", e);
            return false;
        }
    }

    public boolean loginToDice(String email, String password) {
        LogUtil.info("Performing login to Dice with email: " + email);

        // Take a screenshot at the beginning of login process
        try {
            com.umr.utils.ScreenshotUtils.takeScreenshot("Before_Login_Attempt");
        } catch (Exception e) {
            LogUtil.warn("Failed to take screenshot before login", e);
        }

        // Enter email
        if (!enterEmail(email)) {
            return false;
        }

        // Take screenshot after entering email
        try {
            com.umr.utils.ScreenshotUtils.takeScreenshot("After_Email_Entry");
        } catch (Exception e) {
            LogUtil.warn("Failed to take screenshot after email entry", e);
        }

        // Click continue
        if (!clickContinue()) {
            // Check if we're already on a page with both fields (single-page login)
            LogUtil.info("Checking if this is a single-page login form");
            if (!elementUtils.isDisplayed(PASSWORD_INPUT)) {
                LogUtil.error("Cannot find password field after continue button");
                return false;
            }
        }

        // Take screenshot after continuing to password step
        try {
            com.umr.utils.ScreenshotUtils.takeScreenshot("After_Continue_To_Password");
        } catch (Exception e) {
            LogUtil.warn("Failed to take screenshot after continue", e);
        }

        // Enter password
        if (!enterPassword(password)) {
            return false;
        }

        // Take screenshot after entering password
        try {
            com.umr.utils.ScreenshotUtils.takeScreenshot("After_Password_Entry");
        } catch (Exception e) {
            LogUtil.warn("Failed to take screenshot after password entry", e);
        }

        // Click sign in
        boolean signInResult = clickSignIn();

        // Take screenshot after clicking sign in
        try {
            com.umr.utils.ScreenshotUtils.takeScreenshot("After_Sign_In_Click");
        } catch (Exception e) {
            LogUtil.warn("Failed to take screenshot after sign in", e);
        }

        return signInResult;
    }

    public String getErrorMessage() {
        try {
            if (elementUtils.isDisplayed(ERROR_MESSAGE)) {
                return elementUtils.getText(ERROR_MESSAGE);
            }
            return null;
        } catch (Exception e) {
            LogUtil.error("Failed to get error message", e);
            return null;
        }
    }
}