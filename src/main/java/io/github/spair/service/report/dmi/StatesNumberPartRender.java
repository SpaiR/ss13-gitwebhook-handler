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
        StringBuilder bodyPart = new StringBuilder();

        bodyPart.append("States number:").append(NEW_LINE);
        bodyPart.append(renderNumber(OLD_DMI, status.getOldStatesNumber())).append(NEW_LINE);
        bodyPart.append(renderNumber(NEW_DMI, status.getNewStatesNumber())).append(NEW_LINE);

        if (status.isStateOverflow()) {
            bodyPart.append(NEW_LINE);
            bodyPart.append(OVERFLOW_MESSAGE);
            bodyPart.append(NEW_LINE);
        }

        return bodyPart.toString();
    }

    private StringBuilder renderNumber(final String dmiType, final int number) {
        StringBuilder sb = new StringBuilder();
        sb.append("- **").append(dmiType).append(" DMI:** ").append(number);

        if (number > Dmi.MAX_STATES) {
            sb.append(OVERFLOW_WARNING);
        }

        return sb;
    }
}
