package io.github.spair.service.dmi;

import io.github.spair.byond.dmi.DmiDiff;
import io.github.spair.byond.dmi.SpriteDir;
import io.github.spair.service.dmi.entities.DmiDiffReport;
import io.github.spair.service.dmi.entities.ReportEntry;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ReportPrinterTest {

    private final ReportPrinter printer = new ReportPrinter();

    @Test
    public void testPrintReportCreated() throws Exception {
        ReportEntry reportEntry = new ReportEntry("icons/file.dmi");
        reportEntry.setStateDiffReports(Collections.singletonList(createStateDiffReport(DmiDiff.Status.CREATED)));

        DmiDiffReport diffReport = new DmiDiffReport();
        diffReport.setReportEntries(Collections.singletonList(reportEntry));

        File reportFile = new ClassPathResource("dmi-diff-report-created.txt").getFile();
        String reportText = new String(Files.readAllBytes(reportFile.toPath()));

        assertEquals(reportText, printer.printReport(diffReport));
    }

    @Test
    public void testPrintReportModifiedWithDuplicate() throws Exception {
        ReportEntry reportEntry = new ReportEntry("icons/file.dmi");
        reportEntry.setStateDiffReports(Collections.singletonList(createStateDiffReport(DmiDiff.Status.MODIFIED)));
        reportEntry.setDuplication(createDuplicate(Sets.newSet("state, state2"), Sets.newSet("state2")));

        DmiDiffReport diffReport = new DmiDiffReport();
        diffReport.setReportEntries(Collections.singletonList(reportEntry));

        File reportFile = new ClassPathResource("dmi-diff-report-modified-with-duplicates.txt").getFile();
        String reportText = new String(Files.readAllBytes(reportFile.toPath()));

        assertEquals(reportText, printer.printReport(diffReport));
    }

    @Test
    public void testPrintReportDeletedWithMetadataDiff() throws Exception {
        ReportEntry reportEntry = new ReportEntry("icons/file.dmi");
        reportEntry.setStateDiffReports(Collections.singletonList(createStateDiffReport(DmiDiff.Status.DELETED)));
        reportEntry.setMetadata(createMeta());

        DmiDiffReport diffReport = new DmiDiffReport();
        diffReport.setReportEntries(Collections.singletonList(reportEntry));

        File reportFile = new ClassPathResource("dmi-diff-report-deleted-with-meta-diff.txt").getFile();
        String reportText = new String(Files.readAllBytes(reportFile.toPath()));

        assertEquals(reportText, printer.printReport(diffReport));
    }

    private ReportEntry.StateDiffReport createStateDiffReport(DmiDiff.Status status) {
        ReportEntry.StateDiffReport stateDiffReport = new ReportEntry.StateDiffReport();
        stateDiffReport.setName("state");
        stateDiffReport.setSpriteWidth(32);
        stateDiffReport.setSpriteHeight(32);
        stateDiffReport.setDir(SpriteDir.SOUTH);
        stateDiffReport.setOldDmiLink("old.link.com");
        stateDiffReport.setNewDmiLink("new.link.com");
        stateDiffReport.setStatus(status);
        stateDiffReport.setFrameNumber(1);
        return stateDiffReport;
    }

    private ReportEntry.Duplication createDuplicate(Set<String> oldDupls, Set<String> newDupls) {
        ReportEntry.Duplication duplication = new ReportEntry.Duplication();
        duplication.setOldDmiDuplicates(oldDupls);
        duplication.setNewDmiDuplicates(newDupls);
        return duplication;
    }

    private ReportEntry.Metadata createMeta() {
        ReportEntry.Metadata metadata = new ReportEntry.Metadata();
        metadata.setMetadataDiff("[ { 'mocked' : 'diff' } ]");
        return metadata;
    }
}