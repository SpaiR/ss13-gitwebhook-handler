package io.github.spair.service.dmm.entity;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ModifiedDmmTest {

    @Test
    public void testGetSanitizedName() {
        ModifiedDmm modifiedDmm = new ModifiedDmm("maps/z1.dmm", null, null);
        assertEquals("z1", modifiedDmm.getSanitizedName());
    }
}