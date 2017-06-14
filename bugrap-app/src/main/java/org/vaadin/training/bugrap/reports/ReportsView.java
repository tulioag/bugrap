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

import javax.servlet.http.Cookie;

import org.ocpsoft.prettytime.PrettyTime;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.BugrapRepository.ReportsQuery;
import org.vaadin.bugrap.domain.entities.Project;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Report;
import org.vaadin.bugrap.domain.entities.Reporter;
import org.vaadin.training.bugrap.BugrapNavigator;

import com.vaadin.data.ValueProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Grid;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;

@SuppressWarnings("serial")
public class ReportsView extends ReportsDesign implements View {

    private final Presenter presenter;

    private final BugrapNavigator navigator;

    private final PrettyTime prettyTime = new PrettyTime();

    public static final String PATH = "";

    public ReportsView(BugrapNavigator navigator, BugrapRepository repo,
            Reporter user) {
        this.navigator = navigator;
        this.presenter = new Presenter(this, repo, user,
                new VersionSelectionCookieManager());
    }

    @Override
    public void enter(ViewChangeEvent event) {
        this.logout.addClickListener((e) -> navigator.logout());
        this.projects.addSelectionListener(
                e -> presenter.setProject(e.getSelectedItem().orElse(null)));
        new MenuBarSelectionPresenter(assignee, "Everyone",
                new AssigneeConsumer());
        new MenuBarSelectionPresenter(status, "Open",
                new StatusMenubarConsumer(presenter));
        versions.setEmptySelectionAllowed(false);
        versions.addValueChangeListener((e) -> {
            presenter.setProjectVersion(e.getValue());
        });
        presenter.initialize(new ReportsGridPresenter(reportsTable));
        VaadinService.getCurrentRequest().getCookies();
    }

    void setUsername(String username) {
        this.username.setValue(username);
    }

    void setProjects(Collection<Project> ps) {
        projects.setItems(ps);
        projects.setSelectedItem(ps.iterator().next());
    }

    void showNoProjectsMessage() {
        projects.setVisible(false);
        noProjects.setVisible(true);
    }

    void setVersions(Collection<ProjectVersion> versions) {
        this.versions.setItems(versions);
    }

    void selectVersion(ProjectVersion version) {
        this.versions.setSelectedItem(version);
    }

    /**
     * 
     * Controls the view and keeps state
     * 
     * @author Tulio Garcia
     *
     */
    static class Presenter {

        private final ReportsView view;
        private final BugrapRepository repo;
        private final Reporter user;
        private final VersionSelectionPreferenceStorage versionSelectionStorage;

        private final ProjectVersion nullVersion = new ProjectVersion();
        private final ReportsQuery query = new ReportsQuery();

        private ReportsGridPresenter reportsGridPresenter;

        private boolean initialized;

        Presenter(ReportsView view, BugrapRepository repo, Reporter user,
                VersionSelectionPreferenceStorage versionSelectionStorage) {
            this.view = view;
            this.repo = repo;
            this.user = user;
            this.versionSelectionStorage = versionSelectionStorage;
            this.nullVersion.setVersion("All versions");
        }

        /**
         * Called once when the view is entered.
         * 
         * @param reportsGridPresenter {@link ReportsGridPresenter}
         */
        void initialize(ReportsGridPresenter reportsGridPresenter) {
            initialized = true;
            this.reportsGridPresenter = reportsGridPresenter;
            initializeProjects();
            view.setUsername(user.getName());
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
            reportsGridPresenter.setReports(reports);
        }

        private void updateProjectVersions() {
            boolean showVersions = query.projectVersion == nullVersion;
            Collection<ProjectVersion> availableVersions = recoverVersionData();
            view.setVersions(availableVersions);
            reportsGridPresenter.setShowVersions(showVersions);
            ProjectVersion defaultVersion = nullVersion;
            Long result = versionSelectionStorage.recover(query.project.getId());
            if (result != null)
                defaultVersion = availableVersions.stream()
                        .filter(v -> result.longValue() == v.getId()).findAny()
                        .orElse(nullVersion);
            view.selectVersion(defaultVersion);
        }

        private Collection<ProjectVersion> recoverVersionData() {
            Collection<ProjectVersion> versions = repo
                    .findProjectVersions(query.project);
            if (versions.size() == 1) {
                return versions;
            }
            ArrayList<ProjectVersion> result = new ArrayList<>(
                    versions.size() + 1);
            result.add(nullVersion);
            result.addAll(versions);
            return result;
        }

