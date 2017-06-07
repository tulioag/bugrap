package org.vaadin.training.bugrap.reports;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

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
        assertEquals(Arrays.asList("TYPE", "SUMMARY", "ASSIGNED TO"),
                reportsPage.getHeaders());
        assertTrue(reportsPage.getReportCount() > 0);
    }

    @Test
    public void reports_shouldBePossibleToSelectProject3() {
        reportsPage = TestUtil.accessSystem(driver).loginAsAdmin();
        reportsPage.selectProject("Project 3");
        assertEquals("Project 3", reportsPage.getSelectedProject());
        assertEquals(Arrays.asList("TYPE", "SUMMARY", "ASSIGNED TO"),
                reportsPage.getHeaders());
        assertTrue(reportsPage.getReportCount() > 0);
    }

    @Test
    public void reports_selectOnlyMeShouldReturnNoReportsForAdmin() {
        reportsPage = TestUtil.accessSystem(driver).loginAsAdmin();
        assertEquals("Project 1", reportsPage.getSelectedProject());
        reportsPage.filterAssigneeOnlyMe();
        assertEquals(0, reportsPage.getReportCount());
    }

    @After
    public void teardown() {
        reportsPage = null;
    }

}
