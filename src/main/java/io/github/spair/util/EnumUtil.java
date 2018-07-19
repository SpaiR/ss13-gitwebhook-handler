package io.github.spair.util;

import org.springframework.util.ObjectUtils;

public final class EnumUtil {

    public static <E extends Enum<?>> E valueOfOrDefault(
            final E[] values, final String constant, final E defaultValue) {
        if (ObjectUtils.containsConstant(values, constant)) {
            return ObjectUtils.caseInsensitiveValueOf(values, constant);
        } else {
            return defaultValue;
        }
    }

    private EnumUtil() {
    }
}
