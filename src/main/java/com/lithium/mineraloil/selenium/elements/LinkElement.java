package com.lithium.mineraloil.selenium.elements;

import lombok.experimental.Delegate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class LinkElement implements Element {
    @Delegate
    private final ElementImpl<LinkElement> elementImpl;
    private final Driver driver;

    LinkElement(Driver driver, By by) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, by);
    }

    private LinkElement(Driver driver, WebElement webElement) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, webElement);
    }

    public List<LinkElement> toList() {
        return locateElements().stream()
                               .map(element -> new LinkElement(driver, element).withParent(getParentElement())
                                                                               .withIframe(getIframeElement())
                                                                               .withHover(getHoverElement())
                                                                               .withAutoScrollIntoView(isAutoScrollIntoView()))
                               .collect(Collectors.toList());
    }
}
