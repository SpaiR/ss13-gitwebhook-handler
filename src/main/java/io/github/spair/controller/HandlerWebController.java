package io.github.spair.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.handler.IssuesHandler;
import io.github.spair.handler.PullRequestHandler;
import io.github.spair.service.InvalidSignatureException;
import io.github.spair.service.SignatureService;
import io.github.spair.service.git.GitHubConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;

@RestController
@RequestMapping("/handler")
public class HandlerWebController {

    private final SignatureService signatureService;
    private final PullRequestHandler pullRequestHandler;
    private final IssuesHandler issuesHandler;
    private final ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerWebController.class);

    @Autowired
    public HandlerWebController(final SignatureService signatureService, final ObjectMapper objectMapper,
                                final PullRequestHandler pullRequestHandler, final IssuesHandler issuesHandler) {
        this.signatureService = signatureService;
        this.pullRequestHandler = pullRequestHandler;
        this.issuesHandler = issuesHandler;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void catchPullRequestWebhook(
            @RequestHeader(GitHubConstants.SIGNATURE_HEADER) final String signature,
            @RequestHeader(GitHubConstants.EVENT_HEADER) final String event,
            @RequestBody final String webhookPayload) throws IOException {
        // Substring is to cut down 'sha1=' part.
        signatureService.validate(sanitizeSignature(signature), webhookPayload);

        final ObjectNode webhookJson = objectMapper.readValue(webhookPayload, ObjectNode.class);

        switch (event) {
            case GitHubConstants.PING_EVENT:
                LOGGER.info("Ping event caught. Zen: " + webhookJson.get("zen"));
                break;
            case GitHubConstants.PULL_REQUEST_EVENT:
                pullRequestHandler.handle(webhookJson);
                break;
            case GitHubConstants.ISSUES_EVENT:
                issuesHandler.handle(webhookJson);
                break;
            default:
                LOGGER.info("Unhandled event caught: " + event);
        }
    }

    @ExceptionHandler(InvalidSignatureException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleInvalidSignature() {
        return "Invalid signature was provided";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Exception handleGeneralException(final Exception e) {
        LOGGER.error("Uncaught exception happened", e);
        return e;
    }

    // Signature header from GitHub always starts with 'sha1=' part, which should be cut down.
    private String sanitizeSignature(final String sign) {
        return sign.substring("sha1=".length());
    }
}
