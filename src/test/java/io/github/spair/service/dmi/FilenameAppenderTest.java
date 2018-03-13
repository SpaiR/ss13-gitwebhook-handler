package io.github.spair.service.dmi;

import io.github.spair.service.dmi.entities.ReportEntry;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FilenameAppenderTest {

    private final FilenameAppender appender = new FilenameAppender();
    private StringBuilder sb;
    private ReportEntry reportEntry;

    @Before
    public void setUp() {
        sb = new StringBuilder();
        reportEntry = new ReportEntry("icons/file.dmi");
    }

    @Test
    public void testAppendWhenNoStatuses() {
        appender.append(sb, reportEntry);
        assertEquals("<summary>icons/file.dmi</summary>", sb.toString());
    }

    @Test
    public void testAppendWhenHasDuplicates() {
        reportEntry.getDuplication().getNewDmiDuplicates().add("state");
        appender.append(sb, reportEntry);
        assertEquals("<summary>icons/file.dmi <b><< duplicates</b></summary>", sb.toString());
    }

    @Test
    public void testAppendWhenHasStateOverflow() {
        reportEntry.setOldStatesNumber(513);
        appender.append(sb, reportEntry);
        assertEquals("<summary>icons/file.dmi <b><< state overflow</b></summary>", sb.toString());
    }

    @Test
    public void testAppendWhenHasAllStatuses() {
        reportEntry.getDuplication().getNewDmiDuplicates().add("state");
        reportEntry.setOldStatesNumber(513);

        appender.append(sb, reportEntry);

        assertEquals("<summary>icons/file.dmi <b><< duplicates | state overflow</b></summary>", sb.toString());
    }
}