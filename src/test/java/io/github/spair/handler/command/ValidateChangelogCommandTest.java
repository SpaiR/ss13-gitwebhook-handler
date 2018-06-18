package io.github.spair.handler.command;

import io.github.spair.service.changelog.ChangelogService;
import io.github.spair.service.changelog.entities.Changelog;
import io.github.spair.service.changelog.entities.ChangelogValidationStatus;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.git.GitHubService;
import io.github.spair.service.git.entities.PullRequest;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ValidateChangelogCommandTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigService configService;
    @Mock
    private ChangelogService changelogService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GitHubService gitHubService;

    private ValidateChangelogCommand command;
    private Changelog changelog;

    @Before
    public void setUp() {
        command = new ValidateChangelogCommand(configService, changelogService, gitHubService);
    }

    @Test
    public void testExecuteWhenHasChangelogRowsAndInvalidAndNoInvalidLabel() {
        Changelog changelog = createChangelog(true);

        when(changelogService.createFromPullRequest(any(PullRequest.class))).thenReturn(changelog);
        when(gitHubService.listIssueLabels(anyInt()).contains(anyString())).thenReturn(false);
        when(changelogService.validateChangelog(changelog)).thenReturn(createValidationStatus(false));

        command.execute(mock(PullRequest.class));

        verify(gitHubService).createIssueComment(anyInt(), anyString());
        verify(gitHubService).addLabel(anyInt(), anyString());
        verify(gitHubService, never()).removeLabel(anyInt(), anyString());
    }

    @Test
    public void testExecuteWhenHasChangelogRowsAndInvalidAndHasInvalidLabel() {
        Changelog changelog = createChangelog(true);

        when(changelogService.createFromPullRequest(any(PullRequest.class))).thenReturn(changelog);
        when(gitHubService.listIssueLabels(anyInt()).contains(anyString())).thenReturn(true);
        when(changelogService.validateChangelog(changelog)).thenReturn(createValidationStatus(false));

        command.execute(mock(PullRequest.class));

        verify(gitHubService, never()).createIssueComment(anyInt(), anyString());
        verify(gitHubService, never()).addLabel(anyInt(), anyString());
        verify(gitHubService, never()).removeLabel(anyInt(), anyString());
    }

    @Test
    public void testExecuteWhenHasChangelogRowsAndValidAndNoInvalidLabel() {
        Changelog changelog = createChangelog(true);

        when(changelogService.createFromPullRequest(any(PullRequest.class))).thenReturn(changelog);
        when(gitHubService.listIssueLabels(anyInt()).contains(anyString())).thenReturn(false);
        when(changelogService.validateChangelog(changelog)).thenReturn(createValidationStatus(true));

        command.execute(mock(PullRequest.class));

        verify(gitHubService, never()).createIssueComment(anyInt(), anyString());
        verify(gitHubService, never()).addLabel(anyInt(), anyString());
        verify(gitHubService, never()).removeLabel(anyInt(), anyString());
    }

    @Test
    public void testExecuteWhenHasChangelogRowsAndValidAndHasInvalidLabel() {
        Changelog changelog = createChangelog(true);

        when(changelogService.createFromPullRequest(any(PullRequest.class))).thenReturn(changelog);
        when(gitHubService.listIssueLabels(anyInt()).contains(anyString())).thenReturn(true);
        when(changelogService.validateChangelog(changelog)).thenReturn(createValidationStatus(true));

        command.execute(mock(PullRequest.class));

        verify(gitHubService, never()).createIssueComment(anyInt(), anyString());
        verify(gitHubService, never()).addLabel(anyInt(), anyString());
        verify(gitHubService).removeLabel(anyInt(), anyString());
    }

    @Test
    public void testExecuteWhenNoChangelogRowsAndHasInvalidLabel() {
        Changelog changelog = createChangelog(false);

        when(changelogService.createFromPullRequest(any(PullRequest.class))).thenReturn(changelog);
        when(gitHubService.listIssueLabels(anyInt()).contains(anyString())).thenReturn(true);
        when(changelogService.validateChangelog(changelog)).thenReturn(createValidationStatus(true));

        command.execute(mock(PullRequest.class));

        verify(gitHubService, never()).createIssueComment(anyInt(), anyString());
        verify(gitHubService, never()).addLabel(anyInt(), anyString());
        verify(gitHubService).removeLabel(anyInt(), anyString());
    }

    private Changelog createChangelog(final boolean hasRows) {
        Changelog changelog = mock(Changelog.class);
        when(changelog.getChangelogRows()).thenReturn(hasRows ? Lists.emptyList() : null);
        return changelog;
    }

    private ChangelogValidationStatus createValidationStatus(final boolean isValid) {
        ChangelogValidationStatus status = new ChangelogValidationStatus();
        status.setStatus(isValid ? ChangelogValidationStatus.Status.VALID : ChangelogValidationStatus.Status.INVALID);
        return status;
    }
}