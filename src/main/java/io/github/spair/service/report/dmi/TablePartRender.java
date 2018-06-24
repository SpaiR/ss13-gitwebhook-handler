package io.github.spair.service.report.dmi;

import io.github.spair.byond.dmi.SpriteDir;
import io.github.spair.service.dmi.entity.DmiDiffStatus;
import io.github.spair.service.dmi.entity.DmiSpriteDiffStatus;
import io.github.spair.service.image.ImageHelper;
import io.github.spair.service.report.BodyPartRender;

import static io.github.spair.service.report.TextConstants.NEW_LINE;

final class TablePartRender implements BodyPartRender<DmiDiffStatus> {

    private static final String TABLE_DELIMITER = "|";
    private static final int[] IMG_RESIZE_MULTIPLIERS = new int[]{1, 4, 8};

    @Override
    public String render(final DmiDiffStatus status) {
        if (status.getSpritesDiffStatuses().isEmpty()) {
            return "";
        }

        StringBuilder bodyPart = new StringBuilder();

        bodyPart.append("Key | Dir / Frame | Old | New | Status").append(NEW_LINE);
        bodyPart.append("--- | :---------: | --- | --- | ------").append(NEW_LINE);

        status.getSpritesDiffStatuses().stream().map(this::renderTableRows).forEach(bodyPart::append);

        return bodyPart.toString();
    }

    private String renderTableRows(final DmiSpriteDiffStatus spriteDiffStatus) {
        final String simpleName = spriteDiffStatus.getName();
        final String nameWithLink = createRowNameWithLinks(spriteDiffStatus);

        final String dirShortName = spriteDiffStatus.getDir().shortName;
        final int frame = spriteDiffStatus.getFrameNumber();

        final String titleTmpl = simpleName + "-%s-d" + dirShortName + "-f" + frame;
        final String titleForOldImage = String.format(titleTmpl, "O");
        final String titleForNewImage = String.format(titleTmpl, "N");

        final String dirArrow = DirArrowCreator.create(spriteDiffStatus.getDir());

        final String oldImgTag = ImageHelper.wrapInImgTag(spriteDiffStatus.getOldSpriteImageLink(), titleForOldImage);
        final String newImgTag = ImageHelper.wrapInImgTag(spriteDiffStatus.getNewSpriteImageLink(), titleForNewImage);

        final String status = spriteDiffStatus.getStatus();

        return nameWithLink + TABLE_DELIMITER
                + dirArrow + " " + dirShortName + " / " + frame + TABLE_DELIMITER
                + oldImgTag + TABLE_DELIMITER
                + newImgTag + TABLE_DELIMITER
                + status + NEW_LINE;
    }

    private String createRowNameWithLinks(final DmiSpriteDiffStatus status) {
        StringBuilder multipliedLinks = new StringBuilder();

        for (int multiplier : IMG_RESIZE_MULTIPLIERS) {
            multipliedLinks.append(String.format("[x%d](%s) ", multiplier, formatImageLink(status, multiplier)));
        }

        multipliedLinks.deleteCharAt(multipliedLinks.length() - 1);

        return String.format("%s (%s)", status.getName(), multipliedLinks.toString());
    }

    private String formatImageLink(final DmiSpriteDiffStatus status, final int multiplier) {
        final int resizedWidth = status.getSpriteWidth() * multiplier;
        final int resizedHeight = status.getSpriteHeight() * multiplier;
        final String linkBefore = status.getOldSpriteImageLink();
        final String linkAfter = status.getNewSpriteImageLink();
        return ImageHelper.wrapDiffLinksWithResize(linkBefore, linkAfter, resizedWidth, resizedHeight);
    }

    private static final class DirArrowCreator {

        private static String create(final SpriteDir dir) {
            switch (dir) {
                case SOUTH:
                    return "&#x1F87B;";  // 🡻
                case NORTH:
                    return "&#x1F879;";  // 🡹
                case EAST:
                    return "&#x1F87A;";  // 🡺
                case WEST:
                    return "&#x1F878;";  // 🡸
                case SOUTHEAST:
                    return "&#x1F87E;";  // 🡾
                case SOUTHWEST:
                    return "&#x1F87F;";  // 🡿
                case NORTHEAST:
                    return "&#x1F87D;";  // 🡽
                case NORTHWEST:
                    return "&#x1F87C;";  // 🡼
                default:
                    return "?";
            }
        }

        private DirArrowCreator() {
        }
    }
}
