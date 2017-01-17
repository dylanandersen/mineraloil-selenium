package com.lithium.mineraloil.selenium.elements;

import lombok.experimental.Delegate;
import org.openqa.selenium.By;

public class LinkElement implements Element {
    @Delegate
    private final ElementImpl<LinkElement> elementImpl;

    public LinkElement(Driver driver, By by) {
        elementImpl = new ElementImpl(driver, this, by);
    }


}
