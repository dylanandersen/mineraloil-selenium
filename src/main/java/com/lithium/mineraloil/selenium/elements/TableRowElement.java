package com.lithium.mineraloil.selenium.elements;

import lombok.experimental.Delegate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

public class TableRowElement implements Element {
    private List<BaseElement> columns;

    @Delegate
    private final ElementImpl<TableRowElement> elementImpl;
    private final Driver driver;

    TableRowElement(Driver driver, By by) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, by);
    }

    private TableRowElement(Driver driver, WebElement webElement) {
        this.driver = driver;
        elementImpl = new ElementImpl(driver, this, webElement);
    }

    public List<TableRowElement> toList() {
        return locateElements().stream()
                               .map(element -> new TableRowElement(driver, element).withParent(getParentElement())
                                                                                   .withIframe(getIframeElement())
                                                                                   .withHover(getHoverElement())
                                                                                   .withAutoScrollIntoView(isAutoScrollIntoView()))
                               .collect(Collectors.toList());
    }

    public List<BaseElement> getColumns() {
        if (columns == null) {
            columns = createBaseElement(By.tagName("td")).toList();
        }
        return columns;
    }

    public BaseElement getColumn(int index) {
        return getColumns().get(index);
    }

}
