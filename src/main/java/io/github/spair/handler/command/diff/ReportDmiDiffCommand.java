package io.github.spair.handler.command.diff;

import io.github.spair.handler.command.HandlerCommand;
import io.github.spair.handler.command.PullRequestHelper;
import io.github.spair.service.dmi.DmiService;
import io.github.spair.service.dmi.entity.DmiDiffStatus;
import io.github.spair.service.dmi.entity.ModifiedDmi;
import io.github.spair.service.github.GitHubCommentService;
import io.github.spair.service.github.GitHubService;
import io.github.spair.service.github.entity.PullRequestFile;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.service.report.ReportRenderService;
import io.github.spair.service.report.dmi.DmiReportRenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportDmiDiffCommand implements HandlerCommand<PullRequest> {

    private static final String REPORT_ID = DmiReportRenderService.TITLE;

    private final GitHubService gitHubService;
    private final DmiService dmiService;
    private final ReportRenderService<DmiDiffStatus> reportRenderService;
    private final GitHubCommentService gitHubCommentService;

    @Autowired
    public ReportDmiDiffCommand(
            final GitHubService gitHubService,
            final DmiService dmiService,
            final ReportRenderService<DmiDiffStatus> reportRenderService,
            final GitHubCommentService gitHubCommentService) {
        this.gitHubService = gitHubService;
        this.dmiService = dmiService;
        this.reportRenderService = reportRenderService;
        this.gitHubCommentService = gitHubCommentService;
    }

    @Override
    public void execute(final PullRequest pullRequest) {
        final int prNumber = pullRequest.getNumber();
        final List<PullRequestFile> allPullRequestFiles = gitHubService.listPullRequestFiles(prNumber);
        final List<PullRequestFile> dmiPrFiles = PullRequestHelper.filterDmiFiles(allPullRequestFiles);

        if (dmiPrFiles.isEmpty()) {
            return;
        }

        List<ModifiedDmi> modifiedDmis = dmiService.listModifiedDmis(dmiPrFiles);
        List<DmiDiffStatus> dmiDiffStatuses = dmiService.listDmiDiffStatuses(modifiedDmis);

        if (dmiDiffStatuses.isEmpty()) {
            return;
        }

        final String report = reportRenderService.renderStatus(dmiDiffStatuses);
        gitHubCommentService.sendCommentOrUpdate(prNumber, report, REPORT_ID);
    }
}
