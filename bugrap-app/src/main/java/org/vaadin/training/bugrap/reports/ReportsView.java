package org.vaadin.training.bugrap.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

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
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

@SuppressWarnings("serial")
public class ReportsView extends ReportsDesign implements View {

    private final Presenter presenter = new Presenter();

    private final BugrapNavigator navigator;

    private final BugrapRepository repo;

    private final Reporter user;

    public static final String PATH = "";

    public ReportsView(BugrapNavigator navigator, BugrapRepository repo,
            Reporter user) {
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

        new MenuBarSelectionPresenter(assignee, "Everyone",
                new AssigneeConsumer());
        new MenuBarSelectionPresenter(status, "Open",
                new StatusMenubarConsumer(presenter));
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

        private boolean initialized;

        void initialize() {
            initialized = true;
            initializeProjects();
        }

        /**
         * Query the database and update the reports table.
         */
        void update() {
            if (!initialized)
                return;
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

        void setStatus(Set<Report.Status> statuses) {
            query.reportStatuses = statuses.isEmpty() ? null : statuses;
            update();
        }

        /**
         * Initialize the projects selection.
         */
        private void initializeProjects() {
            // Assuming everyone has access to all projects.
            Set<Project> projects = repo.findProjects();
            if (projects.isEmpty())
                showNoProjectsMessage();
            else
                setProjects(new TreeSet<>(projects));
        }
    }

    /**
     * 
     * Links the assignee selection with the presenter.
     *
     */
    class AssigneeConsumer implements Consumer<List<String>> {

        private static final String ONLY_ME_VALUE = "Only me";

        @Override
        public void accept(List<String> t) {
            if (t.isEmpty())
                throw new IllegalArgumentException("No selection");

            if (t.size() > 1)
                throw new IllegalArgumentException("Too many selections");

            Reporter assignee = ONLY_ME_VALUE.equals(t.get(0)) ? user : null;
            presenter.setAssignee(assignee);
        }
    }

}

/**
 * Generates captions for {@link Type}
 *
 */
class ReportTypeFormatter {
    private EnumMap<Report.Type, String> descriptions = new EnumMap<>(
            Report.Type.class);

    static final ReportTypeFormatter INSTANCE = new ReportTypeFormatter();

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

/**
 * Connects the view presenter with the status component. 
 *
 */
class StatusMenubarConsumer implements Consumer<List<String>> {

    private final ReportsView.Presenter presenter;

    public StatusMenubarConsumer(ReportsView.Presenter presenter) {
        this.presenter = presenter;
    }

    /**
     * Maps the labels to statuses. 
     */
    static class StatusMapper {
        private static final Map<String, Report.Status> statuses;
        static {
            Map<String, Report.Status> tmp = new HashMap<>();
            for (Report.Status status : Report.Status.values())
                tmp.put(status.toString(), status);
            statuses = Collections.unmodifiableMap(tmp);
        }

        private static EnumSet<Report.Status> map(List<String> selected) {
            if (selected == null)
                return null;
            EnumSet<Report.Status> result = EnumSet.noneOf(Report.Status.class);
            selected.stream().forEach(s -> {
                Report.Status status = statuses.get(s);
                if (status != null)
                    result.add(status);
            });
            return result;
        }
    }

    /**
     * Update the presenter.
     */
    @Override
    public void accept(List<String> t) {
        EnumSet<Report.Status> statuses = StatusMapper.map(t);
        presenter.setStatus(statuses);
    }

}

/**
 * Controls the menubar selection component, used for the assignee and status filters.
 *
 */
@SuppressWarnings("serial")
class MenuBarSelectionPresenter implements MenuBar.Command {

    private final MenuBar menuBar;

    private final String defaultSelection;

    private final Consumer<List<String>> consumer;

    MenuBarSelectionPresenter(MenuBar menuBar, String defaultSelection,
            Consumer<List<String>> consumer) {
        this.menuBar = menuBar;
        this.defaultSelection = defaultSelection;
        this.consumer = consumer;
        menuBar.getItems().stream().forEach(this::registerCommand);
        defaultSelection();
        consumer.accept(getSelectedValues());
    }

    @Override
    public void menuSelected(MenuItem selectedItem) {
        if (selectedItem.getParent() != null) {
            defineCheckedStatusByChildren(selectedItem.getParent());
            selectedItem = selectedItem.getParent();
        } else {
            selectedItem.setChecked(true);
        }
        if (selectedItem.isChecked()) {
            // Deselect others
            final MenuItem keepSelected = selectedItem;
            final Consumer<MenuItem> uncheck = i -> setChecked(i, false);
            menuBar.getItems().stream().filter(i -> !i.equals(keepSelected))
                    .forEach(i -> {
                        uncheck.accept(i);
                        if (i.getChildren() != null)
                            i.getChildren().stream().forEach(uncheck);
                    });
        }
        List<String> selected = getSelectedValues();
        if (selected.isEmpty()) {
            defaultSelection();
            selected.add(defaultSelection);
        }
        consumer.accept(selected);
    }

    /**
     * Registers this as the {@link Command} for either this menu item or its children.
     * 
     */
    private void registerCommand(MenuItem menuItem) {
        if (menuItem.hasChildren())
            menuItem.getChildren().stream().forEach(this::registerCommand);
        else
            menuItem.setCommand(this);
    }

    /**
     * Makes the default selection, based on the attribute defaultSelection.
     */
    private void defaultSelection() {
        MenuItem item = menuBar.getItems().stream()
                .filter(i -> defaultSelection.equals(i.getText())).findAny()
                .get();
        if (item.getChildren() != null)
            throw new UnsupportedOperationException(
                    "Default selection of item with children not supported");
        setChecked(item, true);
    }

    /**
     * For menu itens with children, set checked if the is at least 1 child selected.
     */
    private void defineCheckedStatusByChildren(MenuItem item) {
        boolean anyChildrenSelected = item.getChildren().stream()
                .anyMatch(MenuItem::isChecked);
        setChecked(item, anyChildrenSelected);
    }

    /**
     * Sets the item as checked or unchecked. For itens with children, that
     * means also adding or removing the style class !selected-children"
     */
    private void setChecked(MenuItem item, boolean checked) {
        item.setChecked(checked);
        if (item.hasChildren()) {
            String styleName = checked ? "selected-children" : null;
            item.setStyleName(styleName);
        }
    }

    /**
     * 
     * @return A list of the labels of the selected itens.
     */
    private List<String> getSelectedValues() {
        List<String> result = new ArrayList<>(10);
        menuBar.getItems().stream().filter(MenuItem::isChecked).forEach(i -> {
            if (i.getChildren() != null)
                i.getChildren().stream().filter(MenuItem::isChecked)
                        .map(MenuItem::getText).forEach(result::add);
            else
                result.add(i.getText());
        });
        return result;
    }

}
