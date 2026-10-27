package com.qa.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.qa.utils.ExtentManager;
import com.qa.utils.ScreenshotUtil;
import com.qa.utils.TestLogger;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {
    private static final ThreadLocal<ExtentTest> CURRENT_TEST = new ThreadLocal<ExtentTest>();
    private ExtentReports extent;

    @Override
    public void onStart(ITestContext context) {
        extent = ExtentManager.getInstance();
        TestLogger.info("Test suite started: " + context.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest test = extent.createTest(result.getMethod().getMethodName());
        CURRENT_TEST.set(test);
        test.info("Test started");
        TestLogger.info("TEST START: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        CURRENT_TEST.get().pass("Test passed");
        TestLogger.info("TEST PASS: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = CURRENT_TEST.get();
        test.fail(result.getThrowable());
        Object instance = result.getInstance();
        try {
            java.lang.reflect.Field field = instance.getClass().getSuperclass().getDeclaredField("driver");
            field.setAccessible(true);
            WebDriver driver = (WebDriver) field.get(instance);
            String path = ScreenshotUtil.capture(driver, "FAIL-" + result.getMethod().getMethodName());
            test.fail("Failure screenshot",
                MediaEntityBuilder.createScreenCaptureFromPath(path).build());
            TestLogger.error("Failure screenshot: " + path);
        } catch (Exception e) {
            TestLogger.error("Could not attach failure screenshot: " + e.getMessage());
        }
        TestLogger.error("TEST FAIL: " + result.getMethod().getMethodName()
            + " | " + result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (CURRENT_TEST.get() != null) {
            CURRENT_TEST.get().skip("Test skipped: " + result.getThrowable());
        }
        TestLogger.warn("TEST SKIP: " + result.getMethod().getMethodName());
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
        TestLogger.info("Test suite finished: " + context.getName());
    }
}
