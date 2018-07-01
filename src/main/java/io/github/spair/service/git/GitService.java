package io.github.spair.service.git;

import io.github.spair.service.github.GitHubConstants;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.RebaseCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.StoredConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import static io.github.spair.service.git.GitConstants.MASTER_REMOTE;
import static io.github.spair.service.git.GitConstants.MASTER_REMOTE_URL;
import static io.github.spair.service.git.GitConstants.MASTER_REMOTE_FETCH;
import static org.eclipse.jgit.lib.ConfigConstants.CONFIG_FETCH_SECTION;
import static org.eclipse.jgit.lib.ConfigConstants.CONFIG_KEY_URL;
import static org.eclipse.jgit.lib.ConfigConstants.CONFIG_REMOTE_SECTION;
import static org.eclipse.jgit.lib.Constants.MASTER;

@Service
public class GitService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitService.class);

    public void cloneRepository(final String org, final String repo, final String branch, final File folder) {
        final String repoURI = String.format(GitHubConstants.PATH + "/%s/%s", org, repo);
        try {
            Git.cloneRepository().setURI(repoURI).setBranch(branch).setDirectory(folder).call().close();
        } catch (GitAPIException e) {
            LOGGER.error("Error on cloning '{}' repository", repoURI, e);
            throw new RuntimeException(e);
        }
    }

    public void pullRepository(final File repoRoot) {
        try (Git repo = Git.open(repoRoot)) {
            repo.pull().call();
        } catch (IOException | GitAPIException e) {
            LOGGER.error("Error on pulling {} repository", repoRoot.getName(), e);
            throw new RuntimeException(e);
        }
    }

    public void configRepositoryRemote(final File repoRoot) {
        try (Git repo = Git.open(repoRoot)) {
            StoredConfig config = repo.getRepository().getConfig();
            config.setString(CONFIG_REMOTE_SECTION, MASTER_REMOTE, CONFIG_KEY_URL, MASTER_REMOTE_URL);
            config.setString(CONFIG_REMOTE_SECTION, MASTER_REMOTE, CONFIG_FETCH_SECTION, MASTER_REMOTE_FETCH);
            config.save();
        } catch (IOException e) {
            LOGGER.error("Error on remote configuration of {} repository", repoRoot.getName(), e);
            throw new UncheckedIOException(e);
        }
    }

    public boolean mergeWithLocalMaster(final File forkRepo) {
        try (Git git = Git.open(forkRepo)) {
            PullCommand pullCommand = git.pull().setRebase(true).setRemote(MASTER_REMOTE).setRemoteBranchName(MASTER);
            PullResult pullResult = pullCommand.call();

            if (!pullResult.isSuccessful()) {
                git.rebase().setOperation(RebaseCommand.Operation.ABORT).call();
                return false;
            }
            return true;
        } catch (IOException | GitAPIException e) {
            LOGGER.error("Error on merging {} with master repository", forkRepo.getName(), e);
            throw new RuntimeException(e);
        }
    }
}
