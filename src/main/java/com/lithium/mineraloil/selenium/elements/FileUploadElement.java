package com.lithium.mineraloil.selenium.elements;

import lombok.experimental.Delegate;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

public class FileUploadElement implements Element {
    @Delegate
    private final ElementImpl<FileUploadElement> elementImpl;
    private final Driver driver;

    FileUploadElement(Driver driver, By by) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, by);
    }

    private FileUploadElement(Driver driver, WebElement webElement) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, webElement);
    }

    public List<FileUploadElement> toList() {
        return locateElements().stream()
                               .map(element -> new FileUploadElement(driver, element)
                                       .withParent(getParentElement())
                                       .withIframe(getIframeElement())
                                       .withHover(getHoverElement())
                                       .withAutoScrollIntoView(isAutoScrollIntoView()))
                               .collect(Collectors.toList());
    }

    public void type(final String text) {
        if (text == null) return;
        int retries = 0;
        long expireTime = Instant.now().toEpochMilli() + SECONDS.toMillis(Waiter.DISPLAY_WAIT_S);
        while (Instant.now().toEpochMilli() < expireTime && retries < 2) {
            try {
                elementImpl.locateElement().sendKeys(text);
                return;
            } catch (WebDriverException e) {
                retries++;
            }
        }
        throw new NoSuchElementException("Unable to locate element: " + getBy());
    }
}
