package org.vaadin.training.bugrap.reports;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.beust.jcommander.internal.Nullable;
import com.google.common.base.Predicate;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ComboBoxElement;
import com.vaadin.testbench.elements.GridElement;
import com.vaadin.testbench.elements.GridElement.GridCellElement;
import com.vaadin.testbench.elements.MenuBarElement;

public class ReportsPage extends TestBenchTestCase {
    private ComboBoxElement comboBox;
    private GridElement grid;

    public ReportsPage(WebDriver driver) {
        setDriver(driver);

        comboBox = $(ComboBoxElement.class).id("projects");
        grid = $(GridElement.class).first();
    }

    public void selectProject(String text) {
        comboBox.selectByText(text);
    }

    public String getSelectedProject() {
        return comboBox.getValue();
    }

    public long getReportCount() {
        return grid.getRowCount();
    }

    public List<String> getHeaders() {
        return grid.getHeaderCells(0).stream().map(GridCellElement::getText)
                .collect(Collectors.toList());
    }

    public void filterAssigneeOnlyMe() {
        try {
            MenuBarElement menuBar = $(MenuBarElement.class).first();
            menuBar.clickItem("Only me");
        } catch (StaleElementReferenceException e) {
            // XXX Ignoring. For some reason the exception is thrown, but the
            // click works as expected.
        }
    }
}
