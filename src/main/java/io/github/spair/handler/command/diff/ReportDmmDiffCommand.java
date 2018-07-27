package io.github.spair.handler.command.diff;

import io.github.spair.byond.dme.Dme;
import io.github.spair.handler.command.HandlerCommand;
import io.github.spair.handler.command.PullRequestHelper;
import io.github.spair.service.dme.DmeService;
import io.github.spair.service.dme.entity.DmePair;
import io.github.spair.service.dmm.DmmService;
import io.github.spair.service.dmm.entity.DmmDiffStatus;
import io.github.spair.service.dmm.entity.ModifiedDmm;
import io.github.spair.service.github.GitHubService;
import io.github.spair.service.github.entity.PullRequestFile;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.service.report.ReportRenderService;
import io.github.spair.service.report.ReportSenderService;
import io.github.spair.service.report.dmm.DmmReportRenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Component
public class ReportDmmDiffCommand implements HandlerCommand<PullRequest> {

    private static final String REPORT_ID = DmmReportRenderService.TITLE;

    private final GitHubService gitHubService;
    private final DmeService dmeService;
    private final DmmService dmmService;
    private final ReportRenderService<DmmDiffStatus> reportRenderService;
    private final ReportSenderService reportSenderService;

    @Autowired
    public ReportDmmDiffCommand(
            final GitHubService gitHubService,
            final DmeService dmeService,
            final DmmService dmmService,
            final ReportRenderService<DmmDiffStatus> reportRenderService,
            final ReportSenderService reportSenderService) {
        this.gitHubService = gitHubService;
        this.dmeService = dmeService;
        this.dmmService = dmmService;
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

        Optional<DmePair> dmePair = dmeService.createDmePairForPullRequest(
                pullRequest, getUpdateCallback(prNumber), getEndCallback(prNumber));

        if (!dmePair.isPresent()) {
            sendRejectMessage(prNumber);
            return;
        }

        Dme oldDme = dmePair.get().getOldDme();
        Dme newDme = dmePair.get().getNewDme();

        List<ModifiedDmm> modifiedDmms = dmmService.listModifiedDmms(dmmPrFiles, oldDme, newDme);
        List<DmmDiffStatus> dmmDiffStatuses = dmmService.listDmmDiffStatuses(modifiedDmms);

        if (dmmDiffStatuses.isEmpty()) {
            return;
        }

        final String report = reportRenderService.renderStatus(dmmDiffStatuses);
        final String errorMessage = reportRenderService.renderError();

        reportSenderService.sendReport(report, errorMessage, REPORT_ID, prNumber);
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

    private void sendRejectMessage(final int prNumber) {
        String errorMessage = DmmReportRenderService.HEADER + "Report will not be generated for non mergeable PR.";
        reportSenderService.sendReport(errorMessage, REPORT_ID, prNumber);
    }
}
