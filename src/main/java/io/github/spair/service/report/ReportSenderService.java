package io.github.spair.service.report;

import io.github.spair.service.github.GitHubCommentService;
import io.github.spair.service.github.GitHubConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

@Service
public class ReportSenderService {

    private final GitHubCommentService gitHubCommentService;

    @Autowired
    public ReportSenderService(final GitHubCommentService gitHubCommentService) {
        this.gitHubCommentService = gitHubCommentService;
    }

    public void sendReport(final String report, final String reportId, final int prNumber) {
        sendReport(report, "Report printing error.", reportId, prNumber);
    }

    public void sendReport(final String report, final String errorMessage, final String reportId, final int prNumber) {
        // TODO: create comment splitting mechanism.
        if (report.length() <= GitHubConstants.COMMENT_LIMIT) {
            sendReport(report, prNumber, reportId);
        } else {
            sendReport(errorMessage, prNumber, reportId);
        }
    }

    private void sendReport(final String report, final int prNumber, @Nullable final String reportId) {
        gitHubCommentService.sendCommentOrUpdate(prNumber, report, reportId);
    }
}
