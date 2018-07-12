package io.github.spair.handler.command.changelog;

import io.github.spair.handler.command.HandlerCommand;
import io.github.spair.service.changelog.ChangelogService;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.github.GitHubService;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.service.pr.entity.PullRequestType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RemoveTestChangelogCommand implements HandlerCommand<PullRequest> {

    private final ConfigService configService;
    private final ChangelogService changelogService;
    private final GitHubService gitHubService;

    @Autowired
    public RemoveTestChangelogCommand(final ConfigService configService,
                                      final ChangelogService changelogService,
                                      final GitHubService gitHubService) {
        this.configService = configService;
        this.changelogService = changelogService;
        this.gitHubService = gitHubService;
    }

    @Override
    public void execute(final PullRequest pullRequest) {
        if (!checkPullRequest(pullRequest)) {
            return;
        }

        int prNumber = pullRequest.getNumber();
        String changelogPath = configService.getConfig().getChangelogConfig().getPathToChangelog();
        String currentChangelogHtml = gitHubService.readDecodedFile(changelogPath);
        String newChangelogHtml = changelogService.removeTestChangelogFromHtml(currentChangelogHtml, prNumber);

        if (currentChangelogHtml.equals(newChangelogHtml)) {
            return;
        }

        String updateMessage = "Remove test merge changelog for PR #" + pullRequest.getNumber();
        gitHubService.updateFile(changelogPath, updateMessage, newChangelogHtml);

        String testMergeLabel = configService.getConfig().getLabels().getTestMerge();
        if (pullRequest.getLabels().contains(testMergeLabel)) {
            gitHubService.removeLabel(prNumber, testMergeLabel);
        }
    }

    private boolean checkPullRequest(final PullRequest pullRequest) {
        if (pullRequest.getType() == PullRequestType.UNLABELED) {
            return PullRequestHelper.checkPRForTestChangelog(pullRequest, configService.getConfig());
        }
        return pullRequest.getType() == PullRequestType.CLOSED || pullRequest.getType() == PullRequestType.MERGED;
    }
}
