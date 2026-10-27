package com.qa.base;

import com.qa.utils.ConfigReader;
import com.qa.utils.ScreenshotUtil;
import com.qa.utils.TestLogger;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseTest {
    protected WebDriver driver;

    @BeforeMethod
    public void setUp() {
        TestLogger.info("========== Starting browser session ==========");
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(ConfigReader.get("headless", "false"))) {
            options.addArguments("--headless");
            options.addArguments("--window-size=1365,900");
        }
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");

        driver = new ChromeDriver(options);
        driver.manage().window().setSize(new Dimension(1365, 900));
        driver.manage().timeouts().pageLoadTimeout(java.time.Duration.ofSeconds(45));

        String url = ConfigReader.get("baseUrl", "https://lovesair.com/");
        TestLogger.info("Opening URL: " + url);
        driver.get(url);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            TestLogger.info("Closing browser session");
            driver.quit();
        }
    }

    protected String takeScreenshot(String name) {
        return ScreenshotUtil.capture(driver, name);
    }
}
