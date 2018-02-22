package io.github.spair.service.git;

public interface GitHubPayloadFields {

    String ISSUE = "issue";
    String PULL_REQUEST = "pull_request";

    String CONTENT = "content";
    String SHA = "sha";
    String MESSAGE = "message";
    String PATH = "path";
    String EVENT = "event";
    String BODY = "body";
    String TITLE = "title";
    String NUMBER = "number";
    String ACTION = "action";
    String USER = "user";
    String LOGIN = "login";
    String HTML_URL = "html_url";
    String DIFF_URL = "diff_url";
    String MERGED = "merged";
    String NAME = "name";

    interface Actions {

        String OPENED = "opened";
        String EDITED = "edited";
        String CLOSED = "closed";
    }
}
