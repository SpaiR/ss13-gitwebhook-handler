package io.github.spair.service.report.dmi;

import io.github.spair.ResourceHelper;
import io.github.spair.service.dmi.entity.DmiDiffStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StatesNumberPartRenderTest {

    @Mock
    private DmiDiffStatus status;
    private StatesNumberPartRender partRender = new StatesNumberPartRender();

    @Test
    public void testRenderWithoutOverflow() {
        when(status.getOldStatesNumber()).thenReturn(23);
        when(status.getNewStatesNumber()).thenReturn(30);
        assertEquals(ResourceHelper.readFile("data/render-parts/dmi-states_number.txt"), partRender.render(status));
    }

    @Test
    public void testRenderWithOverflow() {
        when(status.isStateOverflow()).thenReturn(true);
        when(status.getOldStatesNumber()).thenReturn(510);
        when(status.getNewStatesNumber()).thenReturn(513);
        assertEquals(ResourceHelper.readFile("data/render-parts/dmi-states_number-overflow.txt"), partRender.render(status));
    }
}