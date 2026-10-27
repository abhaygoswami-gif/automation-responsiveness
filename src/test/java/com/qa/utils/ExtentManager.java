package com.qa.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import java.io.File;

public final class ExtentManager {
    private static ExtentReports extent;

    private ExtentManager() {}

    public static synchronized ExtentReports getInstance() {
        if (extent == null) {
            new File("test-output").mkdirs();
            new File("test-output/logs").mkdirs();
            new File("test-output/screenshots").mkdirs();
            ExtentSparkReporter spark = new ExtentSparkReporter("test-output/ExtentReport.html");
            spark.config().setDocumentTitle("Responsive Automation Report");
            spark.config().setReportName("Responsive Website Test Results");

            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Framework", "Selenium + TestNG");
            extent.setSystemInfo("Language", "Java 8 compatible");
            extent.setSystemInfo("Browser", "Chrome");
            extent.setSystemInfo("Target URL", ConfigReader.get("baseUrl",
                "https://lovesair.com/"));
        }
        return extent;
    }
}
