package io.github.spair.service.git;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.service.EnumUtil;
import io.github.spair.service.changelog.ChangelogService;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.git.entities.PullRequest;
import io.github.spair.service.git.entities.PullRequestFile;
import io.github.spair.service.git.entities.PullRequestType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
public class PullRequestService {

    private final ChangelogService changelogService;
    private final ConfigService configService;
    private final GitHubService gitHubService;

    private static final String DMI_SUFFIX = ".dmi";
    private static final String DMM_SUFFIX = ".dmm";

    private static final String DNM_TAG = "[dnm]";
    private static final String WIP_TAG = "[wip]";

    @Autowired
    public PullRequestService(final ChangelogService changelogService,
                              final ConfigService configService,
                              final GitHubService gitHubService) {
        this.changelogService = changelogService;
        this.configService = configService;
        this.gitHubService = gitHubService;
    }

    public PullRequest convertWebhookJson(final ObjectNode webhookJson) {
        JsonNode pullRequestNode = webhookJson.get(GitHubPayload.PULL_REQUEST);

        String author = pullRequestNode.get(GitHubPayload.USER).get(GitHubPayload.LOGIN).asText();
        int number = pullRequestNode.get(GitHubPayload.NUMBER).asInt();
        String title = pullRequestNode.get(GitHubPayload.TITLE).asText();
        PullRequestType type = identifyType(webhookJson);
        String link = pullRequestNode.get(GitHubPayload.HTML_URL).asText();
        String diffLink = pullRequestNode.get(GitHubPayload.DIFF_URL).asText();
        String body = pullRequestNode.get(GitHubPayload.BODY).asText();

        return PullRequest.builder()
                .author(author).number(number).title(title).type(type).link(link).diffLink(diffLink).body(body)
                .build();
    }

    public void processLabels(final PullRequest pullRequest) {
        Set<String> labelsToAdd = new HashSet<>();

        labelsToAdd.addAll(getLabelsFromChangelog(pullRequest));
        labelsToAdd.addAll(getLabelsFromChangedFiles(pullRequest.getNumber()));
        labelsToAdd.addAll(getLabelsFromTitle(pullRequest.getTitle()));

        gitHubService.addLabels(pullRequest.getNumber(), new ArrayList<>(labelsToAdd));
    }

    private Set<String> getLabelsFromChangelog(final PullRequest pullRequest) {
        Set<String> labelsToAdd = new HashSet<>();

        Map<String, String> availableClassesLabels = configService
                .getConfig().getGitHubConfig().getLabels().getAvailableClassesLabels();
        Set<String> changelogClasses = changelogService.getChangelogClassesList(pullRequest);

        changelogClasses.forEach(className -> {
            String classLabel = availableClassesLabels.getOrDefault(className, "");

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

        if (isDNM) {
            labelsToAdd.add(configService.getConfig().getGitHubConfig().getLabels().getDoNotMerge());
        }

        if (isWIP) {
            labelsToAdd.add(configService.getConfig().getGitHubConfig().getLabels().getWorkInProgress());
        }

        return labelsToAdd;
    }

    private PullRequestType identifyType(final ObjectNode webhookJson) {
        if (webhookJson.get(GitHubPayload.PULL_REQUEST).get(GitHubPayload.MERGED).asBoolean()) {
            return PullRequestType.MERGED;
        } else {
            String action = webhookJson.get(GitHubPayload.ACTION).asText();
            return EnumUtil.valueOfOrDefault(PullRequestType.values(), action, PullRequestType.UNDEFINED);
        }
    }
}
