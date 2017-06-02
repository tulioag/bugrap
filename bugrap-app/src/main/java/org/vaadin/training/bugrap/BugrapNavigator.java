package org.vaadin.training.bugrap;

import org.vaadin.training.bugrap.login.LoginView;
import org.vaadin.training.bugrap.reports.ReportsView;

import com.vaadin.navigator.Navigator;
import com.vaadin.ui.SingleComponentContainer;
import com.vaadin.ui.UI;

/**
 * 
 * @author Tulio Garcia
 *
 */
@SuppressWarnings("serial")
public class BugrapNavigator extends Navigator {

    /**
     * Initializes with the only view available for users not logged in.
     * @param ui {@link UI}
     * @param container {@link SingleComponentContainer}
     */
    public BugrapNavigator(UI ui, SingleComponentContainer container) {
        super(ui, container);
        addView(LoginView.PATH, LoginView.class);
        navigateTo(LoginView.PATH);
    }
    
    public void addRestrictedViews()
    {
        addView(ReportsView.PATH, ReportsView.class);
    }

    public void goToDefault()
    {
        navigateTo("");
    }
}
