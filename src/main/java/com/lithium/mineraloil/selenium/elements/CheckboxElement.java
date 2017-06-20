package com.lithium.mineraloil.selenium.elements;

import lombok.experimental.Delegate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class CheckboxElement implements Element {
    @Delegate
    private final ElementImpl<CheckboxElement> elementImpl;
    private final Driver driver;

    CheckboxElement(Driver driver, By by) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, by);
    }

    private CheckboxElement(Driver driver, WebElement webElement) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, webElement);
    }

    public List<CheckboxElement> toList() {
        return locateElements().stream()
                               .map(element -> new CheckboxElement(driver, element).withParent(getParentElement())
                                                                                   .withIframe(getIframeElement())
                                                                                   .withHover(getHoverElement())
                                                                                   .withAutoScrollIntoView(isAutoScrollIntoView()))
                               .collect(Collectors.toList());
    }

    public void check() {
        if (!isChecked()) elementImpl.click();
    }

    public boolean isChecked() {
        return isSelected();
    }

    public void uncheck() {
        if (isChecked()) elementImpl.click();
    }

    public void set(boolean value) {
        if (value != isChecked()) click();
    }

}
