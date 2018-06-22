package io.github.spair.service.dmi.report;

import io.github.spair.service.dmi.entity.ReportEntry;

public interface ReportAppender {
    void append(final StringBuilder sb, final ReportEntry reportEntry);
}
