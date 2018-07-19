package io.github.spair.util;

import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FutureUtilTest {

    private static final String STRING_1 = "string1";
    private static final String STRING_2 = "string2";

    @Test
    public void testCompleteFutures() throws Exception {
        CompletableFuture<String> stringFuture1 = CompletableFuture.completedFuture(STRING_1);
        CompletableFuture<String> stringFuture2 = getAsyncFuture();

        FutureUtil.completeFutures(stringFuture1, stringFuture2);

        assertEquals(STRING_1, stringFuture1.get());
        assertEquals(STRING_2, stringFuture2.get());
    }

    @Test
    public void extractFuture() {
        CompletableFuture<String> stringFuture = CompletableFuture.completedFuture(STRING_1);
        assertEquals(STRING_1, FutureUtil.extractFuture(stringFuture));
    }

    private CompletableFuture<String> getAsyncFuture() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return STRING_2;
        });
    }
}