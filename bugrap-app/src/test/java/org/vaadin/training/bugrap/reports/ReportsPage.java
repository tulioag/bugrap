package org.vaadin.training.bugrap.reports;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;

import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ComboBoxElement;
import com.vaadin.testbench.elements.GridElement;
import com.vaadin.testbench.elements.GridElement.GridCellElement;
import com.vaadin.testbench.elements.MenuBarElement;
import com.vaadin.testbench.elements.NativeSelectElement;

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
        List<String>  headers = grid.getHeaderCells(0).stream().map(GridCellElement::getText)
                .collect(Collectors.toList());
        //Removing selection header.
        List<String> headersExceptSelection = headers.subList(1, headers.size());
        return headersExceptSelection;
    }

    public String getSelectedVersion() {
        return getSelectedVersionElement().getValue();
    }

    public List<String> getVersionOptions() {
        return getSelectedVersionElement().getOptions().stream()
                .map(o -> o.getText()).collect(Collectors.toList());
    }

    public void selectVersion(String text) {
        getSelectedVersionElement().selectByText(text);
    }

    private NativeSelectElement getSelectedVersionElement() {
        return $(NativeSelectElement.class).first();
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
