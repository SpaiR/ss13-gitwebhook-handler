package io.github.spair.handlers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.services.changelog.ChangelogService;
import io.github.spair.services.git.PullRequestService;
import io.github.spair.services.git.entities.PullRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PullRequestHandler {

    private final PullRequestService pullRequestService;
    private final ChangelogService changelogService;

    @Autowired
    public PullRequestHandler(PullRequestService pullRequestService, ChangelogService changelogService) {
        this.pullRequestService = pullRequestService;
        this.changelogService = changelogService;
    }

    public void handle(ObjectNode webhookJson) {
        PullRequest pullRequest = pullRequestService.convertWebhookMap(webhookJson);

        switch (pullRequest.getType()) {
            case OPENED:
                changelogService.validate(pullRequest);
                pullRequestService.processLabels(pullRequest);
                break;
            case EDITED:
                changelogService.validate(pullRequest);
                break;
            case MERGED:
                changelogService.generateAndUpdate(pullRequest);
                break;
        }
    }
}
