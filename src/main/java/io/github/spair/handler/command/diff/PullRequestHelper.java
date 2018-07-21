package io.github.spair.handler.command.diff;

import io.github.spair.service.ByondFiles;
import io.github.spair.service.github.entity.PullRequestFile;

import java.util.List;
import java.util.stream.Collectors;

final class PullRequestHelper {

    static List<PullRequestFile> filterDmmFiles(final List<PullRequestFile> pullRequestFiles) {
        return pullRequestFiles.stream()
                .filter(file -> file.getFilename().endsWith(ByondFiles.DMM_SUFFIX))
                .collect(Collectors.toList());
    }

    static List<PullRequestFile> filterDmiFiles(final List<PullRequestFile> pullRequestFiles) {
        return pullRequestFiles.stream()
                .filter(file -> file.getFilename().endsWith(ByondFiles.DMI_SUFFIX))
                .collect(Collectors.toList());
    }

    private PullRequestHelper() {
    }
}
