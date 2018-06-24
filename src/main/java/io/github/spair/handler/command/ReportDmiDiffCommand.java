package io.github.spair.handler.command;

import io.github.spair.service.dmi.DmiService;
import io.github.spair.service.dmi.entity.DmiDiffStatus;
import io.github.spair.service.dmi.entity.ModifiedDmi;
import io.github.spair.service.report.dmi.DmiReportRenderService;
import io.github.spair.service.github.GitHubService;
import io.github.spair.service.github.entity.IssueComment;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.service.github.entity.PullRequestFile;
import io.github.spair.service.report.ReportRenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ReportDmiDiffCommand implements HandlerCommand<PullRequest> {

    private final GitHubService gitHubService;
    private final DmiService dmiService;
    private final ReportRenderService<DmiDiffStatus> reportService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportDmiDiffCommand.class);

    @Autowired
    public ReportDmiDiffCommand(
            final GitHubService gitHubService,
            final DmiService dmiService,
            @Qualifier(ReportRenderService.DMI) final ReportRenderService<DmiDiffStatus> reportService) {
        this.gitHubService = gitHubService;
        this.dmiService = dmiService;
        this.reportService = reportService;
    }

    @Override
    public void execute(final PullRequest pullRequest) {
        final int prNumber = pullRequest.getNumber();
        final List<PullRequestFile> dmiPrFiles = filterDmiFiles(gitHubService.listPullRequestFiles(prNumber));

        if (dmiPrFiles.isEmpty()) {
            return;
        }

        List<ModifiedDmi> modifiedDmis = extractModifiedDmis(dmiPrFiles);
        List<DmiDiffStatus> dmiDiffStatuses = extractDmiDiffStatuses(modifiedDmis);

        final String report = reportService.renderStatus(dmiDiffStatuses);
        final Integer commentId = getCommentId(gitHubService.listIssueComments(prNumber));

        sendReportOrCreate(report, prNumber, commentId);
    }

    private List<ModifiedDmi> extractModifiedDmis(final List<PullRequestFile> dmiPrFiles) {
        return dmiPrFiles.stream().map(dmiService::createModifiedDmi).collect(Collectors.toList());
    }

    private List<DmiDiffStatus> extractDmiDiffStatuses(final List<ModifiedDmi> modifiedDmis) {
        return modifiedDmis.stream()
                .map(dmiService::createDmiDiffStatus)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private void sendReportOrCreate(final String report, final int prNumber, @Nullable final Integer commentId) {
        try {
            sendReport(report, prNumber, commentId);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                sendReport(reportService.renderError(), prNumber, commentId);
            } else {
                LOGGER.error("Error on sending DMI diff dmi. Resp headers: {}. Resp body: {}",
                        e.getResponseHeaders(), e.getResponseBodyAsString());
                throw e;
            }
        }
    }

    private void sendReport(final String report, final int prNumber, @Nullable final Integer commentId) {
        if (commentId != null) {
            gitHubService.editIssueComment(commentId, report);
        } else {
            gitHubService.createIssueComment(prNumber, report);
        }
    }

    private List<PullRequestFile> filterDmiFiles(final List<PullRequestFile> allPrFiles) {
        return allPrFiles.stream()
                .filter(file -> file.getFilename().endsWith(ByondFiles.DMI_SUFFIX))
                .collect(Collectors.toList());
    }

    @Nullable
    private Integer getCommentId(final List<IssueComment> pullRequestComments) {
        for (IssueComment prComment : pullRequestComments) {
            if (prComment.getBody().startsWith(DmiReportRenderService.TITLE)) {
                return prComment.getId();
            }
        }
        return null;
    }
}
