package io.github.spair.service.report.dmm;

import io.github.spair.service.dmm.entity.DmmChunkDiff;
import io.github.spair.service.dmm.entity.DmmDiffStatus;
import io.github.spair.service.report.BodyPartRender;

import java.util.List;

import static io.github.spair.service.report.ReportConstants.*;

final class RawLinksPartRender implements BodyPartRender<DmmDiffStatus> {

    private static final String TITLE = "Raw Links <i>(use if GitHub didn't show something properly)</i>";
    private static final String TABLE_HEADER = "Old|New|Difference" + NEW_LINE + ":-:|:-:|:--------:";

    private static final String OBJECTS_HEADER = "#### For objects:";
    private static final String AREAS_HEADER = "#### For areas:";

    private static final String EMPTY = "Empty";

    @Override
    public String render(final DmmDiffStatus status) {
        int chunkNumber = 0;

        StringBuilder objectsTableBuilder = new StringBuilder();
        StringBuilder areasTableBuilder = new StringBuilder();

        for (DmmChunkDiff chunkDiff : status.getDmmDiffChunks()) {
            chunkNumber++;

            appendLinks(objectsTableBuilder, chunkNumber, chunkDiff.getOldChunkImagesLinks());
            objectsTableBuilder.append(TABLE_DELIMITER);
            appendLinks(areasTableBuilder, chunkNumber, chunkDiff.getOldChunkAreasImagesLinks());
            areasTableBuilder.append(TABLE_DELIMITER);

            appendLinks(objectsTableBuilder, chunkNumber, chunkDiff.getNewChunkImagesLinks());
            objectsTableBuilder.append(TABLE_DELIMITER);
            appendLinks(areasTableBuilder, chunkNumber, chunkDiff.getNewChunkAreasImagesLinks());
            areasTableBuilder.append(TABLE_DELIMITER);

            appendLinks(objectsTableBuilder, chunkNumber, chunkDiff.getDiffImagesLinks());
            appendLinks(areasTableBuilder, chunkNumber, chunkDiff.getDiffAreasImagesLinks());

            objectsTableBuilder.append(NEW_LINE);
            areasTableBuilder.append(NEW_LINE);
        }

        return DETAILS_OPEN
                + SUMMARY_OPEN + TITLE + SUMMARY_CLOSE
                + NEW_LINE + NEW_LINE
                + OBJECTS_HEADER
                + NEW_LINE + NEW_LINE
                + TABLE_HEADER + NEW_LINE
                + objectsTableBuilder
                + NEW_LINE
                + AREAS_HEADER
                + NEW_LINE + NEW_LINE
                + TABLE_HEADER + NEW_LINE
                + areasTableBuilder
                + DETAILS_CLOSE;
    }

    private void appendLinks(final StringBuilder bodyPart, final int currentChunkNumber, final List<String> links) {
        if (links.isEmpty()) {
            bodyPart.append(EMPTY);
        } else {
            int linkNumber = 0;
            for (String link : links) {
                String linkText = String.format("Chunk %d - Image %d", currentChunkNumber, ++linkNumber);
                bodyPart.append("[").append(linkText).append("](").append(link).append(")").append(LINE_BREAK);
            }
        }
    }
}
