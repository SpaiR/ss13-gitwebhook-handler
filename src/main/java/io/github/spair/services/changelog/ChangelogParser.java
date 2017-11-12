package io.github.spair.services.changelog;

import io.github.spair.services.changelog.entities.Changelog;
import io.github.spair.services.changelog.entities.ChangelogRow;
import io.github.spair.services.git.entities.PullRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
class ChangelogParser {

    private static final Pattern CHANGELOG_TEXT_PATTERN = Pattern.compile(":cl:((?:.|\\n|\\r)*+)|\uD83C\uDD91((?:.|\\n|\\r)*+)");
    private static final Pattern AUTHOR_BEFORE_CHANGES_PATTERN = Pattern.compile(".*");
    private static final Pattern CHANGELOG_ROW_WITH_CLASS_PATTERN = Pattern.compile("-\\s(\\w+)(\\[link])?:\\s(.*)");

    Changelog createFromPullRequest(PullRequest pullRequest) {
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
            sb.append(" [link:").append(prLink).append("]");
        }
    }
}
