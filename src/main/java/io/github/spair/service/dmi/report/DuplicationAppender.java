package io.github.spair.service.dmi.report;

import io.github.spair.service.dmi.entity.ReportEntry;

import java.util.Set;

import static io.github.spair.service.dmi.report.DmiReportCreator.NEW_LINE;

class DuplicationAppender implements ReportAppender {

    @Override
    public void append(final StringBuilder sb, final ReportEntry reportEntry) {
        if (!reportEntry.getDuplication().isHasDuplicates()) {
            return;
        }

        sb.append("**Warning!** States duplication detected:").append(NEW_LINE);

        final Set<String> oldDuplicates = reportEntry.getDuplication().getOldDmiDuplicates();
        final Set<String> newDuplicates = reportEntry.getDuplication().getNewDmiDuplicates();

        if (!oldDuplicates.isEmpty()) {
            sb.append("- **Old DMI:** ").append(oldDuplicates).append(NEW_LINE);
        }

        if (!newDuplicates.isEmpty()) {
            sb.append("- **New DMI:** ").append(newDuplicates).append(NEW_LINE);
        }

        sb.append(NEW_LINE);
        sb.append("*Duplication of states may result into unpredictable behavior in game and incorrect diff report,"
                + " so fix is recommended.*").append(NEW_LINE);
        sb.append(NEW_LINE);
    }
}
