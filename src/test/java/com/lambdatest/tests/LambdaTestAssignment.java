package com.lambdatest.tests;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class LambdaTestAssignment {

    RemoteWebDriver driver;

    // <-- Replace these with your actual LambdaTest credentials
    String username = "ashwinikamblequalitrix";
    String accessKey = "LT_o5TcDi62VKHvy4CoCDThSbYtKEiBTaAdrOftVOX256lQxsy";

    @Parameters({"browserName", "browserVersion", "platformName", "testName"})
    @BeforeClass(alwaysRun = true)
    public void setUp(String browserName, String browserVersion, String platformName, String testName) throws Exception {
        MutableCapabilities caps = new MutableCapabilities();
        caps.setCapability("browserName", browserName);
        caps.setCapability("browserVersion", browserVersion);

        Map<String, Object> ltOptions = new HashMap<>();
        ltOptions.put("platformName", platformName);
        ltOptions.put("build", "LambdaTest Assignment Build");
        ltOptions.put("name", testName);
        ltOptions.put("selenium_version", "4.24.0");

        // enable logs & recordings
        ltOptions.put("network", true);
        ltOptions.put("video", true);
        ltOptions.put("console", true);
        ltOptions.put("visual", true);

        // optional but OK to include
        ltOptions.put("username", username);
        ltOptions.put("accessKey", accessKey);

        caps.setCapability("LT:Options", ltOptions);

        String hubUrl = "https://" + username + ":" + accessKey + "@hub.lambdatest.com/wd/hub";
        driver = new RemoteWebDriver(new URL(hubUrl), caps);

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(20));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @Test
    public void testExploreAllIntegrationsOpensInNewTab() {
        // 1) Navigate
        driver.get("https://www.lambdatest.com");

        // 2) Explicit wait for DOM elements (20s)
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(org.openqa.selenium.By.tagName("a")));

        // 3) Try multiple locators to find 'Explore all Integrations' (3 locator types)
        WebElement exploreLink = null;
        String expectedHref = null;

        // Locator attempt 1: partial link text
        try {
            exploreLink = driver.findElement(org.openqa.selenium.By.partialLinkText("Explore all Integrations"));
        } catch (org.openqa.selenium.NoSuchElementException ignored) {}

        // Locator attempt 2: XPath
        if (exploreLink == null) {
            try {
                exploreLink = driver.findElement(org.openqa.selenium.By.xpath("//a[contains(text(),'Explore all Integrations')]"));
            } catch (org.openqa.selenium.NoSuchElementException ignored) {}
        }

        // Locator attempt 3: CSS selector (href contains integrations)
        if (exploreLink == null) {
            try {
                exploreLink = driver.findElement(org.openqa.selenium.By.cssSelector("a[href*='integrations']"));
            } catch (org.openqa.selenium.NoSuchElementException ignored) {}
        }

        if (exploreLink == null) {
            throw new RuntimeException("Could not find 'Explore all Integrations' link using any locator.");
        }

        expectedHref = exploreLink.getAttribute("href");

        // Scroll it into view
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior:'instant', block:'center'});", exploreLink);

        // Force open in new tab (set target)
        ((JavascriptExecutor) driver).executeScript("arguments[0].setAttribute('target','_blank');", exploreLink);

        // 4) Click the link
        exploreLink.click();

        // 5) Wait and get window handles
        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        List<String> handles = new ArrayList<>(driver.getWindowHandles());
        System.out.println("Window handles: " + handles);

        // 6) Switch to new tab and verify URL
        String parentHandle = driver.getWindowHandle();
        String newHandle = handles.stream().filter(h -> !h.equals(parentHandle)).findFirst().orElse(handles.get(0));
        driver.switchTo().window(newHandle);

        // wait for load
        wait.until(d -> ((JavascriptExecutor)d).executeScript("return document.readyState").equals("complete"));

        String currentUrl = driver.getCurrentUrl();
        System.out.println("Expected href: " + expectedHref);
        System.out.println("Current URL in new tab: " + currentUrl);

        Assert.assertEquals(currentUrl, expectedHref, "Opened URL does not match expected URL.");

        // Print LambdaTest Session ID (Test ID)
        System.out.println("LambdaTest Session ID (Test ID): " + driver.getSessionId());

        // 7) Close new tab and switch back
        driver.close();
        driver.switchTo().window(parentHandle);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (driver != null) driver.quit();
    }
}
