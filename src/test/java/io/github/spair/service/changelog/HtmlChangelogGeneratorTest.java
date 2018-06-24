package io.github.spair.service.changelog;

import io.github.spair.service.changelog.entity.Changelog;
import io.github.spair.service.changelog.entity.ChangelogRow;
import io.github.spair.service.config.ConfigService;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HtmlChangelogGeneratorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigService configService;
    private String currentDate;

    private HtmlChangelogGenerator generator;

    @Before
    public void setUp() {
        generator = new HtmlChangelogGenerator(configService);
        when(configService.getConfig().getTimeZone()).thenReturn("Europe/Moscow");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.YYYY");
        ZoneId zoneId = ZoneId.of("Europe/Moscow");
        currentDate = LocalDate.now(zoneId).format(formatter);
    }

    @Test
    public void testGenerate() {
        ChangelogRow changelogRow1 = new ChangelogRow();
        changelogRow1.setChanges("Some changes.");
        changelogRow1.setClassName("entry1");

        ChangelogRow changelogRow2 = new ChangelogRow();
        changelogRow2.setChanges("Another changes.");
        changelogRow2.setClassName("entry2");

        ChangelogRow changelogRow3 = new ChangelogRow();
        changelogRow3.setChanges("Linked changes. [link:link-to-pr]");
        changelogRow3.setClassName("entry3");

        ChangelogRow changelogRow4 = new ChangelogRow();
        changelogRow4.setChanges("Changes with... [link:some-link] ... link.");
        changelogRow4.setClassName("entry4");

        List<ChangelogRow> changelogRows = Lists.newArrayList(changelogRow1, changelogRow2, changelogRow3, changelogRow4);

        Changelog changelog = new Changelog();
        changelog.setAuthor("Author Name");
        changelog.setChangelogRows(changelogRows);

        String resultHtml = generator.generate("<div id=\"changelogs\"></div>", changelog);
        String assertHtml =
                "<html>\n" +
                " <head></head>\n" +
                " <body>\n" +
                "  <div id=\"changelogs\">\n" +
                "   <div class=\"row\" data-date=\"" + currentDate + "\">\n" +
                "    <div class=\"col-lg-12\">\n" +
                "     <h3 class=\"date\">" + currentDate + "</h3>\n" +
                "     <div data-author=\"Author Name\">\n" +
                "      <h4 class=\"author\">Author Name:</h4>\n" +
                "      <ul class=\"changelog\">\n" +
                "       <li class=\"entry1\">Some changes.</li>\n" +
                "       <li class=\"entry2\">Another changes.</li>\n" +
                "       <li class=\"entry3\">Linked changes. <a class=\"btn btn-xs btn-success link-btn\" href=\"link-to-pr\">Read More</a></li>\n" +
                "       <li class=\"entry4\">Changes with... <a class=\"btn btn-xs btn-success link-btn\" href=\"some-link\">Read More</a> ... link.</li>\n" +
                "      </ul>\n" +
                "     </div>\n" +
                "    </div>\n" +
                "   </div>\n" +
                "  </div>\n" +
                " </body>\n" +
                "</html>";

        assertEquals(assertHtml, resultHtml);
    }
}