package org.vaadin.training.bugrap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Label;
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
@Theme("mytheme")
public class BugrapUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();
        layout.addComponent(new Label("BUGRAP!"));
        Repository.getInstance().findProjects().forEach(project ->
        layout.addComponent(new Label(project.toString())));

        setContent(layout);
    }

    @WebServlet(urlPatterns = "/*", name = "BugrapUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = BugrapUI.class, productionMode = false)
    public static class BugrapUIServlet extends VaadinServlet {

        @Override
        protected void servletInitialized() throws ServletException {
            super.servletInitialized();
            Repository.create("/tmp/bugrap");
        }

    }
}
