package io.github.spair.services;

import io.github.spair.entities.Issue;
import io.github.spair.entities.IssueType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class IssuesService {

    private final GitHubService gitHubService;

    @Autowired
    public IssuesService(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    public Issue convertWebhookMap(HashMap webhook) {
        int number = (int) ((HashMap) webhook.get("issue")).get("number");
        String title = (String) ((HashMap) webhook.get("issue")).get("title");
        IssueType issueType = identifyType(webhook);

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

    private IssueType identifyType(HashMap webhook) {
        String action = (String) webhook.get("action");

        switch (action) {
            case "opened":
                return IssueType.OPENED;
            default:
                return IssueType.UNDEFINED;
        }
    }
}
