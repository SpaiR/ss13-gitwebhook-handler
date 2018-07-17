package io.github.spair.handler.command.diff;

import io.github.spair.service.github.GitHubRepository;
import io.github.spair.service.pr.entity.PullRequest;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DeletePullRequestRepoCommandTest {

    @Test
    public void testExecute() {
        PullRequest pullRequest = PullRequest.builder().number(23).author("author").branchName("branch").build();
        GitHubRepository gitHubRepository = mock(GitHubRepository.class);
        DeletePullRequestRepoCommand command = new DeletePullRequestRepoCommand(gitHubRepository);

        command.execute(pullRequest);

        verify(gitHubRepository).deleteForkRepository(pullRequest);
    }
}