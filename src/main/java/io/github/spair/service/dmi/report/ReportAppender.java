package io.github.spair.service.dmi.report;

import io.github.spair.service.dmi.entities.ReportEntry;

public interface ReportAppender {
    void append(final StringBuilder sb, final ReportEntry reportEntry);
}
