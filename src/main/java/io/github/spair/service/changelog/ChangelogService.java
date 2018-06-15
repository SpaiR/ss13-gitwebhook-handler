package io.github.spair.service.changelog;

import io.github.spair.aspect.ExceptionSuppress;
import io.github.spair.service.changelog.entities.Changelog;
import io.github.spair.service.changelog.entities.ChangelogRow;
import io.github.spair.service.changelog.entities.ChangelogValidationStatus;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.git.GitHubService;
import io.github.spair.service.git.entities.PullRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChangelogService {

    private final GitHubService gitHubService;
    private final HtmlChangelogGenerator htmlChangelogGenerator;
    private final ChangelogValidator changelogValidator;
    private final ConfigService configService;
    private final ChangelogGenerator changelogGenerator;

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangelogService.class);

    @Autowired
    public ChangelogService(final GitHubService gitHubService,
                            final HtmlChangelogGenerator htmlChangelogGenerator,
                            final ChangelogValidator changelogValidator,
                            final ConfigService configService,
                            final ChangelogGenerator changelogGenerator) {
        this.gitHubService = gitHubService;
        this.htmlChangelogGenerator = htmlChangelogGenerator;
        this.changelogValidator = changelogValidator;
        this.configService = configService;
        this.changelogGenerator = changelogGenerator;
    }

    @ExceptionSuppress
    public void generateAndUpdate(final PullRequest pullRequest) {
        Changelog changelog = changelogGenerator.generate(pullRequest);

        if (!changelog.isEmpty()) {
            String changelogPath = configService.getConfig().getChangelogConfig().getPathToChangelog();
            String currentChangelogHtml = gitHubService.readDecodedFile(changelogPath);
            String newChangelogHtml = htmlChangelogGenerator.generate(currentChangelogHtml, changelog);

            String updateMessage = "Automatic changelog generation for PR #" + pullRequest.getNumber();
            gitHubService.updateFile(changelogPath, updateMessage, newChangelogHtml);

            LOGGER.info("Changelog generated for PR #" + pullRequest.getNumber());
        }
    }

    @ExceptionSuppress
    public void validate(final PullRequest pullRequest) {
        String invalidChangelogLabel = configService.getConfig().getGitHubConfig().getLabels().getInvalidChangelog();
        Changelog changelog = changelogGenerator.generate(pullRequest);
        int prNumber = pullRequest.getNumber();

        boolean isValid = true;
        boolean hasInvalidLabel = gitHubService.listIssueLabels(prNumber).contains(invalidChangelogLabel);

        if (Objects.nonNull(changelog.getChangelogRows())) {
            ChangelogValidationStatus validationStatus = changelogValidator.validate(changelog);

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

    public Set<String> getChangelogClassesList(final PullRequest pullRequest) {
        Changelog changelog = changelogGenerator.generate(pullRequest);

        if (changelog.isEmpty()) {
            return Collections.emptySet();
        } else {
            return changelog.getChangelogRows().stream().map(ChangelogRow::getClassName).collect(Collectors.toSet());
        }
    }
}
