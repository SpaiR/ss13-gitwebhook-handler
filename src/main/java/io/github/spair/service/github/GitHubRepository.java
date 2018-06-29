package io.github.spair.service.github;

import io.github.spair.service.config.ConfigService;
import io.github.spair.service.pr.entity.PullRequest;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.FileSystemUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.eclipse.jgit.lib.ConfigConstants.*;
import static org.eclipse.jgit.lib.Constants.*;

@Repository
public class GitHubRepository {

    private static final String REPOS_FOLDER_DIR = ".repos";
    private static final String FORK_FOLDER_TMPL = "fork-author:%s-branch:%s-pr:%d";

    private static final String MASTER_REPO = ".master";
    private static final String MASTER_REPO_PATH = REPOS_FOLDER_DIR + File.separator + MASTER_REPO;

    private static final String MASTER_REMOTE = "master-remote";
    private static final String MASTER_REMOTE_URL = "../" + MASTER_REPO;
    private static final String MASTER_REMOTE_FETCH = "+" + R_HEADS + MASTER + ":" + R_REMOTES + MASTER;

    private final ConfigService configService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubRepository.class);

    @Autowired
    public GitHubRepository(final ConfigService configService) {
        this.configService = configService;
    }

    @PostConstruct
    private void initReposFolder() throws IOException {
        File repoFolder = new File(REPOS_FOLDER_DIR);
        if (!repoFolder.exists() && !repoFolder.mkdir()) {
            throw new IOException("Error on '.repos' directory creation");
        }
    }

    public void initMasterRepo() {
        deleteRepositoryIfExists(new File(MASTER_REPO_PATH));
        loadMasterRepository();
    }

    public File loadMasterRepository() {
        File repoRoot = new File(MASTER_REPO_PATH);

        if (!repoRoot.exists()) {
            final String orgName = configService.getConfig().getGitHubConfig().getOrganizationName();
            final String repoName = configService.getConfig().getGitHubConfig().getRepositoryName();
            cloneRepository(orgName, repoName, MASTER, MASTER_REPO);
        } else {
            pullRepository(repoRoot);
        }

        return repoRoot;
    }

    public File loadForkRepository(final PullRequest pullRequest) {
        File repoRoot = new File(formRepositoryFolderName(pullRequest));

        if (!repoRoot.exists()) {
            final String repoName = configService.getConfig().getGitHubConfig().getRepositoryName();
            cloneRepository(pullRequest.getAuthor(), repoName, pullRequest.getBranchName(), repoRoot.getName());
            configForkRepository(repoRoot);
        } else {
            pullRepository(repoRoot);
        }

        return repoRoot;
    }

    public boolean mergeForkWithMaster(final File forkRepoRoot) {
        try {
            Git git = Git.open(forkRepoRoot);

            PullCommand pullCommand = git.pull().setRebase(true).setRemote(MASTER_REMOTE).setRemoteBranchName(MASTER);
            PullResult pullResult = pullCommand.call();

            if (!pullResult.isSuccessful()) {
                git.rebase().setOperation(RebaseCommand.Operation.ABORT).call();
                return false;
            }
            return true;
        } catch (IOException | GitAPIException e) {
            LOGGER.error("Error on merging fork {} with master repo", forkRepoRoot.getName(), e);
            throw new RuntimeException(e);
        }
    }

    public void deleteRepositoryForPullRequest(final PullRequest pullRequest) {
        deleteRepositoryIfExists(new File(formRepositoryFolderName(pullRequest)));
    }

    private void deleteRepositoryIfExists(final File repoRoot) {
        if (repoRoot.exists()) {
            FileSystemUtils.deleteRecursively(repoRoot);
        }
    }

    public void cleanReposFolder() {
        final File repos = new File(REPOS_FOLDER_DIR);
        final File[] reposList = repos.listFiles();

        if (reposList != null && reposList.length > 0) {
            for (File repoRoot : reposList) {
                FileSystemUtils.deleteRecursively(repoRoot);
            }
        }
    }

    private void cloneRepository(final String org, final String repo, final String branch, final String folder) {
        try {
            final String repoURI = String.format(GitHubConstants.PATH + "/%s/%s", org, repo);
            final String repoDir = REPOS_FOLDER_DIR + File.separator + folder;
            Git.cloneRepository().setURI(repoURI).setBranch(branch).setDirectory(new File(repoDir)).call();
        } catch (GitAPIException e) {
            LOGGER.error("Error on cloning '{}/{}' repository", org, repo, e);
            throw new RuntimeException(e);
        }
    }

    private void pullRepository(final File repoRoot) {
        try {
            Git.open(repoRoot).pull().call();
        } catch (IOException | GitAPIException e) {
            LOGGER.error("Error on pulling repository in {}", repoRoot.getName(), e);
            throw new RuntimeException(e);
        }
    }

    private void configForkRepository(final File repoRoot) {
        try {
            StoredConfig config = Git.open(repoRoot).getRepository().getConfig();
            config.setString(CONFIG_REMOTE_SECTION, MASTER_REMOTE, CONFIG_KEY_URL, MASTER_REMOTE_URL);
            config.setString(CONFIG_REMOTE_SECTION, MASTER_REMOTE, CONFIG_FETCH_SECTION, MASTER_REMOTE_FETCH);
            config.save();
        } catch (IOException e) {
            LOGGER.error("Error on configuration of {} fork repository", repoRoot.getName(), e);
            throw new UncheckedIOException(e);
        }
    }

    private String formRepositoryFolderName(final PullRequest pullRequest) {
        final String author = pullRequest.getAuthor();
        final String branch = pullRequest.getBranchName();
        final int number = pullRequest.getNumber();
        return REPOS_FOLDER_DIR + "/" + String.format(FORK_FOLDER_TMPL, author, branch, number);
    }
}
