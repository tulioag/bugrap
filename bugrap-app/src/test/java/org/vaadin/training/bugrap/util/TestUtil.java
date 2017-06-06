/**
 * 
 */
package org.vaadin.training.bugrap.util;

import org.openqa.selenium.WebDriver;
import org.vaadin.training.bugrap.login.LoginPage;

/**
 * @author Tulio Garcia
 *
 */
public class TestUtil {
    
    public static LoginPage accessSystem(WebDriver driver)
    {
        driver.get("http://localhost:8080/bugrap");
        return new LoginPage(driver);
    }
}
