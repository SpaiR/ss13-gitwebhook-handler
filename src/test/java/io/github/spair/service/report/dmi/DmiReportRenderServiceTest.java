package io.github.spair.service.report.dmi;

import io.github.spair.service.dmi.entity.DmiDiffStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DmiReportRenderServiceTest {

    private static final String NEW_LINE = System.lineSeparator();
    private static final String FILE_NAME = "file_name";

    private static final String DUPLICATION_PART = "Duplication Part";
    private static final String TABLE_PART = "Table Part";
    private static final String STATE_NUMBER_PART = "State Number Part";

    @Mock
    private DuplicationPartRender duplicationPartRender;
    @Mock
    private TablePartRender tablePartRender;
    @Mock
    private StatesNumberPartRender statesNumberPartRender;
    @Mock
    private DmiDiffStatus status;

    private DmiReportRenderService renderService;

    @Before
    public void setUp() {
        renderService = new DmiReportRenderService();
        ReflectionTestUtils.setField(renderService, "duplicationPartRender", duplicationPartRender);
        ReflectionTestUtils.setField(renderService, "tablePartRender", tablePartRender);
        ReflectionTestUtils.setField(renderService, "statesNumberPartRender", statesNumberPartRender);

        when(duplicationPartRender.render(any(DmiDiffStatus.class))).thenReturn(DUPLICATION_PART);
        when(tablePartRender.render(any(DmiDiffStatus.class))).thenReturn(TABLE_PART);
        when(statesNumberPartRender.render(any(DmiDiffStatus.class))).thenReturn(STATE_NUMBER_PART);

        when(status.getFilename()).thenReturn(FILE_NAME);
    }

    @Test
    public void renderTitle() {
        assertEquals("## DMI Diff Report", renderService.renderTitle());
    }

    @Test
    public void renderHeaderWithCleanStatus() {
        when(status.isHasDuplicates()).thenReturn(false);
        when(status.isStateOverflow()).thenReturn(false);
        assertEquals(FILE_NAME, renderService.renderHeader(status));
    }

    @Test
    public void renderHeaderWithDuplicationStatus() {
        when(status.isHasDuplicates()).thenReturn(true);
        when(status.isStateOverflow()).thenReturn(false);
        when(status.isDuplicatesFixed()).thenReturn(false);
        assertEquals(FILE_NAME + " <b><< duplicates</b>", renderService.renderHeader(status));
    }

    @Test
    public void renderHeaderWithFixedDuplicationStatus() {
        when(status.isHasDuplicates()).thenReturn(true);
        when(status.isStateOverflow()).thenReturn(false);
        when(status.isDuplicatesFixed()).thenReturn(true);
        assertEquals(FILE_NAME + " <b><< duplicates (fixed)</b>", renderService.renderHeader(status));
    }

    @Test
    public void renderHeaderWithStateOverflowStatus() {
        when(status.isHasDuplicates()).thenReturn(false);
        when(status.isStateOverflow()).thenReturn(true);
        when(status.isStateOverflowFixed()).thenReturn(false);
        assertEquals(FILE_NAME + " <b><< states overflow</b>", renderService.renderHeader(status));
    }

    @Test
    public void renderHeaderWithFixesStateOverflowStatus() {
        when(status.isHasDuplicates()).thenReturn(false);
        when(status.isStateOverflow()).thenReturn(true);
        when(status.isStateOverflowFixed()).thenReturn(true);
        assertEquals(FILE_NAME + " <b><< states overflow (fixed)</b>", renderService.renderHeader(status));
    }

    @Test
    public void renderHeaderWithAllStatuses() {
        when(status.isHasDuplicates()).thenReturn(true);
        when(status.isStateOverflow()).thenReturn(true);
        assertEquals(FILE_NAME + " <b><< duplicates | states overflow</b>", renderService.renderHeader(status));
    }

    @Test
    public void renderHeaderWithAllStatusesFixed() {
        when(status.isHasDuplicates()).thenReturn(true);
        when(status.isStateOverflow()).thenReturn(true);
        when(status.isDuplicatesFixed()).thenReturn(true);
        when(status.isStateOverflowFixed()).thenReturn(true);
        assertEquals(FILE_NAME + " <b><< duplicates (fixed) | states overflow (fixed)</b>", renderService.renderHeader(status));
    }

    @Test
    public void renderBody() {
        assertEquals(DUPLICATION_PART + NEW_LINE + TABLE_PART + NEW_LINE + STATE_NUMBER_PART, renderService.renderBody(status));
    }
}