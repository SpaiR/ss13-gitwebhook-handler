package io.github.spair.handler.command;

import io.github.spair.byond.dme.Dme;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.dme.DmeService;
import io.github.spair.service.dmm.DmmService;
import io.github.spair.service.dmm.entity.DmmDiffStatus;
import io.github.spair.service.dmm.entity.ModifiedDmm;
import io.github.spair.service.github.GitHubRepository;
import io.github.spair.service.github.GitHubService;
import io.github.spair.service.github.entity.PullRequestFile;
import io.github.spair.service.pr.entity.PullRequest;
import io.github.spair.service.report.ReportRenderService;
import io.github.spair.service.report.ReportSenderService;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.util.List;
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
    private GitHubRepository gitHubRepository;
    @Mock
    private DmmService dmmService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigService configService;
    @Mock
    private DmeService dmeService;
    @Mock
    private ReportRenderService<DmmDiffStatus> reportRenderService;
    @Mock
    private ReportSenderService reportSenderService;

    private ReportDmmDiffCommand command;

    private File masterFolder = new File("masterFolder");
    private File forkFolder = new File("forkFolder");

    private static final String REPORT_ID = "## DMM Diff Report";

    @Before
    public void setUp() {
        command = new ReportDmmDiffCommand(gitHubService, gitHubRepository, dmmService, configService, dmeService, reportRenderService, reportSenderService);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExecuteWithDiffs() {
        PullRequest pullRequest = PullRequest.builder().number(1).build();
        List<PullRequestFile> prFilesList = getPullRequestFileList();

        when(gitHubService.listPullRequestFiles(1)).thenReturn(prFilesList);
        when(gitHubRepository.loadMasterRepository()).thenReturn(masterFolder);
        when(gitHubRepository.loadForkRepository(eq(pullRequest), any(Consumer.class), any(Runnable.class))).thenReturn(forkFolder);
        when(gitHubRepository.mergeForkWithMaster(forkFolder)).thenReturn(true);
        when(configService.getConfig().getGitHubConfig().getPathToDme()).thenReturn("/dme/path");
        when(dmeService.parseDme(any(File.class))).thenReturn(mock(Dme.class));
        when(dmmService.createModifiedDmm(any(PullRequestFile.class), any(Dme.class), any(Dme.class))).thenReturn(mock(ModifiedDmm.class));
        when(dmmService.createDmmDiffStatus(any(ModifiedDmm.class))).thenReturn(mock(DmmDiffStatus.class));
        when(reportRenderService.renderStatus(anyList())).thenReturn("Fake Report");
        when(reportRenderService.renderError()).thenReturn("Fake Error");

        command.execute(pullRequest);

        verify(reportSenderService).sendReport("Fake Report", "Fake Error", REPORT_ID, 1);
    }

    @Test
    public void testExecuteWhenNonMergeable() {
        List<PullRequestFile> prFilesList = getPullRequestFileList();

        when(gitHubService.listPullRequestFiles(1)).thenReturn(prFilesList);
        when(gitHubRepository.mergeForkWithMaster(any())).thenReturn(false);

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

    private List<PullRequestFile> getPullRequestFileList() {
        PullRequestFile prFile1 = new PullRequestFile();
        prFile1.setFilename("test/filename1.dm");
        PullRequestFile prFile2 = new PullRequestFile();
        prFile2.setFilename("test/filename2.dmm");
        return Lists.newArrayList(prFile1, prFile2);
    }
}