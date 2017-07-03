package org.vaadin.training.bugrap.reports;

import static org.vaadin.training.bugrap.util.TimeUtil.createTimeElapsedDateFormatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.persistence.OptimisticLockException;
import javax.servlet.http.Cookie;

import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.BugrapRepository.ReportsQuery;
import org.vaadin.bugrap.domain.entities.Comment;
import org.vaadin.bugrap.domain.entities.Project;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Report;
import org.vaadin.bugrap.domain.entities.Report.Priority;
import org.vaadin.bugrap.domain.entities.Report.Status;
import org.vaadin.bugrap.domain.entities.Report.Type;
import org.vaadin.bugrap.domain.entities.Reporter;
import org.vaadin.training.bugrap.BugrapNavigator;
import org.vaadin.training.bugrap.util.ReportUtil;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.data.provider.GridSortOrderBuilder;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinService;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.ItemCaptionGenerator;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.renderers.Renderer;
import com.vaadin.ui.renderers.TextRenderer;

import elemental.json.JsonValue;

@SuppressWarnings("serial")
public class ReportsView extends ReportsDesign implements View {

    public static final String PATH = "";

    private final Messages messages = new Messages();

    private final Presenter presenter;

    private final BugrapNavigator navigator;

    private Function<Date, String> timeFormatter;

    public ReportsView(BugrapNavigator navigator, BugrapRepository repo,
            Reporter user) {
        this.navigator = navigator;
        this.presenter = new Presenter(this, new ReportViewControl(), repo,
                user, new VersionSelectionCookieManager());
    }

