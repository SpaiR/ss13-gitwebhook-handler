package io.github.spair.aspect;

import io.github.spair.handler.command.HandlerCommand;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AspectTestConfig.class, ExceptionSuppressAspectTest.ContextConfig.class})
@ActiveProfiles("aspect-test")
public class ExceptionSuppressAspectTest {

    private static final String SUPPRESSED_EXCEPTION_MSG = "Suppressed exception in void io.github.spair.handler.command.HandlerCommand.execute(Object)";

    @TestConfiguration
    static class ContextConfig {
        @Bean
        public TestHandlerCommand testHandlerCommand() {
            return new TestHandlerCommandImpl();
        }
    }

    @Autowired
    private TestHandlerCommand testCommand;

    private final PrintStream stdout = System.out;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    @Before
    public void setUp() throws UnsupportedEncodingException {
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8.name()));
    }

    @After
    public void cleanUp() {
        System.setOut(stdout);
    }

    @Test
    public void testAroundWithAspect() {
        testCommand.execute(new Object());

        assertTrue(output.toString().contains(SUPPRESSED_EXCEPTION_MSG));
        assertTrue(output.toString().contains("This exception should be suppressed with stack trace"));
    }

    @Test(expected = RuntimeException.class)
    public void testAroundWithoutAspect() {
        testCommand.executeException();

        assertFalse(output.toString().contains(SUPPRESSED_EXCEPTION_MSG));
        assertTrue(output.toString().contains("This exception should NOT be suppressed"));
    }

    interface TestHandlerCommand extends HandlerCommand<Object> {
        void executeException();
    }

    static class TestHandlerCommandImpl implements TestHandlerCommand {

        @Override
        public void execute(final Object obj) {
            throw new RuntimeException("This exception should be suppressed with stack trace");
        }

        @Override
        public void executeException() {
            throw new RuntimeException("This exception should NOT be suppressed");
        }
    }
}