package io.github.spair.controllers;

import io.github.spair.handlers.PullRequestHandler;
import io.github.spair.services.SignatureService;
import io.github.spair.services.WebhookService;
import io.github.spair.services.exceptions.InvalidSignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/handler")
public class HandlerWebController {

    private final SignatureService signatureService;
    private final WebhookService webhookService;
    private final PullRequestHandler pullRequestHandler;

    @Autowired
    public HandlerWebController(SignatureService signatureService, WebhookService webhookService, PullRequestHandler pullRequestHandler) {
        this.signatureService = signatureService;
        this.webhookService = webhookService;
        this.pullRequestHandler = pullRequestHandler;
    }

    @PostMapping(path = "/pull_request", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void catchPullRequestWebhook(
            @RequestHeader("X-Hub-Signature") String signature,
            @RequestHeader("X-GitHub-Event") String event,
            @RequestBody String webhookPayload
    ) {
        // Substring is to cut down 'sha1=' part.
        signatureService.validate(signature.substring(5), webhookPayload);

        if (event.equals("pull_request")) {
            pullRequestHandler.handle(webhookService.convertWebhookToMap(webhookPayload));
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
        return e;
    }
}