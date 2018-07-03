package io.github.spair.service.report.dmm;

import io.github.spair.service.dmm.entity.DmmChunkDiff;
import io.github.spair.service.dmm.entity.DmmDiffStatus;
import io.github.spair.service.report.BodyPartRender;

import static io.github.spair.service.report.ReportConstants.DETAILS_OPEN;
import static io.github.spair.service.report.ReportConstants.DETAILS_CLOSE;
import static io.github.spair.service.report.ReportConstants.SUMMARY_OPEN;
import static io.github.spair.service.report.ReportConstants.SUMMARY_CLOSE;
import static io.github.spair.service.report.ReportConstants.NEW_LINE;
import static io.github.spair.service.report.ReportConstants.TABLE_DELIMITER;
import static io.github.spair.service.report.dmm.DmmReportHelper.appendImgIfNotEmpty;

final class AreasTablePartRender implements BodyPartRender<DmmDiffStatus> {

    private static final String TITLE = "<b>Areas</b>";

    @Override
    public String render(final DmmDiffStatus status) {
        StringBuilder bodyPart = new StringBuilder();

        bodyPart.append(DETAILS_OPEN);

        bodyPart.append(SUMMARY_OPEN);
        bodyPart.append(TITLE);
        bodyPart.append(SUMMARY_CLOSE);

        bodyPart.append(NEW_LINE).append(NEW_LINE);

        bodyPart.append("Old|New|Difference").append(NEW_LINE);
        bodyPart.append("---|---|----------").append(NEW_LINE);

        int chunkNumber = 0;

        for (DmmChunkDiff dmmChunkDiff : status.getDmmDiffChunks()) {
            chunkNumber++;
            appendImgIfNotEmpty(
                    bodyPart, dmmChunkDiff.getOldChunkAreasImagesLinks(), chunkNumber).append(TABLE_DELIMITER
            );
            appendImgIfNotEmpty(
                    bodyPart, dmmChunkDiff.getNewChunkAreasImagesLinks(), chunkNumber).append(TABLE_DELIMITER
            );
            appendImgIfNotEmpty(
                    bodyPart, dmmChunkDiff.getDiffAreasImagesLinks(), chunkNumber).append(NEW_LINE
            );
        }

        bodyPart.append(DETAILS_CLOSE);

        return bodyPart.toString();
    }
}
