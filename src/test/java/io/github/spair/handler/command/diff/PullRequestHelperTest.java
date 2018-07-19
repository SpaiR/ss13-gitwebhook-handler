package io.github.spair.handler.command.diff;

import io.github.spair.service.github.entity.PullRequestFile;
import org.assertj.core.util.Lists;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class PullRequestHelperTest {

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
}