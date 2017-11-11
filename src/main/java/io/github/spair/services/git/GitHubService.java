package io.github.spair.services.git;

import io.github.spair.services.config.ConfigService;
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
import java.util.*;

@Service
public class GitHubService {

    private final RestOperations restOperations;
    private final ConfigService configService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubService.class);
    private static Map<String, String> FILE_PATH_SHA = new HashMap<>();

    private static final String API_PATH = "https://api.github.com";

    @Autowired
    public GitHubService(RestOperations restOperations, ConfigService configService) {
        this.restOperations = restOperations;
        this.configService = configService;
    }

    public String readFile(String path) {
        HashMap responseMap = restOperations.exchange(
                getContentsApiPath() + path, HttpMethod.GET, new HttpEntity<>(getHttpHeaders()), HashMap.class
        ).getBody();

        FILE_PATH_SHA.put(path, (String) responseMap.get("sha"));

        return decodeContent((String) responseMap.get("content"));
    }

    public void updateFile(String path, String updateMessage, String content) {
        Map<String, String> requestBody = new HashMap<>();

        requestBody.put("path", path);
        requestBody.put("message", updateMessage);
        requestBody.put("content", encodeContent(content));
        requestBody.put("sha", FILE_PATH_SHA.remove(path));

        restOperations.exchange(getContentsApiPath() + path,
                HttpMethod.PUT, new HttpEntity<>(requestBody, getHttpHeaders()), HashMap.class);
    }

    public void addReviewComment(int pullRequestNumber, String message) {
        Map<String, String> requestBody = new HashMap<>();

        requestBody.put("event", "COMMENT");
        requestBody.put("body", message);

        restOperations.exchange(getPullsApiPath() + "/" + pullRequestNumber + "/reviews",
                HttpMethod.POST, new HttpEntity<>(requestBody, getHttpHeaders()), Object.class);
    }

    public void addLabel(int issueNum, String labelName) {
        addLabels(issueNum, Collections.singletonList(labelName));
    }

    @SuppressWarnings("WeakerAccess")
    public void addLabels(int issueNum, List<String> labels) {
        restOperations.exchange(getIssuesApiPath() + "/" + issueNum + "/labels",
                HttpMethod.POST, new HttpEntity<>(labels, getHttpHeaders()), Object.class);
    }

    public void removeLabel(int issueNum, String labelName) {
        try {
            restOperations.exchange(getIssuesApiPath() + "/" + issueNum + "/labels/" + labelName,
                    HttpMethod.DELETE, new HttpEntity<>(getHttpHeaders()), Object.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                LOGGER.warn("Removing label fail. Issue number: " + issueNum + ". Label name: " + labelName + ". " +
                        "Response: " + e.getResponseBodyAsString() + ". Headers: " + e.getResponseHeaders());
            }
        }
    }

    public List<String> getIssueLabels(int issueNum) {
        List responseList = new ArrayList();

        try {
            responseList = restOperations.exchange(getIssuesApiPath() + "/" + issueNum + "/labels",
                    HttpMethod.GET, new HttpEntity<>(getHttpHeaders()), ArrayList.class).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                LOGGER.warn("Getting issue labels fail. Issue number: " + issueNum + ". " +
                        "Response: " + e.getResponseBodyAsString() + ". Headers: " + e.getResponseHeaders());
            }
        }

        List<String> labels = new ArrayList<>();

        for (Object responseElement : responseList) {
            Map elementMap = (HashMap) responseElement;
            labels.add((String) elementMap.get("name"));
        }

        return labels;
    }

    @SuppressWarnings("WeakerAccess")
    public String getPullRequestDiff(String diffLink) {
        return restOperations.getForObject(diffLink, String.class);
    }

    public boolean isOrgAndRepoExist(String org, String repo) {
        try {
            restOperations.getForEntity(API_PATH + "/repos/" + org + "/" + repo, null);
        } catch (HttpStatusCodeException e) {
            return false;
        }

        return true;
    }

    public boolean isFilePathExist(String org, String repo, String path) {
        try {
            restOperations.getForEntity(API_PATH + "/repos/" + org + "/" + repo + "/contents" + path, null);
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
        httpHeaders.set(HttpHeaders.AUTHORIZATION, "token " + githubToken);

        return httpHeaders;
    }

    private String encodeContent(String content) {
        return Base64.getEncoder().encodeToString(content.getBytes());
    }

    private String decodeContent(String encodedContent) {
        try {
            byte[] base64decodedBytes = Base64.getMimeDecoder().decode(encodedContent);
            return new String(base64decodedBytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Decoding github response content error", e);
            throw new RuntimeException(e);
        }
    }

    private String getContentsApiPath() {
        String orgName = configService.getConfig().getGitHubConfig().getOrganizationName();
        String repoName = configService.getConfig().getGitHubConfig().getRepositoryName();
        return API_PATH + "/repos/" + orgName + "/" + repoName + "/contents";
    }

    private String getPullsApiPath() {
        String orgName = configService.getConfig().getGitHubConfig().getOrganizationName();
        String repoName = configService.getConfig().getGitHubConfig().getRepositoryName();
        return API_PATH + "/repos/" + orgName + "/" + repoName + "/pulls";
    }

    private String getIssuesApiPath() {
        String orgName = configService.getConfig().getGitHubConfig().getOrganizationName();
        String repoName = configService.getConfig().getGitHubConfig().getRepositoryName();
        return API_PATH + "/repos/" + orgName + "/" + repoName + "/issues";
    }
}
