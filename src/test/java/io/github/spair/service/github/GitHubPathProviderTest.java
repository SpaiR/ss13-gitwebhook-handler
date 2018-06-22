package io.github.spair.service.github;

import io.github.spair.service.config.ConfigService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GitHubPathProviderTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigService configService;

    private GitHubPathProvider provider;

    @Before
    public void setUp() {
        when(configService.getConfig().getGitHubConfig().getOrganizationName()).thenReturn("GitHub");
        when(configService.getConfig().getGitHubConfig().getRepositoryName()).thenReturn("Handler");
        provider = new GitHubPathProvider(configService);
    }

    @Test
    public void testGeneralPath() {
        String expected = "https://api.github.com/repos/OrgName/RepoName";
        assertEquals(expected, provider.generalPath("OrgName", "RepoName"));
    }

    @Test
    public void testContents() {
        String expected = "https://api.github.com/repos/GitHub/Handler/contents/some/contents";
        assertEquals(expected, provider.contents("/some/contents"));
    }

    @Test
    public void testBlobs() {
        String expected = "https://api.github.com/repos/GitHub/Handler/github/blobs/12345789";
        assertEquals(expected, provider.blobs("12345789"));
    }

    @Test
    public void testContentsWithOrgRepoArgs() {
        String expected = "https://api.github.com/repos/OrgName/RepoName/contents/some/contents";
        assertEquals(expected, provider.contents("OrgName", "RepoName", "/some/contents"));
    }

    @Test
    public void testIssueLabels() {
        String expected = "https://api.github.com/repos/GitHub/Handler/issues/23/labels";
        assertEquals(expected, provider.issueLabels(23));
    }

    @Test
    public void testIssueLabel() {
        String expected = "https://api.github.com/repos/GitHub/Handler/issues/120/labels/Label Name";
        assertEquals(expected, provider.issueLabel(120, "Label Name"));
    }

    @Test
    public void testPullFiles() {
        String expected = "https://api.github.com/repos/GitHub/Handler/pulls/120/files";
        assertEquals(expected, provider.pullFiles(120));
    }
}