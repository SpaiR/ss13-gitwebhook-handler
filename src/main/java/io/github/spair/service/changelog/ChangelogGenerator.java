package io.github.spair.service.changelog;

import io.github.spair.service.DataGenerator;
import io.github.spair.service.changelog.entities.Changelog;
import io.github.spair.service.changelog.entities.ChangelogRow;
import io.github.spair.service.git.entities.PullRequest;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
class ChangelogGenerator implements DataGenerator<PullRequest, Changelog> {

    private static final Pattern CL_TEXT = Pattern.compile(":cl:((?:.|\\n|\\r)*+)|\uD83C\uDD91((?:.|\\n|\\r)*+)");
    private static final Pattern AUTHOR_BEFORE_CHANGES = Pattern.compile(".*");
    private static final Pattern CHANGELOG_ROW_WITH_CLASS = Pattern.compile("-\\s(\\w+)(\\[link])?:\\s(.*)");

    @Override
    public Changelog generate(PullRequest pullRequest) {
        Changelog changelog = new Changelog();
        String changelogText = findChangelogText(Optional.ofNullable(pullRequest.getBody()).orElse(""));

        if (!changelogText.isEmpty()) {
            changelog = parseChangelog(changelogText);
            prepareChangelog(changelog, pullRequest);
        }

        return changelog;
    }

    private String findChangelogText(String prBody) {
        String changelogWithoutComments = prBody.replaceAll("(?s)<!--.*?-->", "");
        Matcher matcher = CL_TEXT.matcher(changelogWithoutComments);

        if (matcher.find()) {
            return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);  // 1 - :cl:, 2 - 🆑
        } else {
            return "";
        }
    }

    private Changelog parseChangelog(String changelogText) {
        Changelog changelog = new Changelog();

        changelog.setAuthor(parseAuthor(changelogText));
        changelog.setChangelogRows(parseChangelogRows(changelogText));

        return changelog;
    }

    private String parseAuthor(String changelogText) {
        Matcher matcher = AUTHOR_BEFORE_CHANGES.matcher(changelogText);

        if (matcher.find()) {
            return matcher.group().trim();
        } else {
            return "";
        }
    }

    private List<ChangelogRow> parseChangelogRows(String changelogText) {
        List<ChangelogRow> changelogRows = new ArrayList<>();

        try (Scanner scanner = new Scanner(changelogText)) {
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                Matcher matcher = CHANGELOG_ROW_WITH_CLASS.matcher(line);

                if (matcher.find()) {
                    ChangelogRow changelogRow = new ChangelogRow();

                    changelogRow.setClassName(matcher.group(1));
                    changelogRow.setHasLink(Objects.nonNull(matcher.group(2)));
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
            StringBuilder sb = new StringBuilder(changelogRow.getChanges().trim());

            capitalizeFirstLetter(sb);
            ensureDotEnd(sb);

            if (changelogRow.isHasLink()) {
                addPullRequestLink(sb, pullRequest.getLink());
            }

            changelogRow.setChanges(sb.toString());
        });
    }

    private void ensureAuthorExist(Changelog changelog, String authorName) {
        if (changelog.getAuthor().isEmpty()) {
            changelog.setAuthor(authorName);
        }
    }

    private void capitalizeFirstLetter(StringBuilder sb) {
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
    }

    private void ensureDotEnd(StringBuilder sb) {
        char lastChar = sb.charAt(sb.length() - 1);

        if (lastChar != '.' && lastChar != '?' && lastChar != '!') {
            sb.append('.');
        }
    }

    private void addPullRequestLink(StringBuilder sb, String prLink) {
        sb.append(" [link:").append(prLink).append("]");
    }
}
