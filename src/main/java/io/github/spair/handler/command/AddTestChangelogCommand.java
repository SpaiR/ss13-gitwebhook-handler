package io.github.spair.handler.command;

import io.github.spair.service.changelog.ChangelogService;
import io.github.spair.service.changelog.entity.Changelog;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.github.GitHubService;
import io.github.spair.service.pr.entity.PullRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;

public class AddTestChangelogCommand implements HandlerCommand<PullRequest> {

    private final ConfigService configService;
    private final ChangelogService changelogService;
    private final GitHubService gitHubService;

    @Autowired
    public AddTestChangelogCommand(final ConfigService configService,
                                   final ChangelogService changelogService,
                                   final GitHubService gitHubService) {
        this.configService = configService;
        this.changelogService = changelogService;
        this.gitHubService = gitHubService;
    }

    @Override
    public void execute(final PullRequest pullRequest) {
        if (!checkPullRequest(pullRequest)) {
            return;
        }

        Optional<Changelog> changelogOpt = changelogService.createFromPullRequest(pullRequest);

        changelogOpt.ifPresent(changelog -> {
            if (!changelog.isEmpty()) {
                String changelogPath = configService.getConfig().getChangelogConfig().getPathToChangelog();
                String currentChangelogHtml = gitHubService.readDecodedFile(changelogPath);
                String newChangelogHtml = changelogService.addTestChangelogToHtml(currentChangelogHtml, changelog);

                String updateMessage = "Add test merge changelog for PR #" + pullRequest.getNumber();
                gitHubService.updateFile(changelogPath, updateMessage, newChangelogHtml);
            }
        });
    }

    private boolean checkPullRequest(final PullRequest pullRequest) {
        String testMergeLabel = configService.getConfig().getLabels().getTestMerge();
        Set<String> masterUsers = configService.getConfig().getGitHubConfig().getMasterUsers();
        String sender = pullRequest.getSender();
        String touchedLabel = pullRequest.getTouchedLabel();
        return masterUsers.contains(sender) && touchedLabel.equals(testMergeLabel);
    }
}
