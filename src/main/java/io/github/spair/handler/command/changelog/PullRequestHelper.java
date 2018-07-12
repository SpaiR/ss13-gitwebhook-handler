package io.github.spair.handler.command.changelog;

import io.github.spair.service.config.entity.HandlerConfig;
import io.github.spair.service.pr.entity.PullRequest;

import java.util.Set;

final class PullRequestHelper {

    static boolean checkPRForTestChangelog(final PullRequest pullRequest, final HandlerConfig config) {
        final String testMergeLabel = config.getLabels().getTestMerge();
        final Set<String> masterUsers = config.getGitHubConfig().getMasterUsers();

        final String sender = pullRequest.getSender();
        final String touchedLabel = pullRequest.getTouchedLabel();

        return masterUsers.contains(sender) && touchedLabel.equals(testMergeLabel);
    }

    private PullRequestHelper() {
    }
}
