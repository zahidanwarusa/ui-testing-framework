package com.umr.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.umr.utils.LogUtil;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchResultsPage extends BasePage {

    private static final By RESULTS_COUNT = By.cssSelector("[data-testid='jobSearchResultsContainer'] p");
    private static final By JOB_CARDS = By.cssSelector("[data-testid='jobSearchResultsContainer'] [data-id]");

    public SearchResultsPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public boolean isPageLoaded() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.visibilityOfElementLocated(RESULTS_COUNT));
            return elementUtils.isDisplayed(RESULTS_COUNT);
        } catch (Exception e) {
            LogUtil.error("Error checking if search results page is loaded", e);
            return false;
        }
    }

    @Override
    protected By getLocatorForElement(String elementName) {
        switch (elementName.toLowerCase()) {
            case "results":
                return RESULTS_COUNT;
            case "jobcards":
                return JOB_CARDS;
            default:
                LogUtil.warn("No locator defined for element: " + elementName);
                return null;
        }
    }

    public int getResultsCount() {
        try {
            String resultsText = elementUtils.getText(RESULTS_COUNT);
            LogUtil.info("Results text: " + resultsText);

            // Extract the number from text like "172 results (19 new)"
            Pattern pattern = Pattern.compile("(\\d+)\\s+results");
            Matcher matcher = pattern.matcher(resultsText);

            if (matcher.find()) {
                String countStr = matcher.group(1);
                return Integer.parseInt(countStr);
            }

            return 0;
        } catch (Exception e) {
            LogUtil.error("Failed to get results count", e);
            return 0;
        }
    }

    public boolean verifyResultsGreaterThanZero() {
        int count = getResultsCount();
        LogUtil.info("Job search returned " + count + " results");
        return count > 0;
    }
}