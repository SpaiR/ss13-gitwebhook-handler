package io.github.spair.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.handlers.IssuesHandler;
import io.github.spair.handlers.PullRequestHandler;
import io.github.spair.services.InvalidSignatureException;
import io.github.spair.services.SignatureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
    public HandlerWebController(SignatureService signatureService,
                                PullRequestHandler pullRequestHandler,
                                IssuesHandler issuesHandler,
                                ObjectMapper objectMapper)
    {
        this.signatureService = signatureService;
        this.pullRequestHandler = pullRequestHandler;
        this.issuesHandler = issuesHandler;
        this.objectMapper = objectMapper;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void catchPullRequestWebhook(
            @RequestHeader("X-Hub-Signature") String signature,
            @RequestHeader("X-GitHub-Event") String event,
            @RequestBody String webhookPayload
    ) throws IOException {
        // Substring is to cut down 'sha1=' part.
        signatureService.validate(signature.substring(5), webhookPayload);

        ObjectNode webhookJson = objectMapper.readValue(webhookPayload, ObjectNode.class);

        switch (event) {
            case "ping":
                LOGGER.info("Ping event caught. Zen: " + webhookJson.get("zen"));
                break;
            case "pull_request":
                pullRequestHandler.handle(webhookJson);
                break;
            case "issues":
                issuesHandler.handle(webhookJson);
                break;
        }
    }

    @ExceptionHandler(InvalidSignatureException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleInvalidSignature() {
        return "Invalid signature was provided.";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Exception handleGeneralException(Exception e) {
        LOGGER.error("Uncaught exception happened", e);
        return e;
    }
}