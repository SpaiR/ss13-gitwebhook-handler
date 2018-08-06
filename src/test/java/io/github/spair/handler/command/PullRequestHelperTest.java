package io.github.spair.handler.command;

import io.github.spair.service.config.entity.HandlerConfig;
import io.github.spair.service.github.entity.PullRequestFile;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.service.pr.entity.PullRequestType;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class PullRequestHelperTest {

    private static final String MASTERUSER = "masteruser";
    private static final String TESTMERGE = "testmerge";
    private static final String IDMAP = "idmap";

    private HandlerConfig config;

    @Before
    public void setUp() {
        config = new HandlerConfig();
        config.getGitHubConfig().getMasterUsers().add(MASTERUSER);
        config.getLabels().setTestMerge(TESTMERGE);
        config.getLabels().setInteractiveDiffMap(IDMAP);
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

    @Test
    public void testCheckForIDMapWhenSynchronizedWithIDMapLabel() {
        PullRequest pr = PullRequest.builder().labels(Sets.newSet(IDMAP)).type(PullRequestType.SYNCHRONIZE).build();
        assertTrue(PullRequestHelper.checkForIDMap(pr, config));
    }

    @Test
    public void testCheckForIDMapWhenSynchronizedWithoutLabel() {
        PullRequest pr = PullRequest.builder().labels(Collections.emptySet()).type(PullRequestType.SYNCHRONIZE).build();
        assertFalse(PullRequestHelper.checkForIDMap(pr, config));
    }

    @Test
    public void testCheckForIDMapWhenLabeledWithMasterUser() {
        PullRequest pr = PullRequest.builder().touchedLabel(IDMAP).sender(MASTERUSER).build();
        assertTrue(PullRequestHelper.checkForIDMap(pr, config));
    }

    @Test
    public void testCheckForIDMapWhenLabeledWithWrongLabel() {
        PullRequest pr = PullRequest.builder().touchedLabel("wrongLabel").sender(MASTERUSER).build();
        assertFalse(PullRequestHelper.checkForIDMap(pr, config));
    }

    @Test
    public void testCheckForIDMapWhenLabeledWithWrongUser() {
        PullRequest pr = PullRequest.builder().touchedLabel(IDMAP).sender("wrongUser").build();
        assertFalse(PullRequestHelper.checkForIDMap(pr, config));
    }
}