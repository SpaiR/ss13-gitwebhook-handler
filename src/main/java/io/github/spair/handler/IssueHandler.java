package io.github.spair.handler;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.handler.command.HandlerCommand;
import io.github.spair.handler.command.Command;
import io.github.spair.service.issue.IssueService;
import io.github.spair.service.issue.entity.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component(Handler.ISSUE)
public class IssueHandler extends AbstractHandler<Issue> implements Handler {

    private final IssueService issueService;

    @Autowired
    public IssueHandler(final IssueService issueService, final Set<HandlerCommand<Issue>> commands) {
        this.issueService = issueService;
        collectCommands(commands);
    }

    @Override
    public void handle(final ObjectNode webhookJson) {
        Issue issue = issueService.convertWebhookJson(webhookJson);

        switch (issue.getType()) {
            case OPENED:
                executeCommands(issue, Command.LABEL_ISSUE);
                break;
        }
    }
}
