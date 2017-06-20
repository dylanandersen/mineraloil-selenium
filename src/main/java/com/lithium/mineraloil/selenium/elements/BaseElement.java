package com.lithium.mineraloil.selenium.elements;


import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class BaseElement implements Element<BaseElement> {

    @Delegate private final ElementImpl<BaseElement> elementImpl;
    protected final Driver driver;

    BaseElement(Driver driver, By by) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, by);
    }

    private BaseElement(Driver driver, WebElement webElement) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, webElement);
    }

    public List<BaseElement> toList() {
        return locateElements().stream()
                               .map(element -> new BaseElement(driver, element).withParent(getParentElement())
                                                                               .withIframe(getIframeElement())
                                                                               .withHover(getHoverElement())
                                                                               .withAutoScrollIntoView(isAutoScrollIntoView()))
                               .collect(Collectors.toList());
    }
}
