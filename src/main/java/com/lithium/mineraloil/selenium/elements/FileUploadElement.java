package com.lithium.mineraloil.selenium.elements;

import lombok.experimental.Delegate;
import org.openqa.selenium.By;

import static java.util.concurrent.TimeUnit.SECONDS;

public class FileUploadElement implements Element {
    @Delegate
    private final ElementImpl<FileUploadElement> elementImpl;

    public FileUploadElement(Driver driver, By by) {
        elementImpl = new ElementImpl(driver, this, by);
    }

    public void type(final String text) {
        if (text == null) return;
        elementImpl.locateElement(Waiter.DISPLAY_WAIT_S, SECONDS).sendKeys(text);
    }
}
