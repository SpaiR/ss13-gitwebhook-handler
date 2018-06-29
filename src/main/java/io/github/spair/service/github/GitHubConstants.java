package io.github.spair.service.github;

@SuppressWarnings("WeakerAccess")
public final class GitHubConstants {

    public static final String PATH = "https://github.com";
    public static final String API = "https://api.github.com";

    public static final String SIGNATURE_HEADER = "X-Hub-Signature";
    public static final String EVENT_HEADER = "X-GitHub-Event";

    public static final String PING_EVENT = "ping";
    public static final String PULL_REQUEST_EVENT = "pull_request";
    public static final String ISSUES_EVENT = "issues";

    private GitHubConstants() {
    }
}
