package io.github.spair.handler.command.changelog;

import io.github.spair.handler.command.HandlerCommand;
import io.github.spair.handler.command.PullRequestHelper;
import io.github.spair.service.changelog.ChangelogService;
import io.github.spair.service.changelog.entity.Changelog;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.github.GitHubService;
import io.github.spair.service.pr.entity.PullRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AddTestChangelogCommand implements HandlerCommand<PullRequest> {

    private final ConfigService configService;
    private final ChangelogService changelogService;
    private final GitHubService gitHubService;

    @Autowired
    public AddTestChangelogCommand(final ConfigService configService,
                                   final ChangelogService changelogService,
                                   final GitHubService gitHubService) {
        this.configService = configService;
        this.changelogService = changelogService;
        this.gitHubService = gitHubService;
    }

    @Override
    public void execute(final PullRequest pullRequest) {
        if (!PullRequestHelper.checkPRForTestChangelog(pullRequest, configService.getConfig())) {
            return;
        }

        Optional<Changelog> changelogOpt = changelogService.createFromPullRequest(pullRequest);

        changelogOpt.ifPresent(changelog -> {
            if (!changelog.isEmpty()) {
                String changelogPath = configService.getConfig().getChangelogConfig().getPathToChangelog();
                String currentChangelogHtml = gitHubService.readDecodedFile(changelogPath);
                String newChangelogHtml = changelogService.addTestChangelogToHtml(currentChangelogHtml, changelog);

                String updateMessage = "Add test merge changelog for PR #" + pullRequest.getNumber();
                gitHubService.updateFile(changelogPath, updateMessage, newChangelogHtml);
            }
        });
    }
}
