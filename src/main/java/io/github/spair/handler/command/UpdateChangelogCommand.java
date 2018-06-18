package io.github.spair.handler.command;

import io.github.spair.service.changelog.ChangelogService;
import io.github.spair.service.changelog.entities.Changelog;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.git.GitHubService;
import io.github.spair.service.git.entities.PullRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateChangelogCommand implements HandlerCommand<PullRequest> {

    private final ChangelogService changelogService;
    private final ConfigService configService;
    private final GitHubService gitHubService;

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateChangelogCommand.class);

    @Autowired
    public UpdateChangelogCommand(
            final ChangelogService changelogService,
            final ConfigService configService,
            final GitHubService gitHubService) {
        this.changelogService = changelogService;
        this.configService = configService;
        this.gitHubService = gitHubService;
    }

    @Override
    public void execute(final PullRequest pullRequest) {
        Changelog changelog = changelogService.createFromPullRequest(pullRequest);

        if (!changelog.isEmpty()) {
            String changelogPath = configService.getConfig().getChangelogConfig().getPathToChangelog();
            String currentChangelogHtml = gitHubService.readDecodedFile(changelogPath);
            String newChangelogHtml = changelogService.mergeHtmlWithChangelog(currentChangelogHtml, changelog);

            String updateMessage = "Automatic changelog generation for PR #" + pullRequest.getNumber();
            gitHubService.updateFile(changelogPath, updateMessage, newChangelogHtml);

            LOGGER.info("Changelog generated for PR #" + pullRequest.getNumber());
        }
    }
}
