package io.github.spair.service.git;

public interface GitHubConstants {

    String API = "https://api.github.com";

    String SIGNATURE_HEADER = "X-Hub-Signature";
    String EVENT_HEADER = "X-GitHub-Event";

    String PING_EVENT = "ping";
    String PULL_REQUEST_EVENT = "pull_request";
    String ISSUES_EVENT = "issues";
}
