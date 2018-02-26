package io.github.spair.service.git;

public interface GitHubPayload {

    interface Fields {
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
        String NAME = "name";
    }

    interface Values {
        String COMMENT = "COMMENT";
    }
}
