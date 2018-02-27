package io.github.spair.service.git;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.service.EnumUtil;
import io.github.spair.service.RestService;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.git.entities.PullRequestFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GitHubService {

    private final ConfigService configService;
    private final GitHubPathProvider pathProvider;
    private final RestService restService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubService.class);
    private static final String TOKEN = "token";

    @Autowired
    public GitHubService(ConfigService configService, GitHubPathProvider pathProvider, RestService restService) {
        this.configService = configService;
        this.pathProvider = pathProvider;
        this.restService = restService;
    }

    public String readTextFile(String relPath) {
        ObjectNode responseMap = restService.getForJson(pathProvider.contents(relPath), getHttpHeaders());
        return decodeContent(responseMap.get(GitHubPayload.Fields.CONTENT).asText());
    }

    public void updateFile(String path, String updateMessage, String content) {
        Map<String, String> requestBody = new HashMap<>();

        requestBody.put(GitHubPayload.Fields.PATH, path);
        requestBody.put(GitHubPayload.Fields.MESSAGE, updateMessage);
        requestBody.put(GitHubPayload.Fields.CONTENT, encodeContent(content));
        requestBody.put(GitHubPayload.Fields.SHA, getFileSha(path));

        restService.put(pathProvider.contents(path), requestBody, getHttpHeaders());
    }

    public void addReviewComment(int pullRequestNumber, String message) {
        Map<String, String> requestBody = new HashMap<>();

        requestBody.put(GitHubPayload.Fields.EVENT, GitHubPayload.Values.COMMENT);
        requestBody.put(GitHubPayload.Fields.BODY, message);

        restService.post(pathProvider.pullReviews(pullRequestNumber), requestBody, getHttpHeaders());
    }

    public void addLabel(int issueNum, String labelName) {
        addLabels(issueNum, Collections.singletonList(labelName));
    }

    public void addLabels(int issueNum, List<String> labels) {
        restService.post(pathProvider.issueLabels(issueNum), labels, getHttpHeaders());
    }

    public void removeLabel(int issueNum, String labelName) {
        try {
            restService.delete(pathProvider.issueLabel(issueNum, labelName), getHttpHeaders());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                LOGGER.warn("Removing label fail. Issue number: {}. Label name: {}. Response: {}. Headers: {}",
                        issueNum, labelName, e.getResponseBodyAsString(), e.getResponseHeaders());
            }
        }
    }

    public List<String> listIssueLabels(int issueNum) {
        List responseList = new ArrayList();

        try {
            responseList = restService.getForObject(pathProvider.issueLabels(issueNum), getHttpHeaders(), ArrayList.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                LOGGER.warn("Removing label fail. Issue number: {}. Response: {}. Headers: {}",
                        issueNum, e.getResponseBodyAsString(), e.getResponseHeaders());
            }
        }

        List<String> labels = new ArrayList<>();

        for (Object responseElement : responseList) {
            Map elementMap = (HashMap) responseElement;
            labels.add((String) elementMap.get(GitHubPayload.Fields.NAME));
        }

        return labels;
    }

    public boolean isOrgAndRepoExist(String org, String repo) {
        try {
            restService.get(pathProvider.generalPath(org, repo), getHttpHeaders());
        } catch (HttpStatusCodeException e) {
            return false;
        }
        return true;
    }

    public boolean isFilePathExist(String org, String repo, String relPath) {
        try {
            restService.get(pathProvider.contents(org, repo, relPath), getHttpHeaders());
        } catch (HttpStatusCodeException e) {
            return false;
        }
        return true;
    }

    public List<PullRequestFile> listPullRequestFiles(int pullRequestNumber) {
        class LinkProcessor {

            private final List<PullRequestFile> requestFileList;
            private final String LINK = "link";
            private final Pattern NEXT_PR_FILES = Pattern.compile("<([\\w\\d/?=:.]*)>;\\srel=\"next\"");

            private LinkProcessor(List<PullRequestFile> requestFileList) {
                this.requestFileList = requestFileList;
            }

            private void recursiveProcess(String link) {
                ResponseEntity<ArrayNode> res = restService.getForEntity(link, getHttpHeaders(), ArrayNode.class);

                res.getBody().forEach(node -> {
                    PullRequestFile pullRequestFile = new PullRequestFile();

                    pullRequestFile.setSha(node.get(GitHubPayload.Fields.SHA).asText());
                    pullRequestFile.setFilename(node.get(GitHubPayload.Fields.FILENAME).asText());
                    pullRequestFile.setStatus(
                            EnumUtil.valueOfOrDefault(
                                    PullRequestFile.Status.values(),
                                    node.get(GitHubPayload.Fields.STATUS).asText(),
                                    PullRequestFile.Status.UNDEFINED)
                    );
                    pullRequestFile.setRawUrl(node.get(GitHubPayload.Fields.RAW_URL).asText());

                    requestFileList.add(pullRequestFile);
                });

                String headerLinks = res.getHeaders().getOrDefault(LINK, Collections.emptyList()).toString();
                Matcher nextLink = NEXT_PR_FILES.matcher(headerLinks);

                if (nextLink.find()) {
                    recursiveProcess(nextLink.group(1));
                }
            }
        }

        List<PullRequestFile> pullRequestFiles = new ArrayList<>();

        LinkProcessor linkProcessor = new LinkProcessor(pullRequestFiles);
        linkProcessor.recursiveProcess(pathProvider.pullFiles(pullRequestNumber));

        return pullRequestFiles;
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
        ObjectNode responseMap = restService.getForJson(pathProvider.contents(relPath), getHttpHeaders());
        return responseMap.get(GitHubPayload.Fields.SHA).asText();
    }
}
