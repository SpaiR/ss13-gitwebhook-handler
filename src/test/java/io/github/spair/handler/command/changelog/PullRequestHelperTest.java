package io.github.spair.handler.command.changelog;

import io.github.spair.service.config.entity.HandlerConfig;
import io.github.spair.service.pr.entity.PullRequest;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PullRequestHelperTest {

    private static final String MASTERUSER = "masteruser";
    private static final String TESTMERGE = "testmerge";

    private HandlerConfig config;

    @Before
    public void setUp() {
        config = new HandlerConfig();
        config.getGitHubConfig().getMasterUsers().add(MASTERUSER);
        config.getLabels().setTestMerge(TESTMERGE);
    }

    @Test
    public void testCheckPRForTestChangelogWhenAllIsValid() {
        PullRequest pr = PullRequest.builder().sender(MASTERUSER).touchedLabel(TESTMERGE).build();
        assertTrue(PullRequestHelper.checkPRForTestChangelog(pr, config));
    }

    @Test
    public void testCheckPRForTestChangelogWhenNoMasterUser() {
        PullRequest pr = PullRequest.builder().touchedLabel(TESTMERGE).build();
        assertFalse(PullRequestHelper.checkPRForTestChangelog(pr, config));
    }

    @Test
    public void testCheckPRForTestChangelogWhenNoLabel() {
        PullRequest pr = PullRequest.builder().sender(MASTERUSER).touchedLabel("").build();
        assertFalse(PullRequestHelper.checkPRForTestChangelog(pr, config));
    }
}