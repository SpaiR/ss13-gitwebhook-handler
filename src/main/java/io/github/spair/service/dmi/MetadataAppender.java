package io.github.spair.service.dmi;

import io.github.spair.service.dmi.entities.ReportEntry;

import static io.github.spair.service.dmi.ReportPrinter.DETAILS_OPEN;
import static io.github.spair.service.dmi.ReportPrinter.DETAILS_CLOSE;
import static io.github.spair.service.dmi.ReportPrinter.NEW_LINE;
import static io.github.spair.service.dmi.ReportPrinter.SUMMARY_TEMPLATE;

public class MetadataAppender implements ReportAppender {

    private static final String CODE_QUOTES = "```";

    @Override
    public void append(final StringBuilder sb, final ReportEntry reportEntry) {
        sb.append(DETAILS_OPEN);
        sb.append(String.format(SUMMARY_TEMPLATE, "DMI Metadata Diff")).append(NEW_LINE);
        sb.append(NEW_LINE);
        sb.append(CODE_QUOTES).append(NEW_LINE);
        sb.append(reportEntry.getMetadata().getMetadataDiff()).append(NEW_LINE);
        sb.append(CODE_QUOTES).append(NEW_LINE);
        sb.append(DETAILS_CLOSE);
    }
}
