package io.github.spair.service.report;

import java.util.List;

import static io.github.spair.service.report.ReportConstants.DETAILS_CLOSE;
import static io.github.spair.service.report.ReportConstants.DETAILS_OPEN;
import static io.github.spair.service.report.ReportConstants.SUMMARY_OPEN;
import static io.github.spair.service.report.ReportConstants.SUMMARY_CLOSE;
import static io.github.spair.service.report.ReportConstants.NEW_LINE;
import static io.github.spair.service.report.ReportConstants.LINE_HORIZONTAL;

public abstract class AbstractReportRenderService<T> implements ReportRenderService<T> {

    @Override
    public final String renderStatus(final List<T> statusList) {
        StringBuilder report = new StringBuilder(renderTitle()).append(NEW_LINE).append(NEW_LINE);

        statusList.forEach(status -> {
            report.append(DETAILS_OPEN);

            report.append(SUMMARY_OPEN);
            report.append(renderHeader(status));
            report.append(SUMMARY_CLOSE);

            report.append(LINE_HORIZONTAL);
            report.append(NEW_LINE).append(NEW_LINE);

            report.append(renderBody(status));

            report.append(LINE_HORIZONTAL);
            report.append(DETAILS_CLOSE);
        });

        return report.toString();
    }

    protected abstract String renderTitle();

    protected abstract String renderHeader(T status);

    protected abstract String renderBody(T status);
}
