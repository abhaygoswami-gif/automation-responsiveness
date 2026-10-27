# Responsive Website Automation (Java + Selenium)

A starter Maven project that checks a website at common desktop, tablet, and mobile viewport sizes and creates screenshots, an HTML report, and execution logs.

## Stack
- Java 8 compatible source code
- Maven
- Selenium WebDriver 4.8.3
- TestNG 7.8.0
- ExtentReports 5.1.1
- Log4j2
- WebDriverManager

## Requirements
1. JDK 8 or newer installed.
2. IntelliJ IDEA.
3. Google Chrome installed.
4. Maven available through IntelliJ or installed locally.
5. Internet access on the first run so Maven can download dependencies and WebDriverManager can resolve the Chrome driver.

> This project is pinned to Selenium 4.8.3 to keep Java 8 source/runtime compatibility. If your environment uses a newer JDK, you can upgrade Selenium later.

## Open in IntelliJ
1. Extract the ZIP.
2. IntelliJ IDEA → **File → Open** → select the extracted `ResponsiveAutomationProject` folder.
3. Trust/open the Maven project and wait for dependencies to download.
4. Open `config.properties` and set `baseUrl` to the target website.
5. Right-click `testng.xml` and select **Run**. Or run `mvn clean test` from the project terminal.

## Run from terminal
```bash
mvn clean test
```

To override the URL without editing the config:
```bash
mvn clean test -Dtarget.url=https://example.com
```
The test setup reads the `baseUrl` config property; for command-line override use:
```bash
mvn clean test -DbaseUrl=https://example.com
```

To run Chrome headless:
```bash
mvn clean test -Dheadless=true
```

## Output files
- `test-output/ExtentReport.html` — ExtentReports HTML report
- `test-output/logs/automation.log` — timestamped execution log
- `test-output/screenshots/` — screenshot for each tested viewport and failure screenshots where the listener can access the driver

Open `test-output/ExtentReport.html` in a browser after the run.

## Current test coverage
- Viewport sizes: 1920×1080, 1366×768, 1024×768, 768×1024, 390×844, 375×812
- Page-level horizontal overflow check
- Basic detection of common login controls
- Screenshot capture for each viewport
- TestNG pass/fail reporting and failure logging

## Important notes
- The horizontal overflow check is a useful first signal, not proof of complete responsiveness. Some wide tables intentionally scroll horizontally.
- The login control selectors are generic. Update them if the website uses custom controls or different markup.
- A browser viewport is not the same as a real physical device. For more comprehensive mobile coverage, consider Chrome DevTools emulation or a cloud device service.
- The target login page may require authentication to test pages behind login. This starter does not submit credentials or bypass authentication.
- Check screenshots and logs before filing bugs; the test deliberately reports potential layout issues for human review.
