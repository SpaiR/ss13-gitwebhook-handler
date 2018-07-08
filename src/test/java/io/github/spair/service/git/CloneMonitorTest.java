package io.github.spair.service.git;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class CloneMonitorTest {

    private static final String RECEIVING_OBJECTS_TASK = "Receiving objects";

    @Test
    public void testOnUpdate() {
        AtomicInteger integer = new AtomicInteger();
        CloneMonitor monitor = new CloneMonitor(integer::set, null);

        monitor.onUpdate(RECEIVING_OBJECTS_TASK, 0, 0, 123);
        assertEquals(123, integer.get());

        monitor.onUpdate("FakeTask", 0, 0, 787);
        assertEquals(123, integer.get());

        monitor.onUpdate(RECEIVING_OBJECTS_TASK, 0, 0, 787);
        assertEquals(787, integer.get());
    }

    @Test
    public void testOnEndTask() {
        AtomicInteger integer = new AtomicInteger();
        CloneMonitor monitor = new CloneMonitor(null, () -> integer.set(666));

        monitor.onEndTask("FakeTask", 0, 0, 0);
        assertEquals(0, integer.get());


        monitor.onEndTask(RECEIVING_OBJECTS_TASK, 0, 0, 0);
        assertEquals(666, integer.get());
    }
}