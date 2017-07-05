/**
 * 
 */
package org.vaadin.training.bugrap.reports;

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.vaadin.bugrap.domain.entities.Comment;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Report;
import org.vaadin.bugrap.domain.entities.Report.Priority;
import org.vaadin.bugrap.domain.entities.Report.Status;
import org.vaadin.bugrap.domain.entities.Report.Type;
import org.vaadin.bugrap.domain.entities.Reporter;
import org.vaadin.training.bugrap.util.ReportUtil;

import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.server.ExternalResource;
import com.vaadin.ui.ItemCaptionGenerator;
import com.vaadin.ui.NativeSelect;

/**
 * @author Tulio Garcia
 *
 */
public class ReportVisualizationController {

    private final Binder<Report> binder = new Binder<>();
    private final ReportVisualizationDesign design;
    private final Function<Date, String> timeFormatter;
    private final Function<Report, String> reportLinkGenerator;

    ReportVisualizationController(ReportVisualizationDesign design,
            Function<Date, String> timeFormatter,
            Function<Report, String> reportLinkGenerator) {
        if (design == null)
            throw new NullPointerException("Design should not be null");
        if (timeFormatter == null)
            throw new NullPointerException("TimeFormatter should not be null");
        this.design = design;
        this.timeFormatter = timeFormatter;
        this.reportLinkGenerator = reportLinkGenerator;
        design.breadcrumb.setVisible(false);
        design.linkOpenReportNewWindow.setVisible(reportLinkGenerator != null);
    }

    public void init(Runnable updateRequestHandler,
            Runnable revertRequestHandler) {
        design.linkOpenReportNewWindow.setTargetName("_blank");
        initSelect(design.updatePriority, EnumSet.allOf(Priority.class),
                ReportUtil::toString);
        initSelect(design.updateType, EnumSet.allOf(Type.class),
                ReportUtil::toString);
        initSelect(design.updateStatus, EnumSet.allOf(Status.class),
                Status::toString);

        binder.forField(design.updatePriority).bind(Report::getPriority,
                Report::setPriority);
        binder.forField(design.updateType).bind(Report::getType,
                Report::setType);
        binder.forField(design.updateStatus).bind(Report::getStatus,
                Report::setStatus);
        binder.forField(design.updateAssignedTo).bind(Report::getAssigned,
                Report::setAssigned);
        binder.forField(design.updateVersion).bind(Report::getVersion,
                Report::setVersion);
        design.updateReportCommand
                .addClickListener(e -> executeIfNotNull(updateRequestHandler));
        design.revertReportCommand
                .addClickListener(e -> executeIfNotNull(revertRequestHandler));

    }

    void showReportPanelMassModification(int quantity) {
        if (!design.isVisible()) {
            design.setVisible(true);
        }
        design.reportDataLayout.removeAllComponents();
        design.linkOpenReportNewWindow.setVisible(false);
        design.massModificationDescription.setVisible(true);
        design.massModificationDescription
                .setValue(quantity + " reports selected");

    }

    void showReportDetails(Report report, List<Comment> comments,Set<Reporter> assignableUsers,
            Set<ProjectVersion> validVersions) {
        if (!design.isVisible()) {
            design.setVisible(true);
        }
        design.massModificationDescription.setVisible(false);
        design.reportDataLayout.removeAllComponents();

        design.linkOpenReportNewWindow.setVisible(reportLinkGenerator != null);
        if (design.linkOpenReportNewWindow.isVisible()) {
            design.linkOpenReportNewWindow.setCaption(report.getSummary());
            design.linkOpenReportNewWindow.setEnabled(true);
            design.linkOpenReportNewWindow.setResource(
                    new ExternalResource(reportLinkGenerator.apply(report)));
        }
        Function<Reporter, String> extractReporterName = r -> r != null
                ? r.getName() : "Unknown";

        design.reportDataLayout.addComponent(new CommentLayout(timeFormatter,
                extractReporterName.apply(report.getAuthor()),
                report.getReportedTimestamp(), report.getDescription()));

        comments.stream()
                .map(c -> new CommentLayout(timeFormatter,
                        extractReporterName.apply(c.getAuthor()),
                        c.getTimestamp(), c.getComment()))
                .forEach(design.reportDataLayout::addComponent);
        setAssignableUsers(assignableUsers);
        setProjectVersions(validVersions);
        read(report);
    }

    void showBreadcrumb(String projectName, Optional<String> version) {
        design.breadcrumb.setVisible(true);
        StringBuilder sb = new StringBuilder();
        sb.append(projectName);
        version.ifPresent(v -> sb.append("﹥").append(v));
        design.breadcrumb.setValue(sb.toString());
    }

    void hideReportDetails() {
        design.setVisible(false);
        design.reportDataLayout.removeAllComponents();
    }

    private static void executeIfNotNull(Runnable r) {
        if (r != null)
            r.run();
    }

    private <T> void initSelect(NativeSelect<T> select, Collection<T> values,
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

    void setAssignableUsers(Collection<Reporter> assignableUsers) {
        initSelect(design.updateAssignedTo, assignableUsers, Reporter::getName);
    }

    void setProjectVersions(Collection<ProjectVersion> versions) {
        initSelect(design.updateVersion, versions, ProjectVersion::toString);
    }

    boolean isVisible() {
        return design.isVisible();
    }
}
