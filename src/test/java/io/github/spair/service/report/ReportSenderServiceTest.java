package io.github.spair.service.report;

import io.github.spair.service.github.GitHubService;
import io.github.spair.service.github.entity.IssueComment;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

    @Before
    public void setUp() {
        reportSenderService = new ReportSenderService(gitHubService);
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
        doThrow(new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY)).when(gitHubService).editIssueComment(anyInt(), eq(REPORT));

        reportSenderService.sendReport(REPORT, ERROR, ID, 1);

        verify(gitHubService).editIssueComment(COMMENT_ID, ERROR);
    }

    @Test
    public void testSendReportWithEmptyErrorArg() {
        List<IssueComment> issueComments = getIssueComment(COMMENT_WITH_ID);
        when(gitHubService.listIssueComments(1)).thenReturn(issueComments);
        doThrow(new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY)).when(gitHubService).editIssueComment(anyInt(), eq(REPORT));

        reportSenderService.sendReport(REPORT, ID, 1);

        verify(gitHubService).editIssueComment(COMMENT_ID, "Report printing error.");
    }

    private List<IssueComment> getIssueComment(String msg) {
        IssueComment issueComment = new IssueComment();
        issueComment.setId(COMMENT_ID);
        issueComment.setBody(msg);
        return Lists.newArrayList(issueComment);
    }
}