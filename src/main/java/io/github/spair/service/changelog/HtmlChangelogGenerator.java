package io.github.spair.service.changelog;

import io.github.spair.service.changelog.entity.Changelog;
import io.github.spair.service.changelog.entity.ChangelogRow;
import io.github.spair.service.config.ConfigService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

final class HtmlChangelogGenerator {

    private final ConfigService configService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.YYYY");

    private static final String LARGE_COLUMN = "col-lg-12";

    private static final String DATA_DATE = "data-date";
    private static final String DATA_AUTHOR = "data-author";

    private static final String CHANGELOGS_ID = "changelogs";
    private static final String CHANGELOG_CLASS = "changelog";
    private static final String CHANGELOG_ELEMENT_TEMPLATE = "<li class=\"%s\">%s</li>";

    private static final String DATE_ROW_TEMPLATE = "<div class=\"row\" data-date=\"%s\"></div>";
    private static final String DATE_ELEMENT_TEMPLATE = "<div class=\"col-lg-12\"><h3 class=\"date\">%s</h3></div>";

    private static final String AUTHOR_COLUMN_TEMPLATE = "<div data-author=\"%s\"></div>";
    private static final String AUTHOR_ELEMENT_TEMPLATE = "<h4 class=\"author\">%s:</h4><ul class=\"changelog\"></ul>";

    // $1 is a group from regex, taken during replacement process.
    private static final String READ_MORE = "<a class=\"btn btn-xs btn-success link-btn\" href=\"$1\">Read More</a>";

    HtmlChangelogGenerator(final ConfigService configService) {
        this.configService = configService;
    }

    String generate(final String currentChangelogHtml, final Changelog newChangelog) {
        Document parsedChangelog = Jsoup.parse(currentChangelogHtml);
        Element currentChangelogs = parsedChangelog.getElementById(CHANGELOGS_ID);

        ZoneId zoneId = ZoneId.of(configService.getConfig().getTimeZone());
        String currentDate = LocalDate.now(zoneId).format(FORMATTER);

        Element currentDateElement = getCurrentDateElement(currentChangelogs, currentDate);

        if (currentDateElement != null) {
            addChangelogToCurrentDate(newChangelog, currentDateElement);
        } else {
            currentChangelogs.prepend(String.format(DATE_ROW_TEMPLATE, currentDate));

            Element newDateElement = getCurrentDateElement(currentChangelogs, currentDate);
            newDateElement.append(String.format(DATE_ELEMENT_TEMPLATE, currentDate));

            addChangelogToCurrentDate(newChangelog, newDateElement);
        }

        return parsedChangelog.toString();
    }

    private void addChangelogToCurrentDate(final Changelog changelog, final Element currentDateElement) {
        Element columnAddTo = currentDateElement.getElementsByClass(LARGE_COLUMN).first();
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

    private void addChangelogRows(final List<ChangelogRow> changelogRows, final Element authorElement) {
        Element changelogElement = authorElement.getElementsByClass(CHANGELOG_CLASS).first();

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
}
