package com.lithium.mineraloil.selenium.elements;

import com.google.common.base.Preconditions;
import com.jayway.awaitility.core.ConditionTimeoutException;
import com.lithium.mineraloil.selenium.browsers.PageLoadWaiter;
import com.lithium.mineraloil.selenium.exceptions.DriverNotFoundException;
import com.lithium.mineraloil.selenium.exceptions.PageLoadWaiterTimeoutException;
import lombok.Setter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.remote.UnreachableBrowserException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Slf4j
public class Driver {
    private int activeDriverIndex = 0;

    @Setter
    private DriverConfiguration driverConfiguration;
    private LinkedList<DriverInstance> drivers = new LinkedList<>();
    private Set<PageLoadWaiter> pageLoadWaiters = new HashSet<>();

    @Delegate
    public WebdriverActions webdriver() {
        return new WebdriverActions(getDriver());
    }

    public DriverConfiguration getDriverConfiguration() {
        Preconditions.checkNotNull(driverConfiguration);
        return driverConfiguration;
    }

    public void startDriver() {
        Preconditions.checkNotNull(driverConfiguration);
        DriverInstance driverInstance = new DriverInstance(driverConfiguration);
        drivers.add(driverInstance);
        resetActiveDriverIndex();
        log.info("User Agent: " + getUserAgent());
    }

    public void useDriver(WebDriver driver) {
        Preconditions.checkNotNull(driver);
        DriverInstance driverInstance = new DriverInstance(driver);
        drivers.add(driverInstance);
        resetActiveDriverIndex();
        log.info("User Agent: " + getUserAgent());
    }

    public void stopDriver() {
        DriverInstance driverInstance = drivers.removeLast();
        log.info(String.format("Stopping Last Opened Driver. Drivers Running: %s", getDriverCount()));
        driverInstance.getDriver().quit();
        resetActiveDriverIndex();
        if (isDriverStarted()) {
            switchWindow();
        } else {
            activeDriverIndex = 0;
        }
    }

    public String toString() {
        return getDriver().toString();
    }

    public int getNumberOfDrivers() {
        return getDriverCount();
    }

    public void get(String url) {
        try {
            getDriver().get(url);
        } catch (UnreachableBrowserException e) {
            // this is a workaround for losing the connection or failing to start driver
            log.info("WebDriver died...attempting restart");
            stopDriver();
            startDriver();
            getDriver().get(url);
        }
        waitForPageLoad();
    }

    private WebDriver getDriver() {
        if (!isDriverStarted()) throw new DriverNotFoundException("Unable to locate a started WebDriver instance");
        return drivers.get(activeDriverIndex).getDriver();
    }

    // switches to the last opened window
    public void switchWindow() {
        List<String> windowHandles = new ArrayList<>(getWindowHandles());
        getDriver().switchTo().window(windowHandles.get(windowHandles.size() - 1));
    }

    // selects active driver
    public void switchDriver(int index) {
        Preconditions.checkArgument(index < getDriverCount());
        this.activeDriverIndex = index;
    }

    // closes the last opened window
    public void closeWindow() {
        switchWindow();
        if (getWindowHandles().size() > 1) {
            getDriver().close();
            switchWindow();
        }
    }

    public void stop() {
        while (isDriverStarted()) {
            try {
                stopDriver();
            } catch (WebDriverException e) {
                log.info(String.format("There was an ignored exception closing the web driver : %s", e));
            }
        }
    }

    public String getDownloadDirectory() {
        return getDriverConfiguration().getDownloadDirectory();
    }

    public boolean isDriverStarted() {
        return getDriverCount() > 0;
    }

    public void addPageLoadWaiter(PageLoadWaiter pageLoadWaiter) {
        pageLoadWaiters.add(pageLoadWaiter);
    }

    public void waitForPageLoad() {
        for (PageLoadWaiter pageLoadWaiter : pageLoadWaiters) {
            String callerClass = pageLoadWaiter.getClass().getEnclosingClass().getName();
            String callerPackage = pageLoadWaiter.getClass().getEnclosingClass().getPackage().getName();
            String exceptionMessage = String.format("Timed out in PageLoadWaiter: package '%s', class '%s'", callerPackage, callerClass);

            try {
                Waiter.await()
                      .atMost(pageLoadWaiter.getTimeout(), pageLoadWaiter.getTimeUnit())
                      .until(() -> pageLoadWaiter.isSatisfied());
            } catch (ConditionTimeoutException e) {
                throw new PageLoadWaiterTimeoutException(exceptionMessage);
            }
        }
    }

    public LogEntries getConsoleLog() {
        log.info("Console Log output: ");
        executeScript("console.log('Logging Errors');");
        return getDriver().manage().logs().get(LogType.BROWSER);
    }

    private void resetActiveDriverIndex() {
        activeDriverIndex = getDriverCount() - 1;
    }

    private int getDriverCount() {
        return drivers.size();
    }

    List<WebElement> findElements(By by) {
        return getDriver().findElements(by);
    }

    WebElement findElement(By by) {
        return getDriver().findElement(by);
    }


    public BaseElement createBaseElement(By by) {
        return new BaseElement(this, by);
    }

    public ElementList<BaseElement> createBaseElements(By by) {
        return new ElementList<>(this, by, BaseElement.class);
    }

    public ButtonElement createButtonElement(By by) {
        return new ButtonElement(this, by);
    }

    public ElementList<ButtonElement> createButtonElements(By by) {
        return new ElementList<>(this, by, ButtonElement.class);
    }

    public RadioElement createRadioElement(By by) {
        return new RadioElement(this, by);
    }

    public ElementList<RadioElement> createRadioElements(By by) {
        return new ElementList<>(this, by, RadioElement.class);
    }

    public CheckboxElement createCheckboxElement(By by) {
        return new CheckboxElement(this, by);
    }

    public ElementList<CheckboxElement> createCheckboxElements(By by) {
        return new ElementList<>(this, by, CheckboxElement.class);
    }

    public ImageElement createImageElement(By by) {
        return new ImageElement(this, by);
    }

    public ElementList<ImageElement> createImageElements(By by) {
        return new ElementList<>(this, by, ImageElement.class);
    }

    public TextElement createTextElement(By by) {
        return new TextElement(this, by);
    }

    public ElementList<TextElement> createTextElements(By by) {
        return new ElementList<>(this, by, TextElement.class);
    }

    public LinkElement createLinkElement(By by) {
        return new LinkElement(this, by);
    }

    public ElementList<LinkElement> createLinkElements(By by) {
        return new ElementList<>(this, by, LinkElement.class);
    }

    public SelectListElement createSelectListElement(By by) {
        return new SelectListElement(this, by);
    }

    public TableElement createTableElement(By by) {
        return new TableElement(this, by);
    }

    public ElementList<TableElement> createTableElements(By by) {
        return new ElementList<>(this, by, TableElement.class);
    }

    public TableRowElement createTableRowElement(By by) {
        return new TableRowElement(this, by);
    }

    public ElementList<TableRowElement> createTableRowElements(By by) {
        return new ElementList<>(this, by, TableRowElement.class);
    }

    public ElementList<SelectListElement> createSelectListElements(By by) {
        return new ElementList<>(this, by, SelectListElement.class);
    }

    public LabelElement createLabelElement(Element referenceElement) {
        return new LabelElement(this, referenceElement);
    }

    public FileUploadElement createFileUploadElement(By by) {
        return new FileUploadElement(this, by);
    }


}
