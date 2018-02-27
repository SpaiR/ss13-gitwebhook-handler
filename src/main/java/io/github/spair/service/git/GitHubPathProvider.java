package io.github.spair.service.git;

import io.github.spair.service.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class GitHubPathProvider {

    private final ConfigService configService;

    private static final String REPOS = "repos";
    private static final String REVIEWS = "reviews";
    private static final String LABELS = "labels";
    private static final String CONTENTS = "contents";
    private static final String PULLS = "pulls";
    private static final String ISSUES = "issues";
    private static final String FILES = "files";

    @Autowired
    GitHubPathProvider(ConfigService configService) {
        this.configService = configService;
    }

    // https://api.github.com/repos/OrgName/RepoName
    String generalPath(String orgName, String repoName) {
        return GitHubConstants.API + "/" + REPOS + "/" + orgName + "/" + repoName;
    }

    // https://api.github.com/repos/OrgName/RepoName/contents/path/to/content
    String contents(String relPath) {
        return getContentsApiPath() + relPath;
    }

    // https://api.github.com/repos/OrgName/RepoName/contents/path/to/content
    String contents(String orgName, String repoName, String relPath) {
        return getContentsApiPath(orgName, repoName) + relPath;
    }

    // https://api.github.com/repos/OrgName/RepoName/pulls/1/reviews
    String pullReviews(int prNum) {
        return getPullsApiPath() + "/" + prNum + "/" + REVIEWS;
    }

    // https://api.github.com/repos/OrgName/RepoName/issues/1/labels
    String issueLabels(int issueNum) {
        return getIssuesApiPath() + "/" + issueNum + "/" + LABELS;
    }

    // https://api.github.com/repos/OrgName/RepoName/issues/1/labels/LabelName
    String issueLabel(int issueNum, String labelName) {
        return getIssuesApiPath() + "/" + issueNum + "/" + LABELS + "/" + labelName;
    }

    // https://api.github.com/repos/OrgName/RepoName/pulls/1/files
    String pullFiles(int prNum) {
        return getPullsApiPath() + "/" + prNum + "/" + FILES;
    }

    private String getContentsApiPath() {
        return getGeneralPath() + "/" + CONTENTS;
    }

    private String getContentsApiPath(String orgName, String repoName) {
        return generalPath(orgName, repoName) + "/" + CONTENTS;
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
