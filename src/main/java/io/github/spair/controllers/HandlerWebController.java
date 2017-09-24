package io.github.spair.controllers;

import io.github.spair.handlers.IssuesHandler;
import io.github.spair.handlers.PullRequestHandler;
import io.github.spair.services.SignatureService;
import io.github.spair.services.WebhookService;
import io.github.spair.services.exceptions.InvalidSignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/handler")
public class HandlerWebController {

    private final SignatureService signatureService;
    private final WebhookService webhookService;
    private final PullRequestHandler pullRequestHandler;
    private final IssuesHandler issuesHandler;

    private static final Logger LOGGER = LoggerFactory.getLogger(HandlerWebController.class);

    @Autowired
    public HandlerWebController(SignatureService signatureService,
                                WebhookService webhookService,
                                PullRequestHandler pullRequestHandler,
                                IssuesHandler issuesHandler)
    {
        this.signatureService = signatureService;
        this.webhookService = webhookService;
        this.pullRequestHandler = pullRequestHandler;
        this.issuesHandler = issuesHandler;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void catchPullRequestWebhook(
            @RequestHeader("X-Hub-Signature") String signature,
            @RequestHeader("X-GitHub-Event") String event,
            @RequestBody String webhookPayload
    ) {
        // Substring is to cut down 'sha1=' part.
        signatureService.validate(signature.substring(5), webhookPayload);

        HashMap webhookMap = webhookService.convertWebhookToMap(webhookPayload);

        switch (event) {
            case "ping":
                LOGGER.info("Ping event caught. Zen: " + webhookMap.get("zen"));
                break;
            case "pull_request":
                pullRequestHandler.handle(webhookMap);
                break;
            case "issues":
                issuesHandler.handle(webhookMap);
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