        void setProject(Project project) {
            query.project = project;
            updateProjectVersions();
            setProjectVersion(nullVersion);
        }

        void setProjectVersion(ProjectVersion projectVersion) {
            boolean isNull = projectVersion == nullVersion;
            query.projectVersion = isNull ? null : projectVersion;
            reportsGridPresenter.setShowVersions(isNull);
            long projectId = query.project.getId();
            if (isNull)
                versionSelectionStorage.remove(projectId);
            else
                versionSelectionStorage.save(projectId, projectVersion.getId());
            update();
        }

        void setAssignee(Reporter assignee) {
            query.reportAssignee = assignee;
            update();
        }

        void setAssigneeOnlyMe(boolean onlyMe) {
            setAssignee(onlyMe ? user : null);
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
                view.showNoProjectsMessage();
            else {
                view.setProjects(new TreeSet<>(projects));
            }
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

            boolean onlyMe = ONLY_ME_VALUE.equals(t.get(0));
            presenter.setAssigneeOnlyMe(onlyMe);
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
 * Controls the menubar selection component, used for the assignee and status
 * filters.
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
     * Registers this as the {@link Command} for either this menu item or its
     * children.
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
     * For menu itens with children, set checked if the is at least 1 child
     * selected.
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

class ReportsGridPresenter {

    private final Grid<Report> grid;

    private boolean showVersions = true;

    public ReportsGridPresenter(Grid<Report> grid) {
        super();
        this.grid = grid;
        formatTable();
    }

    public void setReports(Collection<Report> reports) {
        grid.setItems(reports);
    }

    public void setShowVersions(boolean showVersions) {
        if (this.showVersions != showVersions) {
            this.showVersions = showVersions;
            formatTable();
        }
    }

    private void formatTable() {
        grid.removeAllColumns();
        if (showVersions)
            grid.addColumn(Report::getVersion).setCaption("VERSION");
        ValueProvider<Report, String> getAssignedTo = (
                r) -> r.getAssigned() != null ? r.getAssigned().getName()
                        : null;
        grid.addColumn(ReportTypeFormatter.INSTANCE::getDescription)
                .setCaption("TYPE");
        grid.addColumn(Report::getSummary).setCaption("SUMMARY");
        grid.addColumn(getAssignedTo).setCaption("ASSIGNED TO");

    }

}

/**
 *
 * Interface for saving the version preferences.
 * 
 * @author Tulio Garcia
 *
 */
interface VersionSelectionPreferenceStorage {

    /**
     * Saves the verstion preference for the specified project.
     * 
     * @param projectId
     *            project id
     * @param versionId
     *            version id
     */
    void save(long projectId, long versionId);

    /**
     * Removes the preference, if exists.
     * 
     * @param projectId
     *            project id
     */
    void remove(long projectId);

    /**
     * Recovers the preference value, if exists.
     * 
     * @param projectId
     *            project id
     * @return versionId, if exists. Null otherwise.
     */
    Long recover(long projectId);

}

/**
 * 
 * Implements the {@link VersionSelectionPreferenceStorage} interface using
 * cookies to save the values.
 * 
 * 
 * @author Tulio Garcia
 *
 */
class VersionSelectionCookieManager
        implements VersionSelectionPreferenceStorage {

    private static final int MAX_AGE = 60 * 24 * 30 * 12;

    /** {@inheritDoc} */
    @Override
    public void save(long projectId, long versionId) {
        setCookie(projectId, versionId, MAX_AGE);
    }

    /** {@inheritDoc} */
    @Override
    public void remove(long projectId) {
        setCookie(projectId, 0, 0);
    }

    void setCookie(long projectId, long versionId, int maxAge) {
        String name = getName(projectId);
        Cookie cookie = new Cookie(name, String.valueOf(versionId));
        cookie.setMaxAge(maxAge);
        cookie.setPath(VaadinService.getCurrentRequest().getContextPath());
        VaadinService.getCurrentResponse().addCookie(cookie);

    }

    /** {@inheritDoc} */
    @Override
    public Long recover(long projectId) {
        Long result = null;
        Cookie cookie = findByProjectId(projectId);
        if (cookie != null) {
            result = Long.valueOf(cookie.getValue());
        }
        return result;
    }

    private String getName(long projectId) {
        return String.format("selectedVersion_%d", projectId);
    }

    private Cookie findByProjectId(long projectId) {
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
        final String name = getName(projectId);
        for (Cookie cookie : cookies)
            if (name.equals(cookie.getName()))
                return cookie;
        return null;
    }
}