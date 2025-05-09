package com.umr.core;

import com.umr.utils.LogUtil;
import org.openqa.selenium.WebDriver;

import java.util.HashMap;
import java.util.Map;

/**
 * TestContext maintains state throughout test execution, including test data and runtime context.
 * It provides methods to store and retrieve data during test execution.
 */
public class TestContext {
    private final String testId;
    private final String testName;
    private WebDriver driver;
    private final Map<String, Object> testData;
    private final Map<String, Object> scenarioContext;
    private boolean testPassed;
    private String failureReason;

    /**
     * Creates a new TestContext for the specified test.
     *
     * @param testId The unique test identifier
     * @param testName The descriptive test name
     */
    public TestContext(String testId, String testName) {
        this.testId = testId;
        this.testName = testName;
        this.testData = new HashMap<>();
        this.scenarioContext = new HashMap<>();
        this.testPassed = true;

        LogUtil.info(String.format("Created TestContext for test: %s - %s", testId, testName));
    }

    /**
     * Gets the test ID.
     *
     * @return The test ID
     */
    public String getTestId() {
        return testId;
    }

    /**
     * Gets the test name.
     *
     * @return The test name
     */
    public String getTestName() {
        return testName;
    }

    /**
     * Gets the WebDriver instance associated with this test context.
     *
     * @return The WebDriver instance
     */
    public WebDriver getDriver() {
        return driver;
    }

    /**
     * Sets the WebDriver instance for this test context.
     *
     * @param driver The WebDriver instance
     */
    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * Adds test data to the context.
     *
     * @param key The data key
     * @param value The data value
     */
    public void addTestData(String key, Object value) {
        testData.put(key, value);
    }

    /**
     * Gets test data from the context.
     *
     * @param key The data key
     * @return The data value or null if the key doesn't exist
     */
    public Object getTestData(String key) {
        return testData.get(key);
    }

    /**
     * Gets string test data from the context.
     *
     * @param key The data key
     * @return The data value as a string or null if the key doesn't exist
     */
    public String getTestDataAsString(String key) {
        Object value = getTestData(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Sets all test data from the provided map.
     *
     * @param data The map of test data to set
     */
    public void setTestData(Map<String, Object> data) {
        testData.clear();
        testData.putAll(data);
    }

    /**
     * Gets all test data.
     *
     * @return A map containing all test data
     */
    public Map<String, Object> getAllTestData() {
        return new HashMap<>(testData);
    }

    /**
     * Adds data to the scenario context.
     * The scenario context is used to share data between keywords during test execution.
     *
     * @param key The context key
     * @param value The context value
     */
    public void addToContext(String key, Object value) {
        scenarioContext.put(key, value);
    }

    /**
     * Gets data from the scenario context.
     *
     * @param key The context key
     * @return The context value or null if the key doesn't exist
     */
    public Object getFromContext(String key) {
        return scenarioContext.get(key);
    }

    /**
     * Gets string data from the scenario context.
     *
     * @param key The context key
     * @return The context value as a string or null if the key doesn't exist
     */
    public String getFromContextAsString(String key) {
        Object value = getFromContext(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Checks if the scenario context contains a key.
     *
     * @param key The context key
     * @return True if the key exists in the context, false otherwise
     */
    public boolean hasInContext(String key) {
        return scenarioContext.containsKey(key);
    }

    /**
     * Gets the test pass/fail status.
     *
     * @return True if the test has passed so far, false otherwise
     */
    public boolean isTestPassed() {
        return testPassed;
    }

    /**
     * Sets the test as failed with a reason.
     *
     * @param reason The reason for the test failure
     */
    public void setTestFailed(String reason) {
        this.testPassed = false;
        this.failureReason = reason;
        LogUtil.error("Test failed: " + reason);
    }

    /**
     * Gets the failure reason if the test failed.
     *
     * @return The failure reason or null if the test passed
     */
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * Cleans up resources associated with this test context.
     * Should be called after test execution completes.
     */
    public void cleanup() {
        if (driver != null) {
            try {
                driver.quit();
                LogUtil.info("WebDriver instance closed successfully");
            } catch (Exception e) {
                LogUtil.error("Failed to close WebDriver instance", e);
            } finally {
                driver = null;
            }
        }

        // Clear any other resources that need cleanup
        scenarioContext.clear();
    }
}