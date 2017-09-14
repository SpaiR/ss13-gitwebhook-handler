package io.github.spair.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestOperations;

import java.io.UnsupportedEncodingException;
import java.util.*;

@Service
public class GitHubService {

    @Autowired
    private RestOperations restOperations;
    @Autowired
    private ConfigService configService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubService.class);
    private static Map<String, String> FILE_PATH_SHA = new HashMap<>();

    private static final String API_PATH = "https://api.github.com";

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

    public void createReview(int pullRequestNumber, String message) {
        Map<String, String> requestBody = new HashMap<>();

        requestBody.put("event", "COMMENT");
        requestBody.put("body", message);

        restOperations.exchange(getPullsApiPath() + "/" + pullRequestNumber + "/reviews",
                HttpMethod.POST, new HttpEntity<>(requestBody, getHttpHeaders()), Object.class);
    }

    public void addLabel(int issueNum, String labelName) {
        List<String> labelList = Collections.singletonList(labelName);

        restOperations.exchange(getIssuesApiPath() + "/" + issueNum + "/labels",
                HttpMethod.POST, new HttpEntity<>(labelList, getHttpHeaders()), Object.class);
    }

    public void removeLabel(int issueName, String labelName) {
        restOperations.exchange(getIssuesApiPath()+ "/" + issueName + "/labels/" + labelName,
                HttpMethod.DELETE, new HttpEntity<>(getHttpHeaders()), Object.class);
    }

    public List<String> getIssueLabels(int issueNum) {
        List responseList = restOperations.exchange(getIssuesApiPath() + "/" + issueNum + "/labels",
                HttpMethod.GET, new HttpEntity<>(getHttpHeaders()), ArrayList.class).getBody();

        List<String> labels = new ArrayList<>();

        for (Object responseElement : responseList) {
            Map elementMap = (HashMap) responseElement;
            labels.add((String) elementMap.get("name"));
        }

        return labels;
    }

    boolean isOrgAndRepoExist(String org, String repo) {
        try {
            restOperations.getForEntity(API_PATH + "/repos/" + org + "/" + repo, null);
        } catch (HttpStatusCodeException e) {
            return false;
        }

        return true;
    }

    boolean isFilePathExist(String org, String repo, String path) {
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
        String githubToken = configService.getGitHubConfig().getToken();

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
        String orgName = configService.getGitHubConfig().getOrganizationName();
        String repoName = configService.getGitHubConfig().getRepositoryName();
        return API_PATH + "/repos/" + orgName + "/" + repoName + "/contents";
    }

    private String getPullsApiPath() {
        String orgName = configService.getGitHubConfig().getOrganizationName();
        String repoName = configService.getGitHubConfig().getRepositoryName();
        return API_PATH + "/repos/" + orgName + "/" + repoName + "/pulls";
    }

    private String getIssuesApiPath() {
        String orgName = configService.getGitHubConfig().getOrganizationName();
        String repoName = configService.getGitHubConfig().getRepositoryName();
        return API_PATH + "/repos/" + orgName + "/" + repoName + "/issues";
    }
}
