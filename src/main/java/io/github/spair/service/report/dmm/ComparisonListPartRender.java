package io.github.spair.service.report.dmm;

import io.github.spair.service.dmm.entity.DmmChunkDiff;
import io.github.spair.service.dmm.entity.DmmDiffStatus;
import io.github.spair.service.report.BodyPartRender;
import io.github.spair.service.report.ReportHelper;

import java.util.List;

import static io.github.spair.service.report.ReportConstants.DETAILS_OPEN;
import static io.github.spair.service.report.ReportConstants.DETAILS_CLOSE;
import static io.github.spair.service.report.ReportConstants.SUMMARY_OPEN;
import static io.github.spair.service.report.ReportConstants.SUMMARY_CLOSE;
import static io.github.spair.service.report.ReportConstants.NEW_LINE;

final class ComparisonListPartRender implements BodyPartRender<DmmDiffStatus> {

    private static final String TITLE = "<b>Comparison</b>";

    private static final String OBJECTS_HEADER = "#### For objects:";
    private static final String AREAS_HEADER = "#### For areas:";

    @Override
    public String render(final DmmDiffStatus status) {
        StringBuilder bodyPart = new StringBuilder();

        bodyPart.append(DETAILS_OPEN);

        bodyPart.append(SUMMARY_OPEN);
        bodyPart.append(TITLE);
        bodyPart.append(SUMMARY_CLOSE);

        bodyPart.append(NEW_LINE).append(NEW_LINE);

        int chunkNumber = 0;

        for (DmmChunkDiff dmmChunkDiff : status.getDmmDiffChunks()) {
            chunkNumber++;

            bodyPart.append(OBJECTS_HEADER).append(NEW_LINE);
            appendListsItems(bodyPart, chunkNumber,
                    dmmChunkDiff.getOldChunkImagesLinks(), dmmChunkDiff.getNewChunkImagesLinks()
            );
            bodyPart.append(NEW_LINE);

            bodyPart.append(AREAS_HEADER).append(NEW_LINE);
            appendListsItems(bodyPart, chunkNumber,
                    dmmChunkDiff.getOldChunkAreasImagesLinks(), dmmChunkDiff.getNewChunkAreasImagesLinks()
            );
            bodyPart.append(NEW_LINE);
        }

        bodyPart.append(DETAILS_CLOSE);

        return bodyPart.toString();
    }

    private void appendListsItems(final StringBuilder bodyPart, final int currentChunkNumber,
                                  final List<String> oldImagesLinks, final List<String> newImagesLinks) {
        final int oldImagesLinksSize = oldImagesLinks.size();
        final int newImagesLinksSize = newImagesLinks.size();

        final int maxSize = Math.max(oldImagesLinksSize, newImagesLinksSize);

        for (int i = 0; i < maxSize; i++) {
            String oldLink = "";
            String newLink = "";

            if (i < oldImagesLinksSize) {
                oldLink = oldImagesLinks.get(i);
            }
            if (i < newImagesLinksSize) {
                newLink = newImagesLinks.get(i);
            }

            String beforeAfterLink = ReportHelper.createBeforeAfterDiffLink(oldLink, newLink, 800, 800, false);
            String link = String.format("[Chunk %d - Image %d](%s)", currentChunkNumber, i + 1, beforeAfterLink);

            bodyPart.append(" - ").append(link).append(NEW_LINE);
        }
    }
}
