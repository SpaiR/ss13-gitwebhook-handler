package io.github.spair.handler.command.diff;

import io.github.spair.handler.command.HandlerCommand;
import io.github.spair.service.github.GitHubRepository;
import io.github.spair.service.pr.entity.PullRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeletePullRequestRepoCommand implements HandlerCommand<PullRequest> {

    private final GitHubRepository gitHubRepository;

    @Autowired
    public DeletePullRequestRepoCommand(final GitHubRepository gitHubRepository) {
        this.gitHubRepository = gitHubRepository;
    }

    @Override
    public void execute(final PullRequest pullRequest) {
        gitHubRepository.deleteForkRepository(pullRequest);
    }
}
