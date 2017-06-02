/**
 * 
 */
package org.vaadin.training.bugrap.login;

import org.vaadin.bugrap.domain.entities.Reporter;
import org.vaadin.training.bugrap.BugrapUI;
import org.vaadin.training.bugrap.Repository;

import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.ui.Notification;

/**
 * @author vaadin
 *
 */
@SuppressWarnings("serial")
public class LoginView extends LoginDesign implements View {

    public static final String PATH = "login";
    
    private Presenter presenter = new Presenter();

    public LoginView() {
        loginButton.addClickListener(e -> presenter
                .authenticate(username.getValue(), password.getValue()));
        loginButton.setClickShortcut(KeyCode.ENTER);
    }

    @Override
    public void enter(ViewChangeEvent event) {
        username.focus();
    }

    private void showLoginError(String msg) {
        Notification.show(msg, Notification.Type.ERROR_MESSAGE);
    }

    private void showLoginSuccess()
    {
        Notification notification = new Notification(
                "Login successful",
                Notification.Type.HUMANIZED_MESSAGE);
        notification.setDelayMsec(1000);
        notification.setStyleName("success");
        notification.show(Page.getCurrent());        
    }
    
    class Presenter {

        public void authenticate(String username, String password) {

            Reporter reporter = Repository.getInstance().authenticate(username,
                    password);
            if (reporter == null) {
                showLoginError("Invalid username/password");
            } else {
                showLoginSuccess();
                BugrapUI.getCurrent().loginSuccessful(reporter);
            }
        }
    };
}
