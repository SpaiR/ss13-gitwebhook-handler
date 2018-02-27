package io.github.spair.service.git.entities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PullRequestFileTest {

    @Test
    public void testGetRealNameWhenNonEmpty() {
        PullRequestFile pullRequestFile = new PullRequestFile();
        pullRequestFile.setFilename("some/path/ro/file/file.dm");

        assertEquals("file.dm", pullRequestFile.getRealName());
    }

    @Test
    public void testGetRealNameWhenEmpty() {
        PullRequestFile pullRequestFile = new PullRequestFile();
        pullRequestFile.setFilename("");

        assertEquals("", pullRequestFile.getRealName());
    }

    @Test
    public void testGetRealNameWhenNull() {
        PullRequestFile pullRequestFile = new PullRequestFile();
        assertNull(pullRequestFile.getRealName());
    }
}