package com.lithium.mineraloil.selenium.elements;

import lombok.experimental.Delegate;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ImageElement implements Element {

    @Delegate
    private final ElementImpl<ImageElement> elementImpl;
    private final Driver driver;

    ImageElement(Driver driver, By by) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, by);
    }

    private ImageElement(Driver driver, WebElement webElement) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, webElement);
    }

    public List<ImageElement> toList() {
        return locateElements().stream()
                               .map(element -> new ImageElement(driver, element).withParent(getParentElement())
                                                                                .withParent(getParentElement())
                                                                                .withIframe(getIframeElement())
                                                                                .withHover(getHoverElement())
                                                                                .withAutoScrollIntoView(isAutoScrollIntoView()))
                               .collect(Collectors.toList());
    }

    public String getImageSource() {
        Waiter.await().atMost(Waiter.INTERACT_WAIT_S, SECONDS).until(() -> StringUtils.isNotBlank(getAttribute("src")) || StringUtils.isNotBlank(getCssValue("background-image")));
        if (StringUtils.isNotBlank(getAttribute("src"))) {
            return getAttribute("src");
        } else {
            return getCssValue("background-image");
        }
    }


}
