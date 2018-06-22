package io.github.spair.handler.command;

import io.github.spair.service.dmi.entity.DmiDiffReport;
import io.github.spair.service.dmi.report.DmiReportCreator;
import io.github.spair.service.dmi.report.ReportEntryGenerator;
import io.github.spair.service.git.GitHubService;
import io.github.spair.service.issue.entity.IssueComment;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.service.git.entity.PullRequestFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReportDmiCommand implements HandlerCommand<PullRequest> {

    private static final String DMI_SUFFIX = ".dmi";

    private final GitHubService gitHubService;
    private final ReportEntryGenerator reportEntryGenerator;
    private final DmiReportCreator reportCreator;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportDmiCommand.class);

    @Autowired
    public ReportDmiCommand(
            final GitHubService gitHubService,
            final ReportEntryGenerator reportEntryGenerator,
            final DmiReportCreator reportCreator) {
        this.gitHubService = gitHubService;
        this.reportEntryGenerator = reportEntryGenerator;
        this.reportCreator = reportCreator;
    }

    @Override
    public void execute(final PullRequest pullRequest) {
        final int prNumber = pullRequest.getNumber();
        final List<PullRequestFile> dmiPrFiles = filterDmiFiles(gitHubService.listPullRequestFiles(prNumber));

        if (dmiPrFiles.isEmpty()) {
            return;
        }

        DmiDiffReport dmiDiffReport = new DmiDiffReport();

        dmiPrFiles.forEach(dmiPrFile -> reportEntryGenerator.generate(dmiPrFile)
                .ifPresent(dmiDiffReport.getReportEntries()::add)
        );

        final String report = reportCreator.createReport(dmiDiffReport);
        final Integer reportId = getReportId(gitHubService.listIssueComments(prNumber));

        try {
            sendReport(report, prNumber, reportId);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                sendReport(reportCreator.createErrorReason(), prNumber, reportId);
            } else {
                LOGGER.error("Error on sending DMI diff report. Resp headers: {}. Resp body: {}",
                        e.getResponseHeaders(), e.getResponseBodyAsString());
                throw e;
            }
        }
    }

    private void sendReport(final String report, final int prNumber, @Nullable final Integer reportId) {
        if (reportId != null) {
            gitHubService.editIssueComment(reportId, report);
        } else {
            gitHubService.createIssueComment(prNumber, report);
        }
    }

    private List<PullRequestFile> filterDmiFiles(final List<PullRequestFile> allPrFiles) {
        return allPrFiles.stream().filter(file -> file.getFilename().endsWith(DMI_SUFFIX)).collect(Collectors.toList());
    }

    @Nullable
    private Integer getReportId(final List<IssueComment> pullRequestComments) {
        for (IssueComment prComment : pullRequestComments) {
            if (prComment.getBody().startsWith(DmiDiffReport.TITLE)) {
                return prComment.getId();
            }
        }
        return null;
    }
}
