package org.vaadin.training.bugrap;

import org.vaadin.bugrap.domain.entities.Reporter;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.UI;

/**
 * This UI is the application entry point. A UI may either represent a browser
 * window (or tab) or some part of a html page where a Vaadin application is
 * embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is
 * intended to be overridden to add component to the user interface and
 * initialize non-component functionality.
 */
@SuppressWarnings("serial")
@Theme("bugrap")
@Title("Bugrap")
public class BugrapUI extends UI {

    private BugrapNavigator navigator;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final ComponentContainer layout = new CssLayout();
        layout.addStyleName("main");
        setContent(layout);
        navigator = new BugrapNavigator(Repository.getInstance(), this, layout);
        setNavigator(navigator);
    }

    public void loginSuccessful(Reporter user) {
        navigator.setUser(user);
        navigator.goToDefault();
    }

    public static BugrapUI getCurrent() {
        return (BugrapUI) UI.getCurrent();
    }

}
