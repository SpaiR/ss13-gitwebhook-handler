package io.github.spair.service.report.dmi;

import io.github.spair.service.dmi.entity.DmiDiffStatus;
import io.github.spair.service.report.AbstractReportRenderService;
import io.github.spair.service.report.BodyPartRender;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.github.spair.service.report.TextConstants.NEW_LINE;

@Service
public class DmiReportRenderService extends AbstractReportRenderService<DmiDiffStatus> {

    public static final String TITLE = "## DMI Diff Report";

    private static final String DUPLICATES_HEADER = "duplicates";
    private static final String STATE_OVERFLOW_HEADER = "states overflow";
    private static final String FIXED_HEADER_INFO = " (fixed)";

    private final BodyPartRender<DmiDiffStatus> duplicationPartRender = new DuplicationPartRender();
    private final BodyPartRender<DmiDiffStatus> tablePartRender = new TablePartRender();
    private final BodyPartRender<DmiDiffStatus> statesNumberPartRender = new StatesNumberPartRender();

    @Override
    public String renderError() {
        return TITLE + NEW_LINE + NEW_LINE
                + "Report is too long and can't be printed.";
    }

    @Override
    protected String renderTitle() {
        return TITLE;
    }

    @Override
    protected String renderHeader(final DmiDiffStatus dmiDiffStatus) {
        String filename = dmiDiffStatus.getFilename();
        List<String> statuses = new ArrayList<>();

        if (dmiDiffStatus.isHasDuplicates()) {
            String status = DUPLICATES_HEADER;
            if (dmiDiffStatus.isDuplicatesFixed()) {
                status += FIXED_HEADER_INFO;
            }
            statuses.add(status);
        }

        if (dmiDiffStatus.isStateOverflow()) {
            String status = STATE_OVERFLOW_HEADER;
            if (dmiDiffStatus.isStateOverflowFixed()) {
                status += FIXED_HEADER_INFO;
            }
            statuses.add(status);
        }

        if (!statuses.isEmpty()) {
            filename += " <b><< " + String.join(" | ", statuses) + "</b>";
        }

        return filename;
    }

    @Override
    protected String renderBody(final DmiDiffStatus status) {
        return duplicationPartRender.render(status) + NEW_LINE
                + tablePartRender.render(status) + NEW_LINE
                + statesNumberPartRender.render(status);
    }
}
