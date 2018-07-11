package io.github.spair.service.changelog;

import io.github.spair.service.changelog.entity.Changelog;
import io.github.spair.service.changelog.entity.ChangelogRow;
import io.github.spair.service.pr.entity.PullRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
final class ChangelogGenerator {

    private static final Pattern CL_TEXT = Pattern.compile(":cl:((?:.|\\n|\\r)*+)|\uD83C\uDD91((?:.|\\n|\\r)*+)");
    private static final Pattern AUTHOR_BEFORE_CHANGES = Pattern.compile(".*");
    private static final Pattern CHANGELOG_ROW_WITH_CLASS = Pattern.compile("-\\s(\\w+)(\\[link])?:\\s(.*)");

    Optional<Changelog> generate(final PullRequest pullRequest) {
        Changelog changelog = null;
        String changelogText = findChangelogText(Optional.ofNullable(pullRequest.getBody()).orElse(""));

        if (!changelogText.isEmpty()) {
            changelog = parseChangelog(changelogText);
            changelog.setPullRequestLink(pullRequest.getLink());
            changelog.setPullRequestNumber(pullRequest.getNumber());
            prepareChangelog(changelog, pullRequest);
        }

        return Optional.ofNullable(changelog);
    }

    private String findChangelogText(final String prBody) {
        String changelogWithoutComments = prBody.replaceAll("(?s)<!--.*?-->", "");
        Matcher matcher = CL_TEXT.matcher(changelogWithoutComments);

        if (matcher.find()) {
            return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);  // 1 - :cl:, 2 - 🆑
        } else {
            return "";
        }
    }

    private Changelog parseChangelog(final String changelogText) {
        Changelog changelog = new Changelog();

        changelog.setAuthor(parseAuthor(changelogText));
        changelog.setChangelogRows(parseChangelogRows(changelogText));

        return changelog;
    }

    private String parseAuthor(final String changelogText) {
        Matcher matcher = AUTHOR_BEFORE_CHANGES.matcher(changelogText);

        if (matcher.find()) {
            return matcher.group().trim();
        } else {
            return "";
        }
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    private List<ChangelogRow> parseChangelogRows(final String changelogText) {
        List<ChangelogRow> changelogRows = new ArrayList<>();

        try (Scanner scanner = new Scanner(changelogText)) {
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                Matcher matcher = CHANGELOG_ROW_WITH_CLASS.matcher(line);

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

    private void prepareChangelog(final Changelog changelog, final PullRequest pullRequest) {
        ensureAuthorExist(changelog, pullRequest.getAuthor());

        changelog.getChangelogRows().forEach(changelogRow -> {
            StringBuilder sb = new StringBuilder(changelogRow.getChanges().trim());

            capitalizeFirstLetter(sb);
            ensureDotEnd(sb);

            if (changelogRow.isHasLink()) {
                addPullRequestLink(sb, pullRequest.getLink());
            }

            changelogRow.setChanges(sb.toString());
        });
    }

    private void ensureAuthorExist(final Changelog changelog, final String authorName) {
        if (changelog.getAuthor().isEmpty()) {
            changelog.setAuthor(authorName);
        }
    }

    private void capitalizeFirstLetter(final StringBuilder sb) {
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
    }

    private void ensureDotEnd(final StringBuilder sb) {
        char lastChar = sb.charAt(sb.length() - 1);

        if (lastChar != '.' && lastChar != '?' && lastChar != '!') {
            sb.append('.');
        }
    }

    private void addPullRequestLink(final StringBuilder sb, final String prLink) {
        sb.append(" [link:").append(prLink).append("]");
    }
}
