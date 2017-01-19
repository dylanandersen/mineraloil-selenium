package com.lithium.mineraloil.selenium.elements;


import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

@Slf4j
public class BaseElement implements Element<BaseElement> {

    @Delegate private final ElementImpl<BaseElement> elementImpl;
    protected final Driver driver;

    public BaseElement(Driver driver, By by) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, by);
    }

    public BaseElement(Driver driver, By by, int index) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, by, index);
    }

    public BaseElement(Driver driver, Element parentElement, By by) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, parentElement, by);
    }

    public BaseElement(Driver driver, Element parentElement, By by, int index) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, parentElement, by, index);
    }


}
