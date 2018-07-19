package io.github.spair.service.pr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.util.EnumUtil;
import io.github.spair.service.github.GitHubPayload;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.service.pr.entity.PullRequestType;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class PullRequestService {

    public PullRequest convertWebhookJson(final ObjectNode webhookJson) {
        JsonNode pullRequestNode = webhookJson.get(GitHubPayload.PULL_REQUEST);

        String author = pullRequestNode.get(GitHubPayload.USER).get(GitHubPayload.LOGIN).asText();
        String branchName = pullRequestNode.get(GitHubPayload.HEAD).get(GitHubPayload.REF).asText();
        int number = pullRequestNode.get(GitHubPayload.NUMBER).asInt();
        String title = pullRequestNode.get(GitHubPayload.TITLE).asText();
        PullRequestType type = identifyType(webhookJson);
        String link = pullRequestNode.get(GitHubPayload.HTML_URL).asText();
        String diffLink = pullRequestNode.get(GitHubPayload.DIFF_URL).asText();
        String body = pullRequestNode.get(GitHubPayload.BODY).asText();
        Set<String> labels = extractLabels(pullRequestNode);
        String sender = webhookJson.get(GitHubPayload.SENDER).get(GitHubPayload.LOGIN).asText();
        String touchedLabel = extractTouchedLabel(webhookJson);

        return PullRequest.builder()
                .author(author).branchName(branchName).number(number)
                .title(title).type(type).link(link).diffLink(diffLink).body(body).labels(labels)
                .sender(sender).touchedLabel(touchedLabel)
                .build();
    }

    private PullRequestType identifyType(final ObjectNode webhookJson) {
        String action = webhookJson.get(GitHubPayload.ACTION).asText();
        PullRequestType prType = EnumUtil.valueOfOrDefault(PullRequestType.values(), action, PullRequestType.UNDEFINED);
        boolean isMerged = webhookJson.get(GitHubPayload.PULL_REQUEST).get(GitHubPayload.MERGED).asBoolean();

        if (prType == PullRequestType.CLOSED && isMerged) {
            return PullRequestType.MERGED;
        }
        return prType;
    }

    private Set<String> extractLabels(final JsonNode pullRequestNode) {
        Set<String> labels = new HashSet<>();
        pullRequestNode.get(GitHubPayload.LABELS).forEach(label -> labels.add(label.get(GitHubPayload.NAME).asText()));
        return labels;
    }

    private String extractTouchedLabel(final ObjectNode webjookJson) {
        if (webjookJson.has(GitHubPayload.LABEL)) {
            return webjookJson.get(GitHubPayload.LABEL).get(GitHubPayload.NAME).asText();
        }
        return "";
    }
}
