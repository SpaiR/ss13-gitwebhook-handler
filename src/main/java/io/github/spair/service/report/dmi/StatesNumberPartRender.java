package io.github.spair.service.report.dmi;

import io.github.spair.byond.dmi.Dmi;
import io.github.spair.service.dmi.entity.DmiDiffStatus;
import io.github.spair.service.report.BodyPartRender;

import static io.github.spair.service.report.TextConstants.NEW_LINE;

final class StatesNumberPartRender implements BodyPartRender<DmiDiffStatus> {

    private static final String OLD_DMI = "Old";
    private static final String NEW_DMI = "New";
    private static final String OVERFLOW_WARNING = " (overflow)";
    private static final String OVERFLOW_MESSAGE =
            "*DMI file has limit to 512 states. States overflow is when this limit was overpassed. "
          + "It's a **critical** problem which should be fixed.*";

    @Override
    public String render(final DmiDiffStatus status) {
        String bodyPart = "";

        bodyPart += "States number:" + NEW_LINE;
        bodyPart += renderNumber(OLD_DMI, status.getOldStatesNumber()) + NEW_LINE;
        bodyPart += renderNumber(NEW_DMI, status.getNewStatesNumber()) + NEW_LINE;

        if (status.isStateOverflow()) {
            bodyPart += NEW_LINE;
            bodyPart += OVERFLOW_MESSAGE;
            bodyPart += NEW_LINE;
        }

        return bodyPart;
    }

    private String renderNumber(final String dmiType, final int number) {
        String result = "- **" + dmiType + " DMI:** " + number;

        if (number > Dmi.MAX_STATES) {
            result += OVERFLOW_WARNING;
        }

        return result;
    }
}
