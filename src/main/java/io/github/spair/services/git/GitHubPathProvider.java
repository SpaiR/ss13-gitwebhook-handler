package io.github.spair.services.git;

import io.github.spair.services.config.ConfigService;
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

    @Autowired
    GitHubPathProvider(ConfigService configService) {
        this.configService = configService;
    }

    String generalPath(String orgName, String repoName) {
        return GitHubConstants.API + "/" + REPOS + "/" + orgName + "/" + repoName;
    }

    String contents(String relPath) {
        return getContentsApiPath() + relPath;
    }

    String contents(String orgName, String repoName, String relPath) {
        return getContentsApiPath(orgName, repoName) + relPath;
    }

    String pullReviews(int prNum) {
        return getPullsApiPath() + "/" + prNum + "/" + REVIEWS;
    }

    String issueLabels(int issueNum) {
        return getIssuesApiPath() + "/" + issueNum + "/" + LABELS;
    }

    String issueLabel(int issueNum, String labelName) {
        return getIssuesApiPath() + "/" + issueNum + "/" + LABELS + "/" + labelName;
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
