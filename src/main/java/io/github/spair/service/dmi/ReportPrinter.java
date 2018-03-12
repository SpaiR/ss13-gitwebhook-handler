package io.github.spair.service.dmi;

import io.github.spair.byond.dmi.SpriteDir;
import io.github.spair.service.dmi.entities.DmiDiffReport;
import io.github.spair.service.dmi.entities.ReportEntry;
import io.github.spair.service.dmi.entities.StateDiffReport;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

@Component
class ReportPrinter {

    private static final String ORG_URL_SUFFIX = "org/";

    private static final String DETAILS_OPEN = "<details>";
    private static final String DETAILS_CLOSE = "</details>";
    private static final String SUMMARY_TEMPLATE = "<summary>%s</summary>";
    private static final String LINE_HORIZONTAL = "<hr />";
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String IMG_TEMPLATE = "<img src=\"%s\" title=\"%s\" />";
    private static final String CODE_QUOTES = "```";
    private static final String TABLE_DELIMITER = "|";

    private static final int[] IMG_RESIZE_MULTIPLIERS = new int[]{1, 4, 8};

    String printReport(final DmiDiffReport report) {
        StringBuilder sb = new StringBuilder();

        sb.append(DmiDiffReport.TITLE).append(NEW_LINE);
        sb.append(NEW_LINE);

        report.getReportEntries().forEach(reportEntry -> {
            sb.append(DETAILS_OPEN);
            appendFilename(sb, reportEntry);
            sb.append(LINE_HORIZONTAL).append(NEW_LINE);
            sb.append(NEW_LINE);

            if (reportEntry.getDuplication().isHasDuplicates()) {
                appendDuplication(sb, reportEntry);
            }

            if (!reportEntry.getStateDiffReports().isEmpty()) {
                appendStatesTable(sb, reportEntry.getStateDiffReports());
                sb.append(NEW_LINE);
            }

            appendMetadataTableDiff(sb, reportEntry);
            sb.append(LINE_HORIZONTAL);
            sb.append(DETAILS_CLOSE);
        });

        return sb.toString();
    }

    private void appendFilename(final StringBuilder sb, final ReportEntry reportEntry) {
        String filename = reportEntry.getFilename();

        if (reportEntry.getDuplication().isHasDuplicates()) {
            filename = filename + " << duplicates";
        }

        sb.append(String.format(SUMMARY_TEMPLATE, filename));
    }

    private void appendDuplication(final StringBuilder sb, final ReportEntry reportEntry) {
        sb.append("**Warning!** States duplication detected:").append(NEW_LINE);

        final Set<String> oldDuplicates = reportEntry.getDuplication().getOldDmiDuplicates();
        final Set<String> newDuplicates = reportEntry.getDuplication().getNewDmiDuplicates();

        if (!oldDuplicates.isEmpty()) {
            sb.append("- **Old DMI:** ").append(newDuplicates).append(NEW_LINE);
        }

        if (!newDuplicates.isEmpty()) {
            sb.append("- **New DMI:** ").append(oldDuplicates).append(NEW_LINE);
        }

        sb.append(NEW_LINE);
        sb.append("*Duplication of states may result into unpredictable behavior in game and incorrect diff report,"
                + " so fix is recommended.*").append(NEW_LINE);
        sb.append(NEW_LINE);
    }

    private void appendStatesTable(final StringBuilder sb, final List<StateDiffReport> stateDiffReports) {
        sb.append("Key | Dir / Frame | Old | New | Status").append(NEW_LINE);
        sb.append("--- | :---------: | --- | --- | ------").append(NEW_LINE);

        stateDiffReports.forEach(stateDiffReport -> appendStatesRows(sb, stateDiffReport));
    }

    private void appendStatesRows(final StringBuilder sb, final StateDiffReport stateDiffReport) {
        final String simpleName = stateDiffReport.getName();
        final String nameWithLink = createLinkName(stateDiffReport);

        final String dirShortName = stateDiffReport.getDir().shortName;
        final int frame = stateDiffReport.getFrameNumber();

        final String title = simpleName + "-%s-d" + dirShortName + "-f" + frame;
        final String titleForOldImage = String.format(title, "O");
        final String titleForNewImage = String.format(title, "N");

        final String dirArrow = ArrowDirCreator.create(stateDiffReport.getDir());

        final String oldImgTag = createImgTag(stateDiffReport.getOldDmiLink(), titleForOldImage);
        final String newImgTag = createImgTag(stateDiffReport.getNewDmiLink(), titleForNewImage);

        final String status = stateDiffReport.getStatus();

        sb
                .append(nameWithLink).append(TABLE_DELIMITER)
                .append(dirArrow).append(" ").append(dirShortName).append(" / ").append(frame).append(TABLE_DELIMITER)
                .append(oldImgTag).append(TABLE_DELIMITER)
                .append(newImgTag).append(TABLE_DELIMITER)
                .append(status).append(NEW_LINE);
    }

    private String createLinkName(final StateDiffReport report) {
        StringBuilder multipliedLinks = new StringBuilder();

        for (int mult : IMG_RESIZE_MULTIPLIERS) {
            multipliedLinks.append(String.format("<a href=\"%s\">x%d</a>", createHref(report, mult), mult));
            multipliedLinks.append(" ");
        }

        multipliedLinks.deleteCharAt(multipliedLinks.length() - 1);

        return String.format("%s (%s)", report.getName(), multipliedLinks.toString());
    }

    private String createHref(final StateDiffReport report, final int multiplier) {
        int resizedWidth = report.getSpriteWidth() * multiplier;
        int resizedHeight = report.getSpriteHeight() * multiplier;

        final String resizePrefix = resizedWidth + "x" + resizedHeight + "/forceresize/";

        StringBuilder link = new StringBuilder("http://tauceti.ru/img-diff/?");
        boolean hasBeforeLink = !report.getOldDmiLink().isEmpty();
        boolean hasAfterLink = !report.getNewDmiLink().isEmpty();

        if (hasBeforeLink) {
            link.append("before=").append(
                    report.getOldDmiLink().replaceAll(ORG_URL_SUFFIX, ORG_URL_SUFFIX.concat(resizePrefix))
            );
        }

        if (hasAfterLink) {
            if (hasBeforeLink) {
                link.append('&');
            }

            link.append("after=").append(
                    report.getNewDmiLink().replaceAll(ORG_URL_SUFFIX, ORG_URL_SUFFIX.concat(resizePrefix))
            );
        }

        return link.toString();
    }

    private String createImgTag(@Nonnull final String imageLink, final String title) {
        if (!imageLink.isEmpty()) {
            return String.format(IMG_TEMPLATE, imageLink, title);
        } else {
            return "";
        }
    }

    private void appendMetadataTableDiff(final StringBuilder sb, final ReportEntry reportEntry) {
        sb.append(DETAILS_OPEN);
        sb.append(String.format(SUMMARY_TEMPLATE, "DMI Metadata Diff")).append(NEW_LINE);
        sb.append(NEW_LINE);
        sb.append(CODE_QUOTES).append(NEW_LINE);
        sb.append(reportEntry.getMetadata().getMetadataDiff()).append(NEW_LINE);
        sb.append(CODE_QUOTES).append(NEW_LINE);
        sb.append(DETAILS_CLOSE);
    }

    private static final class ArrowDirCreator {

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

        private ArrowDirCreator() {
        }
    }
}
