package io.github.spair.handler;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.handler.command.Command;
import io.github.spair.handler.command.HandlerCommand;
import io.github.spair.service.pr.PullRequestService;
import io.github.spair.service.pr.entity.PullRequest;
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
        collectCommands(commands);
    }

    public void handle(final ObjectNode webhookJson) {
        PullRequest pullRequest = pullRequestService.convertWebhookJson(webhookJson);
        Command[] commands = wrapCommands();

        switch (pullRequest.getType()) {
            case OPENED:
                commands = wrapCommands(
                        Command.VALIDATE_CHANGELOG,
                        Command.LABEL_PR,
                        Command.REPORT_DMI_DIFF,
                        Command.REPORT_DMM_DIFF
                );
                break;
            case SYNCHRONIZE:
                commands = wrapCommands(Command.REPORT_DMI_DIFF, Command.REPORT_DMM_DIFF);
                break;
            case LABELED:
                commands = wrapCommands(Command.ADD_TEST_CHANGELOG);
                break;
            case EDITED:
                commands = wrapCommands(Command.VALIDATE_CHANGELOG);
                break;
            case MERGED:
                commands = wrapCommands(Command.UPDATE_CHANGELOG, Command.DELETE_PULL_REQUEST_REPO);
                break;
            case CLOSED:
                commands = wrapCommands(Command.DELETE_PULL_REQUEST_REPO);
                break;
        }

        executeCommands(pullRequest, commands);
    }
}
