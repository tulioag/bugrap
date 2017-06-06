package org.vaadin.training.bugrap.login;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.vaadin.training.bugrap.testing.BugrapIT;
import org.vaadin.training.bugrap.util.TestUtil;

public class LoginViewIT extends BugrapIT {

    private LoginPage loginPage;
    

    @Before
    public void setup() {
        loginPage =  TestUtil.accessSystem(driver);
    }

    @After
    public void teardown() {
        loginPage = null;
    }

    @Test
    public void login_adminShouldLogin() {
        final String username = "admin";
        final String password = "admin";
        loginPage.login(username, password);
    }

    @Test
    public void login_adminWithWrongPasswordShouldNotLogin() {
        final String username = "admin";
        final String password = "wrongPassword";
        loginPage.attemptLoginExpectingFailure(username, password);

    }
  
}
