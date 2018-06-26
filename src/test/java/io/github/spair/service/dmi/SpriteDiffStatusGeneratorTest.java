package io.github.spair.service.dmi;

import io.github.spair.byond.dmi.*;
import io.github.spair.service.dmi.entity.DmiSpriteDiffStatus;
import io.github.spair.service.image.ImageUploaderService;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpriteDiffStatusGeneratorTest {

    @Mock
    private ImageUploaderService imageUploaderService;
    private SpriteDiffStatusGenerator generator;

    @Before
    public void setUp() {
        generator = new SpriteDiffStatusGenerator(imageUploaderService);
        when(imageUploaderService.uploadImage("oldSpriteBase64")).thenReturn("oldSpriteLink");
        when(imageUploaderService.uploadImage("newSpriteBase64")).thenReturn("newSpriteLink");
    }

    @Test
    public void testGenerate() {
        List<DmiSpriteDiffStatus> spriteDiffStatusList = generator.generate(createDmiDiff());
        assertEquals(2, spriteDiffStatusList.size());

        spriteDiffStatusList.forEach(spriteDiffStatus -> {
            assertEquals("simpleState", spriteDiffStatus.getName());
            assertEquals("oldSpriteLink", spriteDiffStatus.getOldSpriteImageLink());
            assertEquals("newSpriteLink", spriteDiffStatus.getNewSpriteImageLink());
            assertEquals("Modified", spriteDiffStatus.getStatus());
            assertEquals(1, spriteDiffStatus.getFrameNumber());
            assertEquals(32, spriteDiffStatus.getSpriteHeight());
            assertEquals(32, spriteDiffStatus.getSpriteWidth());
            assertEquals(SpriteDir.NORTH, spriteDiffStatus.getDir());
        });
    }

    private DmiDiff createDmiDiff() {
        DmiSprite oldSpriteMock = mock(DmiSprite.class);
        when(oldSpriteMock.getDir()).thenReturn(SpriteDir.NORTH);
        when(oldSpriteMock.getFrameNum()).thenReturn(1);
        when(oldSpriteMock.getSpriteAsBase64()).thenReturn("oldSpriteBase64");
        DmiSprite newSpriteMock = mock(DmiSprite.class);
        when(newSpriteMock.getSpriteAsBase64()).thenReturn("newSpriteBase64");

        Diff diffMock = mock(Diff.class);
        when(diffMock.getStateName()).thenReturn("simpleState");
        when(diffMock.getOldSprite()).thenReturn(oldSpriteMock);
        when(diffMock.getNewSprite()).thenReturn(newSpriteMock);
        when(diffMock.getStatus()).thenReturn(DiffStatus.MODIFIED);

        DmiMeta metaMock = mock(DmiMeta.class);
        when(metaMock.getSpritesWidth()).thenReturn(32);
        when(metaMock.getSpritesHeight()).thenReturn(32);

        DmiDiff dmiDiff = mock(DmiDiff.class);
        when(dmiDiff.getDiffs()).thenReturn(Lists.newArrayList(diffMock, diffMock));
        when(dmiDiff.getOldMeta()).thenReturn(metaMock);
        when(dmiDiff.getNewMeta()).thenReturn(metaMock);

        return dmiDiff;
    }
}