package com.lithium.mineraloil.selenium.elements;

import lombok.experimental.Delegate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class ButtonElement implements Element {
    @Delegate
    private final ElementImpl<ButtonElement> elementImpl;
    private final Driver driver;

    ButtonElement(Driver driver, By by) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, by);
    }

    private ButtonElement(Driver driver, WebElement webElement) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, webElement);
    }

    public List<ButtonElement> toList() {
        return locateElements().stream()
                               .map(element -> new ButtonElement(driver, element)
                                       .withParent(getParentElement())
                                       .withIframe(getIframeElement())
                                       .withHover(getHoverElement())
                                       .withAutoScrollIntoView(isAutoScrollIntoView()))
                               .collect(Collectors.toList());
    }
}
