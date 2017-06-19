package com.lithium.mineraloil.selenium.elements;

import lombok.experimental.Delegate;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;

public class RadioElement implements Element {

    @Delegate(excludes = {RadioSelection.class})
    private final ElementImpl<RadioElement> elementImpl;

    RadioElement(Driver driver, By by) {
        elementImpl = new ElementImpl(driver, this, by);
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
