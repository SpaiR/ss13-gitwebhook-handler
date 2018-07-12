package io.github.spair.handler.command.changelog;

import io.github.spair.handler.command.changelog.RemoveTestChangelogCommand;
import io.github.spair.service.changelog.ChangelogService;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.github.GitHubService;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.service.pr.entity.PullRequestType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RemoveTestChangelogCommandTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigService configService;
    @Mock
    private ChangelogService changelogService;
    @Mock
    private GitHubService gitHubService;

    private RemoveTestChangelogCommand command;

    private static final String TESTMERGE = "testmerge";
    private static final String MASTER = "master";

    @Before
    public void setUp() {
        command = new RemoveTestChangelogCommand(configService, changelogService, gitHubService);
        when(configService.getConfig().getGitHubConfig().getMasterUsers()).thenReturn(Sets.newSet(MASTER));
        when(configService.getConfig().getLabels().getTestMerge()).thenReturn(TESTMERGE);
    }

    @Test
    public void testExecuteWhenIncorrectPullRequestType() {
        command.execute(PullRequest.builder().sender(MASTER).touchedLabel(TESTMERGE).type(PullRequestType.OPENED).build());
        verifyNeverUpdate();
    }

    @Test
    public void testExecuteWhenNotMasterUser() {
        command.execute(PullRequest.builder().sender("noname").touchedLabel(TESTMERGE).type(PullRequestType.UNLABELED).build());
        verifyNeverUpdate();
    }

    @Test
    public void testExecuteWhenNotTestMergeLabel() {
        command.execute(PullRequest.builder().sender(MASTER).touchedLabel("").type(PullRequestType.UNLABELED).build());
        verifyNeverUpdate();
    }

    @Test
    public void testExecuteWhenDoesntContainTestMergeLabel() {
        when(configService.getConfig().getChangelogConfig().getPathToChangelog()).thenReturn("Changelog 1");
        when(gitHubService.readDecodedFile(anyString())).thenReturn("");
        when(changelogService.removeTestChangelogFromHtml(anyString(), anyInt())).thenReturn("Changelog 2");

        command.execute(PullRequest.builder().sender(MASTER).touchedLabel("").type(PullRequestType.CLOSED).number(23).labels(Sets.newSet()).build());

        verify(configService.getConfig().getChangelogConfig()).getPathToChangelog();
        verify(gitHubService).readDecodedFile(anyString());
        verify(changelogService).removeTestChangelogFromHtml(anyString(), eq(23));
        verify(gitHubService).updateFile(anyString(), eq("Remove test merge changelog for PR #23"), anyString());

        verify(gitHubService, never()).removeLabel(anyInt(), anyString());
    }

    @Test
    public void testExecuteWhenContainTestMergeLabel() {
        when(configService.getConfig().getChangelogConfig().getPathToChangelog()).thenReturn("Changelog 1");
        when(gitHubService.readDecodedFile(anyString())).thenReturn("");
        when(changelogService.removeTestChangelogFromHtml(anyString(), anyInt())).thenReturn("Changelog 2");

        command.execute(PullRequest.builder().sender(MASTER).touchedLabel("").type(PullRequestType.CLOSED).number(23).labels(Sets.newSet(TESTMERGE)).build());

        verify(configService.getConfig().getChangelogConfig()).getPathToChangelog();
        verify(gitHubService).readDecodedFile(anyString());
        verify(changelogService).removeTestChangelogFromHtml(anyString(), eq(23));
        verify(gitHubService).updateFile(anyString(), eq("Remove test merge changelog for PR #23"), anyString());

        verify(gitHubService).removeLabel(23, TESTMERGE);
    }

    @Test
    public void testExecuteWhenSameChangelogs() {
        when(configService.getConfig().getChangelogConfig().getPathToChangelog()).thenReturn("");
        when(gitHubService.readDecodedFile(anyString())).thenReturn("Changelog");
        when(changelogService.removeTestChangelogFromHtml(anyString(), anyInt())).thenReturn("Changelog");

        command.execute(PullRequest.builder().sender(MASTER).touchedLabel("").type(PullRequestType.CLOSED).number(23).labels(Sets.newSet()).build());

        verify(configService.getConfig().getChangelogConfig()).getPathToChangelog();
        verify(gitHubService).readDecodedFile(anyString());
        verify(changelogService).removeTestChangelogFromHtml(anyString(), eq(23));
        verify(gitHubService, never()).updateFile(anyString(), eq("Remove test merge changelog for PR #23"), anyString());

        verify(gitHubService, never()).removeLabel(anyInt(), anyString());
    }

    private void verifyNeverUpdate() {
        verify(configService.getConfig().getChangelogConfig(), never()).getPathToChangelog();
        verify(gitHubService, never()).readDecodedFile(anyString());
        verify(changelogService, never()).removeTestChangelogFromHtml(anyString(), anyInt());
        verify(gitHubService, never()).updateFile(anyString(), anyString(), anyString());
    }
}