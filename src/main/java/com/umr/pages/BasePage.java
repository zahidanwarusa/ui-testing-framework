package com.umr.pages;

import com.umr.core.DriverManager;
import com.umr.utils.LogUtil;
import com.umr.utils.WebElementUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

/**
 * Abstract base class for all page objects in the framework.
 * Provides common functionality for page interactions and verification.
 */
public abstract class BasePage {
    protected WebDriver driver;
    protected WebElementUtils elementUtils;
    private static final int ELEMENT_TIMEOUT = 30; // Default timeout in seconds

    /**
     * Constructor for BasePage.
     *
     * @param driver The WebDriver instance
     */
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.elementUtils = new WebElementUtils(driver);

        // Initialize elements using PageFactory with AjaxElementLocatorFactory for better element loading
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, ELEMENT_TIMEOUT), this);
        LogUtil.debug("Initialized " + this.getClass().getSimpleName() + " page object");
    }

    /**
     * Verifies if the page is loaded by checking for key elements.
     * Each page must implement this method to check page-specific elements.
     *
     * @return True if the page is loaded, false otherwise
     */
    public abstract boolean isPageLoaded();

    /**
     * Maps a logical element name to a locator.
     * Each page must implement this method to provide element locators.
     *
     * @param elementName The logical name of the element
     * @return The By locator for the element, or null if not found
     */
    protected abstract By getLocatorForElement(String elementName);

    /**
     * Navigates to a URL.
     *
     * @param url The URL to navigate to
     */
    public void navigateTo(String url) {
        LogUtil.info("Navigating to: " + url);
        driver.get(url);
        elementUtils.waitForPageLoad();
    }

    /**
     * Gets the current page title.
     *
     * @return The page title
     */
    public String getPageTitle() {
        String title = driver.getTitle();
        LogUtil.debug("Page title: " + title);
        return title;
    }

    /**
     * Gets the current page URL.
     *
     * @return The page URL
     */
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /**
     * Clicks an element by its logical name.
     *
     * @param elementName The logical name of the element
     * @return True if the operation was successful, false otherwise
     */
    public boolean clickElement(String elementName) {
        By locator = getLocatorForElement(elementName);
        if (locator == null) {
            LogUtil.error("No locator defined for element: " + elementName);
            return false;
        }

        try {
            elementUtils.click(locator);
            return true;
        } catch (Exception e) {
            LogUtil.error("Failed to click element: " + elementName, e);
            return false;
        }
    }

    /**
     * Types text into an element by its logical name.
     *
     * @param elementName The logical name of the element
     * @param text The text to type
     * @return True if the operation was successful, false otherwise
     */
    public boolean typeIntoElement(String elementName, String text) {
        By locator = getLocatorForElement(elementName);
        if (locator == null) {
            LogUtil.error("No locator defined for element: " + elementName);
            return false;
        }

        try {
            elementUtils.type(locator, text);
            return true;
        } catch (Exception e) {
            LogUtil.error("Failed to type into element: " + elementName, e);
            return false;
        }
    }

    /**
     * Gets text from an element by its logical name.
     *
     * @param elementName The logical name of the element
     * @return The element text, or null if the operation failed
     */
    public String getElementText(String elementName) {
        By locator = getLocatorForElement(elementName);
        if (locator == null) {
            LogUtil.error("No locator defined for element: " + elementName);
            return null;
        }

        try {
            return elementUtils.getText(locator);
        } catch (Exception e) {
            LogUtil.error("Failed to get text from element: " + elementName, e);
            return null;
        }
    }

    /**
     * Verifies if an element is in the expected state.
     *
     * @param elementName The logical name of the element
     * @param expectedState The expected state (visible, not visible, enabled, disabled, etc.)
     * @return True if the element is in the expected state, false otherwise
     */
    public boolean verifyElementState(String elementName, String expectedState) {
        By locator = getLocatorForElement(elementName);
        if (locator == null) {
            LogUtil.error("No locator defined for element: " + elementName);
            return false;
        }

        expectedState = expectedState.toLowerCase();

        try {
            boolean result;
            switch (expectedState) {
                case "visible":
                    result = elementUtils.isDisplayed(locator);
                    break;
                case "not visible":
                case "invisible":
                    result = !elementUtils.isDisplayed(locator);
                    break;
                case "enabled":
                    WebElement element = driver.findElement(locator);
                    result = element.isEnabled();
                    break;
                case "disabled":
                    element = driver.findElement(locator);
                    result = !element.isEnabled();
                    break;
                case "selected":
                    element = driver.findElement(locator);
                    result = element.isSelected();
                    break;
                case "not selected":
                    element = driver.findElement(locator);
                    result = !element.isSelected();
                    break;
                default:
                    LogUtil.error("Unsupported element state: " + expectedState);
                    return false;
            }

            LogUtil.info("Element '" + elementName + "' " + (result ? "is" : "is not") +
                    " in state '" + expectedState + "'");
            return result;
        } catch (Exception e) {
            LogUtil.error("Failed to verify element state: " + elementName, e);
            return false;
        }
    }

    /**
     * Selects an option from a dropdown by visible text.
     *
     * @param elementName The logical name of the dropdown element
     * @param visibleText The visible text to select
     * @return True if the operation was successful, false otherwise
     */
    public boolean selectDropdownByText(String elementName, String visibleText) {
        By locator = getLocatorForElement(elementName);
        if (locator == null) {
            LogUtil.error("No locator defined for element: " + elementName);
            return false;
        }

        try {
            elementUtils.selectByVisibleText(locator, visibleText);
            return true;
        } catch (Exception e) {
            LogUtil.error("Failed to select dropdown option: " + visibleText, e);
            return false;
        }
    }

    /**
     * Waits for the page to load completely.
     */
    public void waitForPageLoad() {
        elementUtils.waitForPageLoad();
    }

    /**
     * Waits for AJAX requests to complete.
     */
    public void waitForAjaxToComplete() {
        elementUtils.waitForAjax();
    }
}