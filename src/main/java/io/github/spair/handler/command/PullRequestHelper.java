package io.github.spair.handler.command;

import io.github.spair.service.ByondFiles;
import io.github.spair.service.config.entity.HandlerConfig;
import io.github.spair.service.github.entity.PullRequestFile;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.service.pr.entity.PullRequestType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class PullRequestHelper {

    public static boolean checkForTestChangelog(final PullRequest pullRequest, final HandlerConfig config) {
        final String testMergeLabel = config.getLabels().getTestMerge();
        final Set<String> masterUsers = config.getGitHubConfig().getMasterUsers();

        final String sender = pullRequest.getSender();
        final String touchedLabel = pullRequest.getTouchedLabel();

        return masterUsers.contains(sender) && touchedLabel.equals(testMergeLabel);
    }

    public static boolean checkForIDMap(final PullRequest pullRequest, final HandlerConfig config) {
        if (pullRequest.getType() == PullRequestType.SYNCHRONIZE) {
            final String interactiveDiffMapLabel = config.getLabels().getInteractiveDiffMap();
            for (String label : pullRequest.getLabels()) {
                if (label.equals(interactiveDiffMapLabel)) {
                    return true;
                }
            }
            return false;
        } else {
            final String idMapLabel = config.getLabels().getInteractiveDiffMap();
            final Set<String> masterUsers = config.getGitHubConfig().getMasterUsers();

            final String sender = pullRequest.getSender();
            final String touchedLabel = pullRequest.getTouchedLabel();

            return masterUsers.contains(sender) && touchedLabel.equals(idMapLabel);
        }
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
