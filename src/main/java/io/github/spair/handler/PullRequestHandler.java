package io.github.spair.handler;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.handler.command.Command;
import io.github.spair.handler.command.HandlerCommand;
import io.github.spair.service.git.PullRequestService;
import io.github.spair.service.git.entities.PullRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component(Handler.PULL_REQUEST)
public class PullRequestHandler extends AbstractHandler<PullRequest> implements Handler {

    private final PullRequestService pullRequestService;

    @Autowired
    public PullRequestHandler(
            final PullRequestService pullRequestService, final Set<HandlerCommand<PullRequest>> commands) {
        this.pullRequestService = pullRequestService;
        this.commands = filterCommands(commands);
    }

    public void handle(final ObjectNode webhookJson) {
        PullRequest pullRequest = pullRequestService.convertWebhookJson(webhookJson);
        Command[] commands = wrapCommands();

        switch (pullRequest.getType()) {
            case OPENED:
                commands = wrapCommands(Command.VALIDATE_CHANGELOG, Command.LABEL_PR, Command.REPORT_DMI);
                break;
            case SYNCHRONIZE:
                commands = wrapCommands(Command.REPORT_DMI);
                break;
            case EDITED:
                commands = wrapCommands(Command.VALIDATE_CHANGELOG);
                break;
            case MERGED:
                commands = wrapCommands(Command.UPDATE_CHANGELOG);
                break;
        }

        executeCommands(pullRequest, commands);
    }
}
