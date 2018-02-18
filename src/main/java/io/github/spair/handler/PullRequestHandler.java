package io.github.spair.handler;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.service.changelog.ChangelogService;
import io.github.spair.service.git.PullRequestService;
import io.github.spair.service.git.entities.PullRequest;
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
        PullRequest pullRequest = pullRequestService.convertWebhookJson(webhookJson);

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
