package com.qa.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class ScreenshotUtil {
    private ScreenshotUtil() {}

    public static String capture(WebDriver driver, String name) {
        if (driver == null) {
            return "No WebDriver available";
        }
        try {
            File folder = new File("test-output/screenshots");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS").format(new Date());
            String safeName = name.replaceAll("[^a-zA-Z0-9._-]", "_");
            File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destination = new File(folder, safeName + "-" + timestamp + ".png");
            Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return destination.getPath();
        } catch (IOException | RuntimeException e) {
            TestLogger.error("Screenshot capture failed: " + e.getMessage());
            return "Screenshot capture failed: " + e.getMessage();
        }
    }
}
