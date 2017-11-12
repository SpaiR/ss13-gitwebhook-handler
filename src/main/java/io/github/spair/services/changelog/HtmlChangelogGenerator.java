package io.github.spair.services.changelog;

import io.github.spair.services.changelog.entities.Changelog;
import io.github.spair.services.changelog.entities.ChangelogRow;
import io.github.spair.services.config.ConfigService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
class HtmlChangelogGenerator {

    private final ConfigService configService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.YYYY");

    @Autowired
    public HtmlChangelogGenerator(ConfigService configService) {
        this.configService = configService;
    }

    String generate(String currentChangelogHtml, Changelog changelog) {
        Document parsedChangelog = Jsoup.parse(currentChangelogHtml);
        Element currentChangelogs = parsedChangelog.getElementById("changelogs");

        ZoneId zoneId = ZoneId.of(configService.getConfigTimeZone());
        String currentDate = LocalDate.now(zoneId).format(FORMATTER);

        Element currentDateElement = getCurrentDateElement(currentChangelogs, currentDate);

        if (currentDateElement != null) {
            addChangelogToCurrentDate(changelog, currentDateElement);
        } else {
            currentChangelogs.prepend("<div class=\"row\" data-date=\"" + currentDate + "\"></div>");

            Element newDateElement = getCurrentDateElement(currentChangelogs, currentDate);
            newDateElement.append("<div class=\"col-lg-12\"><h3 class=\"date\">" + currentDate + "</h3></div>");

            addChangelogToCurrentDate(changelog, newDateElement);
        }

        return parsedChangelog.toString();
    }

    private void addChangelogToCurrentDate(Changelog changelog, Element currentDateElement) {
        Element columnAddTo = currentDateElement.getElementsByClass("col-lg-12").first();
        Element authorElement = getAuthorElement(columnAddTo, changelog.getAuthor());

        if (authorElement != null) {
            addChangelogRows(changelog.getChangelogRows(), authorElement);
        } else {
            columnAddTo.append("<div data-author=\"" + changelog.getAuthor() + "\"></div>");

            Element newAuthorElement = getAuthorElement(columnAddTo, changelog.getAuthor());
            newAuthorElement.append(
                    "<h4 class=\"author\">" + changelog.getAuthor() + ":</h4><ul class=\"changelog\"></ul>");

            addChangelogRows(changelog.getChangelogRows(), newAuthorElement);
        }
    }

    private void addChangelogRows(List<ChangelogRow> changelogRows, Element authorElement) {
        Element changelogElement = authorElement.getElementsByClass("changelog").first();

        changelogRows.forEach(row -> {
            String changesRow = linkify(row.getChanges());
            changelogElement.append("<li class=\"" + row.getClassName() + "\">" + changesRow + "</li>");
        });
    }

    private String linkify(String changesRow) {
        return changesRow.replaceAll(
                "\\[link:(.*)]",
                "<a class=\"btn btn-xs btn-success link-btn\" href=\"$1\">Read More</a>"
        );
    }

    private Element getCurrentDateElement(Element elementToParse, String currentDate) {
        return elementToParse.getElementsByAttributeValue("data-date", currentDate).first();
    }

    private Element getAuthorElement(Element elementToParse, String author) {
        return elementToParse.getElementsByAttributeValue("data-author", author).first();
    }
}
