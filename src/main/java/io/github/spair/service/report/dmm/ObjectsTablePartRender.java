package io.github.spair.service.report.dmm;

import io.github.spair.service.dmm.entity.DmmChunkDiff;
import io.github.spair.service.dmm.entity.DmmDiffStatus;
import io.github.spair.service.report.BodyPartRender;

import static io.github.spair.service.report.ReportConstants.*;
import static io.github.spair.service.report.dmm.DmmReportHelper.appendImgIfNotEmpty;

final class ObjectsTablePartRender implements BodyPartRender<DmmDiffStatus> {

    @Override
    public String render(final DmmDiffStatus status) {
        StringBuilder bodyPart = new StringBuilder();

        bodyPart.append("Old|New|Difference").append(NEW_LINE);
        bodyPart.append("---|---|----------").append(NEW_LINE);

        int chunkNumber = 0;

        for (DmmChunkDiff dmmChunkDiff : status.getDmmDiffChunks()) {
            chunkNumber++;
            appendImgIfNotEmpty(bodyPart, dmmChunkDiff.getOldChunkImagesLinks(), chunkNumber).append(TABLE_DELIMITER);
            appendImgIfNotEmpty(bodyPart, dmmChunkDiff.getNewChunkImagesLinks(), chunkNumber).append(TABLE_DELIMITER);
            appendImgIfNotEmpty(bodyPart, dmmChunkDiff.getDiffImagesLinks(), chunkNumber).append(NEW_LINE);
        }

        return bodyPart.toString();
    }
}
