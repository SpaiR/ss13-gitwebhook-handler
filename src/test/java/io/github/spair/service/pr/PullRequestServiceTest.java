package io.github.spair.service.pr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.service.pr.entity.PullRequestType;
import org.assertj.core.util.Sets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PullRequestServiceTest {

    private PullRequestService service;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        service = new PullRequestService();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testConvertWebhookJson() throws Exception {
        File jsonFile = new ClassPathResource("data/pull-request-webhook-payload.json").getFile();
        ObjectNode pullRequestPayload = objectMapper.readValue(jsonFile, ObjectNode.class);

        PullRequest expectedPullRequest = PullRequest.builder()
                .author("baxterthehacker").number(1).title("Update the README with new information").type(PullRequestType.OPENED).branchName("changes")
                .link("https://github.com/baxterthehacker/public-repo/pull/1").diffLink("https://github.com/baxterthehacker/public-repo/pull/1.diff")
                .sender("baxterthehacker").touchedLabel("").sourceHeadName("baxterthehacker")
                .body("This is a pretty simple change that we need to pull into master.").labels(Sets.newHashSet()).build();

        assertEquals(expectedPullRequest, service.convertWebhookJson(pullRequestPayload));
    }

    @Test
    public void testConvertWebhookJsonWithUndefinedAction() throws Exception {
        File jsonFile = new ClassPathResource("data/pull-request-webhook-payload.json").getFile();
        ObjectNode pullRequestPayload = objectMapper.readValue(jsonFile, ObjectNode.class);
        pullRequestPayload.set("action", JsonNodeFactory.instance.textNode("unknown_action"));

        PullRequest expectedPullRequest = PullRequest.builder()
                .author("baxterthehacker").number(1).title("Update the README with new information").type(PullRequestType.UNDEFINED).branchName("changes")
                .link("https://github.com/baxterthehacker/public-repo/pull/1").diffLink("https://github.com/baxterthehacker/public-repo/pull/1.diff")
                .sender("baxterthehacker").touchedLabel("").sourceHeadName("baxterthehacker")
                .body("This is a pretty simple change that we need to pull into master.").labels(Sets.newHashSet()).build();

        assertEquals(expectedPullRequest, service.convertWebhookJson(pullRequestPayload));
    }

    @Test
    public void testConvertWebhookJsonWhenClosed() throws Exception {
        File jsonFile = new ClassPathResource("data/pull-request-webhook-payload-closed.json").getFile();
        ObjectNode pullRequestPayload = objectMapper.readValue(jsonFile, ObjectNode.class);

        PullRequest expectedPullRequest = PullRequest.builder()
                .author("baxterthehacker").number(1).title("Update the README with new information").type(PullRequestType.CLOSED).branchName("changes")
                .link("https://github.com/baxterthehacker/public-repo/pull/1").diffLink("https://github.com/baxterthehacker/public-repo/pull/1.diff")
                .sender("baxterthehacker").touchedLabel("").sourceHeadName("baxterthehacker")
                .body("This is a pretty simple change that we need to pull into master.").labels(Sets.newLinkedHashSet("label")).build();

        assertEquals(expectedPullRequest, service.convertWebhookJson(pullRequestPayload));
    }

    @Test
    public void testConvertWebhookJsonWhenMerged() throws Exception {
        File jsonFile = new ClassPathResource("data/pull-request-webhook-payload-merged.json").getFile();
        ObjectNode pullRequestPayload = objectMapper.readValue(jsonFile, ObjectNode.class);

        PullRequest expectedPullRequest = PullRequest.builder()
                .author("baxterthehacker").number(1).title("Update the README with new information").type(PullRequestType.MERGED).branchName("changes")
                .link("https://github.com/baxterthehacker/public-repo/pull/1").diffLink("https://github.com/baxterthehacker/public-repo/pull/1.diff")
                .sender("baxterthehacker").touchedLabel("").sourceHeadName("baxterthehacker")
                .body("This is a pretty simple change that we need to pull into master.").labels(Sets.newLinkedHashSet("label1", "label2")).build();

        assertEquals(expectedPullRequest, service.convertWebhookJson(pullRequestPayload));
    }

    @Test
    public void testConvertWebhookJsonWhenLabeledMerged() throws Exception {
        File jsonFile = new ClassPathResource("data/pull-request-webhook-payload-labeled-merged.json").getFile();
        ObjectNode pullRequestPayload = objectMapper.readValue(jsonFile, ObjectNode.class);

        PullRequest expectedPullRequest = PullRequest.builder()
                .author("baxterthehacker").number(1).title("Update the README with new information").type(PullRequestType.UNDEFINED).branchName("changes")
                .link("https://github.com/baxterthehacker/public-repo/pull/1").diffLink("https://github.com/baxterthehacker/public-repo/pull/1.diff")
                .sender("baxterthehacker").touchedLabel("label name").type(PullRequestType.LABELED).sourceHeadName("baxterthehacker")
                .body("This is a pretty simple change that we need to pull into master.").labels(Sets.newLinkedHashSet("label")).build();

        assertEquals(expectedPullRequest, service.convertWebhookJson(pullRequestPayload));
    }
}
