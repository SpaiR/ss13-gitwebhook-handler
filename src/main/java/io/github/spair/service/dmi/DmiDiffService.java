package io.github.spair.service.dmi;

import io.github.spair.service.dmi.entities.DmiDiffReport;
import io.github.spair.service.git.GitHubService;
import io.github.spair.service.git.entities.IssueComment;
import io.github.spair.service.git.entities.PullRequest;
import io.github.spair.service.git.entities.PullRequestFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class DmiDiffService {

    private final GitHubService gitHubService;
    private final ReportEntryGenerator reportEntryGenerator;
    private final ReportPrinter reportPrinter;

    private static final Logger LOGGER = LoggerFactory.getLogger(DmiDiffService.class);
    private static final String DMI_SUFFIX = ".dmi";

    @Autowired
    public DmiDiffService(final GitHubService gitHubService,
                          final ReportPrinter reportPrinter,
                          final ReportEntryGenerator reportEntryGenerator) {
        this.gitHubService = gitHubService;
        this.reportPrinter = reportPrinter;
        this.reportEntryGenerator = reportEntryGenerator;
    }

    public void generateAndReport(final PullRequest pullRequest) {
        final int prNumber = pullRequest.getNumber();
        final List<PullRequestFile> dmiPrFiles = filterDmiFiles(gitHubService.listPullRequestFiles(prNumber));

        if (dmiPrFiles.isEmpty()) {
            return;
        }

        DmiDiffReport dmiDiffReport = new DmiDiffReport();

        dmiPrFiles.forEach(dmiPrFile -> reportEntryGenerator.generate(dmiPrFile)
                .ifPresent(dmiDiffReport.getReportEntries()::add)
        );

        final String report = reportPrinter.printReport(dmiDiffReport);
        final Integer reportId = getReportId(gitHubService.listIssueComments(prNumber));

        try {
            sendReport(report, prNumber, reportId);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                sendReport(reportPrinter.printErrorReason(), prNumber, reportId);
            } else {
                LOGGER.error("Error on sending DMI diff report. Resp headers: {}. Resp body: {}",
                        e.getResponseHeaders(), e.getResponseBodyAsString());
                throw e;
            }
        }
    }

    private void sendReport(final String report, final int prNumber, @Nullable final Integer reportId) {
        if (Objects.nonNull(reportId)) {
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
