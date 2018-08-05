package io.github.spair.util;

public final class OSInfoUtil {

    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final String WINDOWS = "windows";

    public static boolean isWindows() {
        return OS.startsWith(WINDOWS);
    }

    private OSInfoUtil() {
    }
}
