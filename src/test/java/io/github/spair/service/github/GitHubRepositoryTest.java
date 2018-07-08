package io.github.spair.service.github;

import io.github.spair.service.config.ConfigService;
import io.github.spair.service.git.CloneMonitor;
import io.github.spair.service.git.GitService;
import io.github.spair.service.pr.entity.PullRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GitHubRepository.class)
public class GitHubRepositoryTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigService configService;
    @Mock
    private GitService gitService;

    private GitHubRepository repository;

    private static final String MASTER_FOLDER_PATH = ".repos" + File.separator + ".master";
    private static final String TEST_FORK_FOLDER_PATH = ".repos/fork.author=testUser.pr=23.branch=testBranch";

    @Before
    public void setUp() {
        repository = new GitHubRepository(configService, gitService);
        when(configService.getConfig().getGitHubConfig().getOrganizationName()).thenReturn("Org");
        when(configService.getConfig().getGitHubConfig().getRepositoryName()).thenReturn("Repo");
    }

    @Test
    public void testInitMasterRepository() {
        repository.initMasterRepository();
        verify(gitService).cloneRepository("Org", "Repo", "master", new File(MASTER_FOLDER_PATH));
    }

    @Test
    public void testLoadMasterRepository() {
        repository.loadMasterRepository();

        verify(gitService).cloneRepository("Org", "Repo", "master", new File(MASTER_FOLDER_PATH));
        verify(gitService, never()).pullRepository(any(File.class));
    }

    @Test
    public void testLoadMasterRepositoryWhenExists() throws Exception {
        mockFileExists(MASTER_FOLDER_PATH);

        repository.loadMasterRepository();

        File masterFolder = new File(MASTER_FOLDER_PATH);

        verify(gitService).pullRepository(masterFolder);
        verify(gitService, never()).cloneRepository("Org", "Repo", "master", masterFolder);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLoadForkRepository() {
        Consumer<Integer> updCallback = mock(Consumer.class);
        Runnable endCallback = mock(Runnable.class);

        repository.loadForkRepository(getTestPullRequest(), updCallback, endCallback);

        File forkFolder = new File(TEST_FORK_FOLDER_PATH);

        verify(gitService).cloneRepository("testUser", "Repo", "testBranch", forkFolder, new CloneMonitor(updCallback, endCallback));
        verify(gitService).configRepositoryRemote(forkFolder);
        verify(gitService, never()).pullRepository(forkFolder);
    }

    @Test
    public void testLoadForkRepositoryWhenExists() throws Exception {
        mockFileExists(TEST_FORK_FOLDER_PATH);

        repository.loadForkRepository(getTestPullRequest(), null, null);

        File forkFolder = new File(TEST_FORK_FOLDER_PATH);

        verify(gitService).pullRepository(forkFolder);
        verify(gitService, never()).cloneRepository(anyString(), anyString(), anyString(), any(File.class));
        verify(gitService, never()).configRepositoryRemote(any(File.class));
    }

    @Test
    public void testMergeForkWithMaster() {
        File forkFolder = new File(TEST_FORK_FOLDER_PATH);
        repository.mergeForkWithMaster(forkFolder);
        verify(gitService).mergeWithLocalMaster(forkFolder);
    }

    private void mockFileExists(final String filePath) throws Exception {
        File mockedFile = mock(File.class);
        when(mockedFile.getName()).thenReturn("");
        when(mockedFile.exists()).thenReturn(true);
        PowerMockito.whenNew(File.class).withArguments(filePath).thenReturn(mockedFile);
    }

    private PullRequest getTestPullRequest() {
        return PullRequest.builder().author("testUser").number(23).branchName("testBranch").build();
    }
}