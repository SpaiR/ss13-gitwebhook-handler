package io.github.spair.service.pr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.util.EnumUtil;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.service.pr.entity.PullRequestType;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static io.github.spair.service.github.GitHubPayload.*;

@Service
public class PullRequestService {

    public PullRequest convertWebhookJson(final ObjectNode webhookJson) {
        JsonNode pullRequestNode = webhookJson.get(PULL_REQUEST);

        String author = pullRequestNode.get(USER).get(LOGIN).asText();
        String branchName = pullRequestNode.get(HEAD).get(REF).asText();
        String sourceHeadName = pullRequestNode.get(HEAD).get(USER).get(LOGIN).asText();
        int number = pullRequestNode.get(NUMBER).asInt();
        String title = pullRequestNode.get(TITLE).asText();
        PullRequestType type = identifyType(webhookJson);
        String link = pullRequestNode.get(HTML_URL).asText();
        String diffLink = pullRequestNode.get(DIFF_URL).asText();
        String body = pullRequestNode.get(BODY).asText();
        Set<String> labels = extractLabels(pullRequestNode);
        String sender = webhookJson.get(SENDER).get(LOGIN).asText();
        String touchedLabel = extractTouchedLabel(webhookJson);

        return PullRequest.builder()
                .author(author).branchName(branchName).sourceHeadName(sourceHeadName).number(number)
                .title(title).type(type).link(link).diffLink(diffLink).body(body).labels(labels)
                .sender(sender).touchedLabel(touchedLabel)
                .build();
    }

    private PullRequestType identifyType(final ObjectNode webhookJson) {
        String action = webhookJson.get(ACTION).asText();
        PullRequestType prType = EnumUtil.valueOfOrDefault(PullRequestType.values(), action, PullRequestType.UNDEFINED);
        boolean isMerged = webhookJson.get(PULL_REQUEST).get(MERGED).asBoolean();

        if (prType == PullRequestType.CLOSED && isMerged) {
            return PullRequestType.MERGED;
        }
        return prType;
    }

    private Set<String> extractLabels(final JsonNode pullRequestNode) {
        Set<String> labels = new HashSet<>();
        pullRequestNode.get(LABELS).forEach(label -> labels.add(label.get(NAME).asText()));
        return labels;
    }

    private String extractTouchedLabel(final ObjectNode webjookJson) {
        if (webjookJson.has(LABEL)) {
            return webjookJson.get(LABEL).get(NAME).asText();
        }
        return "";
    }
}
