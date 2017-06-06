package org.vaadin.training.bugrap.reports;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vaadin.training.bugrap.testing.BugrapIT;
import org.vaadin.training.bugrap.util.TestUtil;


public class ReportsViewIT extends BugrapIT {

    private ReportsPage reportsPage;
    

    @Before
    public void setup() {
        reportsPage =  TestUtil.accessSystem(driver).loginAsAdmin();
    }

    @Test
    public void reports_defaultSelectedProjectShoudBeProject1()
    {
        assertEquals("Project 1",reportsPage.getSelectedProject());
    }

    @Test
    public void reports_shouldBePossibleToSelectProject3()
    {
        reportsPage.selectProject("Project 3");
        assertEquals("Project 3",reportsPage.getSelectedProject());
    }

    
    @After
    public void teardown() {
        reportsPage = null;
    }
    
}
