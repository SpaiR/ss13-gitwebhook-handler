package io.github.spair.service.dmi;

import io.github.spair.ReadFileUtil;
import io.github.spair.service.dmi.entities.ReportEntry;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MetadataAppenderTest {

    private final MetadataAppender appender = new MetadataAppender();

    @Test
    public void testAppend() {
        StringBuilder sb = new StringBuilder();
        ReportEntry reportEntry = new ReportEntry("icons/file.dmi");
        reportEntry.getMetadata().setMetadataDiff("[ {'mocked' : 'meta diff'} ]");

        appender.append(sb, reportEntry);

        String expectedReport = ReadFileUtil.readFile("appenders-reports/metadata-report.txt");
        assertEquals(expectedReport, sb.toString());
    }
}