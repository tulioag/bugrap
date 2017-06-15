package org.vaadin.training.bugrap.reports;

import static java.util.Collections.unmodifiableList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.vaadin.training.bugrap.testing.BugrapIT;
import org.vaadin.training.bugrap.util.TestUtil;

public class ReportsViewIT extends BugrapIT {

    private ReportsPage reportsPage;

    @Test
    public void reports_defaultSelections() {
        reportsPage = TestUtil.accessSystem(driver).loginAsAdmin();
        assertEquals("Project 1", reportsPage.getSelectedProject());
        assertEquals(TableHeaders.WITH_VERSION, reportsPage.getHeaders());
        assertTrue(reportsPage.getReportCount() > 0);
    }

    @Test
    public void reports_shouldBePossibleToSelectProject3() {
        reportsPage = TestUtil.accessSystem(driver).loginAsAdmin();
        reportsPage.selectProject("Project 3");
        assertEquals("Project 3", reportsPage.getSelectedProject());
        assertTrue(reportsPage.getReportCount() > 0);
    }

    @Test
    public void reports_selectOnlyMeShouldReturnNoReportsForAdmin() {
        reportsPage = TestUtil.accessSystem(driver).loginAsAdmin();
        assertEquals("Project 1", reportsPage.getSelectedProject());
        reportsPage.filterAssigneeOnlyMe();
        assertEquals(0, reportsPage.getReportCount());
    }

    @Test
    public void reports_versionSelection() {
        reportsPage = TestUtil.accessSystem(driver).loginAsAdmin();
        assertEquals("All versions", reportsPage.getSelectedVersion());
        assertEquals(TableHeaders.WITH_VERSION, reportsPage.getHeaders());
        List<String> options = reportsPage.getVersionOptions();
        reportsPage.selectVersion(options.get(1));
        assertEquals(options.get(1), reportsPage.getSelectedVersion());
        assertEquals(TableHeaders.WITHOUT_VERSION, reportsPage.getHeaders());

    }

    @After
    public void teardown() {
        reportsPage = null;
    }

    static class TableHeaders {
        static final List<String> WITHOUT_VERSION = unmodifiableList(
                Arrays.asList("PRIORITY", "TYPE", "SUMMARY", "ASSIGNED TO",
                        "LAST MODIFIED", "REPORTED"));
        static final List<String> WITH_VERSION;
        static {
            List<String> tmp = new ArrayList<>(WITHOUT_VERSION.size() + 1);
            tmp.add("VERSION");
            tmp.addAll(WITHOUT_VERSION);
            WITH_VERSION = unmodifiableList(tmp);
        }
    }
}
