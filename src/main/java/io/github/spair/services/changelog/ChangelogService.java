package io.github.spair.services.changelog;

import io.github.spair.entities.Changelog;
import io.github.spair.entities.ChangelogRow;
import io.github.spair.entities.PullRequest;
import io.github.spair.services.ConfigService;
import io.github.spair.services.GitHubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ChangelogService {

    private final GitHubService gitHubService;
    private final HtmlChangelogGenerator htmlChangelogGenerator;
    private final ChangelogValidator changelogValidator;
    private final ConfigService configService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangelogService.class);

    private static final Pattern CHANGELOG_TEXT_PATTERN = Pattern.compile(":cl:((?:.|\\n|\\r)*+)|\uD83C\uDD91((?:.|\\n|\\r)*+)");
    private static final Pattern AUTHOR_BEFORE_CHANGES_PATTERN = Pattern.compile(".*");
    private static final Pattern CHANGELOG_ROW_WITH_CLASS_PATTERN = Pattern.compile("-\\s(\\w+)(\\[link])?:\\s(.*)");

    private static final String INVALID_CHANGELOG_LABEL = "Invalid Changelog";

    @Autowired
    public ChangelogService(GitHubService gitHubService, HtmlChangelogGenerator htmlChangelogGenerator, ChangelogValidator changelogValidator, ConfigService configService) {
        this.gitHubService = gitHubService;
        this.htmlChangelogGenerator = htmlChangelogGenerator;
        this.changelogValidator = changelogValidator;
        this.configService = configService;
    }

    public void generateAndUpdate(PullRequest pullRequest) {
        Changelog changelog = findAndCreate(pullRequest);

        if (changelog != null) {
            String changelogPath = configService.getChangelogConfig().getPathToChangelog();
            String currentChangelogHtml = gitHubService.readFile(changelogPath);
            String newChangelogHtml = htmlChangelogGenerator.generate(currentChangelogHtml, changelog);

            String updateMessage = "Automatic changelog generation for PR #" + pullRequest.getNumber();
            gitHubService.updateFile(changelogPath, updateMessage, newChangelogHtml);

            LOGGER.info("Changelog generated for PR #" + pullRequest.getNumber());
        }
    }

    public void validate(PullRequest pullRequest) {
        Changelog changelog = findAndCreate(pullRequest);

        boolean isValid = true;
        boolean hasInvalidLabel = gitHubService.getIssueLabels(pullRequest.getNumber()).contains(INVALID_CHANGELOG_LABEL);

        if (changelog != null) {
            ChangelogValidationStatus validationStatus = changelogValidator.validate(changelog);

            if (validationStatus.getStatus() == ChangelogValidationStatus.Status.INVALID) {
                isValid = false;

                if (!hasInvalidLabel) {
                    String message = "**Warning!** Invalid changelog detected.\n\n" + validationStatus.getMessage();
                    gitHubService.createReview(pullRequest.getNumber(), message);
                    gitHubService.addLabel(pullRequest.getNumber(), INVALID_CHANGELOG_LABEL);
                    return;
                }
            }
        }

        if (hasInvalidLabel && isValid) {
            gitHubService.removeLabel(pullRequest.getNumber(), INVALID_CHANGELOG_LABEL);
        }
    }

    private Changelog findAndCreate(PullRequest pullRequest) {
        Changelog changelog = null;
        String changelogText = findChangelogText(pullRequest.getBody());

        if (changelogText != null) {
            changelog = parseChangelog(changelogText);
            prepareChangelog(changelog, pullRequest);
        }

        return changelog;
    }

    private String findChangelogText(String prBody) {
        String changelogText = null;

        String changelogWithoutComments = prBody.replaceAll("(?s)<!--.*?-->", "");
        Matcher matcher = CHANGELOG_TEXT_PATTERN.matcher(changelogWithoutComments);

        if (matcher.find()) {
            changelogText = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);  // 1 - :cl:, 2 - 🆑
        }

        return changelogText;
    }

    private Changelog parseChangelog(String changelogText) {
        Changelog changelog = new Changelog();

        changelog.setAuthor(parseAuthor(changelogText));
        changelog.setChangelogRows(parseChangelogRows(changelogText));

        return changelog;
    }

    private String parseAuthor(String changelogText) {
        String author = "";

        Matcher matcher = AUTHOR_BEFORE_CHANGES_PATTERN.matcher(changelogText);

        if (matcher.find()) {
            author = matcher.group().trim();
        }

        return author.length() > 0 ? author : null;
    }

    private List<ChangelogRow> parseChangelogRows(String changelogText) {
        List<ChangelogRow> changelogRows = new ArrayList<>();

        try (Scanner scanner = new Scanner(changelogText)) {
            while (scanner.hasNext()) {
                String line = scanner.nextLine();

                Matcher matcher = CHANGELOG_ROW_WITH_CLASS_PATTERN.matcher(line);

                if (matcher.find()) {
                    ChangelogRow changelogRow = new ChangelogRow();

                    changelogRow.setClassName(matcher.group(1));
                    changelogRow.setHasLink(matcher.group(2) != null);
                    changelogRow.setChanges(matcher.group(3));

                    changelogRows.add(changelogRow);
                }
            }
        }

        return changelogRows;
    }

    private void prepareChangelog(Changelog changelog, PullRequest pullRequest) {
        ensureAuthorExist(changelog, pullRequest.getAuthor());

        changelog.getChangelogRows().forEach(changelogRow -> {
            String changes = changelogRow.getChanges().trim();
            StringBuilder sb = new StringBuilder();

            capitalizeFirstLetter(sb, changes);
            ensureDotEnd(sb);
            addPullRequestLink(changelogRow.isHasLink(), sb, pullRequest.getLink());

            changelogRow.setChanges(sb.toString());
        });
    }

    private void ensureAuthorExist(Changelog changelog, String authorName) {
        if (changelog.getAuthor() == null) {
            changelog.setAuthor(authorName);
        }
    }

    private void capitalizeFirstLetter(StringBuilder sb, String changes) {
        sb.append(changes.substring(0, 1).toUpperCase()).append(changes.substring(1));
    }

    private void ensureDotEnd(StringBuilder sb) {
        String lastChar = sb.substring(sb.length() - 1);

        if (!lastChar.equals(".") && !lastChar.equals("?") && !lastChar.equals("!")) {
            sb.append(".");
        }
    }

    private void addPullRequestLink(boolean hasLink, StringBuilder sb, String prLink) {
        if (hasLink) {
            String moreText = configService.getChangelogConfig().getHtml().getMoreText();
            sb.append(" <a href=\"").append(prLink).append("\">- ").append(moreText).append(" -</a>");
        }
    }
}