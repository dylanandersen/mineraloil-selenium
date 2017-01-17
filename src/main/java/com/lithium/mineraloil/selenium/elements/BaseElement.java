package com.lithium.mineraloil.selenium.elements;


import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;

@Slf4j
public class BaseElement implements Element<BaseElement> {

    @Delegate
    private final ElementImpl<BaseElement> elementImpl;

    public BaseElement(Driver driver, By by) {
        elementImpl = new ElementImpl(driver, this, by);
    }

}
