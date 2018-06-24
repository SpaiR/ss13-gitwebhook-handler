package io.github.spair.service.report.dmi;

import io.github.spair.service.dmi.entity.DmiDiffStatus;
import io.github.spair.service.report.AbstractReportRenderService;
import io.github.spair.service.report.BodyPartRender;
import io.github.spair.service.report.ReportRenderService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static io.github.spair.service.report.TextConstants.NEW_LINE;

@Service(ReportRenderService.DMI)
public class DmiReportRenderService extends AbstractReportRenderService<DmiDiffStatus> {

    public static final String TITLE = "## DMI Diff Report";

    private static final String DUPLICATES_HEADER = "duplicates";
    private static final String STATE_OVERFLOW_HEADER = "states overflow";

    private final BodyPartRender<DmiDiffStatus> duplicationPartRender = new DuplicationPartRender();
    private final BodyPartRender<DmiDiffStatus> tablePartRender = new TablePartRender();
    private final BodyPartRender<DmiDiffStatus> statesNumberPartRender = new StatesNumberPartRender();

    @Override
    public String renderError() {
        return TITLE + NEW_LINE + NEW_LINE
                + "Report is too long and can't be print.";
    }

    @Override
    protected String renderTitle() {
        return TITLE;
    }

    @Override
    protected String renderHeader(final DmiDiffStatus status) {
        String filename = status.getFilename();
        List<String> statuses = new ArrayList<>();

        if (status.isHasDuplicates()) {
            statuses.add(DUPLICATES_HEADER);
        }

        if (status.isStateOverflow()) {
            statuses.add(STATE_OVERFLOW_HEADER);
        }

        if (!statuses.isEmpty()) {
            filename = filename + " <b><< " + String.join(" | ", statuses) + "</b>";
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
