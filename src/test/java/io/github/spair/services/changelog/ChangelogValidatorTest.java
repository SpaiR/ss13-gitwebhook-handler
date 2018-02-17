package io.github.spair.services.changelog;

import io.github.spair.services.changelog.entities.Changelog;
import io.github.spair.services.changelog.entities.ChangelogRow;
import io.github.spair.services.changelog.entities.ChangelogValidationStatus;
import io.github.spair.services.config.ConfigService;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mockito;
import org.mockito.internal.util.collections.Sets;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ChangelogValidatorTest {

    private ConfigService configService;

    @Before
    public void setUp() {
        configService = Mockito.mock(ConfigService.class, Answers.RETURNS_DEEP_STUBS.get());
        Mockito.when(configService.getConfig().getChangelogConfig().getHtml().getAvailableClasses())
                .thenReturn(Sets.newSet("entry1", "entry2"));
    }

    @Test
    public void testValidateWhenAllValid() {
        ChangelogValidator validator = new ChangelogValidator(configService);

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
        ChangelogValidator validator = new ChangelogValidator(configService);

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
        ChangelogValidator validator = new ChangelogValidator(configService);

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
        ChangelogValidator validator = new ChangelogValidator(configService);

        Changelog changelog = new Changelog();
        changelog.setChangelogRows(Lists.emptyList());

        ChangelogValidationStatus status = validator.validate(changelog);

        assertEquals(ChangelogValidationStatus.Status.INVALID, status.getStatus());
        assertEquals("Reason: empty changelog. Please, check markdown correctness.", status.getMessage());
    }
}