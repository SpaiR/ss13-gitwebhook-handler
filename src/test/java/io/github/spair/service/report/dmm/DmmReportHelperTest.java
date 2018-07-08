package io.github.spair.service.report.dmm;

import io.github.spair.service.report.ReportHelper;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ReportHelper.class)
public class DmmReportHelperTest {

    @Before
    public void setUp() {
        PowerMockito.mockStatic(ReportHelper.class);
        PowerMockito.when(ReportHelper.createImgTag(anyString(), anyString())).thenAnswer(invocation ->
                "<" + invocation.getArgument(0) + "-" + invocation.getArgument(1) + ">"
        );
    }

    @Test
    public void testAppendImgIfNotEmptyWhenNotEmpty() {
        List<String> links = Lists.newArrayList("link1", "link2");

        StringBuilder actual = DmmReportHelper.appendImgIfNotEmpty(new StringBuilder(), links, 1);
        String expected = "<link1-Chunk 1 - Image 1><br><link2-Chunk 1 - Image 2><br>";

        assertEquals(expected, actual.toString());
    }

    @Test
    public void testAppendImgIfNotEmptyWhenEmpty() {
        List<String> links = Lists.emptyList();

        StringBuilder actual = DmmReportHelper.appendImgIfNotEmpty(new StringBuilder(), links, 1);

        assertEquals("Empty", actual.toString());
    }
}