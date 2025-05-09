package com.umr.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.umr.utils.LogUtil;

import java.time.Duration;

public class HomePage extends BasePage {

    // Profile section locators
    private static final By USER_GREETING = By.cssSelector("h3[aria-label='Greeting']");
    private static final By PROFILE_VISIBILITY = By.xpath("//h2[contains(text(),'Profile Visibility')]");
    private static final By PROFILE_COMPLETENESS = By.xpath("//h5[contains(text(),'Completeness Score')]");

    // Search form locators
    private static final By SEARCH_INPUT = By.cssSelector("input[name='q']");
    private static final By LOCATION_INPUT = By.cssSelector("input[name='location']");
    private static final By SEARCH_BUTTON = By.cssSelector("[data-testid='job-search-search-bar-search-button']");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isPageLoaded() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.visibilityOfElementLocated(USER_GREETING));
            return elementUtils.isDisplayed(USER_GREETING);
        } catch (Exception e) {
            LogUtil.error("Error checking if home page is loaded", e);
            return false;
        }
    }

    @Override
    protected By getLocatorForElement(String elementName) {
        switch (elementName.toLowerCase()) {
            case "greeting":
                return USER_GREETING;
            case "visibility":
                return PROFILE_VISIBILITY;
            case "completeness":
                return PROFILE_COMPLETENESS;
            case "search":
                return SEARCH_INPUT;
            case "location":
                return LOCATION_INPUT;
            case "searchbutton":
                return SEARCH_BUTTON;
            default:
                LogUtil.warn("No locator defined for element: " + elementName);
                return null;
        }
    }

    public String getUserName() {
        try {
            String greetingText = elementUtils.getText(USER_GREETING);
            // Extract user name from the greeting text "You've got this, Zahid."
            if (greetingText.contains(",")) {
                String[] parts = greetingText.split(",");
                String namePart = parts[1].trim();
                // Remove the trailing period if present
                return namePart.endsWith(".") ? namePart.substring(0, namePart.length() - 1) : namePart;
            }
            return greetingText;
        } catch (Exception e) {
            LogUtil.error("Failed to get user name", e);
            return null;
        }
    }

    public boolean verifyProfileDetails() {
        try {
            boolean visibilityDisplayed = elementUtils.isDisplayed(PROFILE_VISIBILITY);
            boolean completenessDisplayed = elementUtils.isDisplayed(PROFILE_COMPLETENESS);

            LogUtil.info("Profile visibility displayed: " + visibilityDisplayed);
            LogUtil.info("Profile completeness displayed: " + completenessDisplayed);

            return visibilityDisplayed && completenessDisplayed;
        } catch (Exception e) {
            LogUtil.error("Failed to verify profile details", e);
            return false;
        }
    }

    public boolean searchForJobs(String searchTerm) {
        try {
            LogUtil.info("Searching for jobs with keyword: " + searchTerm);

            // Enter search term
            elementUtils.type(SEARCH_INPUT, searchTerm);

            // Click search button
            elementUtils.click(SEARCH_BUTTON);

            // Wait for URL to change, indicating search completion
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(driver -> driver.getCurrentUrl().contains("/jobs") ||
                    driver.getCurrentUrl().contains("q=" + searchTerm.toLowerCase()));

            return true;
        } catch (Exception e) {
            LogUtil.error("Failed to perform job search", e);
            return false;
        }
    }
}