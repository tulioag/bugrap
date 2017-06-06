package org.vaadin.training.bugrap.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.openqa.selenium.WebDriver;
import org.vaadin.training.bugrap.reports.ReportsPage;

import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.NotificationElement;
import com.vaadin.testbench.elements.PasswordFieldElement;
import com.vaadin.testbench.elements.TextFieldElement;
import com.vaadin.ui.Notification;

public class LoginPage  extends TestBenchTestCase{

    public LoginPage(WebDriver driver) {
        setDriver(driver);
    }

    public ReportsPage login(String username, String password) {
        NotificationElement notification = attemptLogin(username, password);
        Set<String> classNames = notification.getClassNames();
        assertTrue(classNames.contains("success"));
        return new ReportsPage(driver);
    }

    public ReportsPage loginAsAdmin()
    {
        return login("admin","admin");
    }
    
    public LoginPage attemptLoginExpectingFailure(String username,
            String password) {
        NotificationElement notification = attemptLogin(username, password);
        assertEquals(Notification.Type.ERROR_MESSAGE.getStyle(),
                notification.getType());
        return this;
    }

    private NotificationElement attemptLogin(String username, String password) {
        $(TextFieldElement.class).caption("Username").first()
                .setValue(username);
        $(PasswordFieldElement.class).caption("Password").first()
                .setValue(password);
        $(ButtonElement.class).caption("Log in").first().click();

        return $(NotificationElement.class).first();
    }
}
