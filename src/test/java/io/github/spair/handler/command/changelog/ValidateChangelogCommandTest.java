package io.github.spair.handler.command.changelog;

import io.github.spair.handler.command.changelog.ValidateChangelogCommand;
import io.github.spair.service.changelog.ChangelogService;
import io.github.spair.service.changelog.entity.Changelog;
import io.github.spair.service.changelog.entity.ChangelogValidationStatus;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.github.GitHubService;
import io.github.spair.service.pr.entity.PullRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("ConstantConditions")
public class ValidateChangelogCommandTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigService configService;
    @Mock
    private ChangelogService changelogService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GitHubService gitHubService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private PullRequest pullRequest;

    private ValidateChangelogCommand command;

    @Before
    public void setUp() {
        command = new ValidateChangelogCommand(configService, changelogService, gitHubService);
        when(configService.getConfig().getLabels().getInvalidChangelog()).thenReturn("");
    }

    @Test
    public void testExecuteWhenHasChangelogRowsAndInvalidAndNoInvalidLabel() {
        Optional<Changelog> changelog = createChangelog();

        when(changelogService.createFromPullRequest(any(PullRequest.class))).thenReturn(changelog);
        when(pullRequest.getLabels().contains(anyString())).thenReturn(false);
        when(changelogService.validateChangelog(changelog.get())).thenReturn(createValidationStatus(false));

        command.execute(pullRequest);

        verify(gitHubService).createIssueComment(anyInt(), anyString());
        verify(gitHubService).addLabel(anyInt(), anyString());
        verify(gitHubService, never()).removeLabel(anyInt(), anyString());
    }

    @Test
    public void testExecuteWhenHasChangelogRowsAndInvalidAndHasInvalidLabel() {
        Optional<Changelog> changelog = createChangelog();

        when(changelogService.createFromPullRequest(any(PullRequest.class))).thenReturn(changelog);
        when(pullRequest.getLabels().contains(anyString())).thenReturn(true);
        when(changelogService.validateChangelog(changelog.get())).thenReturn(createValidationStatus(false));

        command.execute(pullRequest);

        verify(gitHubService, never()).createIssueComment(anyInt(), anyString());
        verify(gitHubService, never()).addLabel(anyInt(), anyString());
        verify(gitHubService, never()).removeLabel(anyInt(), anyString());
    }

    @Test
    public void testExecuteWhenHasChangelogRowsAndValidAndNoInvalidLabel() {
        Optional<Changelog> changelog = createChangelog();

        when(changelogService.createFromPullRequest(any(PullRequest.class))).thenReturn(changelog);
        when(pullRequest.getLabels().contains(anyString())).thenReturn(false);
        when(changelogService.validateChangelog(changelog.get())).thenReturn(createValidationStatus(true));

        command.execute(pullRequest);

        verify(gitHubService, never()).createIssueComment(anyInt(), anyString());
        verify(gitHubService, never()).addLabel(anyInt(), anyString());
        verify(gitHubService, never()).removeLabel(anyInt(), anyString());
    }

    @Test
    public void testExecuteWhenNoChangelogRowsAndHasInvalidLabel() {
        Optional<Changelog> changelog = createChangelog();

        when(changelogService.createFromPullRequest(any(PullRequest.class))).thenReturn(changelog);
        when(pullRequest.getLabels().contains(anyString())).thenReturn(true);
        when(changelogService.validateChangelog(changelog.get())).thenReturn(createValidationStatus(true));

        command.execute(pullRequest);

        verify(gitHubService, never()).createIssueComment(anyInt(), anyString());
        verify(gitHubService, never()).addLabel(anyInt(), anyString());
        verify(gitHubService).removeLabel(anyInt(), anyString());
    }

    private Optional<Changelog> createChangelog() {
        Changelog changelog = mock(Changelog.class);
        return Optional.of(changelog);
    }

    private ChangelogValidationStatus createValidationStatus(final boolean isValid) {
        ChangelogValidationStatus status = new ChangelogValidationStatus();
        status.setStatus(isValid ? ChangelogValidationStatus.Status.VALID : ChangelogValidationStatus.Status.INVALID);
        return status;
    }
}