package io.github.spair.service.report;

import io.github.spair.service.github.GitHubCommentService;
import io.github.spair.service.github.entity.IssueComment;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ReportSenderServiceTest {

    @Mock
    private GitHubCommentService gitHubCommentService;
    private ReportSenderService reportSenderService;

    private static final String REPORT = "report";
    private static final String ERROR = "error";
    private static final String ID = "239486vn2r62bncry32c";
    private static final int COMMENT_ID = 23;

    private String longReport;

    @Before
    public void setUp() {
        reportSenderService = new ReportSenderService(gitHubCommentService);

        StringBuilder longReportBuilder = new StringBuilder();
        for (int i = 0; i < 65536; i++) {
            longReportBuilder.append(i);
        }
        longReport = longReportBuilder.toString();
    }

    @Test
    public void testSendReport() {
        reportSenderService.sendReport(REPORT, ERROR, ID, 1);
        verify(gitHubCommentService).sendCommentOrUpdate(1, REPORT, ID);
    }


    @Test
    public void testSendReportWithError() {
        reportSenderService.sendReport(longReport, ERROR, ID, 1);
        verify(gitHubCommentService).sendCommentOrUpdate(1, ERROR, ID);
    }

    private List<IssueComment> getIssueComment(String msg) {
        IssueComment issueComment = new IssueComment();
        issueComment.setId(COMMENT_ID);
        issueComment.setBody(msg);
        return Lists.newArrayList(issueComment);
    }
}