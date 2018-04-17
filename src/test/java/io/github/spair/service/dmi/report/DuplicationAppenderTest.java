package io.github.spair.service.dmi.report;

import io.github.spair.ReadFileUtil;
import io.github.spair.service.dmi.entities.ReportEntry;
import io.github.spair.service.dmi.report.DuplicationAppender;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DuplicationAppenderTest {

    private final DuplicationAppender appender = new DuplicationAppender();
    private StringBuilder sb;
    private ReportEntry reportEntry;

    @Before
    public void setUp() {
        sb = new StringBuilder();
        reportEntry = new ReportEntry("icons/file.dmi");
    }

    @Test
    public void testAppendWheNoDuplicates() {
        appender.append(sb, reportEntry);
        assertTrue(sb.toString().isEmpty());
    }

    @Test
    public void testAppendWhenHasDuplicates() {
        reportEntry.getDuplication().getOldDmiDuplicates().add("state1");
        reportEntry.getDuplication().getNewDmiDuplicates().addAll(Arrays.asList("state1", "state2"));

        appender.append(sb, reportEntry);

        String expectedReport = ReadFileUtil.readFile("appenders-reports/duplication-report.txt");
        assertEquals(expectedReport, sb.toString());
    }
}