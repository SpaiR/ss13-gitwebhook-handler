package io.github.spair.service.changelog;

import io.github.spair.service.changelog.entity.Changelog;
import io.github.spair.service.changelog.entity.ChangelogRow;
import io.github.spair.service.config.ConfigService;
import io.github.spair.service.pr.entity.PullRequest;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ChangelogServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigService configService;
    @Mock
    private ChangelogGenerator changelogGenerator;
    @Mock
    private HtmlChangelogGenerator htmlChangelogGenerator;
    @Mock
    private ChangelogValidator changelogValidator;

    private ChangelogService service;

    @Before
    public void setUp() {
        service = new ChangelogService(configService);
        when(configService.getConfig().getGitHubConfig().getLabels().getInvalidChangelog()).thenReturn("Invalid Changelog");

        ReflectionTestUtils.setField(service, "changelogGenerator", changelogGenerator);
        ReflectionTestUtils.setField(service, "htmlChangelogGenerator", htmlChangelogGenerator);
        ReflectionTestUtils.setField(service, "changelogValidator", changelogValidator);
    }

    @Test
    public void testCreateFromPullRequest() {
        PullRequest pullRequest = mock(PullRequest.class);
        service.createFromPullRequest(pullRequest);
        verify(changelogGenerator).generate(pullRequest);
    }

    @Test
    public void testValidateChangelog() {
        Changelog changelog = mock(Changelog.class);
        service.validateChangelog(changelog);
        verify(changelogValidator).validate(changelog);
    }

    @Test
    public void testMergeHtmlWithChangelog() {
        String html = "";
        Changelog changelog = mock(Changelog.class);
        service.mergeHtmlWithChangelog(html, changelog);
        verify(htmlChangelogGenerator).generate(html, changelog);
    }

    @Test
    public void testGetChangelogClassesList() {
        Changelog changelog = new Changelog();
        changelog.setChangelogRows(Lists.newArrayList(
                new ChangelogRow("map", false, null),
                new ChangelogRow("map", false, null),
                new ChangelogRow("fix", false, null)
        ));

        when(changelogGenerator.generate(any())).thenReturn(Optional.of(changelog));

        Set<String> expectedList = Sets.newSet("fix", "map");

        assertEquals(expectedList, service.getChangelogClassesList(new PullRequest()));
    }

    @Test
    public void testGetChangelogClassesListWhenEmpty() {
        when(changelogGenerator.generate(any())).thenReturn(Optional.empty());
        assertEquals(0, service.getChangelogClassesList(new PullRequest()).size());
    }
}