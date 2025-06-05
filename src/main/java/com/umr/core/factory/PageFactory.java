package com.umr.core.factory;

import com.umr.utils.LogUtil;
import org.openqa.selenium.WebDriver;

public class PageFactory {

    public static BasePage getPage(String pageName, WebDriver driver) {
        LogUtil.debug("Creating page object for: " + pageName);

        if (pageName == null || pageName.isEmpty()) {
            LogUtil.error("Page name cannot be null or empty");
            throw new IllegalArgumentException("Page name cannot be null or empty");
        }

        // Convert to lowercase for case-insensitive comparison
        String pageNameLower = pageName.toLowerCase();

        switch (pageNameLower) {
            case "login":
            case "loginpage":
                return new LoginPage(driver);

            case "home":
            case "homepage":
                return new HomePage(driver);

            case "searchresults":
            case "searchresultspage":
                return new SearchResultsPage(driver);

            default:
                LogUtil.error("Unsupported page: " + pageName);
                throw new IllegalArgumentException("Unsupported page: " + pageName);
        }
    }
}