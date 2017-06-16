package com.lithium.mineraloil.selenium.elements;

import com.google.common.base.Throwables;
import com.jayway.awaitility.core.ConditionTimeoutException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ByXPath;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.lithium.mineraloil.selenium.elements.Waiter.DISPLAY_WAIT_S;
import static com.lithium.mineraloil.selenium.elements.Waiter.await;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

@Slf4j
class ElementImpl<T extends Element> implements Element<T> {
    private int index = -1;
    private Element referenceElement;
    private boolean scrollIntoView = false;
    private Driver driver;

    @Setter private Element iframeElement;
    @Setter private Element hoverElement;
    @Getter private Element parentElement;
    @Getter private final By by;
    @Getter private WebElement webElement;
    private int LOCATE_RETRIES = 2;

    private static boolean autoHoverOnInput;

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
    public WebElement locateElement() {
        if (isWithinIFrame()) {
            ((BaseElement) iframeElement).switchFocusToIFrame();
        } else {
            switchFocusFromIFrame();
        }

        if (hoverElement != null && hoverElement.isDisplayed()) hoverElement.hover();

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

        return webElement;
    }

    public <T> T callSelenium(Callable<T> callable) {
        // default exception that gets thrown on a timeout
        WebDriverException exception = new WebDriverException("Unable to locate element: " + getBy());

        int retries = 0;
        long expireTime = Instant.now().toEpochMilli() + SECONDS.toMillis(Waiter.INTERACT_WAIT_S);
        while (Instant.now().toEpochMilli() < expireTime && retries < LOCATE_RETRIES) {
            try {
                return callable.call();
            } catch (WebDriverException e) {
                exception = e; //update the exception message to reflect what selenium is reporting
                retries++;
            } catch (Exception e) {
                Throwables.propagate(e);
            }
        }
        throw new NoSuchElementException(exception.getMessage());
    }

    public static boolean getAutoHoverOnInput() {
        return autoHoverOnInput;
    }

    public static void setAutoHoverOnInput(Boolean value) {
        autoHoverOnInput = value;
    }

    @Override
    public void click() {
        waitUntilDisplayed();

        autoHover();

        callSelenium(() -> {
            locateElement().click();
            driver.waitForPageLoad();
            return null;
        });
    }

    @Override
    public void clickWithOffset(int x, int y) {
        waitUntilDisplayed();

        autoHover();

        callSelenium(() -> {
            driver.getActions().moveToElement(locateElement(), x, y).click().perform();
            driver.waitForPageLoad();
            return null;
        });
    }

    @Override
    public void doubleClick() {
        waitUntilDisplayed();

        autoHover();

        callSelenium(() -> {
            driver.getActions().doubleClick(locateElement());
            driver.waitForPageLoad();
            return null;
        });
    }

    @Override
    public String getAttribute(final String name) {
        return callSelenium(() -> {
            return locateElement().getAttribute(name);
        });
    }

    @Override
    public String getTagName() {
        return callSelenium(() -> {
            return locateElement().getTagName();
        });
    }

    @Override
    public String getCssValue(final String name) {
        return callSelenium(() -> {
            return locateElement().getCssValue(name);
        });
    }

    @Override
    public String getText() {
        return callSelenium(() -> {
            return locateElement().getAttribute("textContent").replaceAll("\u00A0", " ").trim();
        });
    }

    @Override
    public String getInnerText() {
        return callSelenium(() -> {
            return locateElement().getAttribute("innerText").replaceAll("\u00A0", " ").trim();
        });
    }

    @Override
    public boolean isInDOM() {
        int retries = 0;
        long expireTime = Instant.now().toEpochMilli() + MILLISECONDS.toMillis(Waiter.STALE_ELEMENT_WAIT_MS);
        while (Instant.now().toEpochMilli() < expireTime && retries < LOCATE_RETRIES) {
            try {
                return locateElement() != null;
            } catch (WebDriverException e) {
                retries++;
            }
        }
        return false;
    }

