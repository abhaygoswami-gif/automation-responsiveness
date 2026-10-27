
package com.qa.tests;

import com.qa.base.BaseTest;
import com.qa.utils.ConfigReader;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class ResponsiveTest extends BaseTest {

    private static final int MAX_PAGES = 2;
    private static final String BROWSER_NAME = "Chrome";
    private static final int MAX_SCROLL_STEPS = 100;

    private static final String[] ENVIRONMENTS = {
        "Laptop HD",
        "Laptop Full HD",
        "Small Laptop",
        "iPad Pro 12.9-inch Profile",
        "iPad Air Profile",
        "iPad Mini Profile",
        "Large Mobile",
        "Standard Mobile",
        "Small Mobile",
        "Compact Mobile"
    };

    private static final int[][] VIEWPORTS = {
        {1366, 768},
        {1920, 1080},
        {1280, 720},
        {1024, 1366},
        {820, 1180},
        {768, 1024},
        {430, 932},
        {390, 844},
        {375, 667},
        {360, 800}
    };

    private Workbook workbook;
    private Sheet detailsSheet;
    private Sheet summarySheet;
    private String baseUrl;

    private int passedChecks = 0;
    private int failedChecks = 0;
    private int errorChecks = 0;
    private int totalChecks = 0;

    private File reportFile;

    @Test
    public void testWebsiteResponsiveness() throws Exception {

        baseUrl = ConfigReader.get(
            "baseUrl",
            "https://lovesair.com/"
        );

        File reportDirectory = new File("target");
        if (!reportDirectory.exists() && !reportDirectory.mkdirs()) {
            throw new IOException(
                "Could not create report directory: "
                + reportDirectory.getAbsolutePath()
            );
        }

        reportFile = new File(
            reportDirectory,
            "responsive-report.xlsx"
        );

        File screenshotDirectory =
            new File(reportDirectory, "responsive-screenshots");

        if (!screenshotDirectory.exists()) {
            screenshotDirectory.mkdirs();
        }

        workbook = new XSSFWorkbook();
        detailsSheet = workbook.createSheet("Detailed Results");
        summarySheet = workbook.createSheet("Summary");

        createDetailsHeader();

        Set<String> pages = new LinkedHashSet<String>();

        try {
            System.out.println("Starting responsive testing...");
            System.out.println("Website: " + baseUrl);
            System.out.println("Maximum pages: " + MAX_PAGES);
            System.out.println(
                "Device profiles: " + VIEWPORTS.length
            );

            // Discover a maximum of two unique pages.
            pages.addAll(discoverPages(baseUrl));

            // Always test the homepage if discovery returns no pages.
            if (pages.isEmpty()) {
                pages.add(normalizeUrl(baseUrl));
            }

            System.out.println("Pages discovered: " + pages.size());

            for (String pageUrl : pages) {

                System.out.println("Testing page: " + pageUrl);

                for (int i = 0; i < VIEWPORTS.length; i++) {

                    int width = VIEWPORTS[i][0];
                    int height = VIEWPORTS[i][1];
                    String environment = ENVIRONMENTS[i];

                    totalChecks++;

                    String pageTitle = "";
                    String status = "PASS";
                    String issueText = "No issues detected";
                    String screenshotPath = "";

                    try {
                        driver.manage().window().setSize(
                            new Dimension(width, height)
                        );

                        driver.get(pageUrl);

                        waitForPageLoad();

                        pageTitle = driver.getTitle();

                        List<String> pageIssues =
                            checkEntirePageResponsiveness();

                        if (!pageIssues.isEmpty()) {
                            status = "FAIL";
                            issueText = joinIssues(pageIssues);
                        }

                    } catch (Exception e) {

                        status = "ERROR";

                        issueText =
                            "Test/navigation error: "
                            + e.getClass().getSimpleName()
                            + " - " + safeMessage(e);

                        System.out.println(
                            "ERROR: " + pageUrl
                            + " | " + environment
                            + " | " + issueText
                        );
                    }

                    if (!"PASS".equals(status)) {
                        screenshotPath = takeScreenshot(
                            screenshotDirectory,
                            pageUrl,
                            environment
                        );
                    }

                    // Save one row for every page and viewport.
                    addResultRow(
                        pageUrl,
                        pageTitle,
                        environment,
                        BROWSER_NAME,
                        width,
                        height,
                        status,
                        issueText,
                        screenshotPath
                    );

                    if ("PASS".equals(status)) {
                        passedChecks++;
                    } else if ("FAIL".equals(status)) {
                        failedChecks++;
                    } else {
                        errorChecks++;
                    }

                    System.out.println(
                        status + " | " + environment
                        + " | " + width + "x" + height
                        + " | " + pageUrl
                    );
                }
            }

        } finally {

            // Generate the summary and save the report even if
            // an exception interrupts the testing loop.
            try {
                createSummary(
                    pages.size(),
                    totalChecks,
                    passedChecks,
                    failedChecks,
                    errorChecks
                );

                saveExcelReport();

            } catch (Exception reportError) {
                System.err.println(
                    "Could not save Excel report: "
                    + safeMessage(reportError)
                );
                throw reportError;

            } finally {
                if (workbook != null) {
                    try {
                        workbook.close();
                    } catch (IOException closeError) {
                        System.err.println(
                            "Workbook close error: "
                            + safeMessage(closeError)
                        );
                    }
                }
            }
        }

        System.out.println("\n========== TEST SUMMARY ==========");
        System.out.println("Website: " + baseUrl);
        System.out.println("Pages: " + pages.size());
        System.out.println("Total checks: " + totalChecks);
        System.out.println("Passed: " + passedChecks);
        System.out.println("Failed: " + failedChecks);
        System.out.println("Errors: " + errorChecks);
        System.out.println(
            "Excel report: " + reportFile.getAbsolutePath()
        );
        System.out.println("=================================");

        Assert.assertEquals(
            failedChecks + errorChecks,
            0,
            "Responsive issues or test errors were found. "
            + "Check the Excel report at: "
            + reportFile.getAbsolutePath()
        );
    }

    private void waitForPageLoad() {

        new WebDriverWait(driver, Duration.ofSeconds(30))
            .until(webDriver ->
                "complete".equals(
                    ((JavascriptExecutor) webDriver)
                        .executeScript(
                            "return document.readyState"
                        )
                )
            );
    }

    // Discover up to two same-host pages, starting from the homepage.
    private Set<String> discoverPages(String startUrl) {

        Set<String> visited = new LinkedHashSet<String>();
        Queue<String> queue = new LinkedList<String>();

        String normalizedStart = normalizeUrl(startUrl);
        queue.add(normalizedStart);

        String baseHost = URI.create(startUrl).getHost();

        while (!queue.isEmpty() && visited.size() < MAX_PAGES) {

            String currentUrl = queue.poll();

            if (currentUrl == null || visited.contains(currentUrl)) {
                continue;
            }

            try {
                driver.get(currentUrl);
                waitForPageLoad();

                visited.add(currentUrl);

                System.out.println("Discovered: " + currentUrl);

                List<WebElement> links =
                    driver.findElements(By.cssSelector("a[href]"));

                for (WebElement link : links) {

                    String href = link.getAttribute("href");

                    if (href == null || href.trim().isEmpty()) {
                        continue;
                    }

                    try {
                        URI uri = URI.create(href).normalize();

                        if (uri.getHost() == null
                                || !uri.getHost().equalsIgnoreCase(baseHost)) {
                            continue;
                        }

                        String path = uri.getPath();

                        if (path != null && path.matches(
                            "(?i).*\\.(pdf|jpg|jpeg|png|gif|svg|zip|docx?|xlsx?|css|js)$"
                        )) {
                            continue;
                        }

                        // Ignore links to the same page with only
                        // a different fragment identifier.
                        String cleanUrl = normalizeUrl(href);

                        if (!visited.contains(cleanUrl)
                                && !queue.contains(cleanUrl)
                                && visited.size() + queue.size()
                                    < MAX_PAGES) {

                            queue.add(cleanUrl);
                        }

                    } catch (Exception ignored) {
                        // Skip invalid links.
                    }
                }

            } catch (Exception e) {

                // Keep the page in the report even if navigation fails.
                visited.add(currentUrl);

                System.out.println(
                    "Crawl error: " + currentUrl
                    + " | " + safeMessage(e)
                );
            }
        }

        return visited;
    }

    private String normalizeUrl(String value) {

        try {
            URI uri = URI.create(value).normalize();

            return new URI(
                uri.getScheme(),
                uri.getAuthority(),
                uri.getPath(),
                uri.getQuery(),
                null
            ).toString();

        } catch (Exception e) {
            return value;
        }
    }

    // Scroll through the page and check visible sections.
    private List<String> checkEntirePageResponsiveness()
            throws InterruptedException {

        List<String> allIssues = new ArrayList<String>();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        js.executeScript("window.scrollTo(0, 0);");
        Thread.sleep(400);

        int previousHeight = 0;
        int stableBottomCount = 0;

        for (int step = 0; step < MAX_SCROLL_STEPS; step++) {

            List<String> currentIssues = checkResponsiveIssues();

            for (String issue : currentIssues) {
                if (!allIssues.contains(issue)) {
                    allIssues.add(issue);
                }
            }

            int pageHeight = ((Number) js.executeScript(
                "return document.documentElement.scrollHeight;"
            )).intValue();

            int viewportHeight = ((Number) js.executeScript(
                "return window.innerHeight;"
            )).intValue();

            int scrollY = ((Number) js.executeScript(
                "return window.scrollY;"
            )).intValue();

            boolean atBottom =
                scrollY + viewportHeight >= pageHeight - 5;

            if (atBottom && pageHeight == previousHeight) {
                stableBottomCount++;
            } else {
                stableBottomCount = 0;
            }

            if (stableBottomCount >= 2) {
                break;
            }

            previousHeight = pageHeight;

            js.executeScript(
                "window.scrollBy(0, "
                + "Math.max(200, window.innerHeight * 0.75));"
            );

            // Allow lazy-loaded content to appear.
            Thread.sleep(400);
        }

        js.executeScript(
            "window.scrollTo(0, document.body.scrollHeight);"
        );

        Thread.sleep(500);

        for (String issue : checkResponsiveIssues()) {
            if (!allIssues.contains(issue)) {
                allIssues.add(issue);
            }
        }

        js.executeScript("window.scrollTo(0, 0);");

        return allIssues;
    }

    private List<String> checkResponsiveIssues() {

        List<String> foundIssues = new ArrayList<String>();
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // 1. Horizontal overflow.
        Boolean overflow = (Boolean) js.executeScript(
            "return document.documentElement.scrollWidth > "
            + "document.documentElement.clientWidth + 2;"
        );

        if (Boolean.TRUE.equals(overflow)) {
            foundIssues.add("Horizontal overflow detected");
        }

        // 2. Broken images.
        @SuppressWarnings("unchecked")
        List<String> brokenImages =
            (List<String>) js.executeScript(
                "return Array.from(document.images)"
                + ".filter(img => img.complete && img.naturalWidth === 0)"
                + ".map(img => img.currentSrc || img.src)"
                + ".slice(0, 10);"
            );

        for (String image : brokenImages) {
            foundIssues.add("Broken image: " + image);
        }

        // 3. Possible clipped content.
        @SuppressWarnings("unchecked")
        List<String> clipped = (List<String>) js.executeScript(
            "return Array.from(document.querySelectorAll('body *'))"
            + ".filter(el => {"
            + " const s = getComputedStyle(el);"
            + " const r = el.getBoundingClientRect();"
            + " const t = (el.innerText || '').trim();"
            + " if (!t || r.width < 5 || r.height < 5) return false;"
            + " if (s.display === 'none' || s.visibility === 'hidden')"
            + " return false;"
            + " return (el.scrollHeight > el.clientHeight + 2 &&"
            + " ['hidden','clip'].includes(s.overflowY)) ||"
            + " (el.scrollWidth > el.clientWidth + 2 &&"
            + " ['hidden','clip'].includes(s.overflowX));"
            + "})"
            + ".slice(0, 5)"
            + ".map(el => el.tagName.toLowerCase() + ': ' +"
            + " el.innerText.trim().substring(0, 50));"
        );

        for (String element : clipped) {
            foundIssues.add("Possible clipped content: " + element);
        }

        // 4. Basic overlap heuristic for visible text elements.
        @SuppressWarnings("unchecked")
        List<String> overlaps = (List<String>) js.executeScript(
            "const els = Array.from(document.querySelectorAll('body *'))"
            + ".filter(el => {"
            + " const r = el.getBoundingClientRect();"
            + " const s = getComputedStyle(el);"
            + " return el.children.length === 0 &&"
            + " (el.innerText || '').trim().length > 0 &&"
            + " r.width > 10 && r.height > 8 &&"
            + " s.display !== 'none' && s.visibility !== 'hidden' &&"
            + " r.bottom > 0 && r.right > 0 &&"
            + " r.top < innerHeight && r.left < innerWidth;"
            + "}).slice(0, 150);"
            + "const out = [];"
            + "for (let i = 0; i < els.length; i++) {"
            + " const a = els[i].getBoundingClientRect();"
            + " for (let j = i + 1; j < els.length; j++) {"
            + "  const b = els[j].getBoundingClientRect();"
            + "  const w = Math.min(a.right,b.right)"
            + "    - Math.max(a.left,b.left);"
            + "  const h = Math.min(a.bottom,b.bottom)"
            + "    - Math.max(a.top,b.top);"
            + "  if (w > 3 && h > 3 && w*h > 25) {"
            + "   const ta = (els[i].innerText || '')"
            + "     .trim().substring(0,25);"
            + "   const tb = (els[j].innerText || '')"
            + "     .trim().substring(0,25);"
            + "   if (ta && tb && ta !== tb) {"
            + "    out.push('Possible overlap: [' + ta + '] / [' + tb + ']');"
            + "    if (out.length >= 3) return out;"
            + "   }"
            + "  }"
            + " }"
            + "}"
            + "return out;"
        );

        for (String overlap : overlaps) {
            foundIssues.add(overlap);
        }

        return foundIssues;
    }

    private String takeScreenshot(
            File directory,
            String pageUrl,
            String environment) {

        try {
            String safeName = pageUrl
                .replaceAll("^https?://", "")
                .replaceAll("[^a-zA-Z0-9.-]", "_");

            String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS")
            );

            File file = new File(
                directory,
                safeName + "_"
                + environment.replaceAll("[^a-zA-Z0-9]", "_")
                + "_" + timestamp + ".png"
            );

            File screenshot = ((TakesScreenshot) driver)
                .getScreenshotAs(OutputType.FILE);

            java.nio.file.Files.copy(
                screenshot.toPath(),
                file.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            return file.getAbsolutePath();

        } catch (Exception e) {
            return "Screenshot failed: " + safeMessage(e);
        }
    }

    private void createDetailsHeader() {

        Row header = detailsSheet.createRow(0);

        String[] columns = {
            "Page URL",
            "Page Title",
            "Environment / Device",
            "Browser",
            "Viewport Width (px)",
            "Viewport Height (px)",
            "Status",
            "Issues Found",
            "Screenshot Path",
            "Test Timestamp"
        };

        for (int i = 0; i < columns.length; i++) {
            header.createCell(i).setCellValue(columns[i]);
        }
    }

    private void addResultRow(
            String url,
            String title,
            String environment,
            String browser,
            int width,
            int height,
            String status,
            String issue,
            String screenshotPath) {

        int rowIndex = detailsSheet.getLastRowNum() + 1;
        Row row = detailsSheet.createRow(rowIndex);

        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );

        row.createCell(0).setCellValue(url);
        row.createCell(1).setCellValue(title);
        row.createCell(2).setCellValue(environment);
        row.createCell(3).setCellValue(browser);
        row.createCell(4).setCellValue(width);
        row.createCell(5).setCellValue(height);
        row.createCell(6).setCellValue(status);
        row.createCell(7).setCellValue(issue);
        row.createCell(8).setCellValue(screenshotPath);
        row.createCell(9).setCellValue(timestamp);
    }

    private void createSummary(
            int pages,
            int checks,
            int passed,
            int failed,
            int errors) {

        String[][] summary = {
            {"Responsive Testing Summary", ""},
            {"Website", baseUrl == null ? "" : baseUrl},
            {"Pages Discovered", String.valueOf(pages)},
            {"Maximum Pages", String.valueOf(MAX_PAGES)},
            {"Device Profiles", String.valueOf(VIEWPORTS.length)},
            {"Expected Maximum Checks",
                String.valueOf(MAX_PAGES * VIEWPORTS.length)},
            {"Total Viewport Checks", String.valueOf(checks)},
            {"Passed Checks", String.valueOf(passed)},
            {"Failed Checks", String.valueOf(failed)},
            {"Errors", String.valueOf(errors)},
            {"Generated At", LocalDateTime.now().toString()}
        };

        for (int i = 0; i < summary.length; i++) {
            Row row = summarySheet.createRow(i);
            row.createCell(0).setCellValue(summary[i][0]);
            row.createCell(1).setCellValue(summary[i][1]);
        }
    }

    private void saveExcelReport() throws IOException {

        for (int i = 0; i < 10; i++) {
            detailsSheet.autoSizeColumn(i);
        }

        summarySheet.autoSizeColumn(0);
        summarySheet.autoSizeColumn(1);

        // Write the workbook to the report file.
        try (FileOutputStream output =
                new FileOutputStream(reportFile)) {

            workbook.write(output);
            output.flush();
        }

        System.out.println(
            "Excel report saved to: "
            + reportFile.getAbsolutePath()
        );
    }

    private String joinIssues(List<String> values) {

        StringBuilder result = new StringBuilder();

        for (String value : values) {

            if (result.length() > 0) {
                result.append(" | ");
            }

            result.append(value);

            if (result.length() > 3000) {
                result.append(" ...");
                break;
            }
        }

        return result.toString();
    }

    private String safeMessage(Exception e) {

        String message = e.getMessage();

        if (message == null || message.trim().isEmpty()) {
            return "No additional error details";
        }

        return message.replaceAll("[\\r\\n]+", " ");
    }
}
