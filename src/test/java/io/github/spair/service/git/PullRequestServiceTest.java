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
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PullRequestServiceTest {

    @Mock
    private ChangelogService changelogService;
    @Mock
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
}