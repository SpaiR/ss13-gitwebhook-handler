package io.github.spair.service.git;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.service.EnumUtil;
import io.github.spair.service.RestService;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.git.entities.IssueComment;
import io.github.spair.service.git.entities.PullRequestFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GitHubService {

    private final ConfigService configService;
    private final GitHubPathProvider pathProvider;
    private final RestService restService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubService.class);

    @Autowired
    public GitHubService(ConfigService configService, GitHubPathProvider pathProvider, RestService restService) {
        this.configService = configService;
        this.pathProvider = pathProvider;
        this.restService = restService;
    }

    public String readDecodedFile(String relPath) {
        return decodeContent(readFile(relPath));
    }

    public String readEncodedFile(String relPath) {
        return readFile(relPath);
    }

    private String readFile(String relPath) {
        ObjectNode resp;

        try {
            resp = restService.getForJson(pathProvider.contents(relPath), getHttpHeaders());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                LOGGER.info("Blobs API was used to read file data. Path: {}", relPath);
                resp = restService.getForJson(pathProvider.blobs(getFileSha(relPath)), getHttpHeaders());
            } else {
                LOGGER.error("Error on reading file from GitHub. Path: {}", relPath);
                throw e;
            }
        }

        return resp.get(GitHubPayload.CONTENT).asText();
    }

    public void updateFile(String path, String updateMessage, String content) {
        Map<String, String> requestBody = new HashMap<>();

        requestBody.put(GitHubPayload.PATH, path);
        requestBody.put(GitHubPayload.MESSAGE, updateMessage);
        requestBody.put(GitHubPayload.CONTENT, encodeContent(content));
        requestBody.put(GitHubPayload.SHA, getFileSha(path));

        restService.put(pathProvider.contents(path), requestBody, getHttpHeaders());
    }

    public void createIssueComment(int issueNum, String message) {
        Map<String, String> requestBody = Collections.singletonMap(GitHubPayload.BODY, message);
        restService.post(pathProvider.issueComments(issueNum), requestBody, getHttpHeaders());
    }

    public void editIssueComment(int commentId, String newMessage) {
        Map<String, String> requestBody = Collections.singletonMap(GitHubPayload.BODY, newMessage);
        restService.patch(pathProvider.issueComment(commentId), requestBody, getHttpHeaders());
    }

    public void deleteIssueComment(int commentId) {
        restService.delete(pathProvider.issueComment(commentId), getHttpHeaders());
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
        ArrayNode respList = new ArrayNode(JsonNodeFactory.instance);

        try {
            respList = restService.getForObject(pathProvider.issueLabels(issueNum), getHttpHeaders(), ArrayNode.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                LOGGER.warn("Removing label fail. Issue number: {}. Response: {}. Headers: {}",
                        issueNum, e.getResponseBodyAsString(), e.getResponseHeaders());
            }
        }

        List<String> labels = new ArrayList<>();
        respList.forEach(respNode -> labels.add(respNode.get(GitHubPayload.NAME).asText()));

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
        List<PullRequestFile> pullRequestFiles = new ArrayList<>();

        new LinkProcessor(
                respArray -> respArray.forEach(nodeObject -> {
                    PullRequestFile pullRequestFile = new PullRequestFile();

                    pullRequestFile.setSha(nodeObject.get(GitHubPayload.SHA).asText());
                    pullRequestFile.setFilename(nodeObject.get(GitHubPayload.FILENAME).asText());
                    pullRequestFile.setStatus(
                            EnumUtil.valueOfOrDefault(
                                    PullRequestFile.Status.values(),
                                    nodeObject.get(GitHubPayload.STATUS).asText(),
                                    PullRequestFile.Status.UNDEFINED)
                    );
                    pullRequestFile.setRawUrl(nodeObject.get(GitHubPayload.RAW_URL).asText());

                    pullRequestFiles.add(pullRequestFile);
                })
        ).recursiveProcess(pathProvider.pullFiles(pullRequestNumber));

        return pullRequestFiles;
    }

    public List<IssueComment> listIssueComments(int issueNum) {
        List<IssueComment> issueComments = new ArrayList<>();

        new LinkProcessor(
                respArray -> respArray.forEach(nodeObject -> {
                    IssueComment issueComment = new IssueComment();

                    issueComment.setId(nodeObject.get(GitHubPayload.ID).asInt());
                    issueComment.setUserName(nodeObject.get(GitHubPayload.USER).get(GitHubPayload.LOGIN).asText());
                    issueComment.setBody(nodeObject.get(GitHubPayload.BODY).asText());

                    issueComments.add(issueComment);
                })
        ).recursiveProcess(pathProvider.issueComments(issueNum));

        return issueComments;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();

        String agentName = configService.getConfig().getRequestAgentName();
        String githubToken = configService.getConfig().getGitHubConfig().getToken();

        httpHeaders.set(HttpHeaders.USER_AGENT, agentName);
        httpHeaders.set(HttpHeaders.AUTHORIZATION, "token ".concat(githubToken));

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
            LOGGER.error("Error on decoding GitHub content", e);
            throw new RuntimeException(e);
        }
    }

    private String getFileSha(String relPath) {
        final Matcher partedPath = Pattern.compile("(.*/)(.*)").matcher(relPath);

        final String dirPath;
        final String fileName;

        if (partedPath.find()) {
            dirPath = partedPath.group(1);
            fileName = partedPath.group(2);
        } else {
            dirPath = "/";
            fileName = relPath;
        }

        ArrayNode responseArr = restService.getForObject(
                pathProvider.contents(dirPath), getHttpHeaders(), ArrayNode.class);

        for (JsonNode node : responseArr) {
            if (node.get(GitHubPayload.NAME).asText().equals(fileName)) {
                return node.get(GitHubPayload.SHA).asText();
            }
        }

        LOGGER.error("The file SHA was not found. Argument: {}. Dir path: {}. File name: {}", relPath, dirPath, fileName);
        throw new IllegalArgumentException("Exception on getting file sha");
    }

    private class LinkProcessor {

        private final String LINK = "link";
        private final Pattern NEXT_PR_FILES = Pattern.compile("<([\\w\\d/?=:.]*)>;\\srel=\"next\"");
        private final Consumer<ArrayNode> consumer;

        private LinkProcessor(Consumer<ArrayNode> consumer) {
            this.consumer = consumer;
        }

        private void recursiveProcess(String link) {
            ResponseEntity<ArrayNode> resp = restService.getForEntity(link, getHttpHeaders(), ArrayNode.class);

            consumer.accept(resp.getBody());

            String headerLinks = resp.getHeaders().getOrDefault(LINK, Collections.emptyList()).toString();
            Matcher nextLink = NEXT_PR_FILES.matcher(headerLinks);

            if (nextLink.find()) {
                recursiveProcess(nextLink.group(1));
            }
        }
    }
}
