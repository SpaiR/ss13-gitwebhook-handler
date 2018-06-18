package io.github.spair.handler.command;

import io.github.spair.service.changelog.ChangelogService;
import io.github.spair.service.changelog.entities.Changelog;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.git.GitHubService;
import io.github.spair.service.git.entities.PullRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateChangelogCommandTest {

    @Mock
    private ChangelogService changelogService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigService configService;
    @Mock
    private GitHubService gitHubService;

    private UpdateChangelogCommand command;

    @Before
    public void setUp() {
        command = new UpdateChangelogCommand(changelogService, configService, gitHubService);
    }

    @Test
    public void testExecuteWhenChangelogNotEmpty() {
        Changelog changelog = mock(Changelog.class);
        when(changelog.isEmpty()).thenReturn(false);
        PullRequest pullRequest = PullRequest.builder().number(123).build();
        when(changelogService.createFromPullRequest(pullRequest)).thenReturn(changelog);

        command.execute(pullRequest);

        verify(configService.getConfig().getChangelogConfig()).getPathToChangelog();
        verify(gitHubService).readDecodedFile(anyString());
        verify(changelogService).mergeHtmlWithChangelog(anyString(), eq(changelog));
        verify(gitHubService).updateFile(anyString(), eq("Automatic changelog generation for PR #123"), anyString());
    }

    @Test
    public void testExecuteWhenChangelogIsEmpty() {
        Changelog changelog = mock(Changelog.class);
        when(changelog.isEmpty()).thenReturn(true);
        PullRequest pullRequest = mock(PullRequest.class);
        when(changelogService.createFromPullRequest(pullRequest)).thenReturn(changelog);

        command.execute(pullRequest);

        verify(configService.getConfig().getChangelogConfig(), never()).getPathToChangelog();
        verify(gitHubService, never()).readDecodedFile(anyString());
        verify(changelogService, never()).mergeHtmlWithChangelog(anyString(), eq(changelog));
        verify(gitHubService, never()).updateFile(anyString(), eq("Automatic changelog generation for PR #123"), anyString());
    }
}