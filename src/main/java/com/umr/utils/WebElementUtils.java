package com.umr.utils;

import com.umr.core.DriverManager;
import com.umr.core.config.ConfigLoader;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enhanced utilities for Selenium WebElement interactions.
 * Provides robust element operations with built-in waits and error handling.
 */
public class WebElementUtils {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final WebDriverWait shortWait;
    private final WebDriverWait longWait;
    private final int defaultTimeout;
    private final ConfigLoader config;
    private final JavascriptExecutor jsExecutor;

    /**
     * Creates a new WebElementUtils instance for the provided driver.
     *
     * @param driver The WebDriver instance to use
     */
    public WebElementUtils(WebDriver driver) {
        this.driver = driver;
        this.config = ConfigLoader.getInstance();
        this.defaultTimeout = config.getIntProperty("explicit.wait", 20);
        int shortTimeout = config.getIntProperty("short.wait", 5);
        int longTimeout = config.getIntProperty("long.wait", 60);
        int pollingInterval = config.getIntProperty("polling.interval", 500);

        this.wait = new WebDriverWait(driver, Duration.ofSeconds(defaultTimeout));
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(shortTimeout));
        this.longWait = new WebDriverWait(driver, Duration.ofSeconds(longTimeout));

        // Configure waits with polling interval
        this.wait.pollingEvery(Duration.ofMillis(pollingInterval));
        this.shortWait.pollingEvery(Duration.ofMillis(pollingInterval));
        this.longWait.pollingEvery(Duration.ofMillis(pollingInterval));

