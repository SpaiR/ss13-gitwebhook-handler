package io.github.spair.service.changelog;

import io.github.spair.service.changelog.entities.Changelog;
import io.github.spair.service.changelog.entities.ChangelogRow;
import io.github.spair.service.changelog.entities.ChangelogValidationStatus;
import io.github.spair.service.config.ConfigService;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChangelogValidatorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigService configService;
    private ChangelogValidator validator;

    @Before
    public void setUp() {
        validator = new ChangelogValidator(configService);
        when(configService.getConfig().getChangelogConfig().getHtml().getAvailableClasses())
                .thenReturn(Sets.newSet("entry1", "entry2"));
    }

    @Test
    public void testValidateWhenAllValid() {
        ChangelogRow changelogRow1 = new ChangelogRow();
        changelogRow1.setChanges("Some changes");
        changelogRow1.setClassName("entry1");

        ChangelogRow changelogRow2 = new ChangelogRow();
        changelogRow2.setChanges("Some changes");
        changelogRow2.setClassName("entry2");

        List<ChangelogRow> changelogRows = Lists.newArrayList(changelogRow1, changelogRow2);

        Changelog changelog = new Changelog();
        changelog.setChangelogRows(changelogRows);

        ChangelogValidationStatus status = validator.validate(changelog);

        assertEquals(ChangelogValidationStatus.Status.VALID, status.getStatus());
    }

    @Test
    public void testValidateWhenMultiInvalidEntryClass() {
        ChangelogRow changelogRow1 = new ChangelogRow();
        changelogRow1.setChanges("Some changes");
        changelogRow1.setClassName("invalid-entry1");

        ChangelogRow changelogRow2 = new ChangelogRow();
        changelogRow2.setChanges("Some changes");
        changelogRow2.setClassName("invalid-entry2");

        List<ChangelogRow> changelogRows = Lists.newArrayList(changelogRow1, changelogRow2);

        Changelog changelog = new Changelog();
        changelog.setChangelogRows(changelogRows);

        ChangelogValidationStatus status = validator.validate(changelog);

        assertEquals(ChangelogValidationStatus.Status.INVALID, status.getStatus());
        assertEquals("Reason: unknown classes detected. Next should be changed or removed: `invalid-entry1`, `invalid-entry2`.", status.getMessage());
    }

    @Test
    public void testValidateWhenInvalidEntryClass() {
        ChangelogRow changelogRow = new ChangelogRow();
        changelogRow.setChanges("Some changes");
        changelogRow.setClassName("invalid-entry");

        List<ChangelogRow> changelogRows = Lists.newArrayList(changelogRow);

        Changelog changelog = new Changelog();
        changelog.setChangelogRows(changelogRows);

        ChangelogValidationStatus status = validator.validate(changelog);

        assertEquals(ChangelogValidationStatus.Status.INVALID, status.getStatus());
        assertEquals("Reason: unknown classes detected. Next should be changed or removed: `invalid-entry`.", status.getMessage());
    }

    @Test
    public void testValidateWhenEmptyChangelog() {
        Changelog changelog = new Changelog();
        changelog.setChangelogRows(Lists.emptyList());

        ChangelogValidationStatus status = validator.validate(changelog);

        assertEquals(ChangelogValidationStatus.Status.INVALID, status.getStatus());
        assertEquals("Reason: empty changelog. Please, check markdown correctness.", status.getMessage());
    }
}