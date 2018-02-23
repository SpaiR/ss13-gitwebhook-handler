package io.github.spair.service.git;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.service.git.entities.Issue;
import io.github.spair.service.git.entities.IssueType;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class IssuesServiceTest {

    private IssuesService service;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        service = new IssuesService(mock(GitHubService.class));
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testConvertWebhookJson() throws Exception {
        File jsonFile = new ClassPathResource("issue-webhook-payload.json").getFile();
        ObjectNode issuePayload = objectMapper.readValue(jsonFile, ObjectNode.class);

        Issue expectedIssue = new Issue(2, "Spelling error in the README file", IssueType.OPENED);

        assertEquals(expectedIssue, service.convertWebhookJson(issuePayload));
    }

    @Test
    public void testConvertWebhookJsonWithUndefined() throws Exception {
        File jsonFile = new ClassPathResource("issue-webhook-payload.json").getFile();
        ObjectNode issuePayload = objectMapper.readValue(jsonFile, ObjectNode.class);
        issuePayload.set("action", JsonNodeFactory.instance.textNode("unknown_action"));

        Issue expectedIssue = new Issue(2, "Spelling error in the README file", IssueType.UNDEFINED);

        assertEquals(expectedIssue, service.convertWebhookJson(issuePayload));
    }
}