package io.github.spair.handler.command;

import io.github.spair.service.git.GitHubService;
import io.github.spair.service.git.entities.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LabelIssueCommand implements HandlerCommand<Issue> {

    private static final String PROPOSAL_TAG = "[proposal]";
    private static final String PROPOSAL_LABEL = "Proposal";
    private static final String BUG_LABEL = "Bug";

    private final GitHubService gitHubService;

    @Autowired
    public LabelIssueCommand(final GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @Override
    public void execute(final Issue issue) {
        boolean isProposal = issue.getTitle().toLowerCase().contains(PROPOSAL_TAG);

        if (isProposal) {
            gitHubService.addLabel(issue.getNumber(), PROPOSAL_LABEL);
        } else {
            gitHubService.addLabel(issue.getNumber(), BUG_LABEL);
        }
    }
}
