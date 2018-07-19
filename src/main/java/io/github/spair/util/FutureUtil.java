package io.github.spair.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class FutureUtil {

    public static void completeFutures(final CompletableFuture... futures) {
        try {
            CompletableFuture.allOf(futures).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Exception with while completing futures");
        }
    }

    public static <T> T extractFuture(final CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Exception while extracting future");
        }
    }

    private FutureUtil() {
    }
}
