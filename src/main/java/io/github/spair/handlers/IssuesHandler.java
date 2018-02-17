package io.github.spair.handlers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.services.git.IssuesService;
import io.github.spair.services.git.entities.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IssuesHandler {

    private final IssuesService issuesService;

    @Autowired
    public IssuesHandler(IssuesService issuesService) {
        this.issuesService = issuesService;
    }

    public void handle(ObjectNode webhookJson) {
        Issue issue = issuesService.convertWebhookMap(webhookJson);

        switch (issue.getType()) {
            case OPENED:
                issuesService.processLabels(issue);
                break;
        }
    }
}
