package io.github.spair.handler.command.label;

import io.github.spair.service.github.GitHubService;
import io.github.spair.service.issue.entity.Issue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LabelIssueCommandTest {

    @Mock
    private GitHubService gitHubService;
    private LabelIssueCommand command;

    @Before
    public void setUp() {
        command = new LabelIssueCommand(gitHubService);
    }

    @Test
    public void testExecute() {
        Issue issue = new Issue();
        issue.setNumber(123);
        issue.setTitle("Simple title");

        command.execute(issue);
        verify(gitHubService).addLabel(123, "Bug");

        issue.setTitle("[proposal] Proposal title");
        command.execute(issue);
        verify(gitHubService).addLabel(123, "Proposal");
    }
}