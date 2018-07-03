package io.github.spair.service.report.dmi;

import io.github.spair.service.dmi.entity.DmiDiffStatus;
import io.github.spair.service.report.BodyPartRender;

import java.util.Set;

import static io.github.spair.service.report.ReportConstants.NEW_LINE;

final class DuplicationPartRender implements BodyPartRender<DmiDiffStatus> {

    private static final String DUPLICATION_INFO = "**Fixed** states duplication detected:";
    private static final String DUPLICATION_WARNING = "**Warning!** States duplication detected:";
    private static final String DUPLICATION_WARNING_TEXT =
            "*Duplication of states may result into unpredictable behavior in game and incorrect diff report, "
          + "so fix is recommended.*";

    @Override
    public String render(final DmiDiffStatus status) {
        String bodyPart = "";

        if (!status.isHasDuplicates()) {
            return bodyPart;
        }

        bodyPart += status.isDuplicatesFixed() ? DUPLICATION_INFO : DUPLICATION_WARNING;
        bodyPart += NEW_LINE;

        final Set<String> oldDuplicates = status.getOldDuplicatesNames();
        final Set<String> newDuplicates = status.getNewDuplicatesNames();

        if (!oldDuplicates.isEmpty()) {
            bodyPart += "- **Old DMI:** " + oldDuplicates;
            bodyPart += NEW_LINE;
        }

        if (!newDuplicates.isEmpty()) {
            bodyPart += "- **New DMI:** " + newDuplicates;
            bodyPart += NEW_LINE;
        }

        if (!status.isDuplicatesFixed()) {
            bodyPart += NEW_LINE;
            bodyPart += DUPLICATION_WARNING_TEXT;
            bodyPart += NEW_LINE;
        }

        return bodyPart;
    }
}
