package io.github.spair.service.dmi;

import io.github.spair.service.dmi.entities.ReportEntry;

import static io.github.spair.service.dmi.ReportPrinter.NEW_LINE;

public class StatesNumberAppender implements ReportAppender {

    @Override
    public void append(final StringBuilder sb, final ReportEntry reportEntry) {
        sb.append("States number:").append(NEW_LINE);
        sb.append("- **Old DMI:** ").append(reportEntry.getOldStatesNumber()).append(NEW_LINE);
        sb.append("- **New DMI:** ").append(reportEntry.getNewStatesNumber()).append(NEW_LINE);
    }
}
