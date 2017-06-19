package com.lithium.mineraloil.selenium.elements;

import lombok.experimental.Delegate;
import org.openqa.selenium.By;

public class TableRowElement implements Element {
    private ElementList<BaseElement> columns;

    @Delegate
    private final ElementImpl<TableRowElement> elementImpl;

    TableRowElement(Driver driver, By by) {
        elementImpl = new ElementImpl(driver, this, by);
    }

    public ElementList<BaseElement> getColumns() {
        if (columns == null) {
            columns = createBaseElements(By.tagName("td"));
        }
        return columns;
    }

    public BaseElement getColumn(int index) {
        return getColumns().get(index);
    }

}
