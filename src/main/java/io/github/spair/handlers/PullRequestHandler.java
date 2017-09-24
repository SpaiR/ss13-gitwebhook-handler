package io.github.spair.handlers;

import io.github.spair.entities.PullRequest;
import io.github.spair.services.PullRequestService;
import io.github.spair.services.changelog.ChangelogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class PullRequestHandler {

    private final PullRequestService pullRequestService;
    private final ChangelogService changelogService;

    @Autowired
    public PullRequestHandler(PullRequestService pullRequestService, ChangelogService changelogService) {
        this.pullRequestService = pullRequestService;
        this.changelogService = changelogService;
    }

    public void handle(HashMap webhook) {
        PullRequest pullRequest = pullRequestService.convertWebhookMap(webhook);

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
