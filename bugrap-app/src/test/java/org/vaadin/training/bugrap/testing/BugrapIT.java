/**
 * 
 */
package org.vaadin.training.bugrap.testing;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

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
        ChromeOptions options = new ChromeOptions();

        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);

        options.setExperimentalOption("prefs", prefs);  
        setDriver(TestBench.createDriver(new ChromeDriver(options)));
    }
    
    @After
    public final void teardownBugrapTest() {
        driver.close();
        driver.quit();
    }
}
