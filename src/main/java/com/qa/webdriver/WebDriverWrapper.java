package com.qa.webdriver;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class WebDriverWrapper {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Duration defaultTimeout;
    
    public WebDriverWrapper(WebDriver driver) {
        this(driver, Duration.ofSeconds(10));
    }
    
    public WebDriverWrapper(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.defaultTimeout = timeout;
        this.wait = new WebDriverWait(driver, timeout);
    }
    
    // Navigation methods
    public void navigateTo(String url) {
        driver.get(url);
    }
    
    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }
    
    public String getTitle() {
        return driver.getTitle();
    }
    
    // Element finding with wait
    public WebElement findElement(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }
    
    public List<WebElement> findElements(By locator) {
        wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        return driver.findElements(locator);
    }
    
    public WebElement findClickableElement(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    
    public WebElement findVisibleElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    // Click methods
    public void click(By locator) {
        findClickableElement(locator).click();
    }
    
    public void click(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
    }
    
    // Text input methods
    public void type(By locator, String text) {
        WebElement element = findElement(locator);
        element.clear();
        element.sendKeys(text);
    }
    
    public void type(WebElement element, String text) {
        element.clear();
        element.sendKeys(text);
    }
    
    public void append(By locator, String text) {
        findElement(locator).sendKeys(text);
    }
    
    // Text retrieval methods
    public String getText(By locator) {
        return findVisibleElement(locator).getText();
    }
    
    public String getText(WebElement element) {
        return element.getText();
    }
    
    public String getAttribute(By locator, String attribute) {
        return findElement(locator).getAttribute(attribute);
    }
    
    // Dropdown methods
    public void selectByText(By locator, String text) {
        Select select = new Select(findElement(locator));
        select.selectByVisibleText(text);
    }
    
    public void selectByValue(By locator, String value) {
        Select select = new Select(findElement(locator));
        select.selectByValue(value);
    }
    
    public void selectByIndex(By locator, int index) {
        Select select = new Select(findElement(locator));
        select.selectByIndex(index);
    }
    
    public String getSelectedText(By locator) {
        Select select = new Select(findElement(locator));
        return select.getFirstSelectedOption().getText();
    }
    
    // Checkbox and radio button methods
    public void check(By locator) {
        WebElement element = findElement(locator);
        if (!element.isSelected()) {
            element.click();
        }
    }
    
    public void uncheck(By locator) {
        WebElement element = findElement(locator);
        if (element.isSelected()) {
            element.click();
        }
    }
    
    public boolean isChecked(By locator) {
        return findElement(locator).isSelected();
    }
    
    // Wait methods
    public void waitForElementVisible(By locator) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    
    public void waitForElementClickable(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator));
    }
    
    public void waitForTextPresent(By locator, String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }
    
    public void waitForElementInvisible(By locator) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }
    
    // Verification methods
    public boolean isElementPresent(By locator) {
        try {
            driver.findElement(locator);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }
    
    public boolean isElementVisible(By locator) {
        try {
            return findElement(locator).isDisplayed();
        } catch (TimeoutException | NoSuchElementException e) {
            return false;
        }
    }
    
    public boolean isElementEnabled(By locator) {
        return findElement(locator).isEnabled();
    }
    
    // Alert handling
    public void acceptAlert() {
        wait.until(ExpectedConditions.alertIsPresent()).accept();
    }
    
    public void dismissAlert() {
        wait.until(ExpectedConditions.alertIsPresent()).dismiss();
    }
    
    public String getAlertText() {
        return wait.until(ExpectedConditions.alertIsPresent()).getText();
    }
    
    public void typeInAlert(String text) {
        wait.until(ExpectedConditions.alertIsPresent()).sendKeys(text);
    }
    
    // Window handling
    public void switchToWindow(String windowHandle) {
        driver.switchTo().window(windowHandle);
    }
    
    public void switchToNewWindow() {
        String currentWindow = driver.getWindowHandle();
        for (String windowHandle : driver.getWindowHandles()) {
            if (!windowHandle.equals(currentWindow)) {
                driver.switchTo().window(windowHandle);
                break;
            }
        }
    }
    
    public void closeCurrentWindow() {
        driver.close();
    }
    
    // Frame handling
    public void switchToFrame(By locator) {
        driver.switchTo().frame(findElement(locator));
    }
    
    public void switchToFrame(int index) {
        driver.switchTo().frame(index);
    }
    
    public void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }
    
    // JavaScript execution
    public Object executeScript(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }
    
    public void scrollToElement(By locator) {
        WebElement element = findElement(locator);
        executeScript("arguments[0].scrollIntoView(true);", element);
    }
    
    // Utility methods
    public void refresh() {
        driver.navigate().refresh();
    }
    
    public void back() {
        driver.navigate().back();
    }
    
    public void forward() {
        driver.navigate().forward();
    }
    
    public WebDriver getDriver() {
        return driver;
    }
    
    public void quit() {
        if (driver != null) {
            driver.quit();
        }
    }
}
