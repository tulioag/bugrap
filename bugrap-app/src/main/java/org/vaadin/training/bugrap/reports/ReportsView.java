package org.vaadin.training.bugrap.reports;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.Project;
import org.vaadin.bugrap.domain.entities.Reporter;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Notification;

@SuppressWarnings("serial")
public class ReportsView extends ReportsDesign implements View {

    private final Presenter presenter = new Presenter();

    private final BugrapRepository repo;

    private final Reporter user;

    public static final String PATH = "";

    public ReportsView(BugrapRepository repo, Reporter user) {
        this.repo = repo;
        this.user = user;
    }

    @Override
    public void enter(ViewChangeEvent event) {
        this.username.setValue(user.getName());
        this.projects.addSelectionListener(
                e -> presenter.onSelection(e.getSelectedItem()));
        presenter.initialize();
    }

    private void setProjects(Collection<Project> ps) {
        projects.setItems(ps);
        projects.setSelectedItem(ps.iterator().next());
    }

    private void showNoProjectsMessage() {
        projects.setVisible(false);
        noProjects.setVisible(true);
    }

    class Presenter {
        void initialize() {
            initializeProjects();
        }

        void onSelection(Optional<Project> selection) {
            Notification.show(selection.get().getName() + " selected");
        }

        private void initializeProjects() {
            // Assuming everyone has access to all projects.
            Set<Project> projects = repo.findProjects();
            if (projects.isEmpty())
                showNoProjectsMessage();
            else
                setProjects(new TreeSet<>(projects));

        }
    }
}
