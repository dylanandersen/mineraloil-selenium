package com.lithium.mineraloil.selenium.elements;

import lombok.experimental.Delegate;
import org.openqa.selenium.By;

public class CheckboxElement implements Element {

    @Delegate
    private final ElementImpl<CheckboxElement> elementImpl;

    CheckboxElement(Driver driver, By by) {
        elementImpl = new ElementImpl(driver, this, by);
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
