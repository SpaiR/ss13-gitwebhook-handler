package io.github.spair.service.report;

import io.github.spair.service.github.GitHubService;
import io.github.spair.service.github.entity.IssueComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.Nullable;
import java.util.List;

@Service
public class ReportSenderService {

    private final GitHubService gitHubService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportSenderService.class);

    @Autowired
    public ReportSenderService(final GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    public void sendReport(final String report, final String reportId, final int prNumber) {
        sendReport(report, "Report printing error.", reportId, prNumber);
    }

    public void sendReport(final String report, final String errorMessage, final String reportId, final int prNumber) {
        final Integer commentId = getCommentId(reportId, gitHubService.listIssueComments(prNumber));
        try {
            sendReport(report, prNumber, commentId);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                sendReport(errorMessage, prNumber, commentId);
            } else {
                LOGGER.error("Error on sending report with id '{}'", reportId);
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
