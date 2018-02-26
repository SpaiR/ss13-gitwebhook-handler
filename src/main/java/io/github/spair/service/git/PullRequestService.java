package io.github.spair.service.git;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.service.EnumUtil;
import io.github.spair.service.RestService;
import io.github.spair.service.changelog.ChangelogService;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.git.entities.PullRequest;
import io.github.spair.service.git.entities.PullRequestType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class PullRequestService {

    private final ChangelogService changelogService;
    private final ConfigService configService;
    private final GitHubService gitHubService;
    private final RestService restService;

    private static final Pattern MAP_CHANGES_PATTERN = Pattern.compile("diff.+\\.dmm");
    private static final Pattern ICON_CHANGES_PATTERN = Pattern.compile("diff.+\\.dmi");

    private static final String DNM_TAG = "[dnm]";
    private static final String WIP_DAT = "[wip]";

    @Autowired
    public PullRequestService(ChangelogService changelogService, ConfigService configService, GitHubService gitHubService, RestService restService) {
        this.changelogService = changelogService;
        this.configService = configService;
        this.gitHubService = gitHubService;
        this.restService = restService;
    }

    public PullRequest convertWebhookJson(ObjectNode webhookJson) {
        JsonNode pullRequestNode = webhookJson.get(GitHubPayloadFields.PULL_REQUEST);

        String author = pullRequestNode.get(GitHubPayloadFields.USER).get(GitHubPayloadFields.LOGIN).asText();
        int number = pullRequestNode.get(GitHubPayloadFields.NUMBER).asInt();
        String title = pullRequestNode.get(GitHubPayloadFields.TITLE).asText();
        PullRequestType type = identifyType(webhookJson);
        String link = pullRequestNode.get(GitHubPayloadFields.HTML_URL).asText();
        String diffLink = pullRequestNode.get(GitHubPayloadFields.DIFF_URL).asText();
        String body = pullRequestNode.get(GitHubPayloadFields.BODY).asText();

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

        Map<String, String> availableClassesLabels = configService.getConfig().getGitHubConfig().getLabels().getAvailableClassesLabels();
        Set<String> changelogClasses = changelogService.getChangelogClassesList(pullRequest);

        changelogClasses.forEach(className -> {
            String classLabel = availableClassesLabels.getOrDefault(className, "");

            if (!classLabel.isEmpty()) {
                labelsToAdd.add(classLabel);
            }
        });

        return labelsToAdd;
    }

    private List<String> getLabelsFromDiff(String diffLink) {
        List<String> labelsToAdd = new ArrayList<>();

        String pullRequestDiff = restService.getForObject(diffLink, String.class);

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

        boolean isDNM = loweredTitle.contains(DNM_TAG);
        boolean isWIP = loweredTitle.contains(WIP_DAT);

        if (isDNM) {
            labelsToAdd.add(configService.getConfig().getGitHubConfig().getLabels().getDoNotMerge());
        }

        if (isWIP) {
            labelsToAdd.add(configService.getConfig().getGitHubConfig().getLabels().getWorkInProgress());
        }

        return labelsToAdd;
    }

    private PullRequestType identifyType(ObjectNode webhookJson) {
        String action = webhookJson.get(GitHubPayloadFields.ACTION).asText();
        return EnumUtil.valueOfOrDefault(PullRequestType.values(), action, PullRequestType.UNDEFINED);
    }
}
