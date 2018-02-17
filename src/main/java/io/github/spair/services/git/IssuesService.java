package io.github.spair.services.git;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.services.git.entities.Issue;
import io.github.spair.services.git.entities.IssueType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IssuesService {

    private final GitHubService gitHubService;

    @Autowired
    public IssuesService(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    public Issue convertWebhookMap(ObjectNode webhookJson) {
        int number = webhookJson.get("issue").get("number").asInt();
        String title = webhookJson.get("issue").get("title").asText();
        IssueType issueType = identifyType(webhookJson);

        return new Issue(number, title, issueType);
    }

    public void processLabels(Issue issue) {
        boolean isProposal = issue.getTitle().toLowerCase().contains("[proposal]");

        if (isProposal) {
            gitHubService.addLabel(issue.getNumber(), "Proposal");
        } else {
            gitHubService.addLabel(issue.getNumber(), "Bug");
        }
    }

    private IssueType identifyType(ObjectNode webhookJson) {
        String action = webhookJson.get("action").asText();

        switch (action) {
            case "opened":
                return IssueType.OPENED;
            default:
                return IssueType.UNDEFINED;
        }
    }
}
