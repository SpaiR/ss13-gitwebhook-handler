package io.github.spair.service.report.dmi;

import io.github.spair.ResourceHelper;
import io.github.spair.service.dmi.entity.DmiDiffStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DuplicationPartRenderTest {

    @Mock
    private DmiDiffStatus status;
    private final DuplicationPartRender partRender = new DuplicationPartRender();

    @Test
    public void testRenderWhenNoDuplicates() {
        when(status.isHasDuplicates()).thenReturn(false);
        assertTrue(partRender.render(status).isEmpty());
    }

    @Test
    public void testRenderWhenOldDuplicates() {
        when(status.isHasDuplicates()).thenReturn(true);
        when(status.isDuplicatesFixed()).thenReturn(true);
        when(status.getOldDuplicatesNames()).thenReturn(Sets.newSet("a", "b", "c"));
        when(status.getNewDuplicatesNames()).thenReturn(Collections.emptySet());
        assertEquals(ResourceHelper.readFile("data/render-parts/dmi-duplication-old.txt"), partRender.render(status));
    }

    @Test
    public void testRenderWhenNewDuplicates() {
        when(status.isHasDuplicates()).thenReturn(true);
        when(status.isDuplicatesFixed()).thenReturn(false);
        when(status.getOldDuplicatesNames()).thenReturn(Collections.emptySet());
        when(status.getNewDuplicatesNames()).thenReturn(Sets.newSet("a", "b", "c"));
        assertEquals(ResourceHelper.readFile("data/render-parts/dmi-duplication-new.txt"), partRender.render(status));
    }

    @Test
    public void testRenderWhenAllDuplicates() {
        when(status.isHasDuplicates()).thenReturn(true);
        when(status.isDuplicatesFixed()).thenReturn(false);
        when(status.getOldDuplicatesNames()).thenReturn(Sets.newSet("a", "b", "c"));
        when(status.getNewDuplicatesNames()).thenReturn(Sets.newSet("d", "e", "f"));
        assertEquals(ResourceHelper.readFile("data/render-parts/dmi-duplication-all.txt"), partRender.render(status));
    }
}