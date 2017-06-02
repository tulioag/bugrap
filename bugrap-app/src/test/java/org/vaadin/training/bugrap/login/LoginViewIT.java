package org.vaadin.training.bugrap.login;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.vaadin.testbench.TestBench;
import com.vaadin.testbench.TestBenchTestCase;
import com.vaadin.testbench.elements.ButtonElement;
import com.vaadin.testbench.elements.NotificationElement;
import com.vaadin.testbench.elements.PasswordFieldElement;
import com.vaadin.testbench.elements.TextFieldElement;
import com.vaadin.ui.Notification;

import io.github.bonigarcia.wdm.ChromeDriverManager;

public class LoginViewIT extends TestBenchTestCase {

    @BeforeClass
    public static void setupClass() {
        ChromeDriverManager.getInstance().setup();
    }

    @Before
    public void setup() {
        setDriver(TestBench.createDriver(new ChromeDriver(DesiredCapabilities.chrome())));
    }

    @After
    public void teardown() {
        driver.close();
        driver.quit();
    }

    @Test
    public void login_adminShouldLogin() {
        final String username = "admin";
        final String password = "admin";
        NotificationElement notification = attemptLogin(username, password);
        Set<String> classNames = notification.getClassNames();
        assertTrue(classNames.contains("success"));
    }

    @Test
    public void login_adminWithWrongPasswordShouldNotLogin() {
        final String username = "admin";
        final String password = "wrongPassword";
        NotificationElement notification = attemptLogin(username, password);
        assertEquals(Notification.Type.ERROR_MESSAGE.getStyle(),
                notification.getType());

    }

    private NotificationElement attemptLogin(String username, String password) {
        driver.get("http://localhost:8080/bugrap");
        $(TextFieldElement.class).caption("Username").first()
                .setValue(username);
        $(PasswordFieldElement.class).caption("Password").first()
                .setValue(password);
        $(ButtonElement.class).caption("Log in").first().click();

        return $(NotificationElement.class).first();
    }
}
