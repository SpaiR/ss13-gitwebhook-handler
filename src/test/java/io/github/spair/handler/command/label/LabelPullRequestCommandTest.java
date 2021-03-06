package io.github.spair.handler.command.label;

import io.github.spair.service.changelog.ChangelogService;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.github.GitHubService;
import io.github.spair.service.github.entity.PullRequestFile;
import io.github.spair.service.pr.entity.PullRequest;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LabelPullRequestCommandTest {

    @Mock
    private GitHubService gitHubService;
    @Mock
    private ChangelogService changelogService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigService configService;

    private LabelPullRequestCommand command;

    @Before
    public void setUp() {
        command = new LabelPullRequestCommand(gitHubService, changelogService, configService);
    }

    @Test
    public void testExecute() {
        PullRequest pullRequest = PullRequest.builder()
                .number(2).title("[WiP][DNM]Update the README with new information")
                .diffLink("https://github.com/baxterthehacker/public-repo/pull/1.diff")
                .body("This is a pretty simple change that we need to pull into master.").build();

        when(configService.getConfig().getLabels().getLabelsForClasses())
                .thenReturn(new HashMap<String, String>(){
                    {
                        put("map", "Map Edit");
                        put("fix", "Fix");
                    }
                });
        when(changelogService.getChangelogClassesList(any(PullRequest.class))).thenReturn(Sets.newSet("tweak", "map", "fix"));
        when(gitHubService.listPullRequestFiles(anyInt()))
                .thenReturn(
                        Lists.newArrayList(
                                createPullRequestFile("icon.dmi"),
                                createPullRequestFile("map.dmm"),
                                createPullRequestFile("code.dm")
                        )
                );
        when(configService.getConfig().getLabels().getMapChanges()).thenReturn("Map Edit");
        when(configService.getConfig().getLabels().getIconChanges()).thenReturn("Sprites");
        when(configService.getConfig().getLabels().getDoNotMerge()).thenReturn("Do Not Merge");
        when(configService.getConfig().getLabels().getWorkInProgress()).thenReturn("WIP");

        command.execute(pullRequest);

        verify(gitHubService).addLabels(2, Sets.newSet("Fix", "Sprites", "Map Edit", "Do Not Merge", "WIP"));
    }

    private PullRequestFile createPullRequestFile(String filename) {
        PullRequestFile pullRequestFile = new PullRequestFile();
        pullRequestFile.setFilename(filename);
        return pullRequestFile;
    }
}