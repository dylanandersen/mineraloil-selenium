package com.lithium.mineraloil.selenium.elements;

import lombok.experimental.Delegate;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ButtonElement implements Element {
    @Delegate
    private final ElementImpl<ButtonElement> elementImpl;

    ButtonElement(Driver driver, By by) {
        elementImpl = new ElementImpl(driver, this, by);
    }

    private ButtonElement(Driver driver, By by, int index) {
        elementImpl = new ElementImpl(driver, this, by, index);
    }

    public List<ButtonElement> toList() {
        List<ButtonElement> elements = new ArrayList<>();
        IntStream.range(0, locateElements().size()).forEach(index -> {
            elements.add(new ButtonElement(elementImpl.driver, elementImpl.by, index).withParent(getParentElement())
                                                                                     .withIframe(getIframeElement())
                                                                                     .withHover(getHoverElement())
                                                                                     .withAutoScrollIntoView(isAutoScrollIntoView()));
        });
        return elements;
    }

}
