package io.github.spair.service.git;

import lombok.EqualsAndHashCode;
import org.eclipse.jgit.lib.BatchingProgressMonitor;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = false)
public class CloneMonitor extends BatchingProgressMonitor {

    private static final String RECEIVING_OBJECTS_TASK = "Receiving objects";

    private int lastPcntTick = -1;

    @Nullable
    private Consumer<Integer> updateCallback;
    @Nullable
    private Runnable endCallback;

    public CloneMonitor(@Nullable final Consumer<Integer> updateCallback, @Nullable final Runnable endCallback) {
        this.updateCallback = updateCallback;
        this.endCallback = endCallback;
    }

    @Override
    protected void onUpdate(final String taskName, final int cmp, final int totalWork, final int pcnt) {
        if (updateCallback != null && RECEIVING_OBJECTS_TASK.equals(taskName) && lastPcntTick != pcnt) {
            lastPcntTick = pcnt;
            updateCallback.accept(pcnt);
        }
    }

    @Override
    protected void onEndTask(final String taskName, final int workCurr, final int workTotal, final int percentDone) {
        if (endCallback != null && RECEIVING_OBJECTS_TASK.equals(taskName)) {
            endCallback.run();
        }
    }

    @Override
    protected void onUpdate(final String taskName, final int workCurr) {
    }

    @Override
    protected void onEndTask(final String taskName, final int workCurr) {
    }
}
