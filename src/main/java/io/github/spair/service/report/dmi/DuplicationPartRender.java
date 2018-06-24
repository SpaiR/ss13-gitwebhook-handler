package io.github.spair.service.report.dmi;

import io.github.spair.service.dmi.entity.DmiDiffStatus;
import io.github.spair.service.report.BodyPartRender;

import java.util.Set;

import static io.github.spair.service.report.TextConstants.NEW_LINE;

final class DuplicationPartRender implements BodyPartRender<DmiDiffStatus> {

    private static final String DUPLICATION_WARNING = "**Warning!** States duplication detected:";
    private static final String DUPLICATION_WARNING_TEXT =
            "*Duplication of states may result into unpredictable behavior in game and incorrect diff dmi, "
          + "so fix is recommended.*";

    @Override
    public String render(final DmiDiffStatus status) {
        if (!status.isHasDuplicates()) {
            return "";
        }

        StringBuilder bodyPart = new StringBuilder();

        bodyPart.append(DUPLICATION_WARNING);
        bodyPart.append(NEW_LINE);

        final Set<String> oldDuplicates = status.getOldDuplicatesNames();
        final Set<String> newDuplicates = status.getNewDuplicatesNames();

        if (!oldDuplicates.isEmpty()) {
            bodyPart.append("- **Old DMI:** ").append(oldDuplicates);
            bodyPart.append(NEW_LINE);
        }

        if (!newDuplicates.isEmpty()) {
            bodyPart.append("- **New DMI:** ").append(newDuplicates);
            bodyPart.append(NEW_LINE);
        }

        bodyPart.append(NEW_LINE);
        bodyPart.append(DUPLICATION_WARNING_TEXT);
        bodyPart.append(NEW_LINE);

        return bodyPart.toString();
    }
}
