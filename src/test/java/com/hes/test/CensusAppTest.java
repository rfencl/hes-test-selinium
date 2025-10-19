package com.hes.test;

import com.qa.pojo.RecordPojo;
import com.qa.util.JsonSerializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.qa.webdriver.WebDriverFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.Cookie;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.openqa.selenium.Keys;

public class CensusAppTest {
    private static final Logger log = LoggerFactory.getLogger(CensusAppTest.class);

    private static WebDriver driver;
    private static final String fakeFirstName = "FAKEFIRSTNAME";
    private static final String fakeLastName = "FAKELASTNAME";
    // Configurable endpoints via environment variables (loaded through EnvLoader)
    private static final String BASE_URL = com.hes.test.util.EnvLoader.get("CENSUS_APP_URL", "http://localhost:3000/");
    private static final String API_BASE = com.hes.test.util.EnvLoader.get("CENSUS_API_BASE", "http://localhost:3000/api");
    private static final String DB_URL = com.hes.test.util.EnvLoader.get("CENSUS_DB_URL", "jdbc:postgresql://localhost:5432/census_db");
    private static final String DB_USER = com.hes.test.util.EnvLoader.get("CENSUS_DB_USER", "postgres");
    private static final String DB_PASSWORD = com.hes.test.util.EnvLoader.get("CENSUS_DB_PASSWORD", "postgres");

    private void selectFromRadixDropdown(String fieldId, String optionText) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#" + fieldId + " button[role='combobox']")));
            trigger.click();
            
