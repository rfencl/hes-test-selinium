package com.qa.webdriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

@Ignore
public class WebDriverWrapperExampleTest {
    private static final Logger log = LoggerFactory.getLogger(WebDriverWrapperExampleTest.class);
    private WebDriverWrapper wrapper;
    
    @BeforeMethod
    public void setUp() {
        wrapper = WebDriverFactory.createWrapper("chrome", true);
    }
    
    @AfterMethod
    public void tearDown() {
        if (wrapper != null) {
            wrapper.quit();
        }
    }
    
    @Test
    public void testCompleteLoginWorkflow() {
        log.info("=== Complete Login Workflow Test ===");
        
        // Navigate to login page
        wrapper.navigateTo("https://the-internet.herokuapp.com/login");
        log.info("Navigated to: " + wrapper.getCurrentUrl());
        
        // Verify login form elements are present
        Assert.assertTrue(wrapper.isElementVisible(By.id("username")), "Username field should be visible");
        Assert.assertTrue(wrapper.isElementVisible(By.id("password")), "Password field should be visible");
        Assert.assertTrue(wrapper.isElementVisible(By.cssSelector("button[type='submit']")), "Login button should be visible");
        
        // Perform login
        wrapper.type(By.id("username"), "tomsmith");
        wrapper.type(By.id("password"), "SuperSecretPassword!");
        wrapper.click(By.cssSelector("button[type='submit']"));
        
        // Verify successful login
        wrapper.waitForElementVisible(By.cssSelector(".flash.success"));
        String successMessage = wrapper.getText(By.cssSelector(".flash.success"));
        Assert.assertTrue(successMessage.contains("You logged into a secure area!"));
        
        // Verify we're on the secure page
        Assert.assertTrue(wrapper.getCurrentUrl().contains("/secure"));
        Assert.assertTrue(wrapper.isElementVisible(By.xpath("//a[@href='/logout']")));
        
        log.info("Login workflow completed successfully!");
    }
    
    @Test
    public void testFormInteractionWorkflow() {
        log.info("=== Form Interaction Workflow Test ===");
        
        wrapper.navigateTo("https://the-internet.herokuapp.com/dropdown");
        
        // Test dropdown interactions
        wrapper.selectByText(By.id("dropdown"), "Option 1");
        Assert.assertEquals(wrapper.getSelectedText(By.id("dropdown")), "Option 1");
        log.info("Selected Option 1 from dropdown");
        
        wrapper.selectByValue(By.id("dropdown"), "2");
        Assert.assertEquals(wrapper.getSelectedText(By.id("dropdown")), "Option 2");
        log.info("Selected Option 2 from dropdown");
        
        // Navigate to checkboxes page
        wrapper.navigateTo("https://the-internet.herokuapp.com/checkboxes");
        
        By checkbox1 = By.xpath("//input[@type='checkbox'][1]");
        By checkbox2 = By.xpath("//input[@type='checkbox'][2]");
        
        // Test checkbox interactions
        boolean initialState1 = wrapper.isChecked(checkbox1);
        boolean initialState2 = wrapper.isChecked(checkbox2);
        
        wrapper.check(checkbox1);
        wrapper.uncheck(checkbox2);
        
        Assert.assertTrue(wrapper.isChecked(checkbox1));
        Assert.assertFalse(wrapper.isChecked(checkbox2));
        
        log.info("Checkbox interactions completed successfully!");
    }
    
    @Test
    public void testDynamicContentHandling() {
        log.info("=== Dynamic Content Handling Test ===");
        
        wrapper.navigateTo("https://the-internet.herokuapp.com/dynamic_loading/2");
        
        // Verify initial state
        Assert.assertFalse(wrapper.isElementVisible(By.xpath("//div[@id='finish']/h4")));
        
        // Start dynamic loading
        wrapper.click(By.xpath("//button[text()='Start']"));
        log.info("Started dynamic loading...");
        
        // Wait for loading to complete
        wrapper.waitForElementVisible(By.xpath("//div[@id='finish']/h4"));
        
        // Verify content appeared
        String finishText = wrapper.getText(By.xpath("//div[@id='finish']/h4"));
        Assert.assertEquals(finishText, "Hello World!");
        
        log.info("Dynamic content loaded successfully: " + finishText);
    }
    
