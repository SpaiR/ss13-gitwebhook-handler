package io.github.spair.service.dmi;

import io.github.spair.service.dmi.entities.DmiDiffReport;
import io.github.spair.service.git.GitHubService;
import io.github.spair.service.git.entities.IssueComment;
import io.github.spair.service.git.entities.PullRequest;
import io.github.spair.service.git.entities.PullRequestFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DmiDiffService {

    private final GitHubService gitHubService;
    private final ReportEntryGenerator reportGenerator;
    private final ReportPrinter reportPrinter;

    private static final String DMI_SUFFIX = ".dmi";

    @Autowired
    public DmiDiffService(GitHubService gitHubService, ReportPrinter reportPrinter, ReportEntryGenerator reportGenerator) {
        this.gitHubService = gitHubService;
        this.reportPrinter = reportPrinter;
        this.reportGenerator = reportGenerator;
    }

    public void generateAndReport(PullRequest pullRequest) {
        final int prNumber = pullRequest.getNumber();
        final List<PullRequestFile> dmiPrFiles = filterDmiFiles(gitHubService.listPullRequestFiles(prNumber));

        DmiDiffReport dmiDiffReport = new DmiDiffReport();

        dmiPrFiles.forEach(dmiPrFile -> reportGenerator.generate(dmiPrFile)
                .ifPresent(dmiDiffReport.getReportEntries()::add)
        );

        final String report = reportPrinter.printReport(dmiDiffReport);
        final Optional<Integer> reportId = getReportId(gitHubService.listIssueComments(prNumber));

        if (reportId.isPresent()) {
            gitHubService.editIssueComment(reportId.get(), report);
        } else {
            gitHubService.createIssueComment(prNumber, report);
        }
    }

    private List<PullRequestFile> filterDmiFiles(List<PullRequestFile> allPrFiles) {
        return allPrFiles.stream().filter(file -> file.getFilename().endsWith(DMI_SUFFIX)).collect(Collectors.toList());
    }

    private Optional<Integer> getReportId(List<IssueComment> pullRequestComments) {
        for (IssueComment prComment : pullRequestComments) {
            if (prComment.getBody().startsWith(DmiDiffReport.TITLE)) {
                return Optional.of(prComment.getId());
            }
        }
        return Optional.empty();
    }
}
