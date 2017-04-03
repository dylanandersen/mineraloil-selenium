package com.lithium.mineraloil.selenium.elements;

import com.jayway.awaitility.core.ConditionTimeoutException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ByXPath;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
class ElementImpl<T extends Element> implements Element<T> {
    private int index = -1;
    private Element referenceElement;
    private boolean scrollIntoView = false;

    @Setter private Element iframeElement;
    @Setter private Element hoverElement;
    @Getter private Element parentElement;
    @Getter private final By by;
    @Getter private WebElement webElement;
    private Driver driver;


    public ElementImpl(Driver driver, Element<T> referenceElement, By by) {
        this.driver = driver;
        this.referenceElement = referenceElement;
        this.by = by;
    }

    public ElementImpl(Driver driver, Element<T> referenceElement, By by, int index) {
        this.driver = driver;
        this.referenceElement = referenceElement;
        this.by = by;
        this.index = index;
    }

    public ElementImpl(Driver driver, Element<T> referenceElement, Element parentElement, By by) {
        this.driver = driver;
        this.referenceElement = referenceElement;
        this.parentElement = parentElement;
        this.by = by;
    }

    public ElementImpl(Driver driver, Element<T> referenceElement, Element parentElement, By by, int index) {
        this.driver = driver;
        this.referenceElement = referenceElement;
        this.parentElement = parentElement;
        this.by = by;
        this.index = index;
    }


    @Override
    public WebElement locateElement(long waitTime, TimeUnit timeUnit) {
        try {
            if (timeUnit.equals(MILLISECONDS) && waitTime <= 100) {
                // handle when poll interval is less than awaitility default of 100MS
                Waiter.await()
                      .atMost(waitTime, MILLISECONDS)
                      .pollInterval(10, MILLISECONDS)
                      .until(() -> locateElement() != null);
            } else {
                Waiter.await()
                      .atMost(waitTime, timeUnit)
                      .pollInterval(100, MILLISECONDS)
                      .until(() -> locateElement() != null);
            }
        } catch (Exception e) {
            throw new ConditionTimeoutException("Unable to locate element using by: " + getBy());
        }
        return locateElement();
    }

    @Override
    public WebElement locateElement() {
        log.debug(String.format("WebDriver: locating element: '%s', index '%s', parent '%s'", by, index, parentElement));
        if (log.isDebugEnabled()) {
            if (driver.isAlertPresent()) {
                log.debug("GOT UNEXPECTED ALERT");
            }
            new Screenshot(driver).takeScreenshot("locateElement");
        }

        if (isWithinIFrame()) {
            ((BaseElement) iframeElement).switchFocusToIFrame();
        } else {
            switchFocusFromIFrame();
        }

        if (hoverElement != null && hoverElement.isDisplayed()) hoverElement.hover();

        // cache element
        if (webElement != null) {
            try {
                webElement.isDisplayed();
                return webElement;
            } catch (StaleElementReferenceException e) {
                // page has updated so re-fetch the element
            } catch (WebDriverException e) {
                // browser instance has been reloaded so re-fetch the element
            }
        }

        if (parentElement != null) {
            By parentBy = by;
            if (by instanceof ByXPath) {
                parentBy = getByForParentElement(by);
            }
            if (index >= 0) {
                List<WebElement> elements = parentElement.locateElement().findElements(parentBy);
                if (index > elements.size() - 1) {
                    throw new NoSuchElementException(String.format("Unable to locate an element at index: %s using %s", index, getBy()));
                }
                webElement = elements.get(index);
            } else {
                webElement = parentElement.locateElement().findElement(parentBy);
            }
        } else {
            if (index >= 0) {
                List<WebElement> elements = driver.findElements(by);
                if (index > elements.size() - 1) {
                    throw new NoSuchElementException(String.format("Unable to locate an element at index: %s using %s", index, getBy()));
                }
                webElement = elements.get(index);
            } else {
                webElement = driver.findElement(by);
            }
        }

        if (scrollIntoView) {
            scrollElement(webElement);
        }

        log.debug("WebDriver: Found element: " + webElement);
        return webElement;
    }

