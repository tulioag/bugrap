/**
 * 
 */
package org.vaadin.training.bugrap.reports;

import static org.vaadin.training.bugrap.util.TimeUtil.createTimeElapsedDateFormatter;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import javax.persistence.OptimisticLockException;

import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Report;
import org.vaadin.bugrap.domain.entities.Reporter;

import com.vaadin.data.ValidationException;

/**
 * @author Tulio Garcia
 *
 */
@SuppressWarnings("serial")
public class SingleReportView extends ReportVisualizationDesign {

    private final BugrapRepository repo;
    private final ReportVisualizationController controller;
    private Report report;

    private final Messages messages = new Messages();

    public SingleReportView(BugrapRepository repo, long id, Locale locale) {
        this.repo = repo;
        this.report = repo.getReportById(id);
        controller = new ReportVisualizationController(this,
                createTimeElapsedDateFormatter(locale), null);
        controller.init(this::updateReport, this::revertChangesOnReport);
        Set<Reporter> assignableUsers = repo.findReporters();
        Set<ProjectVersion> validVersions = repo
                .findProjectVersions(report.getProject());
        controller.showReportDetails(report, repo.findComments(report),
                assignableUsers, validVersions);
        controller.showBreadcrumb(report.getProject().getName(),
                Optional.ofNullable(report.getVersion())
                        .map(ProjectVersion::getVersion));

    }

    private void updateReport() {
        try {
            controller.write(report);
            report = repo.save(report);
            messages.showUpdateSucessfullMessage();
        } catch (ValidationException e) {
            messages.showValidationErrorsMessage();
        } catch (OptimisticLockException e) {
            messages.showConcurrentModificationErrorMessage();
        }

    }

    private void revertChangesOnReport() {
        controller.read(report);
    }
}
