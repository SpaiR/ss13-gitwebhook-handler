package io.github.spair.handler.command;

import io.github.spair.service.changelog.ChangelogService;
import io.github.spair.service.changelog.entity.Changelog;
import io.github.spair.service.changelog.entity.ChangelogValidationStatus;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.git.GitHubService;
import io.github.spair.service.pr.entity.PullRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ValidateChangelogCommand implements HandlerCommand<PullRequest> {

    private final ConfigService configService;
    private final ChangelogService changelogService;
    private final GitHubService gitHubService;

    @Autowired
    public ValidateChangelogCommand(
            final ConfigService configService,
            final ChangelogService changelogService,
            final GitHubService gitHubService) {
        this.configService = configService;
        this.changelogService = changelogService;
        this.gitHubService = gitHubService;
    }

    @Override
    public void execute(final PullRequest pullRequest) {
        String invalidChangelogLabel = configService.getConfig().getGitHubConfig().getLabels().getInvalidChangelog();
        Optional<Changelog> changelog = changelogService.createFromPullRequest(pullRequest);
        int prNumber = pullRequest.getNumber();

        boolean isValid = true;
        boolean hasInvalidLabel = gitHubService.listIssueLabels(prNumber).contains(invalidChangelogLabel);

        if (changelog.isPresent()) {
            ChangelogValidationStatus validationStatus = changelogService.validateChangelog(changelog.get());

            if (validationStatus.getStatus() == ChangelogValidationStatus.Status.INVALID) {
                isValid = false;

                if (!hasInvalidLabel) {
                    String message = "**Warning!** Invalid changelog detected.\n\n" + validationStatus.getMessage();
                    gitHubService.createIssueComment(prNumber, message);
                    gitHubService.addLabel(prNumber, invalidChangelogLabel);
                    return;
                }
            }
        }

        if (hasInvalidLabel && isValid) {
            gitHubService.removeLabel(prNumber, invalidChangelogLabel);
        }
    }
}
