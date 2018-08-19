package io.github.spair.handler.command.diff;

import io.github.spair.byond.dme.Dme;
import io.github.spair.handler.command.HandlerCommand;
import io.github.spair.handler.command.PullRequestHelper;
import io.github.spair.service.dme.DmeService;
import io.github.spair.service.dme.entity.DmePair;
import io.github.spair.service.dmm.DmmService;
import io.github.spair.service.dmm.entity.DmmDiffStatus;
import io.github.spair.service.dmm.entity.ModifiedDmm;
import io.github.spair.service.github.GitHubCommentService;
import io.github.spair.service.github.GitHubService;
import io.github.spair.service.github.entity.PullRequestFile;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.service.report.ReportRenderService;
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
    private final GitHubCommentService gitHubCommentService;

    @Autowired
    public ReportDmmDiffCommand(
            final GitHubService gitHubService,
            final DmeService dmeService,
            final DmmService dmmService,
            final ReportRenderService<DmmDiffStatus> reportRenderService,
            final GitHubCommentService gitHubCommentService) {
        this.gitHubService = gitHubService;
        this.dmeService = dmeService;
        this.dmmService = dmmService;
        this.reportRenderService = reportRenderService;
        this.gitHubCommentService = gitHubCommentService;
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
        gitHubCommentService.sendCommentOrUpdate(prNumber, report, REPORT_ID);
    }

    private Consumer<Integer> getUpdateCallback(final int prNumber) {
        final int updatePcntWait = 10;
        final int[] nextUpdateMessage = new int[]{0}; // Hack to increment value inside of lambda.
        return pcnt -> {
            if (pcnt == nextUpdateMessage[0]) {
                nextUpdateMessage[0] += updatePcntWait;

                String message = DmmReportRenderService.HEADER
                        + "Cloning PR repository... Progress: **" + pcnt + "%**";
                gitHubCommentService.sendCommentOrUpdate(prNumber, message, REPORT_ID);
            }
        };
    }

    private Runnable getEndCallback(final int prNumber) {
        return () -> {
            String message = DmmReportRenderService.HEADER
                    + "Cloning is done. Report will be generated in a few minutes...";
            gitHubCommentService.sendCommentOrUpdate(prNumber, message, REPORT_ID);
        };
    }

    private void sendRejectMessage(final int prNumber) {
        String errorMessage = DmmReportRenderService.HEADER + "Report will not be generated for non mergeable PR.";
        gitHubCommentService.sendCommentOrUpdate(prNumber, errorMessage, REPORT_ID);
    }
}
