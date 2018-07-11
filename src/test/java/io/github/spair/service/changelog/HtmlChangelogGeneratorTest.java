package io.github.spair.service.changelog;

import io.github.spair.ResourceHelper;
import io.github.spair.TimeService;
import io.github.spair.service.changelog.entity.Changelog;
import io.github.spair.service.changelog.entity.ChangelogRow;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HtmlChangelogGeneratorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private TimeService timeService;
    private HtmlChangelogGenerator generator;

    @Before
    public void setUp() {
        generator = new HtmlChangelogGenerator(timeService);
        when(timeService.getCurrentDate()).thenReturn("mocked date");
    }

    @Test
    public void testMergeHtmlWithChangelogFromEmpty() {
        String resultHtml = generator.mergeHtmlWithChangelog("<div id=\"changelogs\"></div>", createChangelog());
        String assertHtml = readChangelog("changelog-general-from-empty.txt");

        assertEquals(assertHtml, resultHtml);
    }

    @Test
    public void testMergeHtmlWithChangelogFromFull() {
        String changelogTmpl = readChangelog("changelog-general-template.txt");

        String resultHtml = generator.mergeHtmlWithChangelog(changelogTmpl, createChangelog());
        String assertHtml = readChangelog("changelog-general-from-full.txt");

        assertEquals(assertHtml, resultHtml);
    }

    @Test
    public void testAddTestChangelogToHtmlFromEmpty() {
        String resultHtml = generator.addTestChangelogToHtml("<div id=\"tm-changelogs\"></div>", createChangelog());
        String assertHtml = readChangelog("changelog-test-from-empty.txt");

        assertEquals(assertHtml, resultHtml);
    }

    @Test
    public void testAddTestChangelogToHtmlFromFull() {
        String changelogTmpl = readChangelog("changelog-test-template.txt");

        String resultHtml = generator.addTestChangelogToHtml(changelogTmpl, createChangelog());
        String assertHtml = readChangelog("changelog-test-from-full.txt");

        assertEquals(assertHtml, resultHtml);
    }

    private Changelog createChangelog() {
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
        changelog.setPullRequestNumber(748);
        changelog.setPullRequestLink("mocked-link");
        changelog.setAuthor("Author Name");
        changelog.setChangelogRows(changelogRows);

        return changelog;
    }

    private String readChangelog(final String name) {
        return ResourceHelper.readFile("data" + File.separator + name);
    }
}