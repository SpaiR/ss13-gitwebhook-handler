package io.github.spair.service.report;

import org.assertj.core.util.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractReportRenderServiceTest {

    private static final String NEW_LINE = System.lineSeparator();
    private static final String expected =
            "Title" + NEW_LINE + NEW_LINE
          + "<details>"
          + "<summary>Header</summary>"
          + "<hr>" + NEW_LINE + NEW_LINE
          + "Body"
          + "<hr>"
          + "</details>";

    @Test
    public void testRenderStatus() {
        RenderService renderService = new RenderService();
        String actual = renderService.renderStatus(Lists.newArrayList(new Object()));
        assertEquals(expected, actual);
    }

    private static class RenderService extends AbstractReportRenderService<Object> {

        @Override
        protected String renderTitle() {
            return "Title";
        }

        @Override
        protected String renderHeader(final Object status) {
            return "Header";
        }

        @Override
        protected String renderBody(final Object status) {
            return "Body";
        }
    }
}