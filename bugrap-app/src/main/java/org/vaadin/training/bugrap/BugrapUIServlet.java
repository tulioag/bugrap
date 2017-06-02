package org.vaadin.training.bugrap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/*", name = "BugrapUIServlet", asyncSupported = true)
@VaadinServletConfiguration(ui = BugrapUI.class, productionMode = false)
public class BugrapUIServlet extends VaadinServlet {

    private static final String DATABASE_PARAMETER = "bugrap.database.path";

    private final DatabaseInitializer productionDatabaseInitializer = (
            databasePath) -> {
        if (databasePath == null)
            throw new IllegalArgumentException(
                    "Please specify the database path using the parameter "
                            + DATABASE_PARAMETER);
        if (!Files.exists(Paths.get(databasePath)))
            throw new FileNotFoundException(databasePath);
        Repository.create(databasePath);
    };

    private final DatabaseInitializer defaultDatabaseInitializer = (
            databasePath) -> {
        if (databasePath == null) {
            databasePath = Files.createTempDirectory("bugrap").toString();
        }
        Repository.create(databasePath);
        Repository.getInstance().populateWithTestData();
    };

    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();
        try {
            initializeDatabase();
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    private void initializeDatabase() throws IOException {
        boolean productionMode = getService().getDeploymentConfiguration()
                .isProductionMode();
        String databasePath = System.getProperty(DATABASE_PARAMETER);
        DatabaseInitializer initializer = productionMode
                ? productionDatabaseInitializer : defaultDatabaseInitializer;
        initializer.initialize(databasePath);
    }

    interface DatabaseInitializer {
        void initialize(String databasePath) throws IOException;
    }
}