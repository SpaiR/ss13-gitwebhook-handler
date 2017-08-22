package io.github.spair.services;

import io.github.spair.entities.PullRequest;
import io.github.spair.entities.PullRequestType;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class PullRequestService {

    public PullRequest convertWebhookMap(HashMap webhook) {
        String author = (String) ((HashMap) ((HashMap) webhook.get("pull_request")).get("user")).get("login");
        int number = (int) ((HashMap) webhook.get("pull_request")).get("number");
        PullRequestType type = identifyType(webhook);
        String link = (String) (((HashMap) webhook.get("pull_request")).get("html_url"));
        String body = (String) ((HashMap) webhook.get("pull_request")).get("body");

        return new PullRequest(author, number, type, link, body);
    }

    private PullRequestType identifyType(HashMap webhook) {
        String action = (String) webhook.get("action");

        switch (action) {
            case "opened":
                return PullRequestType.OPENED;
            case "edited":
                return PullRequestType.EDITED;
            case "closed":
                boolean isMerged = ((boolean)((HashMap) webhook.get("pull_request")).get("merged"));
                if (isMerged) {
                    return PullRequestType.MERGED;
                }
            default:
                return PullRequestType.UNDEFINED;
        }
    }
}