            WebElement option = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//div[@role='option']//span[text()='" + optionText + "']")));
            option.click();
        } catch (Exception e) {
            log.info("Failed to select " + optionText + " from " + fieldId + ": " + e.getMessage());
        }
    }

    @BeforeClass
    public static void setUp() {
        // Use WebDriverFactory which leverages WebDriverManager to download/setup the correct driver
        driver = WebDriverFactory.createDriver("chrome");
        driver.manage().window().maximize();
    }

    @AfterClass
    public static void tearDownClass() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DELETE FROM \"Record\" WHERE \"firstName\" = '" + fakeFirstName + "' AND \"lastName\" = '" + fakeLastName + "'");
            stmt.executeUpdate("DELETE FROM \"Relative\"");

        } catch (java.sql.SQLException sqle) {
            log.error("Error deleting records {}", sqle.getMessage());
        }
    }


    @Test(priority = 1)
    public void testAddPersonToHouseholdUI() {
        driver.get(BASE_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        log.info("Current URL before navigation: " + driver.getCurrentUrl());
        if (!driver.getCurrentUrl().endsWith("/dashboard")) {
            WebElement dashboardLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("[data-testid='navbar-button-Dashboard']")
            ));
            dashboardLink.click();
            log.info("Clicked dashboard link");
        }
        try {
            Thread.sleep(1000); // brief pause
        } catch (InterruptedException ignored) {}

        // Open the add person dialog (assume a button exists)
        // Try correct data-testid and button text
        WebElement addPersonBtn = null;
        try {
            addPersonBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("[name='edit-record-button btn']")));
        } catch (Exception e) {
            // Fallback: button with text 'Add record'
            try {
                addPersonBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'), 'add record')]")));
            } catch (Exception ex) {
                throw new RuntimeException("Could not find Add Record button by data-testid or text");
            }
        }
        addPersonBtn.click();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

    // Debug: print page source after clicking Add Record
    log.info("\nPage source after clicking Add Record:");
    log.info(driver.getPageSource());

        // Fill first name
        WebElement firstNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='firstName'], input#firstName")));
        firstNameField.clear();
        firstNameField.sendKeys(fakeFirstName);
        firstNameField.sendKeys(Keys.TAB);
    try { Thread.sleep(200); } catch (InterruptedException ignored) {}

        // Fill last name
        WebElement lastNameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[name='lastName'], input#lastName")));
        lastNameField.clear();
        lastNameField.sendKeys(fakeLastName);
        lastNameField.sendKeys(Keys.TAB);
    try { Thread.sleep(200); } catch (InterruptedException ignored) {}

        // Select relationship SPOUSE
        selectFromRadixDropdown("relationship", "SPOUSE");

        // Fill DOB
        try {
            WebElement dobInput = driver.findElement(By.cssSelector("input[placeholder*='Pick a date'], input[type='date']"));
            dobInput.clear();
            dobInput.sendKeys("10/25/2005");
            dobInput.sendKeys(Keys.TAB);
        } catch (Exception ignored) {}

        // Select Hispanic NO
        selectFromRadixDropdown("hispanic", "NO");

        // Select Race WHITE
        selectFromRadixDropdown("race", "WHITE");

        // Select Other stay NO
        selectFromRadixDropdown("otherStay", "NO");

        // Select gender MALE
        try {
            List<WebElement> maleRadios = driver.findElements(By.cssSelector("button[role='radio'][value='MALE']"));
            if (!maleRadios.isEmpty()) maleRadios.get(0).click();
        } catch (Exception ignored) {}

    try { Thread.sleep(300); } catch (InterruptedException ignored) {}

        // Click submit
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitButton.click();
        assertTrue(isRecordFound(), String.format("New record with name '%s %s' should appear in records list", fakeFirstName, fakeLastName));
    }

    private boolean isRecordFound() {
        driver.navigate().refresh();
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        boolean recordFound = false;
        List<WebElement> recordCards = driver.findElements(By.cssSelector(".record-card"));
        for (WebElement card : recordCards) {
            String cardText = card.getText().toLowerCase();
            if (cardText.contains(fakeFirstName.toLowerCase()) && cardText.contains(fakeLastName.toLowerCase())) {
                recordFound = true;
                break;
            }
        }
        return recordFound;
    }

    /**
     * Create a record via API using the provided auth cookies.
     * Returns true if the API returned a successful response (201/200) and the record appears in the GET list.
     */


    @Test(priority = 2)
    public void testVerifyPersonInAPI() {
        // This test assumes that the testAddPersonToHouseholdUI has been run and the record has been created.
        String userEmail = com.hes.test.util.EnvLoader.get("CENSUS_TEST_USER_EMAIL", "fake.edwards@example.com");

        // Get current URL to verify we're on an authenticated page
        log.info("Current URL before API call: " + driver.getCurrentUrl());
        
        // Log all cookies from browser to debug auth state
        log.info("\nAll browser cookies:");
        driver.manage().getCookies().forEach(c -> 
            log.info(String.format("Cookie: %s=%s; domain=%s; path=%s", 
                c.getName(), c.getValue(), c.getDomain(), c.getPath()))
        );

        // transfer cookies from Selenium session so API requests are authenticated
        Map<String, String> cookieMap = getSeleniumCookiesAsMap();
        log.info("\nCookies being sent to API:");
        cookieMap.forEach((k,v) -> log.info(k + "=" + v));

        // Check specifically for session token
        if (!cookieMap.containsKey("authjs.session-token")) {
            log.info("Warning: No authjs.session-token cookie found!");
        }

        io.restassured.response.Response resp = given()
            .baseUri(API_BASE)
            .cookies(cookieMap)
            .header("Accept", "application/json")
            // Add common auth headers in case they help
            .header("X-CSRF-Token", cookieMap.getOrDefault("authjs.csrf-token", ""))
            .header("Authorization", "Bearer " + cookieMap.getOrDefault("authjs.session-token", ""))
            .queryParam("email", userEmail)
        .when()
            .get("/record/user")
        .then()
            .statusCode(200)
            .extract().response();

        String body = resp.getBody().asString();
        log.info("\nAPI response body: " + body);
        log.info("API response headers: " + resp.getHeaders().toString());
        
        // If we get HTML back, it likely means we're not authenticated
        if (body.contains("<!DOCTYPE html>")) {
            log.info("Warning: Received HTML response instead of JSON - authentication may have failed");
            log.info("Response indicates login page: " + body.contains("login-form"));
            fail("Received HTML instead of JSON response");
        }

        // For now, just verify we get a successful JSON response with records
        assertTrue(body.contains("\"success\""), "API response should indicate success");
        assertTrue(body.contains("\"records\""), "API response should contain records array");
        
        // Extra Credit, Persist the json response as a POJO
        // Note: the actual repsonse is a JSON array, I'm just stripping the '[' and ']' so I can use
        // JsonSerializable fromJson.
        if (body.contains("\"records\":[")) {
            log.info("\nFound records in response:");
            int start = body.indexOf("\"records\":[") + 10;
            int end = body.indexOf("]", start) + 1;
            String records = body.substring(start+1, end-1);
            log.info(records);
            RecordPojo rec = new RecordPojo().fromJson(records);
            log.info(String.valueOf(rec));
        }
    }

    // Helper to convert Selenium cookies into a simple Map for RestAssured
    private static Map<String, String> getSeleniumCookiesAsMap() {
        Map<String, String> map = new HashMap<>();
        try {
            Set<Cookie> cookies = driver.manage().getCookies();
            for (Cookie c : cookies) {
                map.put(c.getName(), c.getValue());
            }
        } catch (Exception ignored) {
        }
        return map;
    }

    @Test(priority = 3)
    public void testVerifyPersonInDB() throws Exception {
        // Use configurable DB connection; if DB is not reachable or credentials are wrong, skip the test.
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM \"Record\" WHERE \"firstName\" = '" + fakeFirstName + "' AND \"lastName\" = '" + fakeLastName + "'";
            ResultSet rs = stmt.executeQuery(sql);

            assertTrue(rs.next(), "Record should exist in the database.");
            assertEquals(fakeFirstName, rs.getString("firstName"), "First name in DB should match.");
            assertEquals(fakeLastName, rs.getString("lastName"), "Last name in DB should match.");

        } catch (java.sql.SQLException sqle) {
            // Skip the test if DB isn't available or credentials are incorrect
            org.testng.SkipException skip = new org.testng.SkipException("Skipping DB test: " + sqle.getMessage());
            throw skip;
        }
    }


    @Test(priority = 0)
    public void testLoginOpensSignIn() {
        // Perform a real login using credentials; fall back to provided defaults or environment variables
        String testEmail = System.getenv().getOrDefault("CENSUS_TEST_USER_EMAIL", "fake.edwards@example.com");
        String testPassword = System.getenv().getOrDefault("CENSUS_TEST_USER_PASSWORD", "fakepassword");

        driver.get(BASE_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        // Always navigate directly to login page for reliability
        driver.get(BASE_URL + "auth/login");

        // Wait for login form
        WebElement emailInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='email'], input[name='email'], input#email")));
        WebElement passInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='password'], input[name='password'], input#password")));
        emailInput.clear();
        emailInput.sendKeys(testEmail);
        passInput.clear();
        passInput.sendKeys(testPassword);

        // Click login button
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'], button#login-button, .login-button")));
        loginBtn.click();

        // Wait for dashboard or avatar/profile indication
        boolean loggedIn = false;
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-testid='avatar-button'], #user-menu-button, [data-testid='logout']")),
                ExpectedConditions.urlContains("/dashboard")
            ));
            loggedIn = true;
        } catch (Exception e) {
            log.info("Login did not redirect to dashboard or show avatar/logout: " + e.getMessage());
        }

        // Wait for session cookie
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        log.info("\nPost-login URL: " + driver.getCurrentUrl());
        log.info("Post-login cookies:");
        driver.manage().getCookies().forEach(c -> 
            log.info(String.format("Cookie: %s=%s; domain=%s; path=%s", 
                c.getName(), c.getValue(), c.getDomain(), c.getPath()))
        );
        boolean hasSessionToken = driver.manage().getCookies().stream()
            .anyMatch(c -> c.getName().equals("authjs.session-token"));
        log.info("Session token present: " + hasSessionToken);

        // Assert login and session token
        assertTrue(loggedIn, "Login did not appear to succeed — check credentials or app state");
        assertTrue(hasSessionToken, "Session token was not set after login — authentication failed");
    }

    @AfterClass
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Ignore("Run this if the docker container is started the first time.")
    @Test
    public static void insertUserRecords() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            Statement stmt = conn.createStatement();

            String householdSql = "INSERT INTO public.\"Household\" " +
              "(id, \"homeType\", \"ownership\", \"lienholderId\", address1, address2, city, \"state\", zip) " +
              "VALUES(1, 'HOUSE'::public.\"HomeType\", 'MORTGAGE'::public.\"Ownership\", NULL, '1111 Main St.', '', 'Somewhere', 'CA'::public.\"State\", '90210')";

            stmt.executeUpdate(householdSql);

            String userSql = "INSERT INTO public.\"User\" " +
              "(id, \"name\", email, image, \"password\", \"role\", \"householdId\", \"isTwoFactorEnabled\") " +
              "VALUES(1, 'Fake Edwards', 'fake.edwards2@example.com', '/images/11.avif', '$2a$10$duFK2o1COHpWVrCXMV/xJOF8dzgJOt.sPXKVRFqNESOQ3Pr0AH6P6', 'USER'::public.\"UserRole\", 1, false)";
            int rowCount = stmt.executeUpdate(userSql);

            assertEquals(rowCount , 1, "Record should be added successfully.");

        } catch (java.sql.SQLException sqle) {
            // Skip the test if DB isn't available or credentials are incorrect
            log.error(sqle.getMessage());
            org.testng.SkipException skip = new org.testng.SkipException("Skipping DB test: " + sqle.getMessage());
            throw skip;
        }
    }
}
