package io.github.spair.service.dmi.report;

import io.github.spair.ReadFileUtil;
import io.github.spair.byond.dmi.DmiDiff;
import io.github.spair.byond.dmi.SpriteDir;
import io.github.spair.service.dmi.entities.ReportEntry;
import io.github.spair.service.dmi.entities.StateDiffReport;
import io.github.spair.service.dmi.report.StatesTableAppender;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class StatesTableAppenderTest {

    private final StatesTableAppender appender = new StatesTableAppender();

    @Test
    public void testAppend() {
        StringBuilder sb = new StringBuilder();
        ReportEntry reportEntry = new ReportEntry("icons/file.dmi");

        reportEntry.setStateDiffReports(Arrays.asList(
                createStateDiff("state1", SpriteDir.SOUTH, 1, DmiDiff.Status.CREATED),
                createStateDiff("state2", SpriteDir.SOUTH, 1, DmiDiff.Status.MODIFIED),
                createStateDiff("state3", SpriteDir.NORTH, 2, DmiDiff.Status.DELETED)
        ));

        appender.append(sb, reportEntry);

        String expectedReport = ReadFileUtil.readFile("appenders-reports/states-table-report.txt");
        assertEquals(expectedReport, sb.toString());
    }

    private StateDiffReport createStateDiff(String name, SpriteDir dir, int frame, DmiDiff.Status status) {
        StateDiffReport report = new StateDiffReport();

        report.setName(name);
        report.setDir(dir);
        report.setSpriteWidth(32);
        report.setSpriteHeight(32);
        report.setFrameNumber(frame);

        switch (status) {
            case CREATED:
                report.setNewDmiLink("img.org/" + name + ".NEW");
                break;
            case MODIFIED:
                report.setOldDmiLink("img.org/" + name + ".OLD");
                report.setNewDmiLink("img.org/" + name + ".NEW");
                break;
            case DELETED:
                report.setNewDmiLink("img.org/" + name + ".NEW");
                break;
        }

        report.setStatus(status);

        return report;
    }
}