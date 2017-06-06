/**
 * 
 */
package org.vaadin.training.bugrap.testing;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.testbench.TestBench;
import com.vaadin.testbench.TestBenchTestCase;

import io.github.bonigarcia.wdm.ChromeDriverManager;

/**
 * @author Tulio Garcia
 *
 */
public abstract class BugrapIT extends TestBenchTestCase {
    
    @BeforeClass
    public static final void setupClassBugrapIT() {
        ChromeDriverManager.getInstance().setup();
    }

    @Before
    public final void setupBugrapTest() {
        setDriver(TestBench.createDriver(new ChromeDriver(DesiredCapabilities.chrome())));
    }
    
    @After
    public final void teardownBugrapTest() {
        driver.close();
        driver.quit();
    }
}
