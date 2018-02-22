package io.github.spair.service.git;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.service.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GitHubService {

    private final RestOperations restOperations;
    private final ConfigService configService;
    private final GitHubPathProvider pathProvider;

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubService.class);
    private static final String TOKEN = "token";

    @Autowired
    public GitHubService(RestOperations restOperations, ConfigService configService, GitHubPathProvider pathProvider) {
        this.restOperations = restOperations;
        this.configService = configService;
        this.pathProvider = pathProvider;
    }

    public String readTextFile(String relPath) {
        ObjectNode responseMap = restOperations.exchange(
                pathProvider.contents(relPath), HttpMethod.GET, new HttpEntity<>(getHttpHeaders()), ObjectNode.class
        ).getBody();

        return decodeContent(responseMap.get(GitHubPayloadFields.CONTENT).asText());
    }

    public void updateFile(String path, String updateMessage, String content) {
        Map<String, String> requestBody = new HashMap<>();

        requestBody.put(GitHubPayloadFields.PATH, path);
        requestBody.put(GitHubPayloadFields.MESSAGE, updateMessage);
        requestBody.put(GitHubPayloadFields.CONTENT, encodeContent(content));
        requestBody.put(GitHubPayloadFields.SHA, getFileSha(path));

        restOperations.exchange(pathProvider.contents(path),
                HttpMethod.PUT, new HttpEntity<>(requestBody, getHttpHeaders()), HashMap.class);
    }

    public void addReviewComment(int pullRequestNumber, String message) {
        Map<String, String> requestBody = new HashMap<>();

        requestBody.put(GitHubPayloadFields.EVENT, GitHubReviewTypes.COMMENT);
        requestBody.put(GitHubPayloadFields.BODY, message);

        restOperations.exchange(pathProvider.pullReviews(pullRequestNumber),
                HttpMethod.POST, new HttpEntity<>(requestBody, getHttpHeaders()), Object.class);
    }

    public void addLabel(int issueNum, String labelName) {
        addLabels(issueNum, Collections.singletonList(labelName));
    }

    @SuppressWarnings("WeakerAccess")
    public void addLabels(int issueNum, List<String> labels) {
        restOperations.exchange(pathProvider.issueLabels(issueNum),
                HttpMethod.POST, new HttpEntity<>(labels, getHttpHeaders()), Object.class);
    }

    public void removeLabel(int issueNum, String labelName) {
        try {
            restOperations.exchange(pathProvider.issueLabel(issueNum, labelName),
                    HttpMethod.DELETE, new HttpEntity<>(getHttpHeaders()), Object.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                LOGGER.warn("Removing label fail. Issue number: {}. Label name: {}. Response: {}. Headers: {}",
                        issueNum, labelName, e.getResponseBodyAsString(), e.getResponseHeaders());
            }
        }
    }

    public List<String> getIssueLabels(int issueNum) {
        List responseList = new ArrayList();

        try {
            responseList = restOperations.exchange(pathProvider.issueLabels(issueNum),
                    HttpMethod.GET, new HttpEntity<>(getHttpHeaders()), ArrayList.class).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                LOGGER.warn("Removing label fail. Issue number: {}. Response: {}. Headers: {}",
                        issueNum, e.getResponseBodyAsString(), e.getResponseHeaders());
            }
        }

        List<String> labels = new ArrayList<>();

        for (Object responseElement : responseList) {
            Map elementMap = (HashMap) responseElement;
            labels.add((String) elementMap.get(GitHubPayloadFields.NAME));
        }

        return labels;
    }

    @SuppressWarnings("WeakerAccess")
    public String getPullRequestDiff(String diffLink) {
        return restOperations.getForObject(diffLink, String.class);
    }

    public boolean isOrgAndRepoExist(String org, String repo) {
        try {
            restOperations.getForEntity(pathProvider.generalPath(org, repo), null);
        } catch (HttpStatusCodeException e) {
            return false;
        }

        return true;
    }

    public boolean isFilePathExist(String org, String repo, String relPath) {
        try {
            restOperations.getForEntity(pathProvider.contents(org, repo, relPath), null);
        } catch (HttpStatusCodeException e) {
            return false;
        }

        return true;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();

        String agentName = configService.getConfig().getRequestAgentName();
        String githubToken = configService.getConfig().getGitHubConfig().getToken();

        httpHeaders.set(HttpHeaders.USER_AGENT, agentName);
        httpHeaders.set(HttpHeaders.AUTHORIZATION, String.format("%s %s", TOKEN, githubToken));

        return httpHeaders;
    }

    private String encodeContent(String content) {
        return Base64.getEncoder().encodeToString(content.getBytes());
    }

    private String decodeContent(String encodedContent) {
        try {
            byte[] base64decodedBytes = Base64.getMimeDecoder().decode(encodedContent);
            return new String(base64decodedBytes, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Decoding github response content error", e);
            throw new RuntimeException(e);
        }
    }

    private String getFileSha(String relPath) {
        ObjectNode responseMap = restOperations.exchange(
                pathProvider.contents(relPath), HttpMethod.GET, new HttpEntity<>(getHttpHeaders()), ObjectNode.class
        ).getBody();

        return responseMap.get(GitHubPayloadFields.SHA).asText();
    }
}
