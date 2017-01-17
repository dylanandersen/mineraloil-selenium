package com.lithium.mineraloil.selenium.elements;

import lombok.experimental.Delegate;
import org.openqa.selenium.By;

public class ButtonElement implements Element {

    @Delegate
    private final ElementImpl<ButtonElement> elementImpl;

    public ButtonElement(Driver driver, By by) {
        elementImpl = new ElementImpl(driver, this, by);
    }

}
