package io.github.spair.service.dme;

import io.github.spair.byond.dme.Dme;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.dme.entity.DmePair;
import io.github.spair.service.github.GitHubRepository;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.util.FutureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Component
public class DmePairGenerator {

    private final GitHubRepository gitHubRepository;
    private final ConfigService configService;
    private final DmeService dmeService;

    @Autowired
    public DmePairGenerator(
            final GitHubRepository gitHubRepository,
            final ConfigService configService,
            final DmeService dmeService) {
        this.gitHubRepository = gitHubRepository;
        this.configService = configService;
        this.dmeService = dmeService;
    }

    public Optional<DmePair> generate(final PullRequest pullRequest,
                                      @Nullable final Consumer<Integer> updateCallback,
                                      @Nullable final Runnable endCallback) {
        CompletableFuture<File> loadMasterFuture = getMasterRepoAsync();
        CompletableFuture<File> loadForkFuture = getForkRepoAsync(pullRequest, updateCallback, endCallback);
        FutureUtil.completeFutures(loadMasterFuture, loadForkFuture);

        final File master = FutureUtil.extractFuture(loadMasterFuture);
        final File fork = FutureUtil.extractFuture(loadForkFuture);

        if (!gitHubRepository.mergeForkWithMaster(fork)) {
            return Optional.empty();
        }

        final String pathToDme = configService.getConfig().getDmmBotConfig().getPathToDme();

        CompletableFuture<Dme> parseOldDmeFuture = getDmeAsync(master.getPath() + pathToDme);
        CompletableFuture<Dme> parseNewDmeFuture = getDmeAsync(fork.getPath() + pathToDme);
        FutureUtil.completeFutures(parseOldDmeFuture, parseNewDmeFuture);

        final Dme oldDme = FutureUtil.extractFuture(parseOldDmeFuture);
        final Dme newDme = FutureUtil.extractFuture(parseNewDmeFuture);

        return Optional.of(new DmePair(oldDme, newDme));
    }

    private CompletableFuture<File> getMasterRepoAsync() {
        return CompletableFuture.supplyAsync(gitHubRepository::loadMasterRepository);
    }

    private CompletableFuture<File> getForkRepoAsync(final PullRequest pullRequest,
                                                     @Nullable final Consumer<Integer> updateCallback,
                                                     @Nullable final Runnable endCallback) {
        return CompletableFuture.supplyAsync(() ->
                gitHubRepository.loadForkRepository(pullRequest, updateCallback, endCallback)
        );
    }

    private CompletableFuture<Dme> getDmeAsync(final String path) {
        return CompletableFuture.supplyAsync(() -> dmeService.parseDme(new File(path)));
    }
}
