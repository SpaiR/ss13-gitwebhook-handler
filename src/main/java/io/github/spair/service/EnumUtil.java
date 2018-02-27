package io.github.spair.service;

import org.springframework.util.ObjectUtils;

public final class EnumUtil {

    public static <E extends Enum<?>> E valueOfOrDefault(E[] values, String constant, E defaultValue) {
        if (ObjectUtils.containsConstant(values, constant)) {
            return ObjectUtils.caseInsensitiveValueOf(values, constant);
        } else {
            return defaultValue;
        }
    }

    private EnumUtil() {
    }
}
