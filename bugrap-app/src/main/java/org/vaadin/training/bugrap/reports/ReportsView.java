package org.vaadin.training.bugrap.reports;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Set;
import java.util.TreeSet;

import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.BugrapRepository.ReportsQuery;
import org.vaadin.bugrap.domain.entities.Project;
import org.vaadin.bugrap.domain.entities.Report;
import org.vaadin.bugrap.domain.entities.Reporter;
import org.vaadin.training.bugrap.BugrapNavigator;

import com.vaadin.data.ValueProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;

@SuppressWarnings("serial")
public class ReportsView extends ReportsDesign implements View {

    private final Presenter presenter = new Presenter();

    private final BugrapNavigator navigator;
    
    private final BugrapRepository repo;

    private final Reporter user;

    public static final String PATH = "";

    public ReportsView(BugrapNavigator navigator,BugrapRepository repo, Reporter user) {
        this.navigator = navigator;
        this.repo = repo;
        this.user = user;
    }

    @Override
    public void enter(ViewChangeEvent event) {
        this.username.setValue(user.getName());
        this.logout.addClickListener((e) -> navigator.logout());
        this.projects.addSelectionListener(
                e -> presenter.setProject(e.getSelectedItem().orElse(null)));
        reportsTable.removeAllColumns();
        ValueProvider<Report, String> getAssignedTo = (
                r) -> r.getAssigned() != null ? r.getAssigned().getName()
                        : null;
        reportsTable.addColumn(ReportTypeFormatter.INSTANCE::getDescription)
                .setCaption("TYPE");
        reportsTable.addColumn(Report::getSummary).setCaption("SUMMARY");
        reportsTable.addColumn(getAssignedTo).setCaption("ASSIGNED TO");
        
        MenuItem assigneeOnlyMe = assignee.getItems().get(0);
        MenuItem assigneeEveryone = assignee.getItems().get(1);
        AssigneeCommand command = new AssigneeCommand(assigneeOnlyMe,assigneeEveryone);
        assignee.getItems().forEach(i -> i.setCommand(command));

        presenter.initialize();
    }

    private void setProjects(Collection<Project> ps) {
        projects.setItems(ps);
        projects.setSelectedItem(ps.iterator().next());
    }

    private void setReports(Collection<Report> reports) {
        reportsTable.setItems(reports);
    }

    private void showNoProjectsMessage() {
        projects.setVisible(false);
        noProjects.setVisible(true);
    }

    class Presenter {

        private ReportsQuery query = new ReportsQuery();

        void initialize() {
            initializeProjects();
        }

        void update() {
            Set<Report> reports = Collections.emptySet();
            if (query.project != null) {
                reports = repo.findReports(query);
            }
            setReports(reports);
        }

        void setProject(Project project) {
            query.project = project;
            update();
        }

        void setAssignee(Reporter assignee) {
            query.reportAssignee = assignee;
            update();
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

    class AssigneeCommand implements MenuBar.Command {

        private final MenuItem onlyMe;
        private final MenuItem everyone;

        public AssigneeCommand(MenuItem onlyMe, MenuItem everyone) {
            this.onlyMe = onlyMe;
            this.everyone = everyone;
        }

        @Override
        public void menuSelected(MenuItem selectedItem) {
            boolean onlyMeChecked = false;
            boolean everyoneChecked = false;
            Reporter assignee = null;
            if (selectedItem.equals(onlyMe)) {
                onlyMeChecked = true;
                assignee = user;
            } else {
                everyoneChecked = true;
            }
            onlyMe.setChecked(onlyMeChecked);
            everyone.setChecked(everyoneChecked);
            presenter.setAssignee(assignee);
        }
    }
}

class ReportTypeFormatter {
    private EnumMap<Report.Type, String> descriptions = new EnumMap<>(
            Report.Type.class);

    static ReportTypeFormatter INSTANCE = new ReportTypeFormatter();

    private ReportTypeFormatter() {
        for (Report.Type type : Report.Type.values()) {
            String name = type.name();
            String description = new StringBuilder().append(name.charAt(0))
                    .append(name.substring(1).toLowerCase()).toString();
            descriptions.put(type, description);
        }
    }

    String getDescription(Report report) {
        return toString(report.getType());
    }

    String toString(Report.Type type) {
        return descriptions.get(type);
    }
}
