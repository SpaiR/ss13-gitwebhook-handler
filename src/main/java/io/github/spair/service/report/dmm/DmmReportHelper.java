package io.github.spair.service.report.dmm;

import io.github.spair.service.report.ReportHelper;

import java.util.List;

import static io.github.spair.service.report.ReportConstants.LINE_BREAK;

final class DmmReportHelper {

    private static final String EMPTY = "Empty";

    static StringBuilder appendImgIfNotEmpty(
            final StringBuilder bodyPart, final List<String> links, final int currentChunkNumber) {
        if (links.isEmpty()) {
            bodyPart.append(EMPTY);
        } else {
            int imageNumber = 0;
            for (String link : links) {
                String imageTitle = String.format("Chunk %d - Image %d", currentChunkNumber, ++imageNumber);
                bodyPart.append(ReportHelper.createImgTag(link, imageTitle)).append(LINE_BREAK);
            }
        }
        return bodyPart;
    }

    private DmmReportHelper() {
    }
}
