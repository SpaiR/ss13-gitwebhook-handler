package io.github.spair.service.dmi.report;

import io.github.spair.byond.dmi.Dmi;
import io.github.spair.service.dmi.entity.ReportEntry;

import static io.github.spair.service.dmi.report.DmiReportCreator.NEW_LINE;

class StatesNumberAppender implements ReportAppender {

    private static final String OVERFLOW_MESSAGE = " (overflow)";
    private static final String OLD_DMI = "Old";
    private static final String NEW_DMI = "New";

    @Override
    public void append(final StringBuilder sb, final ReportEntry reportEntry) {
        sb.append("States number:").append(NEW_LINE);
        appendNumber(sb, OLD_DMI, reportEntry.getOldStatesNumber());
        appendNumber(sb, NEW_DMI, reportEntry.getNewStatesNumber());

        if (reportEntry.isStateOverflow()) {
            sb.append(NEW_LINE);
            sb.append("*DMI file has limit to 512 states. States overflow is when this limit was overpassed."
                    + " It's a **critical** problem which should be fixed.*");
            sb.append(NEW_LINE);
        }
    }

    private void appendNumber(final StringBuilder sb, final String dmiType, final int number) {
        sb.append("- **").append(dmiType).append(" DMI:** ").append(number);

        if (number > Dmi.MAX_STATES) {
            sb.append(OVERFLOW_MESSAGE);
        }

        sb.append(NEW_LINE);
    }
}
