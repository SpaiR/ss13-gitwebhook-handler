package io.github.spair.service.dmi;

import io.github.spair.ReadFileUtil;
import io.github.spair.service.dmi.entities.DmiDiffReport;
import io.github.spair.service.dmi.entities.ReportEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ReportPrinterTest {

    private final ReportPrinter printer = new ReportPrinter();
    private final String NEW_LINE = System.getProperty("line.separator");

    @Mock
    private FilenameAppender filenameAppender;
    @Mock
    private DuplicationAppender duplicationAppender;
    @Mock
    private StatesTableAppender statesTableAppender;
    @Mock
    private StatesNumberAppender statesNumberAppender;
    @Mock
    private MetadataAppender metadataAppender;

    @Before
    public void setUp() throws Exception {
        setField("filenameAppender", filenameAppender);
        setField("duplicationAppender", duplicationAppender);
        setField("statesTableAppender", statesTableAppender);
        setField("statesNumberAppender", statesNumberAppender);
        setField("metadataAppender", metadataAppender);

        mockAppender("mocked filename", filenameAppender);
        mockAppender("mocked duplication" + NEW_LINE, duplicationAppender);
        mockAppender("mocked states table" + NEW_LINE, statesTableAppender);
        mockAppender("mocked states number" + NEW_LINE, statesNumberAppender);
        mockAppender("mocked metadata" + NEW_LINE + NEW_LINE, metadataAppender);
    }

    @Test
    public void testPrintReport() {
        String expectedReport = ReadFileUtil.readFile("report-printer-result.txt");
        DmiDiffReport dmiDiffReport = new DmiDiffReport();
        dmiDiffReport.getReportEntries().add(mock(ReportEntry.class));
        dmiDiffReport.getReportEntries().add(mock(ReportEntry.class));

        assertEquals(expectedReport, printer.printReport(dmiDiffReport));
    }

    private void setField(String fieldName, ReportAppender value) throws Exception {
        Class<?> c = printer.getClass();
        Field f = c.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(printer, value);
    }

    private void mockAppender(String appendText, ReportAppender appender) {
        doAnswer(invok -> {
            StringBuilder sb = (StringBuilder) invok.getArguments()[0];
            sb.append(appendText);
            return null;
        }).when(appender).append(any(StringBuilder.class), any(ReportEntry.class));
    }
}