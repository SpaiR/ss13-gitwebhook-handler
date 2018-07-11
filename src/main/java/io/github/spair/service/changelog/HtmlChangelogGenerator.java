package io.github.spair.service.changelog;

import io.github.spair.TimeService;
import io.github.spair.service.changelog.entity.Changelog;
import io.github.spair.service.changelog.entity.ChangelogRow;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Scanner;

@Component
final class HtmlChangelogGenerator {

    private final TimeService timeService;

    private static final String COL_LARGE = "col-lg-12";

    private static final String DATA_DATE = "data-date";
    private static final String DATA_AUTHOR = "data-author";
    private static final String DATA_PR = "data-pr";

    private static final String CHANGELOGS_ID = "changelogs";
    private static final String TEST_MERGE_CHANGELOG_ID = "tm-changelogs";

    private static final String CHANGELOG_CLASS = "changelog";
    private static final String CHANGELOG_ELEMENT_TEMPLATE = "<li class=\"%s\">%s</li>";

    private static final String TM_ROW =
            "<div class=\"row\">"
          +   "<div class=\"col-lg-12\">"
          +     "<h3 class=\"row__header\">Test Merge:</h3>"
          +   "</div>"
          + "</div>";
    private static final String TM_COLUMN_TEMPLATE =
              "<div data-pr=\"%d\">"
            +   "<h4 class=\"author\">%s, <a href=\"%s\">PR #%d</a></h4>"
            +   "<ul class=\"changelog\"></ul>"
            + "</div>";

    private static final String DATE_ROW_TEMPLATE = "<div class=\"row\" data-date=\"%s\"></div>";
    private static final String DATE_ELEMENT_TEMPLATE =
            "<div class=\"col-lg-12\">"
          +   "<h3 class=\"row__header\">%s</h3>"
          + "</div>";

    private static final String AUTHOR_COLUMN_TEMPLATE = "<div data-author=\"%s\"></div>";
    private static final String AUTHOR_ELEMENT_TEMPLATE = "<h4 class=\"author\">%s:</h4><ul class=\"changelog\"></ul>";

    // $1 is a group from regex, taken during replacement process.
    private static final String READ_MORE = "<a class=\"btn btn-xs btn-success link-btn\" href=\"$1\">Read More</a>";

    @Autowired
    HtmlChangelogGenerator(final TimeService timeService) {
        this.timeService = timeService;
    }

    String mergeHtmlWithChangelog(final String html, final Changelog newChangelog) {
        Document parsedChangelog = Jsoup.parse(html);
        Element currentChangelogs = parsedChangelog.getElementById(CHANGELOGS_ID);
        String currentDate = timeService.getCurrentDate();

        Element currentDateElement = getCurrentDateElement(currentChangelogs, currentDate);

        if (currentDateElement != null) {
            addChangelogToCurrentDate(newChangelog, currentDateElement);
        } else {
            currentChangelogs.prepend(String.format(DATE_ROW_TEMPLATE, currentDate));

            Element newDateElement = getCurrentDateElement(currentChangelogs, currentDate);
            newDateElement.append(String.format(DATE_ELEMENT_TEMPLATE, currentDate));

            addChangelogToCurrentDate(newChangelog, newDateElement);
        }

        return cleanHtml(parsedChangelog.toString());
    }

    String addTestChangelogToHtml(final String html, final Changelog testChangelog) {
        Document parsedChangelog = Jsoup.parse(html);
        Element currentChangelogs = parsedChangelog.getElementById(TEST_MERGE_CHANGELOG_ID);

        if (currentChangelogs.childNodes().isEmpty()) {
            currentChangelogs.append(TM_ROW);
        }

        Element columnAddTo = currentChangelogs.getElementsByClass(COL_LARGE).first();

        int prNumber = testChangelog.getPullRequestNumber();
        String author = testChangelog.getAuthor();
        String link = testChangelog.getPullRequestLink();

        columnAddTo.append(String.format(TM_COLUMN_TEMPLATE, prNumber, author, link, prNumber));

        Element elementToAddChangelogRows = getPrElement(columnAddTo, prNumber);
        addChangelogRows(testChangelog.getChangelogRows(), elementToAddChangelogRows);

        return cleanHtml(parsedChangelog.toString());
    }

    private void addChangelogToCurrentDate(final Changelog changelog, final Element currentDateElement) {
        Element columnAddTo = currentDateElement.getElementsByClass(COL_LARGE).first();
        Element authorElement = getAuthorElement(columnAddTo, changelog.getAuthor());

        if (authorElement != null) {
            addChangelogRows(changelog.getChangelogRows(), authorElement);
        } else {
            columnAddTo.append(String.format(AUTHOR_COLUMN_TEMPLATE, changelog.getAuthor()));

            Element newAuthorElement = getAuthorElement(columnAddTo, changelog.getAuthor());
            newAuthorElement.append(String.format(AUTHOR_ELEMENT_TEMPLATE, changelog.getAuthor()));

            addChangelogRows(changelog.getChangelogRows(), newAuthorElement);
        }
    }

    private void addChangelogRows(final List<ChangelogRow> changelogRows, final Element elementToAddChangelogRows) {
        Element changelogElement = elementToAddChangelogRows.getElementsByClass(CHANGELOG_CLASS).first();

        changelogRows.forEach(row -> {
            String changesRow = linkify(row.getChanges());
            changelogElement.append(String.format(CHANGELOG_ELEMENT_TEMPLATE, row.getClassName(), changesRow));
        });
    }

    private String linkify(final String changesRow) {
        return changesRow.replaceAll("\\[link:(.*)]", READ_MORE);
    }

    private Element getCurrentDateElement(final Element elementToParse, final String currentDate) {
        return elementToParse.getElementsByAttributeValue(DATA_DATE, currentDate).first();
    }

    private Element getAuthorElement(final Element elementToParse, final String author) {
        return elementToParse.getElementsByAttributeValue(DATA_AUTHOR, author).first();
    }

    private Element getPrElement(final Element elementToParse, final int prNumber) {
        return elementToParse.getElementsByAttributeValue(DATA_PR, String.valueOf(prNumber)).first();
    }

    private String cleanHtml(final String html) {
        try (Scanner scanner = new Scanner(html)) {
            StringBuilder res = new StringBuilder();
            while (scanner.hasNextLine()) {
                res.append(scanner.nextLine().replaceAll("\\s+$", "")).append(System.lineSeparator());
            }
            res.delete(res.lastIndexOf(System.lineSeparator()), res.length());
            return res.toString();
        }
    }
}
