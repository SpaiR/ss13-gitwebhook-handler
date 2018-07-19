package io.github.spair.util;

import io.github.spair.util.EnumUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EnumUtilTest {

    enum TestEnum {
        Case1, CASE_2, DEFAULT
    }

    @Test
    public void testValueOfOrDefault() {
        TestEnum result = EnumUtil.valueOfOrDefault(TestEnum.values(), "case1", TestEnum.DEFAULT);
        assertEquals(TestEnum.Case1, result);

        result = EnumUtil.valueOfOrDefault(TestEnum.values(), "CASE_2", TestEnum.DEFAULT);
        assertEquals(TestEnum.CASE_2, result);

        result = EnumUtil.valueOfOrDefault(TestEnum.values(), "undefined", TestEnum.DEFAULT);
        assertEquals(TestEnum.DEFAULT, result);
    }
}