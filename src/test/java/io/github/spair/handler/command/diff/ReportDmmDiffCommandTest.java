package io.github.spair.handler.command.diff;

import io.github.spair.service.dme.DmeService;
import io.github.spair.service.dme.entity.DmePair;
import io.github.spair.service.dmm.DmmService;
import io.github.spair.service.dmm.entity.DmmDiffStatus;
import io.github.spair.service.github.GitHubService;
import io.github.spair.service.github.entity.PullRequestFile;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.service.report.ReportRenderService;
import io.github.spair.service.report.ReportSenderService;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReportDmmDiffCommandTest {

    @Mock
    private GitHubService gitHubService;
    @Mock
    private DmeService dmeService;
    @Mock
    private DmmService dmmService;
    @Mock
    private ReportRenderService<DmmDiffStatus> reportRenderService;
    @Mock
    private ReportSenderService reportSenderService;

    private ReportDmmDiffCommand command;

    private static final String REPORT_ID = "## DMM Diff Report";

    @Before
    public void setUp() {
        command = new ReportDmmDiffCommand(gitHubService, dmeService, dmmService, reportRenderService, reportSenderService);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteWithDiffs() {
        PullRequest pullRequest = PullRequest.builder().number(1).build();
        List<PullRequestFile> prFilesList = getPullRequestFileList();

        when(gitHubService.listPullRequestFiles(1)).thenReturn(prFilesList);
        when(dmeService.createDmePairForPullRequest(eq(pullRequest), any(), any())).thenReturn(Optional.of(mock(DmePair.class)));
        when(dmmService.listDmmDiffStatuses(any(List.class))).thenReturn(Lists.newArrayList(mock(DmmDiffStatus.class)));
        when(reportRenderService.renderStatus(anyList())).thenReturn("Fake Report");
        when(reportRenderService.renderError()).thenReturn("Fake Error");

        command.execute(pullRequest);

        verify(reportSenderService).sendReport("Fake Report", "Fake Error", REPORT_ID, 1);
    }

    @Test
    public void testExecuteWhenNonMergeable() {
        List<PullRequestFile> prFilesList = getPullRequestFileList();

        when(gitHubService.listPullRequestFiles(1)).thenReturn(prFilesList);
        when(dmeService.createDmePairForPullRequest(any(PullRequest.class), any(), any())).thenReturn(Optional.empty());

        command.execute(PullRequest.builder().number(1).build());

        verify(reportSenderService).sendReport(
                "## DMM Diff Report" + System.lineSeparator() + System.lineSeparator()
                        + "Report will not be generated for non mergeable PR.", REPORT_ID, 1);
    }

    @Test
    public void testExecuteWithoutDiffs() {
        when(gitHubService.listPullRequestFiles(1)).thenReturn(Lists.emptyList());
        command.execute(PullRequest.builder().number(1).build());
        verify(reportSenderService, never()).sendReport(anyString(), anyString(), anyString(), anyInt());
    }

    @Test
    public void testUpdateCallback() {
        Consumer<Integer> updateCallback = ReflectionTestUtils.invokeMethod(command, "getUpdateCallback", 23);

        for (int i = 0; i <= 100; i++) {
            updateCallback.accept(i);
        }

        verify(reportSenderService, times(11)).sendReport(anyString(), eq(REPORT_ID), eq(23));
    }

    @Test
    public void testEndCallback() {
        Runnable endCallback = ReflectionTestUtils.invokeMethod(command, "getEndCallback", 23);
        endCallback.run();
        verify(reportSenderService).sendReport(
                "## DMM Diff Report" + System.lineSeparator() + System.lineSeparator()
                        + "Cloning is done. Report will be generated in a few minutes...", REPORT_ID, 23);
    }

    private List<PullRequestFile> getPullRequestFileList() {
        PullRequestFile prFile1 = new PullRequestFile();
        prFile1.setFilename("test/filename1.dm");
        PullRequestFile prFile2 = new PullRequestFile();
        prFile2.setFilename("test/filename2.dmm");
        return Lists.newArrayList(prFile1, prFile2);
    }
}