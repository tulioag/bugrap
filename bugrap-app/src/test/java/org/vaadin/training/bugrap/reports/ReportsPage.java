package org.vaadin.training.bugrap.reports;

import org.openqa.selenium.WebDriver;

import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ComboBoxElement;

public class ReportsPage extends TestBenchTestCase {
    private ComboBoxElement comboBox;

    public ReportsPage(WebDriver driver) {
        setDriver(driver);

        comboBox = $(ComboBoxElement.class).id("projects");
    }

    public void selectProject(String text) {
        comboBox.selectByText(text);
    }

    public String getSelectedProject() {
        return comboBox.getValue();
    }
}
