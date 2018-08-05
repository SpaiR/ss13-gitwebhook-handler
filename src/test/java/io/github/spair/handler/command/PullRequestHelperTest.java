package io.github.spair.handler.command;

import io.github.spair.service.config.entity.HandlerConfig;
import io.github.spair.service.github.entity.PullRequestFile;
import io.github.spair.service.pr.entity.PullRequest;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

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
    public void testCheckForTestChangelogWhenAllIsValid() {
        PullRequest pr = PullRequest.builder().sender(MASTERUSER).touchedLabel(TESTMERGE).build();
        assertTrue(PullRequestHelper.checkForTestChangelog(pr, config));
    }

    @Test
    public void testCheckForTestChangelogWhenNoMasterUser() {
        PullRequest pr = PullRequest.builder().touchedLabel(TESTMERGE).build();
        assertFalse(PullRequestHelper.checkForTestChangelog(pr, config));
    }

    @Test
    public void testCheckForTestChangelogWhenNoLabel() {
        PullRequest pr = PullRequest.builder().sender(MASTERUSER).touchedLabel("").build();
        assertFalse(PullRequestHelper.checkForTestChangelog(pr, config));
    }

    @Test
    public void testFilterDmmFiles() {
        List<PullRequestFile> rawPrFiles = Lists.newArrayList(
                new PullRequestFile(){{setFilename("file1.dm");}},
                new PullRequestFile(){{setFilename("file2.dmm");}},
                new PullRequestFile(){{setFilename("file3.dmi");}}
        );
        List<PullRequestFile> filteredPrFiles = PullRequestHelper.filterDmmFiles(rawPrFiles);

        assertEquals(1, filteredPrFiles.size());
        assertEquals(new PullRequestFile(){{setFilename("file2.dmm");}}, filteredPrFiles.get(0));
    }

    @Test
    public void testFilterDmiFiles() {
        List<PullRequestFile> rawPrFiles = Lists.newArrayList(
                new PullRequestFile(){{setFilename("file1.dm");}},
                new PullRequestFile(){{setFilename("file2.dmm");}},
                new PullRequestFile(){{setFilename("file3.dmi");}}
        );
        List<PullRequestFile> filteredPrFiles = PullRequestHelper.filterDmiFiles(rawPrFiles);

        assertEquals(1, filteredPrFiles.size());
        assertEquals(new PullRequestFile(){{setFilename("file3.dmi");}}, filteredPrFiles.get(0));
    }
}