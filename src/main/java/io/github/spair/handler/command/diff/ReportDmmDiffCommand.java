package io.github.spair.handler.command.diff;

import io.github.spair.byond.dme.Dme;
import io.github.spair.handler.command.HandlerCommand;
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
import io.github.spair.util.FutureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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
        final List<PullRequestFile> allPullRequestFiles = gitHubService.listPullRequestFiles(prNumber);
        final List<PullRequestFile> dmmPrFiles = PullRequestHelper.filterDmmFiles(allPullRequestFiles);

        if (dmmPrFiles.isEmpty()) {
            return;
        }

        CompletableFuture<File> loadMasterFuture = getMasterRepoAsync();
        CompletableFuture<File> loadForkFuture = getForkRepoAsync(pullRequest);
        FutureUtil.completeFutures(loadMasterFuture, loadForkFuture);

        final File master = FutureUtil.extractFuture(loadMasterFuture);
        final File fork = FutureUtil.extractFuture(loadForkFuture);

        if (!gitHubRepository.mergeForkWithMaster(fork)) {
            sendRejectMessage(prNumber);
            return;
        }

        final String pathToDme = configService.getConfig().getDmmBotConfig().getPathToDme();

        CompletableFuture<Dme> parseOldDmeFuture = getDmeAsync(master.getPath() + pathToDme);
        CompletableFuture<Dme> parseNewDmeFuture = getDmeAsync(fork.getPath() + pathToDme);
        FutureUtil.completeFutures(parseOldDmeFuture, parseNewDmeFuture);

        final Dme oldDme = FutureUtil.extractFuture(parseOldDmeFuture);
        final Dme newDme = FutureUtil.extractFuture(parseNewDmeFuture);

        List<ModifiedDmm> modifiedDmms = dmmService.listModifiedDmms(dmmPrFiles, oldDme, newDme);
        List<DmmDiffStatus> dmmDiffStatuses = dmmService.listDmmDiffStatuses(modifiedDmms);

        if (dmmDiffStatuses.isEmpty()) {
            return;
        }

        final String report = reportRenderService.renderStatus(dmmDiffStatuses);
        final String errorMessage = reportRenderService.renderError();

        reportSenderService.sendReport(report, errorMessage, REPORT_ID, prNumber);
    }

    private CompletableFuture<File> getMasterRepoAsync() {
        return CompletableFuture.supplyAsync(gitHubRepository::loadMasterRepository);
    }

    private CompletableFuture<File> getForkRepoAsync(final PullRequest pullRequest) {
        return CompletableFuture.supplyAsync(() -> {
            final Consumer<Integer> updateCallback = getUpdateCallback(pullRequest.getNumber());
            final Runnable endCallback = getEndCallback(pullRequest.getNumber());
            return gitHubRepository.loadForkRepository(pullRequest, updateCallback, endCallback);
        });
    }

    private Consumer<Integer> getUpdateCallback(final int pullRequestNumber) {
        final int updatePcntWait = 10;
        final int[] nextUpdateMessage = new int[]{0}; // Hack to increment value inside of lambda.
        return pcnt -> {
            if (pcnt == nextUpdateMessage[0]) {
                nextUpdateMessage[0] += updatePcntWait;

                String message = DmmReportRenderService.HEADER
                        + "Cloning PR repository... Progress: **" + pcnt + "%**";
                reportSenderService.sendReport(message, REPORT_ID, pullRequestNumber);
            }
        };
    }

    private Runnable getEndCallback(final int pullRequestNumber) {
        return () -> {
            String message = DmmReportRenderService.HEADER
                    + "Cloning is done. Report will be generated in a few minutes...";
            reportSenderService.sendReport(message, REPORT_ID, pullRequestNumber);
        };
    }

    private CompletableFuture<Dme> getDmeAsync(final String path) {
        return CompletableFuture.supplyAsync(() -> dmeService.parseDme(new File(path)));
    }

    private void sendRejectMessage(final int prNumber) {
        String errorMessage = DmmReportRenderService.HEADER + "Report will not be generated for non mergeable PR.";
        reportSenderService.sendReport(errorMessage, REPORT_ID, prNumber);
    }
}
