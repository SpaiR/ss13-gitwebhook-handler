package io.github.spair.handler.command;

import io.github.spair.service.ByondFiles;
import io.github.spair.service.config.entity.HandlerConfig;
import io.github.spair.service.github.entity.PullRequestFile;
import io.github.spair.service.pr.entity.PullRequest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class PullRequestHelper {

    public static boolean checkPRForTestChangelog(final PullRequest pullRequest, final HandlerConfig config) {
        final String testMergeLabel = config.getLabels().getTestMerge();
        final Set<String> masterUsers = config.getGitHubConfig().getMasterUsers();

        final String sender = pullRequest.getSender();
        final String touchedLabel = pullRequest.getTouchedLabel();

        return masterUsers.contains(sender) && touchedLabel.equals(testMergeLabel);
    }

    public static List<PullRequestFile> filterDmmFiles(final List<PullRequestFile> pullRequestFiles) {
        return pullRequestFiles.stream()
                .filter(file -> file.getFilename().endsWith(ByondFiles.DMM_SUFFIX))
                .collect(Collectors.toList());
    }

    public static List<PullRequestFile> filterDmiFiles(final List<PullRequestFile> pullRequestFiles) {
        return pullRequestFiles.stream()
                .filter(file -> file.getFilename().endsWith(ByondFiles.DMI_SUFFIX))
                .collect(Collectors.toList());
    }

    private PullRequestHelper() {
    }
}
