package io.github.spair.service.git;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.service.changelog.ChangelogService;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.git.entities.PullRequest;
import io.github.spair.service.git.entities.PullRequestType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PullRequestServiceTest {

    @Mock
    private ChangelogService changelogService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigService configService;
    @Mock
    private GitHubService gitHubService;

    private PullRequestService service;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        service = new PullRequestService(changelogService, configService, gitHubService);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testConvertWebhookMap() throws Exception {
        File jsonFile = new ClassPathResource("pull-request-webhook-payload.json").getFile();
        ObjectNode pullRequestPayload = objectMapper.readValue(jsonFile, ObjectNode.class);

        PullRequest expectedPullRequest = PullRequest.builder()
                .author("baxterthehacker").number(1).title("Update the README with new information").type(PullRequestType.OPENED)
                .link("https://github.com/baxterthehacker/public-repo/pull/1").diffLink("https://github.com/baxterthehacker/public-repo/pull/1.diff")
                .body("This is a pretty simple change that we need to pull into master.").build();

        assertEquals(expectedPullRequest, service.convertWebhookJson(pullRequestPayload));
    }

    @Test
    public void testProcessLabels() {
        PullRequest pullRequest = PullRequest.builder()
                .number(2).title("[WiP][DNM]Update the README with new information")
                .diffLink("https://github.com/baxterthehacker/public-repo/pull/1.diff")
                .body("This is a pretty simple change that we need to pull into master.").build();

        when(configService.getConfig().getGitHubConfig().getLabels().getAvailableClassesLabels())
                .thenReturn(new HashMap<String, String>(){
                    {
                        put("map", "Map Edit");
                        put("fix", "Fix");
                    }
                });
        when(changelogService.getChangelogClassesList(any(PullRequest.class))).thenReturn(Sets.newSet("tweak", "map", "fix"));
        when(gitHubService.getPullRequestDiff(anyString())).thenReturn("\ndiff smthg.dm\ndiff smthg.dmm\ndiff smthg.dmi");
        when(configService.getConfig().getGitHubConfig().getLabels().getMapChanges()).thenReturn("Map Edit");
        when(configService.getConfig().getGitHubConfig().getLabels().getIconChanges()).thenReturn("Sprites");
        when(configService.getConfig().getGitHubConfig().getLabels().getDoNotMerge()).thenReturn("Do Not Merge");
        when(configService.getConfig().getGitHubConfig().getLabels().getWorkInProgress()).thenReturn("WIP");

        service.processLabels(pullRequest);

        verify(gitHubService).addLabels(2, Arrays.asList("Fix", "Sprites", "Map Edit", "Do Not Merge", "WIP"));
    }
}