package io.github.spair.handler.command.diff;

import io.github.spair.handler.command.HandlerCommand;
import io.github.spair.handler.command.PullRequestHelper;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.github.GitHubCommentService;
import io.github.spair.service.idmap.IDMapService;
import io.github.spair.service.pr.entity.PullRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeleteIDMapCommand implements HandlerCommand<PullRequest> {

    private final ConfigService configService;
    private final IDMapService idMapService;
    private final GitHubCommentService gitHubCommentService;

    @Autowired
    public DeleteIDMapCommand(
            final ConfigService configService,
            final IDMapService idMapService,
            final GitHubCommentService gitHubCommentService) {
        this.configService = configService;
        this.idMapService = idMapService;
        this.gitHubCommentService = gitHubCommentService;
    }

    @Override
    public void execute(final PullRequest pullRequest) {
        if (PullRequestHelper.checkForIDMap(pullRequest, configService.getConfig())) {
            idMapService.deletePrMapFolder(pullRequest.getNumber());
            gitHubCommentService.removeCommentWithId(pullRequest.getNumber(), BuildIDMapCommand.TITLE);
        }
    }
}
