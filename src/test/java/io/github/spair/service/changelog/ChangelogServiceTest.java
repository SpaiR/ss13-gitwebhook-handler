package io.github.spair.service.changelog;

import io.github.spair.service.changelog.entities.Changelog;
import io.github.spair.service.changelog.entities.ChangelogRow;
import io.github.spair.service.changelog.entities.ChangelogValidationStatus;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.git.GitHubService;
import io.github.spair.service.git.entities.PullRequest;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ChangelogServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GitHubService gitHubService;
    @Mock
    private HtmlChangelogGenerator htmlChangelogGenerator;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigService configService;
    @Mock
    private ChangelogValidator changelogValidator;
    @Mock
    private ChangelogGenerator changelogGenerator;

    private ChangelogService service;

    @Before
    public void setUp() {
        service = new ChangelogService(gitHubService, htmlChangelogGenerator, changelogValidator, configService, changelogGenerator);
        when(configService.getConfig().getGitHubConfig().getLabels().getInvalidChangelog()).thenReturn("Invalid Changelog");
    }

    @Test
    public void testValidateWhenValid() {
        Changelog changelog = new Changelog();
        changelog.setChangelogRows(Lists.newArrayList(new ChangelogRow("map", false, "")));

        when(changelogGenerator.generate(any())).thenReturn(changelog);
        when(gitHubService.listIssueLabels(anyInt()).contains(anyString())).thenReturn(false);
        when(changelogValidator.validate(changelog)).thenReturn(new ChangelogValidationStatus());

        service.validate(new PullRequest());

        verify(gitHubService, times(0)).createIssueComment(anyInt(), anyString());
        verify(gitHubService, times(0)).addLabel(anyInt(), anyString());
        verify(gitHubService, times(0)).removeLabel(anyInt(), anyString());
    }

    @Test
    public void testValidateWhenValidAndHasLabel() {
        Changelog changelog = new Changelog();
        changelog.setChangelogRows(Lists.emptyList());

        when(changelogGenerator.generate(any())).thenReturn(changelog);
        when(gitHubService.listIssueLabels(anyInt()).contains(anyString())).thenReturn(true);
        when(changelogValidator.validate(changelog)).thenReturn(new ChangelogValidationStatus());

        PullRequest pullRequest = new PullRequest();
        pullRequest.setNumber(1);
        service.validate(pullRequest);

        verify(gitHubService, times(0)).createIssueComment(anyInt(), anyString());
        verify(gitHubService, times(0)).addLabel(anyInt(), anyString());
        verify(gitHubService).removeLabel(1, "Invalid Changelog");
    }

    @Test
    public void testValidateWhenChangelogEmpty() {
        Changelog changelog = new Changelog();
        changelog.setChangelogRows(Lists.emptyList());

        ChangelogValidationStatus validationStatus = new ChangelogValidationStatus();
        validationStatus.setMessage("Reason: empty changelog. Please, check markdown correctness.");
        validationStatus.setStatus(ChangelogValidationStatus.Status.INVALID);

        when(changelogGenerator.generate(any())).thenReturn(changelog);
        when(gitHubService.listIssueLabels(anyInt()).contains(anyString())).thenReturn(false);
        when(changelogValidator.validate(changelog)).thenReturn(validationStatus);

        PullRequest pullRequest = new PullRequest();
        pullRequest.setNumber(1);
        service.validate(pullRequest);

        verify(gitHubService).createIssueComment(1,
                "**Warning!** Invalid changelog detected.\n\n" +
                        "Reason: empty changelog. Please, check markdown correctness.");
        verify(gitHubService).addLabel(1, "Invalid Changelog");
        verify(gitHubService, times(0)).removeLabel(anyInt(), anyString());
    }

    @Test
    public void testValidateWhenChangelogEmptyAndHasLabel() {
        Changelog changelog = new Changelog();
        changelog.setChangelogRows(Lists.emptyList());

        ChangelogValidationStatus validationStatus = new ChangelogValidationStatus();
        validationStatus.setStatus(ChangelogValidationStatus.Status.INVALID);

        when(changelogGenerator.generate(any())).thenReturn(changelog);
        when(gitHubService.listIssueLabels(anyInt()).contains(anyString())).thenReturn(true);
        when(changelogValidator.validate(changelog)).thenReturn(validationStatus);

        PullRequest pullRequest = new PullRequest();
        pullRequest.setNumber(1);
        service.validate(pullRequest);

        verify(gitHubService, times(0)).createIssueComment(anyInt(), anyString());
        verify(gitHubService, times(0)).addLabel(anyInt(), anyString());
        verify(gitHubService, times(0)).removeLabel(anyInt(), anyString());
    }

    @Test
    public void testValidateWhenHasInvalidClass() {
        Changelog changelog = new Changelog();
        changelog.setChangelogRows(Lists.emptyList());

        ChangelogValidationStatus validationStatus = new ChangelogValidationStatus();
        validationStatus.setMessage("Reason: unknown classes detected. Next should be changed or removed: `invalid`.");
        validationStatus.setStatus(ChangelogValidationStatus.Status.INVALID);

        when(changelogGenerator.generate(any())).thenReturn(changelog);
        when(gitHubService.listIssueLabels(anyInt()).contains(anyString())).thenReturn(false);
        when(changelogValidator.validate(changelog)).thenReturn(validationStatus);

        PullRequest pullRequest = new PullRequest();
        pullRequest.setNumber(1);
        service.validate(pullRequest);

        verify(gitHubService).createIssueComment(1,
                "**Warning!** Invalid changelog detected.\n\n" +
                        "Reason: unknown classes detected. Next should be changed or removed: `invalid`.");
        verify(gitHubService).addLabel(1, "Invalid Changelog");
        verify(gitHubService, times(0)).removeLabel(anyInt(), anyString());
    }

    @Test
    public void testValidateWhenNoChangelog() {
        when(changelogGenerator.generate(any())).thenReturn(new Changelog());
        when(gitHubService.listIssueLabels(anyInt()).contains(anyString())).thenReturn(false);

        service.validate(new PullRequest());

        verify(gitHubService, times(0)).createIssueComment(anyInt(), anyString());
        verify(gitHubService, times(0)).addLabel(anyInt(), anyString());
        verify(gitHubService, times(0)).removeLabel(anyInt(), anyString());
        verify(changelogValidator, times(0)).validate(any());
    }

    @Test
    public void testGetChangelogClassesList() {
        Changelog changelog = new Changelog();
        changelog.setChangelogRows(Lists.newArrayList(
                new ChangelogRow("map", false, null),
                new ChangelogRow("map", false, null),
                new ChangelogRow("fix", false, null)
        ));

        when(changelogGenerator.generate(any())).thenReturn(changelog);

        Set<String> expectedList = Sets.newSet("fix", "map");

        assertEquals(expectedList, service.getChangelogClassesList(new PullRequest()));
    }

    @Test
    public void testGetChangelogClassesListWhenEmpty() {
        when(changelogGenerator.generate(any())).thenReturn(new Changelog());
        assertEquals(0, service.getChangelogClassesList(new PullRequest()).size());
    }
}