    @Override
    public void click() {
        hover();
        clickNoHover();
    }

    @Override
    public void doubleClick() {
        hover();
        driver.getActions().doubleClick(locateElement());
        driver.waitForPageLoad();
    }

    @Override
    public void clickNoHover() {
        waitUntilDisplayed();
        locateElement().click();
        driver.waitForPageLoad();
    }

    @Override
    public String getAttribute(final String name) {
        log.debug("BaseElement: getting attribute: " + name);
        try {
            return locateElement(Waiter.DISPLAY_WAIT_S, SECONDS).getAttribute(name); // may not be displayed
        } catch (ConditionTimeoutException | WebDriverException e) {
            return "";
        }
    }

    @Override
    public String getTagName() {
        return locateElement(Waiter.DISPLAY_WAIT_S, SECONDS).getTagName(); // may not be displayed
    }

    @Override
    public String getCssValue(final String name) {
        log.debug("BaseElement: getting css value: " + name);
        try {
            return locateElement(Waiter.DISPLAY_WAIT_S, SECONDS).getCssValue(name); // may not be displayed
        } catch (ConditionTimeoutException | WebDriverException e) {
            return "";
        }
    }

    @Override
    public String getText() {
        waitUntilDisplayed();
        return locateElement().getText();
    }

    @Override
    public boolean isInDOM() {
        try {
            locateElement(Waiter.STALE_ELEMENT_WAIT_MS, MILLISECONDS); // may not be displayed
        } catch (ConditionTimeoutException | WebDriverException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isDisplayed() {
        try {
            int waitTime = Waiter.STALE_ELEMENT_WAIT_MS;
            if (hoverElement != null) waitTime = Waiter.STALE_ELEMENT_WAIT_MS * 2;
            return locateElement(waitTime, MILLISECONDS).isDisplayed();
        } catch (ConditionTimeoutException | WebDriverException e) {
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        try {
            waitUntilEnabled(MILLISECONDS, Waiter.STALE_ELEMENT_WAIT_MS);
        } catch (ConditionTimeoutException | WebDriverException e) {
            return false;
        }
        return true;
    }

    public boolean isDisabled() {
        return "true".equals(getAttribute("disabled"));
    }

    @Override
    public void waitUntilDisplayed() {
        waitUntilDisplayed(SECONDS, Waiter.DISPLAY_WAIT_S);
    }

    @Override
    public void waitUntilDisplayed(TimeUnit timeUnit, final int waitTime) {
        Waiter.await().atMost(waitTime, timeUnit).until(() -> locateElement().isDisplayed());
    }

    @Override
    public void waitUntilNotDisplayed() {
        waitUntilNotDisplayed(SECONDS, Waiter.DISPLAY_WAIT_S);
    }

    @Override
    public void waitUntilNotDisplayed(TimeUnit timeUnit, final int waitTime) {
        Waiter.await().atMost(waitTime, timeUnit).until(() -> {
            try {
                // If the element is not in the DOM, Selenium will throw a NoSuchElementException
                return !locateElement().isDisplayed();
            } catch (WebDriverException e) {
                return true;
            }
        });
    }

    @Override
    public void waitUntilEnabled() {
        waitUntilEnabled(SECONDS, Waiter.DISPLAY_WAIT_S);
    }

    @Override
    public void waitUntilEnabled(TimeUnit timeUnit, final int timeout) {
        Waiter.await().atMost(timeout, timeUnit).until(() -> locateElement().isDisplayed() && locateElement().isEnabled());
    }

    @Override
    public void waitUntilNotEnabled() {
        waitUntilNotDisplayed(SECONDS, Waiter.DISPLAY_WAIT_S);
    }

    @Override
    public void waitUntilNotEnabled(TimeUnit timeUnit, final int timeout) {
        Waiter.await().atMost(timeout, timeUnit).until(() -> !locateElement().isDisplayed() || !locateElement().isEnabled());
    }

    @Override
    public void hover() {
        waitUntilDisplayed();
        final Actions hoverHandler = driver.getActions();
        final WebElement element = locateElement();

        try {
            Waiter.await().atMost(Waiter.INTERACT_WAIT_S, SECONDS).ignoreExceptions().until(() -> {
                hoverHandler.moveToElement(element).perform();
                return true;
            });
        } catch (ConditionTimeoutException e) {
            // ignore, best effort retry
        }
    }

    @Override
    public void sendKeys(final Keys... keys) {
        waitUntilDisplayed();
        locateElement().sendKeys(keys);
    }

    @Override
    public boolean isSelected() {
        waitUntilDisplayed();
        try {
            Waiter.await().atMost(Waiter.STALE_ELEMENT_WAIT_MS, MILLISECONDS).until(() -> locateElement().isSelected());
            return true;
        } catch (ConditionTimeoutException | WebDriverException e) {
            return false;
        }
    }

    @Override
    public void scrollIntoView() {
        locateElement(Waiter.DISPLAY_WAIT_S, SECONDS); // may not be displayed
        scrollElement(locateElement());
    }

    @Override
    public BaseElement createBaseElement(By childBy) {
        return new BaseElement(driver, childBy).withParent(this);
    }

    @Override
    public ElementList<BaseElement> createBaseElements(By childBy) {
        return new ElementList<BaseElement>(driver, childBy, BaseElement.class).withParent(this);
    }

    @Override
    public ButtonElement createButtonElement(By childBy) {
        return new ButtonElement(driver, childBy).withParent(this);
    }

    @Override
    public ElementList<ButtonElement> createButtonElements(By childBy) {
        return new ElementList<ButtonElement>(driver, childBy, ButtonElement.class).withParent(this);
    }

    @Override
    public CheckboxElement createCheckboxElement(By childBy) {
        return new CheckboxElement(driver, childBy).withParent(this);
    }

    @Override
    public ElementList<CheckboxElement> createCheckboxElements(By childBy) {
        return new ElementList<CheckboxElement>(driver, childBy, CheckboxElement.class).withParent(this);
    }

    @Override
    public RadioElement createRadioElement(By childBy) {
        return new RadioElement(driver, childBy).withParent(this);
    }

    @Override
    public ElementList<RadioElement> createRadioElements(By childBy) {
        return new ElementList<RadioElement>(driver, childBy, RadioElement.class).withParent(this);
    }

    @Override
    public ImageElement createImageElement(By childBy) {
        return new ImageElement(driver, childBy).withParent(this);
    }

    @Override
    public ElementList<ImageElement> createImageElements(By childBy) {
        return new ElementList<ImageElement>(driver, childBy, ImageElement.class).withParent(this);
    }

    @Override
    public LinkElement createLinkElement(By childBy) {
        return new LinkElement(driver, childBy).withParent(this);
    }

    @Override
    public ElementList<LinkElement> createLinkElements(By childBy) {
        return new ElementList<LinkElement>(driver, childBy, LinkElement.class).withParent(this);
    }

    @Override
    public TextElement createTextElement(By childBy) {
        return new TextElement(driver, childBy).withParent(this);
    }

    @Override
    public ElementList<TextElement> createTextElements(By childBy) {
        return new ElementList<TextElement>(driver, childBy, TextElement.class).withParent(this);
    }

    @Override
    public SelectListElement createSelectListElement(By by) {
        return new SelectListElement(driver, by).withParent(this);
    }

    @Override
    public ElementList<SelectListElement> createSelectListElements(By by) {
        return new ElementList<SelectListElement>(driver, by, SelectListElement.class).withParent(this);
    }

    @Override
    public FileUploadElement createFileUploadElement(By childBy) {
        return new FileUploadElement(driver, childBy).withParent(this);
    }

    @Override
    public TableElement createTableElement(By childBy) {
        return new TableElement(driver, childBy).withParent(this);
    }

    @Override
    public ElementList<TableElement> createTableElements(By childBy) {
        return new ElementList<TableElement>(driver, childBy, TableElement.class).withParent(this);
    }

    @Override
    public TableRowElement createTableRowElement(By childBy) {
        return new TableRowElement(driver, childBy).withParent(this);
    }

    @Override
    public ElementList<TableRowElement> createTableRowElements(By childBy) {
        return new ElementList<TableRowElement>(driver, childBy, TableRowElement.class).withParent(this);
    }

    @Override
    public boolean isFocused() {
        waitUntilDisplayed();
        try {
            Waiter.await().atMost(Waiter.INTERACT_WAIT_S, SECONDS)
                  .ignoreExceptions()
                  .until(() -> driver.switchTo().activeElement().equals(locateElement()));
            return true;
        } catch (ConditionTimeoutException | WebDriverException e) {
            return false;
        }
    }

    @Override
    public void focus() {
        waitUntilDisplayed();
        driver.getActions().moveToElement(locateElement()).perform();
    }

    public void flash() {
        waitUntilDisplayed();
        final WebElement element = locateElement();
        String elementColor = (String) driver.executeScript("arguments[0].style.backgroundColor", element);
        elementColor = (elementColor == null) ? "" : elementColor;
        for (int i = 0; i < 20; i++) {
            String bgColor = (i % 2 == 0) ? "red" : elementColor;
            driver.executeScript(String.format("arguments[0].style.backgroundColor = '%s'", bgColor), element);
        }
        driver.executeScript("arguments[0].style.backgroundColor = arguments[1]", element, elementColor);
    }

    public void switchFocusToIFrame() {
        driver.switchTo().frame(locateElement());
    }

    public void switchFocusFromIFrame() {
        try {
            driver.switchTo().parentFrame();
        } catch (Exception e) {
            driver.switchTo().defaultContent();
        }
    }

    public void fireEvent(String eventName) {
        Waiter.await().atMost(Waiter.DISPLAY_WAIT_S, SECONDS)
              .pollInterval(Waiter.STALE_ELEMENT_WAIT_MS, MILLISECONDS)
              .ignoreExceptions()
              .until(() -> dispatchJSEvent(locateElement(), eventName, true, true));
    }

    private boolean isWithinIFrame() {
        return iframeElement != null;
    }

    @Override
    public T withIframe(Element iframeElement) {
        this.iframeElement = iframeElement;
        return (T) referenceElement;
    }

    @Override
    public T withHover(Element hoverElement) {
        this.hoverElement = hoverElement;
        return (T) referenceElement;
    }

    @Override
    public T withAutoScrollIntoView() {
        this.scrollIntoView = true;
        return (T) referenceElement;
    }

    @Override
    public T withParent(Element parentElement) {
        this.parentElement = parentElement;
        return (T) referenceElement;
    }

    private void dispatchJSEvent(WebElement element, String event, boolean eventParam1, boolean eventParam2) {
        String cancelPreviousEventJS = "if (evObj && evObj.stopPropagation) { evObj.stopPropagation(); }";
        String dispatchEventJS = String.format("var evObj = document.createEvent('Event'); evObj.initEvent('%s', arguments[1], arguments[2]); arguments[0].dispatchEvent(evObj);",
                                               event);
        driver.executeScript(cancelPreviousEventJS + " " + dispatchEventJS,
                             element,
                             eventParam1,
                             eventParam2);
    }

    private void scrollElement(WebElement webElement) {
        driver.executeScript("arguments[0].scrollIntoView(true);", webElement);
    }


    /*
    Allows users to be able to do a complete node search within its parent without
    having to always remember to add .// before the xpath
    Example:
          parent.createBaseElement("//div[@id='testId']");
          parent.createBaseElement(".//div[@id='testId']");

    Both examples will now search within the parent.
    */
    public static By getByForParentElement(By by) {
        if (by instanceof ByXPath) {
            String xpath = by.toString().replace("By.xpath: ", "").replaceFirst("^.?//", ".//");
            return By.xpath(xpath);
        }
        return by;
    }

}
