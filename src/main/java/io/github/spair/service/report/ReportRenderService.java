package io.github.spair.service.report;

import java.util.List;

public interface ReportRenderService<T> {

    String DMI = "dmiReportService";

    String renderStatus(List<T> statusList);

    String renderError();
}
