package io.github.spair.handler;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface Handler {

    void handle(ObjectNode webhookJson);
}
