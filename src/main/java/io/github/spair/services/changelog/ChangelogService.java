package io.github.spair.services.changelog;

import io.github.spair.services.changelog.entities.Changelog;
import io.github.spair.services.changelog.entities.ChangelogValidationStatus;
import io.github.spair.services.config.ConfigService;
import io.github.spair.services.git.GitHubService;
import io.github.spair.services.git.entities.PullRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChangelogService {

    private final GitHubService gitHubService;
    private final HtmlChangelogGenerator htmlChangelogGenerator;
    private final ChangelogValidator changelogValidator;
    private final ConfigService configService;
    private final ChangelogParser changelogParser;

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangelogService.class);

    @Autowired
    public ChangelogService(GitHubService gitHubService,
                            HtmlChangelogGenerator htmlChangelogGenerator,
                            ChangelogValidator changelogValidator,
                            ConfigService configService, ChangelogParser changelogParser)
    {
        this.gitHubService = gitHubService;
        this.htmlChangelogGenerator = htmlChangelogGenerator;
        this.changelogValidator = changelogValidator;
        this.configService = configService;
        this.changelogParser = changelogParser;
    }

    public void generateAndUpdate(PullRequest pullRequest) {
        Changelog changelog = changelogParser.createFromPullRequest(pullRequest);

        if (!changelog.isEmpty()) {
            String changelogPath = configService.getConfig().getChangelogConfig().getPathToChangelog();
            String currentChangelogHtml = gitHubService.readFile(changelogPath);
            String newChangelogHtml = htmlChangelogGenerator.generate(currentChangelogHtml, changelog);

            String updateMessage = "Automatic changelog generation for PR #" + pullRequest.getNumber();
            gitHubService.updateFile(changelogPath, updateMessage, newChangelogHtml);

            LOGGER.info("Changelog generated for PR #" + pullRequest.getNumber());
        }
    }

    public void validate(PullRequest pullRequest) {
        String invalidChangelogLabel = configService.getConfig().getGitHubConfig().getLabels().getInvalidChangelog();
        Changelog changelog = changelogParser.createFromPullRequest(pullRequest);

        boolean isValid = true;
        boolean hasInvalidLabel = gitHubService.getIssueLabels(pullRequest.getNumber()).contains(invalidChangelogLabel);

        if (!changelog.isEmpty()) {
            ChangelogValidationStatus validationStatus = changelogValidator.validate(changelog);

            if (validationStatus.getStatus() == ChangelogValidationStatus.Status.INVALID) {
                isValid = false;

                if (!hasInvalidLabel) {
                    String message = "**Warning!** Invalid changelog detected.\n\n" + validationStatus.getMessage();
                    gitHubService.addReviewComment(pullRequest.getNumber(), message);
                    gitHubService.addLabel(pullRequest.getNumber(), invalidChangelogLabel);
                    return;
                }
            }
        }

        if (hasInvalidLabel && isValid) {
            gitHubService.removeLabel(pullRequest.getNumber(), invalidChangelogLabel);
        }
    }

    public List<String> getChangelogClassesList(PullRequest pullRequest) {
        Changelog changelog = changelogParser.createFromPullRequest(pullRequest);

        if (!changelog.isEmpty()) {
            List<String> changelogClasses = new ArrayList<>();
            changelog.getChangelogRows().forEach(row -> changelogClasses.add(row.getClassName()));
            return changelogClasses;
        }

        return new ArrayList<>();
    }
}