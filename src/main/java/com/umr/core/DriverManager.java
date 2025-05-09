package com.umr.core;

import com.umr.core.config.ConfigLoader;
import com.umr.utils.LogUtil;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;

import java.time.Duration;

/**
 * Manages WebDriver instances for browser automation.
 * Handles browser initialization, configuration, and cleanup.
 * Uses ThreadLocal to support parallel test execution.
 */
public class DriverManager {
    // ThreadLocal allows each thread to have its own WebDriver instance
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ConfigLoader config = ConfigLoader.getInstance();

    // ADD THIS: Flag to track if the driver has been quit
    private static final ThreadLocal<Boolean> driverQuit = new ThreadLocal<>();

    /**
     * Private constructor to prevent instantiation.
     */
    private DriverManager() {
        // Utility class, should not be instantiated
    }

    /**
     * Gets the WebDriver instance for the current thread.
     *
     * @return The WebDriver instance
     * @throws IllegalStateException if the driver hasn't been initialized
     */
    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        // ADD THIS: Check if driver is null or has been quit
        Boolean hasQuit = driverQuit.get();
        if (driver == null || (hasQuit != null && hasQuit)) {
            throw new IllegalStateException("WebDriver not initialized or has been quit. Call initializeDriver() first.");
        }
        return driver;
    }

    /**
     * Initializes a new WebDriver instance for the current thread based on configuration.
     *
     * @return The initialized WebDriver instance
     */
    public static WebDriver initializeDriver() {
        // ADD THIS: Check if driver has been quit and create a new one if needed
        Boolean hasQuit = driverQuit.get();
        if (driverThreadLocal.get() != null && (hasQuit == null || !hasQuit)) {
            LogUtil.warn("WebDriver already initialized. Returning existing instance.");
            return driverThreadLocal.get();
        }

        // If we get here, we need a new driver
        if (driverThreadLocal.get() != null) {
            // Ensure any existing driver is properly quit before creating a new one
            try {
                driverThreadLocal.get().quit();
            } catch (Exception e) {
                LogUtil.warn("Exception while quitting existing driver", e);
            }
            driverThreadLocal.remove();
        }

        String browser = config.getProperty("browser", "chrome").toLowerCase();
        boolean headless = config.getBooleanProperty("headless", false);
        boolean maximize = config.getBooleanProperty("maximize", true);

        LogUtil.info("Initializing WebDriver for browser: " + browser + " (headless: " + headless + ")");

        WebDriver driver;

        try {
            switch (browser) {
                case "chrome":
                    driver = initializeChromeDriver(headless);
                    break;
                case "firefox":
                    driver = initializeFirefoxDriver(headless);
                    break;
                case "edge":
                    driver = initializeEdgeDriver(headless);
                    break;
                case "safari":
                    driver = initializeSafariDriver();
                    break;
                default:
                    LogUtil.warn("Unsupported browser: " + browser + ". Defaulting to Chrome.");
                    driver = initializeChromeDriver(headless);
            }

            configureDriver(driver, maximize);
            driverThreadLocal.set(driver);
            // ADD THIS: Mark driver as not quit
            driverQuit.set(false);
            LogUtil.info("WebDriver initialized successfully");
            return driver;
        } catch (Exception e) {
            LogUtil.error("Failed to initialize WebDriver", e);
            throw new RuntimeException("Failed to initialize WebDriver", e);
        }
    }

    /**
     * Initializes a Chrome WebDriver instance.
     *
     * @param headless Whether to run in headless mode
     * @return The Chrome WebDriver instance
     */
    private static WebDriver initializeChromeDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        // Add additional Chrome-specific options
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.setAcceptInsecureCerts(true);
        return new ChromeDriver(options);
    }

    /**
     * Initializes a Firefox WebDriver instance.
     *
     * @param headless Whether to run in headless mode
     * @return The Firefox WebDriver instance
     */
    private static WebDriver initializeFirefoxDriver(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        return new FirefoxDriver(options);
    }

    /**
     * Initializes an Edge WebDriver instance.
     *
     * @param headless Whether to run in headless mode
     * @return The Edge WebDriver instance
     */
    private static WebDriver initializeEdgeDriver(boolean headless) {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        return new EdgeDriver(options);
    }

    /**
     * Initializes a Safari WebDriver instance.
     *
     * @return The Safari WebDriver instance
     */
    private static WebDriver initializeSafariDriver() {
        // Safari doesn't support headless mode
        return new SafariDriver();
    }

    /**
     * Configures the WebDriver instance with common settings.
     *
     * @param driver The WebDriver instance to configure
     * @param maximize Whether to maximize the browser window
     */
    private static void configureDriver(WebDriver driver, boolean maximize) {
        // Configure timeout settings from properties
        int implicitWait = config.getIntProperty("implicit.wait", 10);
        int pageLoadTimeout = config.getIntProperty("page.load.timeout", 60);

        // Apply timeout settings
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeout));

        // Maximize window if configured
        if (maximize) {
            driver.manage().window().maximize();
        }
    }

    /**
     * Quits the WebDriver instance for the current thread and removes it from ThreadLocal storage.
     */
    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            LogUtil.info("Quitting WebDriver instance");
            try {
                driver.quit();
                // ADD THIS: Mark the driver as quit
                driverQuit.set(true);
            } catch (Exception e) {
                LogUtil.error("Error quitting WebDriver", e);
            } finally {
                // We don't remove the driver from ThreadLocal here anymore
                // to track which drivers have been quit
                LogUtil.info("WebDriver instance has been quit");
            }
        }
    }

    /**
     * Reset driver state completely by removing from ThreadLocal
     * This is useful when you want to completely clean up resources
     */
    public static void resetDriver() {
        quitDriver();
        driverThreadLocal.remove();
        driverQuit.remove();
        LogUtil.info("WebDriver ThreadLocal storage has been reset");
    }
}