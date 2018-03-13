package io.github.spair.service.dmi;

import io.github.spair.service.dmi.entities.ReportEntry;

import java.util.ArrayList;
import java.util.List;

import static io.github.spair.service.dmi.ReportPrinter.SUMMARY_TEMPLATE;

public class FilenameAppender implements ReportAppender {

    private static final String DUPLICATES = "duplicates";
    private static final String STATE_OVERFLOW = "state overflow";

    @Override
    public void append(final StringBuilder sb, final ReportEntry reportEntry) {
        String filename = reportEntry.getFilename();
        List<String> statuses = new ArrayList<>();

        if (reportEntry.getDuplication().isHasDuplicates()) {
            statuses.add(DUPLICATES);
        }

        if (reportEntry.isStateOverflow()) {
            statuses.add(STATE_OVERFLOW);
        }

        if (!statuses.isEmpty()) {
            filename = filename + " <b><< " + String.join(" | ", statuses) + "</b>";
        }

        sb.append(String.format(SUMMARY_TEMPLATE, filename));
    }
}
