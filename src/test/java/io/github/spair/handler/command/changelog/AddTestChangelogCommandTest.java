package io.github.spair.handler.command.changelog;

import io.github.spair.service.changelog.ChangelogService;
import io.github.spair.service.changelog.entity.Changelog;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.github.GitHubService;
import io.github.spair.service.pr.entity.PullRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AddTestChangelogCommandTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigService configService;
    @Mock
    private ChangelogService changelogService;
    @Mock
    private GitHubService gitHubService;

    private AddTestChangelogCommand command;

    private static final String TESTMERGE = "testmerge";
    private static final String MASTER = "master";

    @Before
    public void setUp() {
        command = new AddTestChangelogCommand(configService, changelogService, gitHubService);
        when(configService.getConfig().getGitHubConfig().getMasterUsers()).thenReturn(Sets.newSet(MASTER));
        when(configService.getConfig().getLabels().getTestMerge()).thenReturn(TESTMERGE);
    }

    @Test
    public void testExecuteWhenNotMasterUser() {
        command.execute(PullRequest.builder().sender("noname").touchedLabel(TESTMERGE).build());
        verifyNeverUpdate();
    }

    @Test
    public void testExecuteWhenNotTestMergeLabel() {
        command.execute(PullRequest.builder().sender(MASTER).touchedLabel("").build());
        verifyNeverUpdate();
    }

    @Test
    public void testExecuteWhenNoChangelog() {
        when(changelogService.createFromPullRequest(any(PullRequest.class))).thenReturn(Optional.empty());
        command.execute(PullRequest.builder().sender(MASTER).touchedLabel(TESTMERGE).build());
        verifyNeverUpdate();
    }

    @Test
    public void testExecuteWhenChangelogIsEmpty() {
        Changelog changelog = mock(Changelog.class);
        when(changelog.isEmpty()).thenReturn(true);
        when(changelogService.createFromPullRequest(any(PullRequest.class))).thenReturn(Optional.of(changelog));

        command.execute(PullRequest.builder().sender(MASTER).touchedLabel(TESTMERGE).build());

        verifyNeverUpdate();
    }

    @Test
    public void testExecuteWhenChangelogIsNotEmpty() {
        Changelog changelog = mock(Changelog.class);
        when(changelog.isEmpty()).thenReturn(false);

        when(changelogService.createFromPullRequest(any(PullRequest.class))).thenReturn(Optional.of(changelog));
        when(configService.getConfig().getChangelogConfig().getPathToChangelog()).thenReturn("");
        when(gitHubService.readDecodedFile(anyString())).thenReturn("");
        when(changelogService.addTestChangelogToHtml(anyString(), eq(changelog))).thenReturn("");

        command.execute(PullRequest.builder().sender("master").touchedLabel("testmerge").number(123).build());

        verify(configService.getConfig().getChangelogConfig()).getPathToChangelog();
        verify(gitHubService).readDecodedFile(anyString());
        verify(changelogService).addTestChangelogToHtml(anyString(), any(Changelog.class));
        verify(gitHubService).updateFile(anyString(), eq("Add test merge changelog for PR #123"), anyString());

    }

    private void verifyNeverUpdate() {
        verify(configService.getConfig().getChangelogConfig(), never()).getPathToChangelog();
        verify(gitHubService, never()).readDecodedFile(anyString());
        verify(changelogService, never()).mergeHtmlWithChangelog(anyString(), any(Changelog.class));
        verify(gitHubService, never()).updateFile(anyString(), anyString(), anyString());
    }
}