    @Override
    public void enter(ViewChangeEvent event) {
        linkOpenReportNewWindow
                .addClickListener(e -> Notification.show("Implement"));
        this.timeFormatter = createTimeElapsedDateFormatter(getLocale());
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
        reportsTable.setSelectionMode(SelectionMode.MULTI);
        reportsTable.addSelectionListener(
                e -> presenter.onSelection(e.getAllSelectedItems()));
        // reportsTable.addItemClickListener(e -> )1
        presenter.initialize(
                new ReportsGridPresenter(reportsTable, timeFormatter));
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

    public Messages getMessages() {
        return messages;
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
        private final ReportViewControl reportViewControl;
        private final BugrapRepository repo;
        private final Reporter user;
        private final VersionSelectionPreferenceStorage versionSelectionStorage;

        private final ProjectVersion nullVersion = new ProjectVersion();
        private final ReportsQuery query = new ReportsQuery();

        private Report currentReport;
        private Set<Report> realReports;

        private ReportsGridPresenter reportsGridPresenter;

        private boolean initialized;

        Presenter(ReportsView view, ReportViewControl reportViewControl,
                BugrapRepository repo, Reporter user,
                VersionSelectionPreferenceStorage versionSelectionStorage) {
            this.view = view;
            this.reportViewControl = reportViewControl;
            this.repo = repo;
            this.user = user;
            this.versionSelectionStorage = versionSelectionStorage;
            this.nullVersion.setVersion("All versions");
        }

        /**
         * Called once, when the view is entered.
         * 
         * @param reportsGridPresenter
         *            {@link ReportsGridPresenter}
         */
        void initialize(ReportsGridPresenter reportsGridPresenter) {
            initialized = true;
            this.reportsGridPresenter = reportsGridPresenter;
            initializeProjects();
            view.setUsername(user.getName());
            onSelection(Collections.emptySet());
        }

        /**
         * Query the database and update the reports table.
         */
        Set<Report> update() {
            if (!initialized)
                return null;
            Set<Report> reports = queryDB();
            reportsGridPresenter.setReports(reports);
            return reports;
        }

        private Set<Report> queryDB() {
            Set<Report> reports = Collections.emptySet();
            if (query.project != null) {
                reports = repo.findReports(query);
            }
            return reports;
        }

        private void updateProjectVersions() {
            boolean showVersions = query.projectVersion == nullVersion;
            Collection<ProjectVersion> availableVersions = recoverVersionData();
            view.setVersions(availableVersions);
            reportsGridPresenter.setShowVersions(showVersions);
            ProjectVersion defaultVersion = nullVersion;
            Long result = versionSelectionStorage
                    .recover(query.project.getId());
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
            else
                view.setProjects(new TreeSet<>(projects));
        }

        void onSelection(Set<Report> reports) {
            if (reports.isEmpty()) {
                // No projects are selected.
                reportViewControl.hideReportDetails();
                currentReport = null;
                realReports = null;
            } else if (reports.size() == 1) {
                // Single report mode
                Report report = reports.iterator().next();
                currentReport = report;
                realReports = null;
                List<Comment> comments = repo.findComments(report);
                Set<Reporter> assignableUsers = repo.findReporters();
                Set<ProjectVersion> validVersions = repo
                        .findProjectVersions(query.project);
                reportViewControl.showReportDetails(report, comments);
                reportViewControl.initialize(assignableUsers, validVersions);
                reportViewControl.read(report);
            } else {
                currentReport = createDummyReport(reports);
                realReports = reports;
                reportViewControl.hideReportDetails();
                reportViewControl
                        .showReportPanelMassModification(reports.size());
                reportViewControl.read(currentReport);
            }
        }

        Report createDummyReport(Set<Report> reports) {
            Report dummy = new Report();
            fillReport(dummy, findValue(reports, Report::getPriority),
                    findValue(reports, Report::getType),
                    findValue(reports, Report::getStatus),
                    findValue(reports, Report::getAssigned),
                    findValue(reports, Report::getVersion));
            return dummy;
        }

        void fillReport(Report report, Priority priority, Type type,
                Status status, Reporter assigned, ProjectVersion version) {
            
            consumeIfNotNull(report::setPriority, priority);
            consumeIfNotNull(report::setType,type);
            consumeIfNotNull(report::setStatus,status);
            consumeIfNotNull(report::setVersion,version);
            
            //Assigned is nullable
            report.setAssigned(assigned);

        }

        <T> void consumeIfNotNull(Consumer<T> c,T value) {
            if(value != null)
                c.accept(value);
        }
        
        <T> T findValue(Set<Report> reports,
                Function<Report, T> valueExtractor) {
            T result = null;
            Set<T> uniqueValues = new HashSet<>();
            reports.stream().map(valueExtractor).forEach(uniqueValues::add);
            if (uniqueValues.size() == 1) {
                result = uniqueValues.iterator().next();
            }
            return result;
        }

        void updateReport() {
            if (realReports == null)
                updateSingleReport();
            else {
                executeEntityUpdate(() -> {

                    reportViewControl.write(currentReport);
                    realReports.forEach(report -> {
                        fillReport(report, currentReport.getPriority(),
                                currentReport.getType(),
                                currentReport.getStatus(),
                                currentReport.getAssigned(),
                                currentReport.getVersion());
                        repo.save(report);
                    });
                    view.getMessages().showUpdateSucessfullMessage();
                    reportsGridPresenter.setReports(queryDB());
                });
            }
        }

        void updateSingleReport() {
            executeEntityUpdate(() -> {
                reportViewControl.write(currentReport);
                Report report = repo.save(currentReport);
                currentReport = report;
                view.getMessages().showUpdateSucessfullMessage();
                Set<Report> updatedReportsSet = queryDB();
                reportsGridPresenter.setReports(updatedReportsSet);
                if (updatedReportsSet.contains(report)) {
                    reportsGridPresenter.select(report);
                }
            });
        }

        void executeEntityUpdate(EntityUpdate entityUpdate) {
            try {
                entityUpdate.execute();
            } catch (ValidationException e) {
                view.getMessages().showValidationErrorsMessage();
            } catch (OptimisticLockException e) {
                view.getMessages().showConcurrentModificationErrorMessage();
            }

        }

        @FunctionalInterface
        interface EntityUpdate {

            void execute() throws ValidationException;
        }

        void revertReport() {
            reportViewControl.read(currentReport);
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

    /**
     * Layout of a single comment.
     * 
     * @author Tulio Garcia
     *
     */
    class CommentLayout extends Label {

        CommentLayout(String autorName, Date date, String text) {
            setIcon(VaadinIcons.USER);
            setStyleName("comment-content");
            setCaption(formatCaption(autorName, date));
            setValue(text);
        }

        String formatCaption(String autorName, Date date) {
            String formattedDate = timeFormatter.apply(date);
            return String.format("%s (%s)", autorName, formattedDate);
        }
    }

    /**
     * 
     * Controls the report viewing.
     * 
     * @author Tulio Garcia
     *
     */
    class ReportViewControl {

        private Binder<Report> binder = new Binder<>();

        private float defaultSplitPosition = 70;

        ReportViewControl() {
            initSelect(updatePriority, EnumSet.allOf(Priority.class),
                    ReportUtil::toString);
            initSelect(updateType, EnumSet.allOf(Type.class),
                    ReportUtil::toString);
            initSelect(updateStatus, EnumSet.allOf(Status.class),
                    Status::toString);
            binder.forField(updatePriority).bind(Report::getPriority,
                    Report::setPriority);
            binder.forField(updateType).bind(Report::getType, Report::setType);
            binder.forField(updateStatus).bind(Report::getStatus,
                    Report::setStatus);
            binder.forField(updateAssignedTo).bind(Report::getAssigned,
                    Report::setAssigned);
            binder.forField(updateVersion).bind(Report::getVersion,
                    Report::setVersion);
            updateReportCommand.addClickListener(e -> presenter.updateReport());
            revertReportCommand.addClickListener(e -> presenter.revertReport());
            storeDefaultSplitPosition();
        }

        void initialize(Collection<Reporter> assignableUsers,
                Collection<ProjectVersion> versions) {

            initSelect(updateAssignedTo, assignableUsers, Reporter::getName);
            initSelect(updateVersion, versions, ProjectVersion::toString);
            // updatePriority.setItemCaptionGenerator(itemCaptionGenerator);
        }

        private void storeDefaultSplitPosition() {
            float splitPosition = splitPanel.getSplitPosition();
            defaultSplitPosition = splitPosition < 100 ? splitPosition : 70;

        }

        private <T> void initSelect(NativeSelect<T> select,
                Collection<T> values,
                ItemCaptionGenerator<T> captionGenerator) {
            select.clear();
            select.setItems(values);
            select.setItemCaptionGenerator(captionGenerator);
        }

        void read(Report report) {
            binder.readBean(report);
        }

        void write(Report report) throws ValidationException {
            binder.writeBean(report);
        }

        void showReportPanelMassModification(int quantity) {
            if (!reportPanel.isVisible()) {
                reportPanel.setVisible(true);
            }
            reportDataLayout.removeAllComponents();
            splitPanel.setSplitPosition(80);
            splitPanel.setLocked(true);
            linkOpenReportNewWindow.setVisible(false);
            massModificationDescription.setVisible(true);
            massModificationDescription
                    .setValue(quantity + " reports selected");

        }

        void showReportDetails(Report report, List<Comment> comments) {
            if (!reportPanel.isVisible()) {
                reportPanel.setVisible(true);
                splitPanel.setSplitPosition(defaultSplitPosition);
            }
            massModificationDescription.setVisible(false);
            splitPanel.setLocked(false);
            reportDataLayout.removeAllComponents();
            linkOpenReportNewWindow.setCaption(report.getSummary());
            linkOpenReportNewWindow.setEnabled(true);
            linkOpenReportNewWindow
                    .addClickListener(e -> Notification.show("Implement"));
            Function<Reporter, String> extractReporterName = r -> r != null
                    ? r.getName() : "Unknown";

            reportDataLayout.addComponent(new CommentLayout(
                    extractReporterName.apply(report.getAuthor()),
                    report.getReportedTimestamp(), report.getDescription()));

            comments.stream()
                    .map(c -> new CommentLayout(
                            extractReporterName.apply(c.getAuthor()),
                            c.getTimestamp(), c.getComment()))
                    .forEach(reportDataLayout::addComponent);
        }

        void hideReportDetails() {
            reportPanel.setVisible(false);
            reportDataLayout.removeAllComponents();
            storeDefaultSplitPosition();
            splitPanel.setSplitPosition(100);
            splitPanel.setLocked(true);
        }

    }

    /**
     * 
     * Messages to the user available in this view.
     * 
     * @author Tulio Garcia
     *
     */
    class Messages {

        void showValidationErrorsMessage() {
            Notification.show("There are validation errors",
                    Notification.Type.ERROR_MESSAGE);
        }

        void showConcurrentModificationErrorMessage() {
            Notification.show("The report has been modified by another user",
                    Notification.Type.ERROR_MESSAGE);
        }

        void showUpdateSucessfullMessage() {
            Notification notification = new Notification("Update successful",
                    Notification.Type.TRAY_NOTIFICATION);
            notification.setDelayMsec(1000);
            notification.setStyleName("success");
            notification.show(Page.getCurrent());

        }
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
 * Controls the MenuBar selection component, used for the assignee and status
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

    private Column<Report, ?> columnVersion;

    private Column<Report, ?> priorityColumn;

    public ReportsGridPresenter(Grid<Report> grid,
            Function<Date, String> dateFormatter) {
        this.grid = grid;

        columnVersion = grid.getColumn("version");

        priorityColumn = grid.getColumn("priority")
                .setRenderer(new PriorityRenderer())
                .setStyleGenerator(r -> "priority").setMinimumWidth(100);

        grid.getColumn("type").setRenderer(new ReportTypeRenderer());

        grid.getColumn("summary").setExpandRatio(2);

        grid.getColumn("assigned")
                .setRenderer(new ReportsViewRenderer<Reporter>(Reporter.class,
                        Reporter::getName))
                .setExpandRatio(1);

        Supplier<Renderer<Object>> dateRendererSupplier = () -> new ReportsViewRenderer<Date>(
                Date.class, dateFormatter);

        grid.getColumn("timestamp").setRenderer(dateRendererSupplier.get());

        grid.getColumn("reportedTimestamp")
                .setRenderer(dateRendererSupplier.get());
        formatTable();
    }

    public void setReports(Collection<Report> reports) {
        grid.setItems(reports);
    }

    public void select(Report report) {
        grid.select(report);
    }

    public void setShowVersions(boolean showVersions) {
        if (this.showVersions != showVersions) {
            this.showVersions = showVersions;
            formatTable();
        }
    }

    private void formatTable() {
        columnVersion.setHidden(!showVersions);
        /*
         * Because of issue #8316, sorting indicator is not being shown, but
         * sorting works. https://github.com/vaadin/framework/issues/8316
         */
        GridSortOrderBuilder<Report> sortOrderBuilder = new GridSortOrderBuilder<>();
        if (showVersions)
            sortOrderBuilder.thenAsc(columnVersion);
        grid.setSortOrder(sortOrderBuilder.thenDesc(priorityColumn).build());

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

@SuppressWarnings("serial")
class ReportsViewRenderer<T> extends TextRenderer {
    private final Class<T> type;
    private final Function<T, String> renderer;

    ReportsViewRenderer(Class<T> type, Function<T, String> renderer) {
        this.type = type;
        this.renderer = renderer;
    }

    @Override
    public JsonValue encode(Object value) {
        T element = type.cast(value);
        if (element != null) {
            return super.encode(renderer.apply(element));
        }
        return super.encode(value);
    }

}

@SuppressWarnings("serial")
class PriorityRenderer extends ReportsViewRenderer<Priority> {

    PriorityRenderer() {
        super(Priority.class, ReportUtil::toString);
    }
}

/**
 * Generates captions for {@link Type}
 *
 */
@SuppressWarnings("serial")
class ReportTypeRenderer extends ReportsViewRenderer<Report.Type> {

    ReportTypeRenderer() {
        super(Report.Type.class, ReportUtil::toString);
    }
}

