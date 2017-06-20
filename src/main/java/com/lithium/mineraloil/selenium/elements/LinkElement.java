package com.lithium.mineraloil.selenium.elements;

import lombok.experimental.Delegate;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class LinkElement implements Element {
    @Delegate
    private final ElementImpl<LinkElement> elementImpl;

    LinkElement(Driver driver, By by) {
        elementImpl = new ElementImpl(driver, this, by);
    }

    private LinkElement(Driver driver, By by, int index) {
        elementImpl = new ElementImpl(driver, this, by, index);
    }

    public List<LinkElement> toList() {
        List<LinkElement> elements = new ArrayList<>();
        IntStream.range(0, locateElements().size()).forEach(index -> {
            elements.add(new LinkElement(elementImpl.driver, elementImpl.by, index).withParent(getParentElement())
                                                                                   .withIframe(getIframeElement())
                                                                                   .withHover(getHoverElement())
                                                                                   .withAutoScrollIntoView(isAutoScrollIntoView()));
        });
        return elements;
    }

}
