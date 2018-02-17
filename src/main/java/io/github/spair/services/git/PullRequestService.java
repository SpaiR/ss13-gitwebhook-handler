package io.github.spair.services.git;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.services.changelog.ChangelogService;
import io.github.spair.services.config.ConfigService;
import io.github.spair.services.git.entities.PullRequest;
import io.github.spair.services.git.entities.PullRequestType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class PullRequestService {

    private final ChangelogService changelogService;
    private final ConfigService configService;
    private final GitHubService gitHubService;

    private static final Pattern MAP_CHANGES_PATTERN = Pattern.compile("diff.+\\.dmm");
    private static final Pattern ICON_CHANGES_PATTERN = Pattern.compile("diff.+\\.dmi");

    @Autowired
    public PullRequestService(ChangelogService changelogService, ConfigService configService, GitHubService gitHubService) {
        this.changelogService = changelogService;
        this.configService = configService;
        this.gitHubService = gitHubService;
    }

    public PullRequest convertWebhookMap(ObjectNode webhookJson) {
        String author = webhookJson.get("pull_request").get("user").get("login").asText();
        int number = webhookJson.get("pull_request").get("number").asInt();
        String title = webhookJson.get("pull_request").get("title").asText();
        PullRequestType type = identifyType(webhookJson);
        String link = webhookJson.get("pull_request").get("html_url").asText();
        String diffLink = webhookJson.get("pull_request").get("diff_url").asText();
        String body = webhookJson.get("pull_request").get("body").asText();

        return PullRequest.builder()
                .author(author).number(number).title(title).type(type).link(link).diffLink(diffLink).body(body)
                .build();
    }

    public void processLabels(PullRequest pullRequest) {
        Set<String> labelsToAdd = new HashSet<>();

        labelsToAdd.addAll(getLabelsFromChangelog(pullRequest));
        labelsToAdd.addAll(getLabelsFromDiff(pullRequest.getDiffLink()));
        labelsToAdd.addAll(getLabelsFromTitle(pullRequest.getTitle()));

        gitHubService.addLabels(pullRequest.getNumber(), new ArrayList<>(labelsToAdd));
    }

    private Set<String> getLabelsFromChangelog(PullRequest pullRequest) {
        Set<String> labelsToAdd = new HashSet<>();

        Map<String, String> classesLabels = configService.getConfig().getGitHubConfig().getLabels().getAvailableClassesLabels();
        List<String> changelogClasses = changelogService.getChangelogClassesList(pullRequest);

        changelogClasses.forEach(className -> {
            String classLabel = classesLabels.get(className);

            if (classLabel != null && classLabel.length() > 0) {
                labelsToAdd.add(classLabel);
            }
        });

        return labelsToAdd;
    }

    private List<String> getLabelsFromDiff(String diffLink) {
        List<String> labelsToAdd = new ArrayList<>();

        String pullRequestDiff = gitHubService.getPullRequestDiff(diffLink);

        boolean hasMapChanges = MAP_CHANGES_PATTERN.matcher(pullRequestDiff).find();
        boolean hasIconChanges = ICON_CHANGES_PATTERN.matcher(pullRequestDiff).find();

        if (hasMapChanges) {
            labelsToAdd.add(configService.getConfig().getGitHubConfig().getLabels().getMapChanges());
        }

        if (hasIconChanges) {
            labelsToAdd.add(configService.getConfig().getGitHubConfig().getLabels().getIconChanges());
        }

        return labelsToAdd;
    }

    private List<String> getLabelsFromTitle(String title) {
        List<String> labelsToAdd = new ArrayList<>();
        String loweredTitle = title.toLowerCase();

        boolean isDNM = loweredTitle.contains("[dnm]");
        boolean isWIP = loweredTitle.contains("[wip]");

        if (isDNM) {
            labelsToAdd.add(configService.getConfig().getGitHubConfig().getLabels().getDoNotMerge());
        }

        if (isWIP) {
            labelsToAdd.add(configService.getConfig().getGitHubConfig().getLabels().getWorkInProgress());
        }

        return labelsToAdd;
    }

    private PullRequestType identifyType(ObjectNode webhookJson) {
        String action = webhookJson.get("action").asText();

        switch (action) {
            case "opened":
                return PullRequestType.OPENED;
            case "edited":
                return PullRequestType.EDITED;
            case "closed":
                if (webhookJson.get("pull_request").get("merged").asBoolean()) {
                    return PullRequestType.MERGED;
                }
            default:
                return PullRequestType.UNDEFINED;
        }
    }
}
