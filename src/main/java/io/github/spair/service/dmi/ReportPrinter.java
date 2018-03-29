package io.github.spair.service.dmi;

import io.github.spair.service.dmi.entities.DmiDiffReport;
import org.springframework.stereotype.Component;

@Component
class ReportPrinter {

    private final ReportAppender filenameAppender = new FilenameAppender();
    private final ReportAppender duplicationAppender = new DuplicationAppender();
    private final ReportAppender statesTableAppender = new StatesTableAppender();
    private final ReportAppender statesNumberAppender = new StatesNumberAppender();

    static final String SUMMARY_TEMPLATE = "<summary>%s</summary>";
    static final String NEW_LINE = System.getProperty("line.separator");

    private static final String DETAILS_OPEN = "<details>";
    private static final String DETAILS_CLOSE = "</details>";
    private static final String LINE_HORIZONTAL = "<hr />";

    String printReport(final DmiDiffReport report) {
        StringBuilder sb = new StringBuilder();

        sb.append(DmiDiffReport.TITLE).append(NEW_LINE).append(NEW_LINE);

        report.getReportEntries().forEach(reportEntry -> {
            sb.append(DETAILS_OPEN);

            filenameAppender.append(sb, reportEntry);

            sb.append(LINE_HORIZONTAL);
            sb.append(NEW_LINE).append(NEW_LINE);

            duplicationAppender.append(sb, reportEntry);
            statesTableAppender.append(sb, reportEntry);

            statesNumberAppender.append(sb, reportEntry);

            sb.append(LINE_HORIZONTAL);
            sb.append(DETAILS_CLOSE);
        });

        return sb.toString();
    }

    String printErrorReason() {
        return DmiDiffReport.TITLE + NEW_LINE + NEW_LINE
                + "Report is too long, it can't be print. Make PR more atomic, please.";
    }
}