    @Override
    public boolean isDisplayed() {
        int retries = 0;
        int waitTime = Waiter.STALE_ELEMENT_WAIT_MS;
        if (hoverElement != null) waitTime = Waiter.STALE_ELEMENT_WAIT_MS * 2;
        long expireTime = Instant.now().toEpochMilli() + MILLISECONDS.toMillis(waitTime);
        while (Instant.now().toEpochMilli() < expireTime && retries < LOCATE_RETRIES) {
            try {
                return locateElement().isDisplayed();
            } catch (WebDriverException e) {
                retries++;
            }
        }
        return false;
    }

    @Override
    public boolean isEnabled() {
        int retries = 0;
        long expireTime = Instant.now().toEpochMilli() + MILLISECONDS.toMillis(Waiter.STALE_ELEMENT_WAIT_MS);
        while (Instant.now().toEpochMilli() < expireTime && retries < LOCATE_RETRIES) {
            try {
                return locateElement().isEnabled();
            } catch (WebDriverException e) {
                retries++;
            }
        }
        return false;
    }

    public boolean isDisabled() {
        return "true".equals(getAttribute("disabled"));
    }

    @Override
    public void hover() {
        waitUntilDisplayed();
        try {
            await().until(() -> {
                try {
                    final Actions hoverHandler = driver.getActions();
                    hoverHandler.moveToElement(locateElement()).perform();
                    return true;
                } catch (WebDriverException e) {
                    return false;
                }
            });
        } catch (ConditionTimeoutException e) {
            throw new NoSuchElementException("Unable to hover over element: " + getBy().toString());
        }
    }

    @Override
    public void sendKeys(final Keys... keys) {
        waitUntilDisplayed();

        autoHover();

        callSelenium(() -> {
            locateElement().sendKeys(keys);
            return null;
        });
    }

    @Override
    public void autoHover() {
        if (autoHoverOnInput && hoverElement == null) hover();
    }

    @Override
    public boolean isSelected() {
        waitUntilDisplayed();

        return callSelenium(() -> {
            return locateElement().isSelected();
        });
    }

    @Override
    public boolean isFocused() {
        waitUntilDisplayed();

        return callSelenium(() -> {
            return driver.switchTo().activeElement().equals(locateElement());
        });
    }

    @Override
    public void focus() {
        waitUntilDisplayed();

        callSelenium(() -> {
            driver.getActions().moveToElement(locateElement()).perform();
            return null;
        });
    }

    @Override
    public void scrollIntoView() {
        callSelenium(() -> {
            scrollElement(locateElement());
            return null;
        });
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
    public TextInputElement createTextInputElement(By childBy) {
        return new TextInputElement(driver, childBy).withParent(this);
    }

    @Override
    public ElementList<TextInputElement> createTextInputElements(By childBy) {
        return new ElementList<TextInputElement>(driver, childBy, TextInputElement.class).withParent(this);
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
        await().atMost(DISPLAY_WAIT_S, SECONDS)
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

    @Override
    public void waitUntilDisplayed() {
        waitUntilDisplayed(SECONDS, DISPLAY_WAIT_S);
    }

    @Override
    public void waitUntilDisplayed(TimeUnit timeUnit, final int waitTime) {
        await().atMost(waitTime, timeUnit).until(() -> isDisplayed());
    }

    @Override
    public void waitUntilNotDisplayed() {
        waitUntilNotDisplayed(SECONDS, DISPLAY_WAIT_S);
    }

    @Override
    public void waitUntilNotDisplayed(TimeUnit timeUnit, final int waitTime) {
        await().atMost(waitTime, timeUnit).until(() -> !isDisplayed());
    }

    @Override
    public void waitUntilEnabled() {
        waitUntilEnabled(SECONDS, DISPLAY_WAIT_S);
    }

    @Override
    public void waitUntilEnabled(TimeUnit timeUnit, final int timeout) {
        await().atMost(timeout, timeUnit).until(() -> isDisplayed() && isEnabled());
    }

    @Override
    public void waitUntilNotEnabled() {
        waitUntilNotDisplayed(SECONDS, DISPLAY_WAIT_S);
    }

    @Override
    public void waitUntilNotEnabled(TimeUnit timeUnit, final int timeout) {
        await().atMost(timeout, timeUnit).until(() -> !isDisplayed() || !isEnabled());
    }

}
