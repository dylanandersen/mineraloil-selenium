package com.lithium.mineraloil.selenium.elements;

import com.lithium.mineraloil.selenium.helpers.BaseTest;
import org.junit.Test;
import org.openqa.selenium.By;

import static org.assertj.core.api.Assertions.assertThat;

public class BaseElementTest extends BaseTest {

    @Test
    public void constructorWithIndex() {
        BaseElement div = new BaseElement(By.xpath("//div[@class='duplicate_class']"), 0);
        BaseElement div2 = new BaseElement(By.xpath("//div[@class='duplicate_class']"), 1);
        assertThat(div.getText()).isEqualTo("Element With Shared Class");
        assertThat(div2.getText()).isEqualTo("Nested Value With Shared Class");
    }

    @Test
    public void baseElementLocateElement() {
        BaseElement div = new BaseElement(By.xpath("//div[@id='displayed_element']"));
        assertThat(div.isDisplayed()).isTrue();
        assertThat(div.getTagName()).isEqualTo("div");
        assertThat(div.getCssValue("display")).isEqualTo("inline");
    }

    @Test
    public void nestedElementLocate() {
        BaseElement grandparent = new BaseElement(By.xpath("//div[@id='nested_div']"));
        BaseElement parent = grandparent.createBaseElement(By.xpath("div[@id='last_level']"));
        BaseElement child = parent.createBaseElement(By.xpath("div[@class='duplicate_class']"));
        assertThat(child.getText()).isEqualTo("Nested Value With Shared Class");
    }

    @Test
    public void doubleSlashNestedElementLocate() {
        BaseElement parent = new BaseElement(By.xpath("//div[@id='nested_div']"));
        BaseElement child = parent.createBaseElement(By.xpath("//div[@class='duplicate_class']"));
        assertThat(child.getText()).isEqualTo("Nested Value With Shared Class");
    }

    @Test
    public void dotDoubleSlashNestedElementLocate() {
        BaseElement parent = new BaseElement(By.xpath("//div[@id='nested_div']"));
        BaseElement child = parent.createBaseElement(By.xpath(".//div[@class='duplicate_class']"));
        assertThat(child.getText()).isEqualTo("Nested Value With Shared Class");
    }

    @Test
    public void grabElementFromIframe() {
        BaseElement div = new BaseElement(By.xpath("//div[@id='iframe_div']"));
        BaseElement iframe = new BaseElement(By.xpath("//iframe"));
        assertThat(div.isDisplayed()).isFalse();
        assertThat(div.withIframe(iframe).getText()).isEqualTo("Iframe Things!");
    }

    @Test
    public void check() {
        CheckboxElement checkboxElement = new CheckboxElement(By.xpath("//input[@type='checkbox']"));
        checkboxElement.check();
        assertThat(checkboxElement.isChecked()).isTrue();
        checkboxElement.uncheck();
        assertThat(checkboxElement.isChecked()).isFalse();
    }

    @Test
    public void nestedElementCollapseXpath() {
        BaseElement levelOne = new BaseElement(By.xpath("//div[@id='level_1']"));
        BaseElement levelTwo = levelOne.createBaseElement(By.xpath("//div[@id='level_2']"));
        BaseElement levelThree = levelTwo.createBaseElement(By.xpath("//div[@id='level_3']"));
        assertThat(levelOne.getCollapsedXpathBy()).isNull();
        assertThat(levelTwo.getCollapsedXpathBy().toString()).contains("//div[@id='level_1']",
                                                                 "//div[@id='level_2']");
        assertThat(levelTwo.getCollapsedXpathBy().toString()).doesNotContain("//div[@id='level_3']");
        assertThat(levelThree.getCollapsedXpathBy().toString()).contains("//div[@id='level_1']",
                                                                   "//div[@id='level_2']",
                                                                   "//div[@id='level_3']");
        assertThat(levelThree.getText()).contains("Level 3", "Welcome to the last level");
        assertThat(levelTwo.getText()).contains("Level 2", "Level 3", "Welcome to the last level");
        assertThat(levelOne.getText()).contains("Level 1", "Level 2", "Level 3", "Welcome to the last level");
    }


}
