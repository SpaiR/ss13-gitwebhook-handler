package io.github.spair.service.git;

import io.github.spair.service.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class GitHubPathProvider {

    private final ConfigService configService;

    private static final String REPOS = "repos";
    private static final String LABELS = "labels";
    private static final String CONTENTS = "contents";
    private static final String PULLS = "pulls";
    private static final String ISSUES = "issues";
    private static final String FILES = "files";
    private static final String COMMENTS = "comments";
    private static final String GIT_BLOBS = "git/blobs";

    @Autowired
    GitHubPathProvider(final ConfigService configService) {
        this.configService = configService;
    }

    // https://api.github.com/repos/OrgName/RepoName
    String generalPath(final String orgName, final String repoName) {
        return GitHubConstants.API + "/" + REPOS + "/" + orgName + "/" + repoName;
    }

    // https://api.github.com/repos/OrgName/RepoName/contents/path/to/content
    String contents(final String relPath) {
        return getContentsApiPath() + relPath;
    }

    // https://api.github.com/repos/OrgName/RepoName/contents/path/to/content
    String contents(final String orgName, final String repoName, final String relPath) {
        return getContentsApiPath(orgName, repoName) + relPath;
    }

    // https://api.github.com/repos/OrgName/RepoName/git/blobs/sha1hash
    String blobs(final String fileSha) {
        return getBlobsApiPath() + "/" + fileSha;
    }

    // https://api.github.com/repos/OrgName/RepoName/issues/1/labels
    String issueLabels(final int issueNum) {
        return getIssuesApiPath() + "/" + issueNum + "/" + LABELS;
    }

    // https://api.github.com/repos/OrgName/RepoName/issues/1/labels/LabelName
    String issueLabel(final int issueNum, final String labelName) {
        return getIssuesApiPath() + "/" + issueNum + "/" + LABELS + "/" + labelName;
    }

    // https://api.github.com/repos/OrgName/RepoName/issues/1/comments
    String issueComments(final int issueNum) {
        return getIssuesApiPath() + "/" + issueNum + "/" + COMMENTS;
    }

    // https://api.github.com/repos/OrgName/RepoName/issues/comments/123
    String issueComment(final int commentId) {
        return getIssuesApiPath() + "/" + COMMENTS + "/" + commentId;
    }

    // https://api.github.com/repos/OrgName/RepoName/pulls/1/files
    String pullFiles(final int prNum) {
        return getPullsApiPath() + "/" + prNum + "/" + FILES;
    }

    private String getContentsApiPath() {
        return getGeneralPath() + "/" + CONTENTS;
    }

    private String getContentsApiPath(final String orgName, final String repoName) {
        return generalPath(orgName, repoName) + "/" + CONTENTS;
    }

    private String getBlobsApiPath() {
        return getGeneralPath() + "/" + GIT_BLOBS;
    }

    private String getPullsApiPath() {
        return getGeneralPath() + "/" + PULLS;
    }

    private String getIssuesApiPath() {
        return getGeneralPath() + "/" + ISSUES;
    }

    private String getGeneralPath() {
        String orgName = configService.getConfig().getGitHubConfig().getOrganizationName();
        String repoName = configService.getConfig().getGitHubConfig().getRepositoryName();
        return generalPath(orgName, repoName);
    }
}
