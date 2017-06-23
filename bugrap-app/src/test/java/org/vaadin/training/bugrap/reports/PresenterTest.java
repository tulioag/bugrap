/**
 * 
 */
package org.vaadin.training.bugrap.reports;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collector;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.Project;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Reporter;
import org.vaadin.training.bugrap.reports.ReportsView.Presenter;
import org.vaadin.training.bugrap.reports.ReportsView.ReportViewControl;

/**
 * @author Tulio Garcia
 *
 */
public class PresenterTest {

    @Mock
    private ReportsView view;

    @Mock
    private BugrapRepository repo;

    @Mock
    private Reporter user;

    @Mock
    private VersionSelectionPreferenceStorage versionSelectionStorage;

    @Mock
    private ReportsGridPresenter reportsGridPresenter;

    @Mock
    private ReportViewControl reportViewControl;

    @Mock
    private Project project1;

    @Mock
    private Project project2;

    private Presenter presenter;

    private Set<Project> projects;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        presenter = new Presenter(view, reportViewControl, repo, user,
                versionSelectionStorage);
        projects = asSet(project1, project2);
    }

    @Test
    public void should_setUsername_when_initializing() {
        final String USERNAME = "username";
        when(user.getName()).thenReturn(USERNAME);
        presenter.initialize(reportsGridPresenter);
        verify(view).setUsername(USERNAME);
    }

    @Test
    public void should_showNoProjectsMessage_when_thereAreNoProjects() {
        presenter.initialize(reportsGridPresenter);
        verify(view).showNoProjectsMessage();
    }

    @Test
    public void should_setProjects_when_initializing() {
        when(repo.findProjects()).thenReturn(projects);
        presenter.initialize(reportsGridPresenter);
        verify(view).setProjects(projects);
    }

    @Test
    public void should_updateProjectVersions_when_projectChanged() {
        Set<ProjectVersion> versionsProject1 = asSet(mock(ProjectVersion.class),
                mock(ProjectVersion.class));
        Set<ProjectVersion> versionsProject2 = asSet(mock(ProjectVersion.class),
                mock(ProjectVersion.class));

        when(repo.findProjects()).thenReturn(projects);
        when(repo.findProjectVersions(project1)).thenReturn(versionsProject1);
        when(repo.findProjectVersions(project2)).thenReturn(versionsProject2);

        presenter.initialize(reportsGridPresenter);
        presenter.setProject(project1);

        verify(view)
                .setVersions(matchProjectVersions(project1, versionsProject1));

        presenter.setProject(project2);
        verify(view)
                .setVersions(matchProjectVersions(project2, versionsProject2));
    }

    private Collection<ProjectVersion> matchProjectVersions(Project project,
            Set<ProjectVersion> versions) {
        ArgumentMatcher<Collection<ProjectVersion>> matcher = l -> {
            if (l.size() != 3)
                return false;
            Iterator<ProjectVersion> it = l.iterator();
            it.next(); // All versions
            while (it.hasNext())
                if (!versions.contains(it.next()))
                    return false;
            return true;
        };
        return Mockito.argThat(matcher);
    }

    @SafeVarargs
    private static <T> LinkedHashSet<T> asSet(T... ts) {
        return Arrays.stream(ts).collect(Collector.of(LinkedHashSet::new,
                LinkedHashSet::add, (left, right) -> {
                    left.addAll(right);
                    return left;
                }));
    }

}
