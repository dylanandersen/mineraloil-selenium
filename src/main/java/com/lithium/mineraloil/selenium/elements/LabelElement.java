package com.lithium.mineraloil.selenium.elements;

import lombok.experimental.Delegate;
import org.openqa.selenium.By;

public class LabelElement implements Element {
    @Delegate
    private final ElementImpl<LabelElement> elementImpl;

    public LabelElement(Driver driver, Element referenceElement) {
        elementImpl = new ElementImpl(driver, this, By.xpath(String.format("//label[@for='%s']", referenceElement.getAttribute("name"))));
    }

}
