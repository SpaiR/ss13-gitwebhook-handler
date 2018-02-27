package io.github.spair.service.dmi;

import io.github.spair.byond.dmi.SpriteDir;
import io.github.spair.service.dmi.entities.DmiDiffReport;
import io.github.spair.service.dmi.entities.ReportEntry;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

@Component
class ReportPrinter {

    private static final String IMG_DIFF_API = "http://tauceti.ru/img-diff/?";
    private static final String ORG_URL_SUFFIX = "org/";
    private static final String BEFORE_URL_PARAM = "before=";
    private static final String AFTER_URL_PARAM = "after=";

    private static final String DETAILS_OPEN = "<details>";
    private static final String DETAILS_CLOSE = "</details>";
    private static final String SUMMARY_TEMPLATE = "<summary>%s</summary>";
    private static final String LINE_HORIZONTAL = "<hr />";
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String IMG_TEMPLATE = "<img src=\"%s\" title=\"%s\" />";
    private static final String LINK_NAME_TEMPLATE = "%s (<a href=\"%s\">x1</a> <a href=\"%s\">x4</a> <a href=\"%s\">x8</a>)";
    private static final String CODE_QUOTES = "```";
    private static final String TABLE_DELIMITER = "|";

    String printReport(DmiDiffReport report) {
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

    private void appendFilename(StringBuilder sb, ReportEntry reportEntry) {
        String filename = reportEntry.getFilename();

        if (!reportEntry.getDuplication().getNewDmiDuplicates().isEmpty()) {
            filename = filename + " << duplicates";
        }

        sb.append(String.format(SUMMARY_TEMPLATE, filename));
    }

    private void appendDuplication(StringBuilder sb, ReportEntry reportEntry) {
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

    private void appendStatesTable(StringBuilder sb, List<ReportEntry.StateDiffReport> stateDiffReports) {
        sb.append("Key | Dir / Frame | Old | New | Status").append(NEW_LINE);
        sb.append("--- | :---------: | --- | --- | ------").append(NEW_LINE);

        stateDiffReports.forEach(stateDiffReport -> appendStatesRows(sb, stateDiffReport));
    }

    private void appendStatesRows(StringBuilder stringBuilder, ReportEntry.StateDiffReport stateDiffReport) {
        final String simpleName = stateDiffReport.getName();
        final String nameWithLink = createLinkName(stateDiffReport);

        final String dirShortName = stateDiffReport.getDir().shortName;
        final int frame = stateDiffReport.getFrameNumber();

        final String titleForOldImage = simpleName + "-O-d" + dirShortName + "-f" + frame;
        final String titleForNewImage = simpleName + "-N-d" + dirShortName + "-f" + frame;

        final String dirArrow = ArrowDirCreator.create(stateDiffReport.getDir());

        final String oldImgTag = createImgTag(stateDiffReport.getOldDmiLink(), titleForOldImage);
        final String newImgTag = createImgTag(stateDiffReport.getNewDmiLink(), titleForNewImage);

        final String status = stateDiffReport.getStatus();

        stringBuilder
                .append(nameWithLink).append(TABLE_DELIMITER)
                .append(dirArrow).append(" ").append(dirShortName).append(" / ").append(frame).append(TABLE_DELIMITER)
                .append(oldImgTag).append(TABLE_DELIMITER)
                .append(newImgTag).append(TABLE_DELIMITER)
                .append(status).append(NEW_LINE);
    }

    private String createLinkName(ReportEntry.StateDiffReport report) {
        return String.format(LINK_NAME_TEMPLATE,
                report.getName(),
                createHref(report, 1),
                createHref(report, 4),
                createHref(report, 8)
        );
    }

    private String createHref(ReportEntry.StateDiffReport report, int multiplier) {
        int resizedWidth = report.getSpriteWidth() * multiplier;
        int resizedHeight = report.getSpriteHeight() * multiplier;

        final String resizePrefix = resizedWidth + "x" + resizedHeight + "/forceresize/";

        StringBuilder link = new StringBuilder(IMG_DIFF_API);
        boolean hasBeforeLink = !report.getOldDmiLink().isEmpty();
        boolean hasAfterLink = !report.getNewDmiLink().isEmpty();

        if (hasBeforeLink) {
            link.append(BEFORE_URL_PARAM).append(
                    report.getOldDmiLink().replaceAll(ORG_URL_SUFFIX, ORG_URL_SUFFIX.concat(resizePrefix))
            );
        }

        if (hasAfterLink) {
            if (hasBeforeLink) {
                link.append('&');
            }

            link.append(AFTER_URL_PARAM).append(
                    report.getNewDmiLink().replaceAll(ORG_URL_SUFFIX, ORG_URL_SUFFIX.concat(resizePrefix))
            );
        }

        return link.toString();
    }

    private String createImgTag(@Nonnull String imageLink, String title) {
        if (!imageLink.isEmpty()) {
            return String.format(IMG_TEMPLATE, imageLink, title);
        } else {
            return "";
        }
    }

    private void appendMetadataTableDiff(StringBuilder sb, ReportEntry reportEntry) {
        sb.append(DETAILS_OPEN);
        sb.append(String.format(SUMMARY_TEMPLATE, "DMI Metadata Diff")).append(NEW_LINE);
        sb.append(NEW_LINE);
        sb.append(CODE_QUOTES).append(NEW_LINE);
        sb.append(reportEntry.getMetadata().getMetadataDiff()).append(NEW_LINE);
        sb.append(CODE_QUOTES).append(NEW_LINE);
        sb.append(DETAILS_CLOSE);
    }

    private final static class ArrowDirCreator {

        private static String create(SpriteDir dir) {
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
