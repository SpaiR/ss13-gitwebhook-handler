package io.github.spair.service.github;

import io.github.spair.service.config.ConfigService;
import io.github.spair.service.git.CloneMonitor;
import io.github.spair.service.git.GitConstants;
import io.github.spair.service.git.GitService;
import io.github.spair.service.pr.entity.PullRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.FileSystemUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Repository
public class GitHubRepository {

    private static final String FORK_FOLDER_TMPL = "fork.author=%s.pr=%d.branch=%s";

    private final ConfigService configService;
    private final GitService gitService;

    private final Map<String, Lock> repoLocks = new HashMap<>();

    @Autowired
    public GitHubRepository(final ConfigService configService, final GitService gitService) {
        this.configService = configService;
        this.gitService = gitService;
    }

    @PostConstruct
    private void initReposFolder() throws IOException {
        File repoFolder = new File(GitConstants.REPOS_FOLDER);
        if (!repoFolder.exists() && !repoFolder.mkdir()) {
            throw new IOException("Error on '.repos' directory creation");
        }
    }

    public void initMasterRepository() {
        deleteRepositoryIfExists(new File(GitConstants.MASTER_REPO_PATH));
        loadMasterRepository();
    }

    public File loadMasterRepository() {
        File masterFolder = new File(GitConstants.MASTER_REPO_PATH);

        doWithRepoLock(masterFolder.getName(), () -> {
            if (!masterFolder.exists()) {
                final String orgName = configService.getConfig().getGitHubConfig().getOrganizationName();
                final String repoName = configService.getConfig().getGitHubConfig().getRepositoryName();

                gitService.cloneRepository(orgName, repoName, GitConstants.MASTER, masterFolder);
            } else {
                gitService.pullRepository(masterFolder);
            }
        });

        return masterFolder;
    }

    public File loadForkRepository(
            final PullRequest pullRequest, final Consumer<Integer> updateCallback, final Runnable endCallback) {
        final File forkFolder = new File(formRepositoryFolderPath(pullRequest));

        doWithRepoLock(forkFolder.getName(), () -> {
            if (!forkFolder.exists()) {
                final String author = pullRequest.getAuthor();
                final String repoName = configService.getConfig().getGitHubConfig().getRepositoryName();
                final String branchName = pullRequest.getBranchName();
                final CloneMonitor cloneMonitor = new CloneMonitor(updateCallback, endCallback);

                gitService.cloneRepository(author, repoName, branchName, forkFolder, cloneMonitor);
                gitService.configRepositoryRemote(forkFolder);
            } else {
                gitService.pullRepository(forkFolder);
            }
        });

        return forkFolder;
    }

    public boolean mergeForkWithMaster(final File forkRepoRoot) {
        return returnWithRepoLock(forkRepoRoot.getName(), () -> gitService.mergeWithLocalMaster(forkRepoRoot));
    }

    public void deleteForkRepository(final PullRequest pullRequest) {
        File repoFolder = new File(formRepositoryFolderPath(pullRequest));
        doWithRepoLock(repoFolder.getName(), () -> {
            deleteRepositoryIfExists(repoFolder);
            repoLocks.remove(repoFolder.getName());
        });
    }

    public void cleanReposFolder() {
        final File repos = new File(GitConstants.REPOS_FOLDER);
        final File[] reposList = repos.listFiles();

        if (reposList != null && reposList.length > 0) {
            for (File repoRoot : reposList) {
                FileSystemUtils.deleteRecursively(repoRoot);
            }
        }
    }

    private void deleteRepositoryIfExists(final File repoRoot) {
        if (repoRoot.exists()) {
            FileSystemUtils.deleteRecursively(repoRoot);
        }
    }

    private String formRepositoryFolderPath(final PullRequest pullRequest) {
        final String author = pullRequest.getAuthor();
        final String branch = pullRequest.getBranchName();
        final int number = pullRequest.getNumber();
        return GitConstants.REPOS_FOLDER + "/" + String.format(FORK_FOLDER_TMPL, author, number, branch);
    }

    private void doWithRepoLock(final String repoName, final Runnable action) {
        Lock repoLock = getRepoLock(repoName);
        try {
            repoLock.lock();
            action.run();
        } finally {
            repoLock.unlock();
        }
    }

    private <T> T returnWithRepoLock(final String repoName, final Supplier<T> action) {
        Lock repoLock = getRepoLock(repoName);
        try {
            repoLock.lock();
            return action.get();
        } finally {
            repoLock.unlock();
        }
    }

    private synchronized Lock getRepoLock(final String repoName) {
        Lock lock = repoLocks.get(repoName);
        if (lock == null) {
            lock = new ReentrantLock();
            repoLocks.put(repoName, lock);
        }
        return lock;
    }
}
