package org.vaadin.training.bugrap;

import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.Reporter;
import org.vaadin.training.bugrap.reports.SingleReportView;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
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
        String pathInfo = vaadinRequest.getPathInfo();
        BugrapRepository repo = Repository.getInstance();
        Reporter user = getSession().getAttribute(Reporter.class);

        if ("/viewReport".equals(pathInfo) && user != null) {
            String id = vaadinRequest.getParameter("id");
            setContent(
                    new SingleReportView(repo, Long.valueOf(id), getLocale()));
        } else {
            navigator = new BugrapNavigator(repo, this, layout);
            setNavigator(navigator);
            if (user != null) {
                navigator.setUser(user);
                navigator.goToDefault();
            }
        }
    }

    public void loginSuccessful(Reporter user) {
        getSession().setAttribute(Reporter.class, user);
        navigator.setUser(user);
        navigator.goToDefault();
    }

    public void logout() {
        getPage().setLocation(VaadinServlet.getCurrent().getServletContext()
                .getContextPath());
        getSession().close();
    }

    public static BugrapUI getCurrent() {
        return (BugrapUI) UI.getCurrent();
    }

    public String getReportVisualizationPath(Long reportId) {
        return "viewReport?id="+reportId;
    }
}
