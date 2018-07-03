package io.github.spair.service.report.dmm;

import io.github.spair.service.dmm.entity.DmmDiffStatus;
import io.github.spair.service.report.AbstractReportRenderService;
import io.github.spair.service.report.BodyPartRender;
import org.springframework.stereotype.Service;

import static io.github.spair.service.report.ReportConstants.NEW_LINE;

@Service
public class DmmReportRenderService extends AbstractReportRenderService<DmmDiffStatus> {

    public static final String TITLE = "## DMM Diff Report";

    private final BodyPartRender<DmmDiffStatus> objectsTablePartRender = new ObjectsTablePartRender();
    private final BodyPartRender<DmmDiffStatus> areasTablePartRender = new AreasTablePartRender();
    private final BodyPartRender<DmmDiffStatus> comparisonListPartRender = new ComparisonListPartRender();
    private final BodyPartRender<DmmDiffStatus> rawLinksPartRender = new RawLinksPartRender();

    @Override
    protected String renderTitle() {
        return TITLE;
    }

    @Override
    protected String renderHeader(final DmmDiffStatus status) {
        return status.getFilename();
    }

    @Override
    protected String renderBody(final DmmDiffStatus status) {
        return objectsTablePartRender.render(status)
                + areasTablePartRender.render(status)
                + comparisonListPartRender.render(status)
                + rawLinksPartRender.render(status);
    }

    @Override
    public String renderError() {
        return TITLE + NEW_LINE + NEW_LINE
                + "Report is too long and can't be printed.";
    }
}
