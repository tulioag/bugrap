package org.vaadin.training.bugrap;

import java.nio.file.Path;

import org.vaadin.bugrap.domain.entities.Reporter;

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

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
public class BugrapUI extends UI {

    private BugrapNavigator navigator;

    private Reporter user;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();
        setContent(layout);
        navigator = new BugrapNavigator(this, this);
        setNavigator(navigator);
    }

    public void loginSuccessful(Reporter user) {
        if (user == null)
            throw new IllegalArgumentException("Reporter missing");
        this.user = user;
        navigator.addRestrictedViews();
        navigator.goToDefault();
    }

    public static BugrapUI getCurrent() {
        return (BugrapUI) UI.getCurrent();
    }
}
