package io.github.spair.aspect;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.handler.Handler;
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
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {AspectTestConfig.class, StartHandlerInThreadAspectTest.ContextConfig.class})
@ActiveProfiles("aspect-test")
public class StartHandlerInThreadAspectTest {

    private static final String START = "Start";
    private static final String TEXT_FROM_HANDLER = "Text from handler";

    @TestConfiguration
    static class ContextConfig {
        @Bean
        public TestHandlerImpl testHandler() {
            return new TestHandlerImpl();
        }
    }

    @Autowired
    private Handler testHandler;

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
    public void testAround() throws Exception {
        assertFalse(output.toString().contains(TEXT_FROM_HANDLER));
        testHandler.handle(null);

        TimeUnit.MILLISECONDS.sleep(10);
        assertTrue(output.toString().contains(START));
        assertFalse("Handler::handler didn't called from separate thread", output.toString().contains(TEXT_FROM_HANDLER));

        TimeUnit.MILLISECONDS.sleep(50);
        assertFalse(output.toString().contains(TEXT_FROM_HANDLER));

        TimeUnit.MILLISECONDS.sleep(300);
        assertTrue(output.toString().contains(TEXT_FROM_HANDLER));
    }

    static class TestHandlerImpl implements Handler {

        @Override
        public void handle(ObjectNode webhookJson) {
            try {
                System.out.println(START);
                TimeUnit.MILLISECONDS.sleep(200);
                System.out.println(TEXT_FROM_HANDLER);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}