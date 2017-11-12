package io.github.spair.services.changelog;

import io.github.spair.services.changelog.entities.Changelog;
import io.github.spair.services.changelog.entities.ChangelogRow;
import io.github.spair.services.config.ConfigService;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HtmlChangelogGeneratorTest {

    private ConfigService configService;
    private String currentDate;

    @Before
    public void setUp() {
        configService = mock(ConfigService.class);
        when(configService.getChangelogUpdateText()).thenReturn("updated");
        when(configService.getConfigTimeZone()).thenReturn("Europe/Moscow");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.YYYY");
        ZoneId zoneId = ZoneId.of("Europe/Moscow");
        currentDate = LocalDate.now(zoneId).format(formatter);
    }

    @Test
    public void testGenerate() {
        HtmlChangelogGenerator generator = new HtmlChangelogGenerator(configService);

        ChangelogRow changelogRow1 = new ChangelogRow();
        changelogRow1.setChanges("Some changes.");
        changelogRow1.setClassName("entry1");

        ChangelogRow changelogRow2 = new ChangelogRow();
        changelogRow2.setChanges("Another changes.");
        changelogRow2.setClassName("entry2");

        List<ChangelogRow> changelogRows = Lists.newArrayList(changelogRow1, changelogRow2);

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
                "      <h4 class=\"author\">Author Name updated:</h4>\n" +
                "      <ul class=\"changelog\">\n" +
                "       <li class=\"entry1\">Some changes.</li>\n" +
                "       <li class=\"entry2\">Another changes.</li>\n" +
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