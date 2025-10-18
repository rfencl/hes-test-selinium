package com.qa.webdriver;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;
@Ignore
public class WebDriverWrapperTest {
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
    public void testNavigationMethods() {
        wrapper.navigateTo("https://the-internet.herokuapp.com/");
        
        Assert.assertTrue(wrapper.getCurrentUrl().contains("the-internet.herokuapp.com"));
        Assert.assertEquals(wrapper.getTitle(), "The Internet");
    }
    
    @Test
    public void testElementFinding() {
        wrapper.navigateTo("https://the-internet.herokuapp.com/login");
        
        // Test finding elements
        Assert.assertTrue(wrapper.isElementPresent(By.id("username")));
        Assert.assertTrue(wrapper.isElementVisible(By.id("username")));
        Assert.assertTrue(wrapper.isElementEnabled(By.id("username")));
    }
    
    @Test
    public void testTextInput() {
        wrapper.navigateTo("https://the-internet.herokuapp.com/login");
        
        // Test typing in input fields
        wrapper.type(By.id("username"), "tomsmith");
        wrapper.type(By.id("password"), "SuperSecretPassword!");
        
        // Verify text was entered
        Assert.assertEquals(wrapper.getAttribute(By.id("username"), "value"), "tomsmith");
        Assert.assertEquals(wrapper.getAttribute(By.id("password"), "value"), "SuperSecretPassword!");
    }
    
    @Test
    public void testClickAndLogin() {
        wrapper.navigateTo("https://the-internet.herokuapp.com/login");
        
        wrapper.type(By.id("username"), "tomsmith");
        wrapper.type(By.id("password"), "SuperSecretPassword!");
        wrapper.click(By.cssSelector("button[type='submit']"));
        
        // Verify successful login
        wrapper.waitForElementVisible(By.cssSelector(".flash.success"));
        String successMessage = wrapper.getText(By.cssSelector(".flash.success"));
        Assert.assertTrue(successMessage.contains("You logged into a secure area!"));
    }
    
    @Test
    public void testDropdownSelection() {
        wrapper.navigateTo("https://the-internet.herokuapp.com/dropdown");
        
        // Test dropdown selection
        wrapper.selectByText(By.id("dropdown"), "Option 1");
        Assert.assertEquals(wrapper.getSelectedText(By.id("dropdown")), "Option 1");
        
        wrapper.selectByValue(By.id("dropdown"), "2");
        Assert.assertEquals(wrapper.getSelectedText(By.id("dropdown")), "Option 2");
    }
    
    @Test
    public void testCheckboxes() {
        wrapper.navigateTo("https://the-internet.herokuapp.com/checkboxes");
        
        By checkbox1 = By.xpath("//input[@type='checkbox'][1]");
        By checkbox2 = By.xpath("//input[@type='checkbox'][2]");
        
        // Test checkbox operations
        wrapper.check(checkbox1);
        Assert.assertTrue(wrapper.isChecked(checkbox1));
        
        wrapper.uncheck(checkbox2);
        Assert.assertFalse(wrapper.isChecked(checkbox2));
    }
    
    @Test
    public void testAlertHandling() {
        wrapper.navigateTo("https://the-internet.herokuapp.com/javascript_alerts");
        
        // Test simple alert
        wrapper.click(By.xpath("//button[text()='Click for JS Alert']"));
        String alertText = wrapper.getAlertText();
        Assert.assertEquals(alertText, "I am a JS Alert");
        wrapper.acceptAlert();
        
        // Verify result
        String result = wrapper.getText(By.id("result"));
        Assert.assertEquals(result, "You successfully clicked an alert");
    }
    
    @Test
    public void testConfirmAlert() {
        wrapper.navigateTo("https://the-internet.herokuapp.com/javascript_alerts");
        
        // Test confirm alert - accept
        wrapper.click(By.xpath("//button[text()='Click for JS Confirm']"));
        wrapper.acceptAlert();
        
        String result = wrapper.getText(By.id("result"));
        Assert.assertEquals(result, "You clicked: Ok");
        
        // Test confirm alert - dismiss
        wrapper.click(By.xpath("//button[text()='Click for JS Confirm']"));
        wrapper.dismissAlert();
        
        result = wrapper.getText(By.id("result"));
        Assert.assertEquals(result, "You clicked: Cancel");
    }
    
    @Test
    public void testPromptAlert() {
        wrapper.navigateTo("https://the-internet.herokuapp.com/javascript_alerts");
        
        wrapper.click(By.xpath("//button[text()='Click for JS Prompt']"));
        wrapper.typeInAlert("Test Input");
        wrapper.acceptAlert();
        
        String result = wrapper.getText(By.id("result"));
        Assert.assertEquals(result, "You entered: Test Input");
    }
    
    @Test
    public void testScrollToElement() {
        wrapper.navigateTo("https://the-internet.herokuapp.com/large");
        
        // Get initial scroll position
        Long initialPosition = (Long) wrapper.executeScript("return window.pageYOffset;");
        
        // Scroll down
        wrapper.executeScript("window.scrollTo(0, 1000);");
        
        // Get final scroll position
        Long finalPosition = (Long) wrapper.executeScript("return window.pageYOffset;");
        
        // Verify page scrolled
        Assert.assertTrue(finalPosition > initialPosition);
    }
    
    @Test
    public void testWaitMethods() {
        wrapper.navigateTo("https://the-internet.herokuapp.com/dynamic_loading/1");
        
        wrapper.click(By.xpath("//button[text()='Start']"));
        
        // Wait for loading to complete
        wrapper.waitForElementInvisible(By.id("loading"));
        wrapper.waitForElementVisible(By.xpath("//div[@id='finish']/h4"));
        
        String finishText = wrapper.getText(By.xpath("//div[@id='finish']/h4"));
        Assert.assertEquals(finishText, "Hello World!");
    }
}
