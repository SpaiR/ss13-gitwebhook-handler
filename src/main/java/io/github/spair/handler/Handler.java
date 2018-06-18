package io.github.spair.handler;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface Handler {

    String PULL_REQUEST = "pullRequest";
    String ISSUE = "issue";

    void handle(ObjectNode webhookJson);
}
