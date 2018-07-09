package io.github.spair.handler.command;

import io.github.spair.service.ByondFiles;
import io.github.spair.byond.dme.Dme;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.dme.DmeService;
import io.github.spair.service.dmm.DmmService;
import io.github.spair.service.dmm.entity.DmmDiffStatus;
import io.github.spair.service.dmm.entity.ModifiedDmm;
import io.github.spair.service.github.GitHubRepository;
import io.github.spair.service.github.GitHubService;
import io.github.spair.service.github.entity.PullRequestFile;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.service.report.ReportRenderService;
import io.github.spair.service.report.ReportSenderService;
import io.github.spair.service.report.dmm.DmmReportRenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
public class ReportDmmDiffCommand implements HandlerCommand<PullRequest> {

    private static final String REPORT_ID = DmmReportRenderService.TITLE;

    private final GitHubService gitHubService;
    private final GitHubRepository gitHubRepository;
    private final DmmService dmmService;
    private final ConfigService configService;
    private final DmeService dmeService;
    private final ReportRenderService<DmmDiffStatus> reportRenderService;
    private final ReportSenderService reportSenderService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportDmmDiffCommand.class);

    @Autowired
    public ReportDmmDiffCommand(
            final GitHubService gitHubService,
            final GitHubRepository gitHubRepository,
            final DmmService dmmService,
            final ConfigService configService,
            final DmeService dmeService,
            final ReportRenderService<DmmDiffStatus> reportRenderService,
            final ReportSenderService reportSenderService) {
        this.gitHubService = gitHubService;
        this.gitHubRepository = gitHubRepository;
        this.dmmService = dmmService;
        this.configService = configService;
        this.dmeService = dmeService;
        this.reportRenderService = reportRenderService;
        this.reportSenderService = reportSenderService;
    }

    @Override
    public void execute(final PullRequest pullRequest) {
        final int prNumber = pullRequest.getNumber();
        final List<PullRequestFile> dmmPrFiles = filterDmmFiles(gitHubService.listPullRequestFiles(prNumber));

        if (dmmPrFiles.isEmpty()) {
            return;
        }

        CompletableFuture<File> loadMasterFuture = getMasterRepoAsync();
        CompletableFuture<File> loadForkFuture = getForkRepoAsync(pullRequest);
        completeFutures(loadMasterFuture, loadForkFuture);

        final File master = extractFuture(loadMasterFuture);
        final File fork = extractFuture(loadForkFuture);

        if (!gitHubRepository.mergeForkWithMaster(fork)) {
            sendRejectMessage(prNumber);
            return;
        }

        final String pathToDme = configService.getConfig().getBotConfig().getPathToDme();

        CompletableFuture<Dme> parseOldDmeFuture = getDmeAsync(master.getPath() + pathToDme);
        CompletableFuture<Dme> parseNewDmeFuture = getDmeAsync(fork.getPath() + pathToDme);
        completeFutures(parseOldDmeFuture, parseNewDmeFuture);

        final Dme oldDme = extractFuture(parseOldDmeFuture);
        final Dme newDme = extractFuture(parseNewDmeFuture);

        List<ModifiedDmm> modifiedDmms = getModifiedDmms(dmmPrFiles, oldDme, newDme);
        List<DmmDiffStatus> dmmDiffStatuses = getDmmDiffStatuses(modifiedDmms);

        final String report = reportRenderService.renderStatus(dmmDiffStatuses);
        final String errorMessage = reportRenderService.renderError();

        reportSenderService.sendReport(report, errorMessage, REPORT_ID, prNumber);
    }

    private List<ModifiedDmm> getModifiedDmms(final List<PullRequestFile> prFiles, final Dme oldDme, final Dme newDme) {
        return prFiles.stream()
                .map(dmmPrFile -> dmmService.createModifiedDmm(dmmPrFile, oldDme, newDme))
                .collect(Collectors.toList());
    }

    private List<DmmDiffStatus> getDmmDiffStatuses(final List<ModifiedDmm> modifiedDmms) {
        return modifiedDmms.stream().map(dmmService::createDmmDiffStatus).collect(Collectors.toList());
    }

    private List<PullRequestFile> filterDmmFiles(final List<PullRequestFile> allPrFiles) {
        return allPrFiles.stream()
                .filter(file -> file.getFilename().endsWith(ByondFiles.DMM_SUFFIX))
                .collect(Collectors.toList());
    }

    private CompletableFuture<File> getMasterRepoAsync() {
        return CompletableFuture.supplyAsync(gitHubRepository::loadMasterRepository);
    }

    private CompletableFuture<File> getForkRepoAsync(final PullRequest pullRequest) {
        return CompletableFuture.supplyAsync(() -> {
            final int updatePcntWait = 10;
            final int[] nextUpdateMessage = new int[]{0};   // Hack for increment inside of lambda.

            final Consumer<Integer> updateCallback = pcnt -> {
                if (pcnt == nextUpdateMessage[0]) {
                    nextUpdateMessage[0] += updatePcntWait;

                    String message = DmmReportRenderService.HEADER
                            + String.format("Cloning PR repository... Progress: **%d%%**", pcnt);

                    reportSenderService.sendReport(message, REPORT_ID, pullRequest.getNumber());
                }
            };
            final Runnable endCallback = () -> {
                String message = DmmReportRenderService.HEADER
                        + "Cloning is done. Report will be generated in a few minutes...";
                reportSenderService.sendReport(message, REPORT_ID, pullRequest.getNumber());
            };

            return gitHubRepository.loadForkRepository(pullRequest, updateCallback, endCallback);
        });
    }

    private CompletableFuture<Dme> getDmeAsync(final String path) {
        return CompletableFuture.supplyAsync(() -> dmeService.parseDme(new File(path)));
    }

    private void sendRejectMessage(final int prNumber) {
        String errorMessage = DmmReportRenderService.HEADER + "Report will not be generated for non mergeable PR.";
        reportSenderService.sendReport(errorMessage, REPORT_ID, prNumber);
    }

    private void completeFutures(final CompletableFuture... futures) {
        try {
            CompletableFuture.allOf(futures).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Exception on loading repositories", e);
            throw new RuntimeException(e);
        }
    }

    private <T> T extractFuture(final CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Extract future exception");
        }
    }
}
