package io.github.spair.service.report;

import java.util.List;

import static io.github.spair.service.report.TextConstants.NEW_LINE;

public abstract class AbstractReportRenderService<T> implements ReportRenderService<T> {

    private static final String DETAILS_OPEN = "<details>";
    private static final String DETAILS_CLOSE = "</details>";

    private static final String SUMMARY_OPEN = "<summary>";
    private static final String SUMMARY_CLOSE = "/<summary>";

    private static final String LINE_HORIZONTAL = "<hr />";

    @Override
    public final String renderStatus(final List<T> statusList) {
        StringBuilder report = new StringBuilder();

        report.append(renderTitle()).append(NEW_LINE).append(NEW_LINE);
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
