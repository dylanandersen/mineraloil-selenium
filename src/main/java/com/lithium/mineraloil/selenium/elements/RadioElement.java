package com.lithium.mineraloil.selenium.elements;

import lombok.experimental.Delegate;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class RadioElement implements Element {
    @Delegate(excludes = {RadioSelection.class})
    private final ElementImpl<RadioElement> elementImpl;
    private final Driver driver;

    RadioElement(Driver driver, By by) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, by);
    }

    private RadioElement(Driver driver, WebElement webElement) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, webElement);
    }

    public List<RadioElement> toList() {
        return locateElements().stream()
                               .map(element -> new RadioElement(driver, element)
                                       .withParent(getParentElement())
                                       .withIframe(getIframeElement())
                                       .withHover(getHoverElement())
                                       .withAutoScrollIntoView(isAutoScrollIntoView()))
                               .collect(Collectors.toList());
    }

    private interface RadioSelection {
        boolean isSelected();
    }

    public void select() {
        if (isDisabled()) throw new ElementNotVisibleException("RadioElement is disabled and not selectable.");
        click();
    }

    @Override
    public boolean isSelected() {
        return "true".equals(getAttribute("checked"));
    }

}
