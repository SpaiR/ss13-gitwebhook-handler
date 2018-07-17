package io.github.spair.handler.command.diff;

import io.github.spair.handler.command.HandlerCommand;
import io.github.spair.service.ByondFiles;
import io.github.spair.service.dmi.DmiService;
import io.github.spair.service.dmi.entity.DmiDiffStatus;
import io.github.spair.service.dmi.entity.ModifiedDmi;
import io.github.spair.service.github.GitHubService;
import io.github.spair.service.github.entity.PullRequestFile;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.service.report.ReportRenderService;
import io.github.spair.service.report.ReportSenderService;
import io.github.spair.service.report.dmi.DmiReportRenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ReportDmiDiffCommand implements HandlerCommand<PullRequest> {

    private static final String REPORT_ID = DmiReportRenderService.TITLE;

    private final GitHubService gitHubService;
    private final DmiService dmiService;
    private final ReportRenderService<DmiDiffStatus> reportRenderService;
    private final ReportSenderService reportSenderService;

    @Autowired
    public ReportDmiDiffCommand(
            final GitHubService gitHubService,
            final DmiService dmiService,
            final ReportRenderService<DmiDiffStatus> reportRenderService,
            final ReportSenderService reportSenderService) {
        this.gitHubService = gitHubService;
        this.dmiService = dmiService;
        this.reportRenderService = reportRenderService;
        this.reportSenderService = reportSenderService;
    }

    @Override
    public void execute(final PullRequest pullRequest) {
        final int prNumber = pullRequest.getNumber();
        final List<PullRequestFile> dmiPrFiles = filterDmiFiles(gitHubService.listPullRequestFiles(prNumber));

        if (dmiPrFiles.isEmpty()) {
            return;
        }

        List<ModifiedDmi> modifiedDmis = getModifiedDmis(dmiPrFiles);
        List<DmiDiffStatus> dmiDiffStatuses = getDmiDiffStatuses(modifiedDmis);

        if (dmiDiffStatuses.isEmpty()) {
            return;
        }

        final String report = reportRenderService.renderStatus(dmiDiffStatuses);
        final String errorMessage = reportRenderService.renderError();

        reportSenderService.sendReport(report, errorMessage, REPORT_ID, prNumber);
    }

    private List<ModifiedDmi> getModifiedDmis(final List<PullRequestFile> dmiPrFiles) {
        return dmiPrFiles.stream().map(dmiService::createModifiedDmi).collect(Collectors.toList());
    }

    private List<DmiDiffStatus> getDmiDiffStatuses(final List<ModifiedDmi> modifiedDmis) {
        return modifiedDmis.stream()
                .map(dmiService::createDmiDiffStatus).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<PullRequestFile> filterDmiFiles(final List<PullRequestFile> allPrFiles) {
        return allPrFiles.stream()
                .filter(file -> file.getFilename().endsWith(ByondFiles.DMI_SUFFIX))
                .collect(Collectors.toList());
    }
}
