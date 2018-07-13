package io.github.spair.service.report;

import io.github.spair.service.github.GitHubService;
import io.github.spair.service.github.entity.IssueComment;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReportSenderServiceTest {

    @Mock
    private GitHubService gitHubService;
    private ReportSenderService reportSenderService;

    private static final String REPORT = "report";
    private static final String ERROR = "error";
    private static final String ID = "239486vn2r62bncry32c";
    private static final int COMMENT_ID = 23;

    private static final String COMMENT_WITH_ID = "comment " + ID;
    private static final String COMMENT_WO_ID = "comment";

    private String longReport;

    @Before
    public void setUp() {
        reportSenderService = new ReportSenderService(gitHubService);

        StringBuilder longReportBuilder = new StringBuilder();
        for (int i = 0; i < 65536; i++) {
            longReportBuilder.append(i);
        }
        longReport = longReportBuilder.toString();
    }

    @Test
    public void testSendReportWhenNoComment() {
        List<IssueComment> issueComments = getIssueComment(COMMENT_WO_ID);
        when(gitHubService.listIssueComments(1)).thenReturn(issueComments);

        reportSenderService.sendReport(REPORT, ERROR, ID, 1);

        verify(gitHubService).createIssueComment(1, REPORT);
    }

    @Test
    public void testSendReportWhenHasComment() {
        List<IssueComment> issueComments = getIssueComment(COMMENT_WITH_ID);
        when(gitHubService.listIssueComments(1)).thenReturn(issueComments);

        reportSenderService.sendReport(REPORT, ERROR, ID, 1);

        verify(gitHubService).editIssueComment(COMMENT_ID, REPORT);
    }

    @Test
    public void testSendReportWithError() {
        List<IssueComment> issueComments = getIssueComment(COMMENT_WITH_ID);
        when(gitHubService.listIssueComments(1)).thenReturn(issueComments);

        reportSenderService.sendReport(longReport, ERROR, ID, 1);

        verify(gitHubService).editIssueComment(COMMENT_ID, ERROR);
    }

    @Test
    public void testSendReportWithEmptyErrorArg() {
        List<IssueComment> issueComments = getIssueComment(COMMENT_WITH_ID);
        when(gitHubService.listIssueComments(1)).thenReturn(issueComments);

        reportSenderService.sendReport(longReport, ID, 1);

        verify(gitHubService).editIssueComment(COMMENT_ID, "Report printing error.");
    }

    private List<IssueComment> getIssueComment(String msg) {
        IssueComment issueComment = new IssueComment();
        issueComment.setId(COMMENT_ID);
        issueComment.setBody(msg);
        return Lists.newArrayList(issueComment);
    }
}