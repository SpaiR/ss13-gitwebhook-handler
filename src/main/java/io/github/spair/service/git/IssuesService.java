package io.github.spair.service.git;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.service.EnumUtil;
import io.github.spair.service.git.entities.Issue;
import io.github.spair.service.git.entities.IssueType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IssuesService {

    private final GitHubService gitHubService;

    private static final String PROPOSAL_TAG = "[proposal]";
    private static final String PROPOSAL_LABEL = "Proposal";
    private static final String BUG_LABEL = "Bug";

    @Autowired
    public IssuesService(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    public Issue convertWebhookJson(ObjectNode webhookJson) {
        JsonNode issueNode = webhookJson.get(GitHubPayload.Fields.ISSUE);

        int number = issueNode.get(GitHubPayload.Fields.NUMBER).asInt();
        String title = issueNode.get(GitHubPayload.Fields.TITLE).asText();
        IssueType issueType = identifyType(webhookJson);

        return new Issue(number, title, issueType);
    }

    public void processLabels(Issue issue) {
        boolean isProposal = issue.getTitle().toLowerCase().contains(PROPOSAL_TAG);

        if (isProposal) {
            gitHubService.addLabel(issue.getNumber(), PROPOSAL_LABEL);
        } else {
            gitHubService.addLabel(issue.getNumber(), BUG_LABEL);
        }
    }

    private IssueType identifyType(ObjectNode webhookJson) {
        String action = webhookJson.get(GitHubPayload.Fields.ACTION).asText();
        return EnumUtil.valueOfOrDefault(IssueType.values(), action, IssueType.UNDEFINED);
    }
}
