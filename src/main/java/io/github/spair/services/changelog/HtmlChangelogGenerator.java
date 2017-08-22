package io.github.spair.services.changelog;

import io.github.spair.entities.Changelog;
import io.github.spair.entities.ChangelogRow;
import io.github.spair.services.ConfigService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
class HtmlChangelogGenerator {

    @Autowired
    private ConfigService configService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.YYYY");

    String generate(String currentChangelogHtml, Changelog changelog) {
        Document parsedChangelog = Jsoup.parse(currentChangelogHtml);
        Element currentChangelogs = parsedChangelog.getElementById("changelogs");

        ZoneId zoneId = ZoneId.of(configService.getConfig().getTimeZone());
        String currentDate = LocalDate.now(zoneId).format(FORMATTER);
        Element currentDateElement = currentChangelogs.getElementById(currentDate);

        if (currentDateElement != null) {
            addChangelogToCurrentDate(changelog, currentDateElement);
        } else {
            currentChangelogs.prepend("<div class=\"row\" id=\"" + currentDate + "\"></div>");

            Element newCurrentDateElement = currentChangelogs.getElementById(currentDate);
            newCurrentDateElement.append("<div class=\"col-lg-12\"><h3 class=\"date\">" + currentDate + "</h3></div>");

            addChangelogToCurrentDate(changelog, newCurrentDateElement);
        }

        return parsedChangelog.toString();
    }

    private void addChangelogToCurrentDate(Changelog changelog, Element currentDateElement) {
        Element columnAddTo = currentDateElement.getElementsByClass("col-lg-12").first();
        Element authorElement = columnAddTo.getElementById(changelog.getAuthor());

        if (authorElement != null) {
            addChangelogRows(changelog.getChangelogRows(), authorElement);
        } else {
            columnAddTo.append("<div id=\"" + changelog.getAuthor() + "\"></div>");

            Element newAuthorElement = columnAddTo.getElementById(changelog.getAuthor());
            String updateText = configService.getChangelogConfig().getHtml().getUpdateText();
            newAuthorElement.append(
                    "<h4 class=\"author\">" + changelog.getAuthor() + " "+ updateText +":</h4><ul class=\"changelog\"></ul>");

            addChangelogRows(changelog.getChangelogRows(), newAuthorElement);
        }
    }

    private void addChangelogRows(List<ChangelogRow> changelogRows, Element authorElement) {
        Element changelogElement = authorElement.getElementsByClass("changelog").first();

        changelogRows.forEach(row ->
                changelogElement.append("<li class=\"" + row.getClassName() + "\">" + row.getChanges() + "</li>"));
    }
}
