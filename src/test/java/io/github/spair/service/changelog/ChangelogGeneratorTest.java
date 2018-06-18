package io.github.spair.service.changelog;

import io.github.spair.service.changelog.entities.Changelog;
import io.github.spair.service.changelog.entities.ChangelogRow;
import io.github.spair.service.git.entities.PullRequest;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ChangelogGeneratorTest {

    private final ChangelogGenerator changelogGenerator = new ChangelogGenerator();

    @Test
    public void testCreateFromPullRequestWithCustomAuthorAndCommentsAndLink() {
        String bodyText = "Lorem ipsum dolor sit amet.\n\n" +
                "<!-- Comment \n ... \r text -->" +
                ":cl: Custom author\n" +
                " - entry1: Value 1\n" +
                " - entry2: value 2.\n" +
                " - entry3[link]: Value 3";

        PullRequest pullRequest = PullRequest.builder().author("Author Name").body(bodyText).link("pr-link").build();
        Changelog changelog = changelogGenerator.generate(pullRequest);
        List<ChangelogRow> changelogRows = changelog.getChangelogRows();

        assertEquals("Custom author", changelog.getAuthor());

        assertEquals("entry1", changelogRows.get(0).getClassName());
        assertEquals("Value 1.", changelogRows.get(0).getChanges());

        assertEquals("entry2", changelogRows.get(1).getClassName());
        assertEquals("Value 2.", changelogRows.get(1).getChanges());

        assertEquals("entry3", changelogRows.get(2).getClassName());
        assertEquals("Value 3. [link:pr-link]", changelogRows.get(2).getChanges());
    }

    @Test
    public void testCreateFromPullRequestWithGitHubAuthor() {
        String bodyText = "Lorem ipsum dolor sit amet.\n\n" +
                ":cl:\n" +
                "- entry: Value!\n";

        PullRequest pullRequest = PullRequest.builder().author("Author Name").body(bodyText).build();
        Changelog changelog = changelogGenerator.generate(pullRequest);
        List<ChangelogRow> changelogRows = changelog.getChangelogRows();

        assertEquals("Author Name", changelog.getAuthor());

        assertEquals("entry", changelogRows.get(0).getClassName());
        assertEquals("Value!", changelogRows.get(0).getChanges());
    }

    @Test
    public void testCreateFromPullRequestWithoutChangelog() {
        String bodyText = "Lorem ipsum dolor sit amet";

        PullRequest pullRequest = PullRequest.builder().body(bodyText).build();
        Changelog changelog = changelogGenerator.generate(pullRequest);

        assertTrue(changelog.isEmpty());
    }

    @Test
    public void testCreateFromPullRequestWithInvalidChangelog() {
        String bodyText = "Lorem ipsum dolor sit amet\n" +
                ":cl: entry: Value.";

        PullRequest pullRequest = PullRequest.builder().body(bodyText).build();
        Changelog changelog = changelogGenerator.generate(pullRequest);

        assertTrue(changelog.isEmpty());

        bodyText = "Lorem ipsum dolor sit amet\n" +
                ":cl:\n" +
                "-entry: Value.";

        pullRequest = PullRequest.builder().body(bodyText).build();
        changelog = changelogGenerator.generate(pullRequest);

        assertTrue(changelog.isEmpty());
    }

    @Test
    public void testCreateFromPullRequestWithClAsIcon() {
        String bodyText = "Lorem ipsum dolor sit amet\n" +
                "\uD83C\uDD91\n" +
                " - entry: value";

        PullRequest pullRequest = PullRequest.builder().body(bodyText).build();
        Changelog changelog = changelogGenerator.generate(pullRequest);
        List<ChangelogRow> changelogRows = changelog.getChangelogRows();

        assertEquals("entry", changelogRows.get(0).getClassName());
        assertEquals("Value.", changelogRows.get(0).getChanges());
    }
}