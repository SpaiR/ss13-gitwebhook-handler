package io.github.spair.service.git;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.lib.StoredConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Git.class)
public class GitServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Git git;

    private GitService gitService;

    private File testFolder = new File("testFolder");

    @Before
    public void setUp() throws Exception {
        gitService = new GitService();
        PowerMockito.mockStatic(Git.class);
        when(Git.open(any(File.class))).thenReturn(git);
    }

    @Test
    public void testCloneRepositoryWithoutMonitor() throws Exception {
        CloneCommand cloneCommand = mockClone();

        gitService.cloneRepository("Org", "Repo", "testBranch", testFolder);

        verify(cloneCommand).setURI("https://github.com/Org/Repo");
        verify(cloneCommand).setBranch("testBranch");
        verify(cloneCommand).setDirectory(testFolder);
        verify(cloneCommand).setProgressMonitor(null);
        verify(cloneCommand).call();
        verify(git).close();
    }

    @Test
    public void testCloneRepositoryWithMonitor() throws Exception {
        CloneCommand cloneCommand = mockClone();
        CloneMonitor cloneMonitor = mock(CloneMonitor.class);

        gitService.cloneRepository("Org", "Repo", "testBranch", testFolder, cloneMonitor);

        verify(cloneCommand).setURI("https://github.com/Org/Repo");
        verify(cloneCommand).setBranch("testBranch");
        verify(cloneCommand).setDirectory(testFolder);
        verify(cloneCommand).setProgressMonitor(cloneMonitor);
        verify(cloneCommand).call();
        verify(git).close();
    }

    @Test
    public void testPullRepository() throws Exception {
        PullCommand pullCommand = mock(PullCommand.class);
        when(git.pull()).thenReturn(pullCommand);

        gitService.pullRepository(testFolder);

        verify(pullCommand).call();
    }

    @Test
    public void testConfigRepositoryRemote() throws Exception {
        StoredConfig storedConfig = mock(StoredConfig.class);
        when(git.getRepository().getConfig()).thenReturn(storedConfig);

        gitService.configRepositoryRemote(testFolder);

        verify(storedConfig).setString("remote", "master-remote", "url", "../.master");
        verify(storedConfig).setString("remote", "master-remote", "fetch", "+refs/heads/master:refs/remotes/master");
        verify(storedConfig).save();
    }

    @Test
    public void testMergeWithLocalMasterWhenSuccess() throws Exception {
        RebaseCommand rebaseCommand = mockPull(true);
        assertTrue(gitService.mergeWithLocalMaster(testFolder));
        verify(rebaseCommand, never()).call();
    }

    @Test
    public void testMergeWithLocalMasterWhenFail() throws Exception {
        RebaseCommand rebaseCommand = mockPull(false);
        assertFalse(gitService.mergeWithLocalMaster(testFolder));
        verify(rebaseCommand).call();
    }

    private CloneCommand mockClone() throws Exception {
        CloneCommand cloneCommand = mock(CloneCommand.class, RETURNS_DEEP_STUBS);
        when(Git.cloneRepository()).thenReturn(cloneCommand);

        when(cloneCommand.setURI(anyString())).thenReturn(cloneCommand);
        when(cloneCommand.setBranch(anyString())).thenReturn(cloneCommand);
        when(cloneCommand.setDirectory(any(File.class))).thenReturn(cloneCommand);
        when(cloneCommand.setProgressMonitor(any())).thenReturn(cloneCommand);
        when(cloneCommand.call()).thenReturn(git);

        return cloneCommand;
    }

    private RebaseCommand mockPull(final boolean isSuccess) throws Exception {
        PullCommand pullCommand = mock(PullCommand.class);
        when(git.pull()).thenReturn(pullCommand);

        when(pullCommand.setRebase(anyBoolean())).thenReturn(pullCommand);
        when(pullCommand.setRemote(anyString())).thenReturn(pullCommand);
        when(pullCommand.setRemoteBranchName(anyString())).thenReturn(pullCommand);

        PullResult pullResult = mock(PullResult.class);
        when(pullCommand.call()).thenReturn(pullResult);
        when(pullResult.isSuccessful()).thenReturn(isSuccess);

        RebaseCommand rebaseCommand = mock(RebaseCommand.class);
        when(git.rebase()).thenReturn(rebaseCommand);
        when(rebaseCommand.setOperation(any(RebaseCommand.Operation.class))).thenReturn(rebaseCommand);

        return rebaseCommand;
    }
}