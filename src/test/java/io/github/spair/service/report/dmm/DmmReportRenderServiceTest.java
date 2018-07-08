package io.github.spair.service.report.dmm;

import io.github.spair.service.dmm.entity.DmmDiffStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DmmReportRenderServiceTest {

    private static final String NEW_LINE = System.lineSeparator();

    private static final String OBJECTS_TABLE_PART = "Objects Table Part";
    private static final String AREAS_TABLE_PART = "Areas Table Part";
    private static final String COMPARISON_PART = "Comparison Part";
    private static final String RAW_LINKS_PART = "Raw Links Part";

    @Mock
    private ObjectsTablePartRender objectsTablePartRender;
    @Mock
    private AreasTablePartRender areasTablePartRender;
    @Mock
    private ComparisonListPartRender comparisonListPartRender;
    @Mock
    private RawLinksPartRender rawLinksPartRender;

    private DmmReportRenderService renderService;

    @Before
    public void setUp() {
        renderService = new DmmReportRenderService();

        ReflectionTestUtils.setField(renderService, "objectsTablePartRender", objectsTablePartRender);
        ReflectionTestUtils.setField(renderService, "areasTablePartRender", areasTablePartRender);
        ReflectionTestUtils.setField(renderService, "comparisonListPartRender", comparisonListPartRender);
        ReflectionTestUtils.setField(renderService, "rawLinksPartRender", rawLinksPartRender);

        when(objectsTablePartRender.render(any(DmmDiffStatus.class))).thenReturn(OBJECTS_TABLE_PART);
        when(areasTablePartRender.render(any(DmmDiffStatus.class))).thenReturn(AREAS_TABLE_PART);
        when(comparisonListPartRender.render(any(DmmDiffStatus.class))).thenReturn(COMPARISON_PART);
        when(rawLinksPartRender.render(any(DmmDiffStatus.class))).thenReturn(RAW_LINKS_PART);
    }

    @Test
    public void testRenderTitle() {
        assertEquals("## DMM Diff Report", renderService.renderTitle());
    }

    @Test
    public void testRenderHeader() {
        DmmDiffStatus status = new DmmDiffStatus("filename");
        assertEquals("filename", renderService.renderHeader(status));
    }

    @Test
    public void testRenderBody() {
        String expected = OBJECTS_TABLE_PART + AREAS_TABLE_PART + COMPARISON_PART + RAW_LINKS_PART;
        assertEquals(expected, renderService.renderBody(mock(DmmDiffStatus.class)));
    }

    @Test
    public void testRenderError() {
        String expected = "## DMM Diff Report" + NEW_LINE + NEW_LINE + "Report is too long and can't be printed.";
        assertEquals(expected, renderService.renderError());
    }
}