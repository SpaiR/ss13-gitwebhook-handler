package io.github.spair.service.report;

import io.github.spair.service.github.GitHubService;
import io.github.spair.service.github.entity.IssueComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;

@Service
public class ReportSenderService {

    private static final int COMMENT_LIMIT = 65535;

    private final GitHubService gitHubService;

    @Autowired
    public ReportSenderService(final GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    public void sendReport(final String report, final String reportId, final int prNumber) {
        sendReport(report, "Report printing error.", reportId, prNumber);
    }

    public void sendReport(final String report, final String errorMessage, final String reportId, final int prNumber) {
        final Integer commentId = getCommentId(reportId, gitHubService.listIssueComments(prNumber));

        // TODO: create comment splitting mechanism.
        if (report.length() <= COMMENT_LIMIT) {
            sendReport(report, prNumber, commentId);
        } else {
            sendReport(errorMessage, prNumber, commentId);
        }
    }

    private void sendReport(final String report, final int prNumber, @Nullable final Integer commentId) {
        if (commentId != null) {
            gitHubService.editIssueComment(commentId, report);
        } else {
            gitHubService.createIssueComment(prNumber, report);
        }
    }

    @Nullable
    private Integer getCommentId(final String reportId, final List<IssueComment> pullRequestComments) {
        for (IssueComment prComment : pullRequestComments) {
            if (prComment.getBody().contains(reportId)) {
                return prComment.getId();
            }
        }
        return null;
    }
}
