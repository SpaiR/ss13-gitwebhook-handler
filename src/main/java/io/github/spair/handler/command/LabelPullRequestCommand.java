package io.github.spair.handler.command;

import io.github.spair.service.changelog.ChangelogService;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.git.GitHubService;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.service.git.entity.PullRequestFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

@Component
public class LabelPullRequestCommand implements HandlerCommand<PullRequest> {

    private static final String DMI_SUFFIX = ".dmi";
    private static final String DMM_SUFFIX = ".dmm";
    private static final String DNM_TAG = "[dnm]";
    private static final String WIP_TAG = "[wip]";

    private final GitHubService gitHubService;
    private final ChangelogService changelogService;
    private final ConfigService configService;

    @Autowired
    public LabelPullRequestCommand(
            final GitHubService gitHubService,
            final ChangelogService changelogService,
            final ConfigService configService) {
        this.gitHubService = gitHubService;
        this.changelogService = changelogService;
        this.configService = configService;
    }

    @Override
    public void execute(final PullRequest pullRequest) {
        Set<String> labelsToAdd = new HashSet<>();

        labelsToAdd.addAll(getLabelsFromChangelog(pullRequest));
        labelsToAdd.addAll(getLabelsFromChangedFiles(pullRequest.getNumber()));
        labelsToAdd.addAll(getLabelsFromTitle(pullRequest.getTitle()));

        if (!labelsToAdd.isEmpty()) {
            gitHubService.addLabels(pullRequest.getNumber(), labelsToAdd);
        }
    }

    private Set<String> getLabelsFromChangelog(final PullRequest pullRequest) {
        Set<String> labelsToAdd = new HashSet<>();

        Map<String, String> labelsForClasses = configService
                .getConfig().getGitHubConfig().getLabels().getLabelsForClasses();
        Set<String> changelogClasses = changelogService.getChangelogClassesList(pullRequest);

        changelogClasses.forEach(className -> {
            String classLabel = labelsForClasses.getOrDefault(className, "");

            if (!classLabel.isEmpty()) {
                labelsToAdd.add(classLabel);
            }
        });

        return labelsToAdd;
    }

    private List<String> getLabelsFromChangedFiles(final int prNumber) {
        final List<PullRequestFile> prFiles = gitHubService.listPullRequestFiles(prNumber);

        boolean hasMapChanges = false;
        boolean hasIconChanges = false;

        for (PullRequestFile file : prFiles) {
            final String filename = file.getFilename();

            if (filename.endsWith(DMM_SUFFIX)) {
                hasMapChanges = true;
            } else if (filename.endsWith(DMI_SUFFIX)) {
                hasIconChanges = true;
            }

            if (hasMapChanges && hasIconChanges) {
                break;
            }
        }

        List<String> labelsToAdd = new ArrayList<>();

        if (hasMapChanges) {
            labelsToAdd.add(configService.getConfig().getGitHubConfig().getLabels().getMapChanges());
        }

        if (hasIconChanges) {
            labelsToAdd.add(configService.getConfig().getGitHubConfig().getLabels().getIconChanges());
        }

        return labelsToAdd;
    }

    private List<String> getLabelsFromTitle(final String title) {
        List<String> labelsToAdd = new ArrayList<>();
        String loweredTitle = title.toLowerCase();

        boolean isDNM = loweredTitle.contains(DNM_TAG);
        boolean isWIP = loweredTitle.contains(WIP_TAG);

        Consumer<String> addLabelIfNotEmpty = (label) -> {
            if (label != null && !label.isEmpty()) {
                labelsToAdd.add(label);
            }
        };

        if (isDNM) {
            addLabelIfNotEmpty.accept(configService.getConfig().getGitHubConfig().getLabels().getDoNotMerge());
        }

        if (isWIP) {
            addLabelIfNotEmpty.accept(configService.getConfig().getGitHubConfig().getLabels().getWorkInProgress());
        }

        return labelsToAdd;
    }
}