    @Test
    public void testAlertHandlingWorkflow() {
        log.info("=== Alert Handling Workflow Test ===");
        
        wrapper.navigateTo("https://the-internet.herokuapp.com/javascript_alerts");
        
        // Test 1: Simple Alert
        wrapper.click(By.xpath("//button[text()='Click for JS Alert']"));
        String alertText = wrapper.getAlertText();
        log.info("Alert text: " + alertText);
        wrapper.acceptAlert();
        
        String result = wrapper.getText(By.id("result"));
        Assert.assertEquals(result, "You successfully clicked an alert");
        log.info("Simple alert handled successfully");
        
        // Test 2: Confirm Alert (Accept)
        wrapper.click(By.xpath("//button[text()='Click for JS Confirm']"));
        wrapper.acceptAlert();
        
        result = wrapper.getText(By.id("result"));
        Assert.assertEquals(result, "You clicked: Ok");
        log.info("Confirm alert accepted successfully");
        
        // Test 3: Confirm Alert (Dismiss)
        wrapper.click(By.xpath("//button[text()='Click for JS Confirm']"));
        wrapper.dismissAlert();
        
        result = wrapper.getText(By.id("result"));
        Assert.assertEquals(result, "You clicked: Cancel");
        log.info("Confirm alert dismissed successfully");
        
        // Test 4: Prompt Alert
        wrapper.click(By.xpath("//button[text()='Click for JS Prompt']"));
        wrapper.typeInAlert("Automated Test Input");
        wrapper.acceptAlert();
        
        result = wrapper.getText(By.id("result"));
        Assert.assertEquals(result, "You entered: Automated Test Input");
        log.info("Prompt alert handled successfully");
    }
    
    @Test
    public void testNavigationAndUtilityMethods() {
        log.info("=== Navigation and Utility Methods Test ===");
        
        // Navigate to initial page
        wrapper.navigateTo("https://the-internet.herokuapp.com/");
        String initialUrl = wrapper.getCurrentUrl();
        String initialTitle = wrapper.getTitle();
        
        log.info("Initial URL: " + initialUrl);
        log.info("Initial Title: " + initialTitle);
        
        // Navigate to another page
        wrapper.click(By.linkText("A/B Testing"));
        wrapper.waitForElementVisible(By.xpath("//h3[contains(text(),'A/B Test')]"));
        
        String newUrl = wrapper.getCurrentUrl();
        Assert.assertTrue(newUrl.contains("/abtest"));
        log.info("Navigated to: " + newUrl);
        
        // Test back navigation
        wrapper.back();
        Assert.assertEquals(wrapper.getCurrentUrl(), initialUrl);
        log.info("Back navigation successful");
        
        // Test forward navigation
        wrapper.forward();
        Assert.assertTrue(wrapper.getCurrentUrl().contains("/abtest"));
        log.info("Forward navigation successful");
        
        // Test refresh
        wrapper.refresh();
        Assert.assertTrue(wrapper.getCurrentUrl().contains("/abtest"));
        log.info("Page refresh successful");
    }
    
    @Test
    public void testScrollAndJavaScriptExecution() {
        log.info("=== Scroll and JavaScript Execution Test ===");
        
        wrapper.navigateTo("https://the-internet.herokuapp.com/large");
        
        // Get initial scroll position
        Long initialScroll = (Long) wrapper.executeScript("return window.pageYOffset;");
        log.info("Initial scroll position: " + initialScroll);
        
        // Scroll to bottom
        wrapper.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        
        // Verify scroll position changed
        Long finalScroll = (Long) wrapper.executeScript("return window.pageYOffset;");
        log.info("Final scroll position: " + finalScroll);
        Assert.assertTrue(finalScroll > initialScroll);
        
        // Test scrolling to specific element
        wrapper.navigateTo("https://the-internet.herokuapp.com/");
        wrapper.scrollToElement(By.linkText("Sortable Data Tables"));
        
        // Verify element is in view
        Boolean isInView = (Boolean) wrapper.executeScript(
            "var element = arguments[0];" +
            "var rect = element.getBoundingClientRect();" +
            "return rect.top >= 0 && rect.bottom <= window.innerHeight;",
            wrapper.findElement(By.linkText("Sortable Data Tables"))
        );
        
        log.info("Element in view after scroll: " + isInView);
    }
}
