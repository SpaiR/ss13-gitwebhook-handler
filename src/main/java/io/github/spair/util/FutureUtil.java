package io.github.spair.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public final class FutureUtil {

    public static void completeFutures(final CompletableFuture... futures) {
        try {
            CompletableFuture.allOf(futures).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Exception with while completing futures", e);
        }
    }

    public static <T> T extractFuture(final CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Exception while extracting future", e);
        }
    }

    public static void awaitTermination(final int timeout, final TimeUnit timeUnit) {
        try {
            ForkJoinPool.commonPool().awaitTermination(timeout, timeUnit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private FutureUtil() {
    }
}