        this.jsExecutor = (JavascriptExecutor) driver;
    }

    /**
     * Creates a WebElementUtils instance for the current driver.
     *
     * @return A new WebElementUtils instance
     */
    public static WebElementUtils getInstance() {
        return new WebElementUtils(DriverManager.getDriver());
    }

    /**
     * Waits for an element to be visible.
     *
     * @param locator The element locator
     * @return The visible WebElement
     */
    public WebElement waitForVisible(By locator) {
        LogUtil.debug("Waiting for element to be visible: " + locator);
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            LogUtil.error("Element not visible after " + defaultTimeout + " seconds: " + locator);
            throw e;
        }
    }

    /**
     * Waits for an element to be clickable.
     *
     * @param locator The element locator
     * @return The clickable WebElement
     */
    public WebElement waitForClickable(By locator) {
        LogUtil.debug("Waiting for element to be clickable: " + locator);
        try {
            return wait.until(ExpectedConditions.elementToBeClickable(locator));
        } catch (TimeoutException e) {
            LogUtil.error("Element not clickable after " + defaultTimeout + " seconds: " + locator);
            throw e;
        }
    }

    /**
     * Waits for an element to be present in the DOM.
     *
     * @param locator The element locator
     * @return The present WebElement
     */
    public WebElement waitForPresent(By locator) {
        LogUtil.debug("Waiting for element to be present: " + locator);
        try {
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (TimeoutException e) {
            LogUtil.error("Element not present after " + defaultTimeout + " seconds: " + locator);
            throw e;
        }
    }

    /**
     * Waits for multiple elements to be present in the DOM.
     *
     * @param locator The element locator
     * @return A list of present WebElements
     */
    public List<WebElement> waitForAllPresent(By locator) {
        LogUtil.debug("Waiting for all elements to be present: " + locator);
        try {
            return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(locator));
        } catch (TimeoutException e) {
            LogUtil.error("Elements not present after " + defaultTimeout + " seconds: " + locator);
            throw e;
        }
    }

    /**
     * Clicks on an element with retry logic and JavaScript fallback.
     *
     * @param locator The element locator
     */
    public void click(By locator) {
        String elementDesc = locator.toString();
        LogUtil.debug("Clicking on element: " + elementDesc);

        try {
            WebElement element = waitForClickable(locator);
            try {
                element.click();
                LogUtil.debug("Successfully clicked element: " + elementDesc);
            } catch (ElementClickInterceptedException e) {
                LogUtil.warn("Element click intercepted, trying JavaScript click: " + elementDesc);
                jsExecutor.executeScript("arguments[0].click();", element);
            }
        } catch (Exception e) {
            LogUtil.error("Failed to click element: " + elementDesc, e);
            // Attempt one more retry with JS click if regular click fails
            try {
                WebElement element = waitForPresent(locator);
                LogUtil.warn("Regular click failed, attempting JavaScript click as last resort");
                jsExecutor.executeScript("arguments[0].click();", element);
            } catch (Exception jsException) {
                LogUtil.error("JavaScript click also failed: " + elementDesc, jsException);
                throw new RuntimeException("Failed to click element: " + elementDesc, e);
            }
        }
    }

    /**
     * Types text into an element with proper clearing and validation.
     *
     * @param locator The element locator
     * @param text The text to type
     */
    public void type(By locator, String text) {
        String elementDesc = locator.toString();
        LogUtil.debug("Typing text into element: " + elementDesc + " Text: " + text);

        try {
            WebElement element = waitForVisible(locator);
            // Clear the field properly
            element.clear();
            // Some applications need extra clearing
            String currentValue = element.getAttribute("value");
            if (currentValue != null && !currentValue.isEmpty()) {
                LogUtil.debug("Field not cleared properly. Using alternative clearing method.");
                element.sendKeys(Keys.chord(Keys.CONTROL, "a", Keys.DELETE));
            }
            // Send the text
            element.sendKeys(text);

            // Validate that text was entered correctly
            String actualValue = element.getAttribute("value");
            if (actualValue != null && !actualValue.equals(text)) {
                LogUtil.warn("Text validation failed. Expected: '" + text + "' Actual: '" + actualValue + "'");
                // Try once more
                element.clear();
                element.sendKeys(text);
            }

            LogUtil.debug("Successfully typed text into element: " + elementDesc);
        } catch (Exception e) {
            LogUtil.error("Failed to type text into element: " + elementDesc, e);
            throw new RuntimeException("Failed to type text into element: " + elementDesc, e);
        }
    }

    /**
     * Selects an option from a dropdown by visible text.
     *
     * @param locator The select element locator
     * @param visibleText The visible text to select
     */
    public void selectByVisibleText(By locator, String visibleText) {
        String elementDesc = locator.toString();
        LogUtil.debug("Selecting option with text '" + visibleText + "' from dropdown: " + elementDesc);

        try {
            WebElement element = waitForPresent(locator);
            Select select = new Select(element);
            select.selectByVisibleText(visibleText);
            LogUtil.debug("Successfully selected option: " + visibleText);
        } catch (Exception e) {
            LogUtil.error("Failed to select option: " + visibleText, e);
            throw new RuntimeException("Failed to select option: " + visibleText, e);
        }
    }

    /**
     * Selects an option from a dropdown by value.
     *
     * @param locator The select element locator
     * @param value The value to select
     */
    public void selectByValue(By locator, String value) {
        String elementDesc = locator.toString();
        LogUtil.debug("Selecting option with value '" + value + "' from dropdown: " + elementDesc);

        try {
            WebElement element = waitForPresent(locator);
            Select select = new Select(element);
            select.selectByValue(value);
            LogUtil.debug("Successfully selected option with value: " + value);
        } catch (Exception e) {
            LogUtil.error("Failed to select option with value: " + value, e);
            throw new RuntimeException("Failed to select option with value: " + value, e);
        }
    }

    /**
     * Gets text from an element.
     *
     * @param locator The element locator
     * @return The element's text content
     */
    public String getText(By locator) {
        String elementDesc = locator.toString();
        LogUtil.debug("Getting text from element: " + elementDesc);

        try {
            WebElement element = waitForVisible(locator);
            String text = element.getText();
            LogUtil.debug("Got text: '" + text + "' from element: " + elementDesc);
            return text;
        } catch (Exception e) {
            LogUtil.error("Failed to get text from element: " + elementDesc, e);
            throw new RuntimeException("Failed to get text from element: " + elementDesc, e);
        }
    }

    /**
     * Gets an attribute value from an element.
     *
     * @param locator The element locator
     * @param attribute The attribute name
     * @return The attribute value
     */
    public String getAttribute(By locator, String attribute) {
        String elementDesc = locator.toString();
        LogUtil.debug("Getting attribute '" + attribute + "' from element: " + elementDesc);

        try {
            WebElement element = waitForPresent(locator);
            String value = element.getAttribute(attribute);
            LogUtil.debug("Got attribute value: '" + value + "' for attribute: " + attribute);
            return value;
        } catch (Exception e) {
            LogUtil.error("Failed to get attribute from element: " + elementDesc, e);
            throw new RuntimeException("Failed to get attribute from element: " + elementDesc, e);
        }
    }

    /**
     * Checks if an element is displayed.
     *
     * @param locator The element locator
     * @return true if the element is displayed, false otherwise
     */
    public boolean isDisplayed(By locator) {
        String elementDesc = locator.toString();
        LogUtil.debug("Checking if element is displayed: " + elementDesc);

        try {
            // Use a shorter timeout for is-displayed checks to avoid long waits when element doesn't exist
            WebElement element = shortWait.until(ExpectedConditions.presenceOfElementLocated(locator));
            boolean displayed = element.isDisplayed();
            LogUtil.debug("Element is displayed: " + displayed);
            return displayed;
        } catch (TimeoutException | NoSuchElementException e) {
            LogUtil.debug("Element is not displayed: " + elementDesc);
            return false;
        }
    }

    /**
     * Waits for the page to load completely.
     */
    public void waitForPageLoad() {
        LogUtil.debug("Waiting for page to load completely");

        try {
            wait.until(driver -> (Boolean) ((JavascriptExecutor) driver)
                    .executeScript("return document.readyState").equals("complete"));
            LogUtil.debug("Page loaded successfully");
        } catch (TimeoutException e) {
            LogUtil.warn("Page load timeout reached. Continuing anyway.");
        }
    }

    /**
     * Waits for AJAX requests to complete.
     */
    public void waitForAjax() {
        LogUtil.debug("Waiting for AJAX requests to complete");

        try {
            wait.until(driver -> {
                Boolean jQueryDefined = (Boolean) ((JavascriptExecutor) driver)
                        .executeScript("return typeof jQuery != 'undefined'");
                if (Boolean.TRUE.equals(jQueryDefined)) {
                    return (Boolean) ((JavascriptExecutor) driver)
                            .executeScript("return jQuery.active == 0");
                }
                return true; // If jQuery is not defined, we assume there are no AJAX requests
            });
            LogUtil.debug("AJAX requests completed");
        } catch (TimeoutException e) {
            LogUtil.warn("AJAX wait timeout reached. Continuing anyway.");
        }
    }

    /**
     * Scrolls to an element.
     *
     * @param locator The element locator
     */
    public void scrollToElement(By locator) {
        String elementDesc = locator.toString();
        LogUtil.debug("Scrolling to element: " + elementDesc);

        try {
            WebElement element = waitForPresent(locator);
            jsExecutor.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
            // Brief pause to allow smooth scrolling to complete
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            LogUtil.debug("Scrolled to element: " + elementDesc);
        } catch (Exception e) {
            LogUtil.error("Failed to scroll to element: " + elementDesc, e);
            throw new RuntimeException("Failed to scroll to element: " + elementDesc, e);
        }
    }

    /**
     * Highlights an element for debugging purposes.
     *
     * @param locator The element locator
     */
    public void highlightElement(By locator) {
        String elementDesc = locator.toString();
        LogUtil.debug("Highlighting element: " + elementDesc);

        try {
            WebElement element = waitForPresent(locator);
            String originalStyle = element.getAttribute("style");

            jsExecutor.executeScript(
                    "arguments[0].setAttribute('style', 'border: 2px solid red; background-color: yellow;');",
                    element);

            // Restore original style after a short delay
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            jsExecutor.executeScript(
                    "arguments[0].setAttribute('style', arguments[1]);",
                    element, originalStyle);

            LogUtil.debug("Element highlighted: " + elementDesc);
        } catch (Exception e) {
            LogUtil.warn("Failed to highlight element: " + elementDesc, e);
            // Do not throw an exception since highlighting is just for debugging
        }
    }

    /**
     * Finds an element by text content from a list of elements.
     *
     * @param elements List of WebElements to search through
     * @param text The text to search for
     * @return The WebElement containing the text
     * @throws NoSuchElementException if no element with the text is found
     */
    public WebElement findElementByText(List<WebElement> elements, String text) {
        LogUtil.debug("Finding element with text: " + text);

        for (WebElement element : elements) {
            if (text.equals(element.getText().trim())) {
                LogUtil.debug("Found element with text: " + text);
                return element;
            }
        }

        LogUtil.error("No element found with text: " + text);
        throw new NoSuchElementException("No element found with text: " + text);
    }

    /**
     * Finds elements containing the specified text.
     *
     * @param elements List of WebElements to search through
     * @param text The text to search for
     * @return List of WebElements containing the text
     */
    public List<WebElement> findElementsContainingText(List<WebElement> elements, String text) {
        LogUtil.debug("Finding elements containing text: " + text);

        List<WebElement> matchingElements = elements.stream()
                .filter(element -> element.getText().contains(text))
                .collect(Collectors.toList());

        LogUtil.debug("Found " + matchingElements.size() + " elements containing text: " + text);
        return matchingElements;
    }

    /**
     * Finds elements by attribute value.
     *
     * @param elements List of WebElements to search through
     * @param attribute The attribute name
     * @param value The attribute value
     * @return List of WebElements with matching attribute value
     */
    public List<WebElement> findElementsByAttribute(List<WebElement> elements, String attribute, String value) {
        LogUtil.debug("Finding elements with attribute " + attribute + "=" + value);

        List<WebElement> matchingElements = elements.stream()
                .filter(element -> value.equals(element.getAttribute(attribute)))
                .collect(Collectors.toList());

        LogUtil.debug("Found " + matchingElements.size() + " elements with attribute " + attribute + "=" + value);
        return matchingElements;
    }

    /**
     * Hovers over an element.
     *
     * @param locator The element locator
     */
    public void hover(By locator) {
        String elementDesc = locator.toString();
        LogUtil.debug("Hovering over element: " + elementDesc);

        try {
            WebElement element = waitForVisible(locator);
            Actions actions = new Actions(driver);
            actions.moveToElement(element).perform();
            LogUtil.debug("Hovered over element: " + elementDesc);
        } catch (Exception e) {
            LogUtil.error("Failed to hover over element: " + elementDesc, e);
            throw new RuntimeException("Failed to hover over element: " + elementDesc, e);
        }
    }

    /**
     * Performs a custom wait until the specified condition is met.
     *
     * @param <T> The expected return type
     * @param condition The condition to wait for
     * @return The result of the condition function
     */
    public <T> T waitUntil(Function<WebDriver, T> condition) {
        LogUtil.debug("Waiting for custom condition");
        return wait.until(condition);
    }

    /**
     * Waits for an element to disappear.
     *
     * @param locator The element locator
     * @return true if the element disappeared, false if timeout occurred
     */
    public boolean waitForElementToDisappear(By locator) {
        String elementDesc = locator.toString();
        LogUtil.debug("Waiting for element to disappear: " + elementDesc);

        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
            LogUtil.debug("Element disappeared: " + elementDesc);
            return true;
        } catch (TimeoutException e) {
            LogUtil.warn("Element did not disappear within timeout: " + elementDesc);
            return false;
        }
    }

    /**
     * Waits for text to be present in an element.
     *
     * @param locator The element locator
     * @param text The text to wait for
     * @return true if the text is present, false if timeout occurred
     */
    public boolean waitForTextPresent(By locator, String text) {
        String elementDesc = locator.toString();
        LogUtil.debug("Waiting for text '" + text + "' to be present in element: " + elementDesc);

        try {
            wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
            LogUtil.debug("Text '" + text + "' is present in element: " + elementDesc);
            return true;
        } catch (TimeoutException e) {
            LogUtil.warn("Text '" + text + "' did not appear in element: " + elementDesc);
            return false;
        }
    }
}