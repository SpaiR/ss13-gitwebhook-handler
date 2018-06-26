package io.github.spair.service.report.dmi;

import io.github.spair.ResourceHelper;
import io.github.spair.byond.dmi.SpriteDir;
import io.github.spair.service.dmi.entity.DmiDiffStatus;
import io.github.spair.service.dmi.entity.DmiSpriteDiffStatus;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TablePartRenderTest {

    @Mock
    private DmiDiffStatus status;
    private TablePartRender partRender = new TablePartRender();

    @Test
    public void testRender() {
        List<DmiSpriteDiffStatus> dmiSpriteDiffStatusList = getDmiSpriteDiffStatusList();
        when(status.getSpritesDiffStatuses()).thenReturn(dmiSpriteDiffStatusList);
        assertEquals(ResourceHelper.readFile("data/render-parts/dmi-table.txt"), partRender.render(status));
    }

    @Test
    public void testRenderWhenSpriteDiffStatusesEmpty() {
        when(status.getSpritesDiffStatuses()).thenReturn(Collections.emptyList());
        assertTrue(partRender.render(status).isEmpty());
    }

    private List<DmiSpriteDiffStatus> getDmiSpriteDiffStatusList() {
        return Lists.newArrayList(
                createDmiSpriteDiffStatus("state1", "", "img.org/newSprite1", SpriteDir.NORTH, "Added"),
                createDmiSpriteDiffStatus("state2", "img.org/oldSprite2", "img.org/newSprite2", SpriteDir.NORTHEAST, "Modified"),
                createDmiSpriteDiffStatus("state3", "img.org/oldSprite3", "", SpriteDir.SOUTH, "Removed")
        );
    }

    private DmiSpriteDiffStatus createDmiSpriteDiffStatus(final String name, final String oldSpriteLink, final String newSpriteLink, final SpriteDir dir, final String status) {
        DmiSpriteDiffStatus diffStatus = mock(DmiSpriteDiffStatus.class);

        when(diffStatus.getName()).thenReturn(name);
        when(diffStatus.getSpriteWidth()).thenReturn(32);
        when(diffStatus.getSpriteHeight()).thenReturn(32);
        when(diffStatus.getOldSpriteImageLink()).thenReturn(oldSpriteLink);
        when(diffStatus.getNewSpriteImageLink()).thenReturn(newSpriteLink);
        when(diffStatus.getDir()).thenReturn(dir);
        when(diffStatus.getStatus()).thenReturn(status);
        when(diffStatus.getFrameNumber()).thenReturn(1);

        return diffStatus;
    }
}