package io.github.spair.service.dmi;

import io.github.spair.byond.dmi.DmiMeta;
import io.github.spair.byond.dmi.DmiSprite;
import io.github.spair.byond.dmi.SpriteDir;
import io.github.spair.byond.dmi.comparator.DiffStatus;
import io.github.spair.byond.dmi.comparator.DmiDiff;
import io.github.spair.byond.dmi.comparator.DmiDiffEntry;
import io.github.spair.service.dmi.entity.DmiSpriteDiffStatus;
import io.github.spair.service.image.ImageUploaderService;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SpriteDiffStatusGeneratorTest {

    @Mock
    private ImageUploaderService imageUploaderService;
    @Mock
    private BufferedImage oldImage = mock(BufferedImage.class, "oldSpriteImageMock");
    @Mock
    private BufferedImage newImage = mock(BufferedImage.class, "newSpriteImageMock");

    private SpriteDiffStatusGenerator generator;

    @Before
    public void setUp() {
        generator = new SpriteDiffStatusGenerator(imageUploaderService);
        when(imageUploaderService.uploadImage(oldImage)).thenReturn("oldSpriteLink");
        when(imageUploaderService.uploadImage(newImage)).thenReturn("newSpriteLink");
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
        when(oldSpriteMock.getSprite()).thenReturn(oldImage);
        DmiSprite newSpriteMock = mock(DmiSprite.class);
        when(newSpriteMock.getSprite()).thenReturn(newImage);

        DmiDiffEntry diffMock = mock(DmiDiffEntry.class);
        when(diffMock.getStateName()).thenReturn("simpleState");
        when(diffMock.getOldSprite()).thenReturn(oldSpriteMock);
        when(diffMock.getNewSprite()).thenReturn(newSpriteMock);
        when(diffMock.getStatus()).thenReturn(DiffStatus.MODIFIED);

        DmiMeta metaMock = mock(DmiMeta.class);
        when(metaMock.getSpritesWidth()).thenReturn(32);
        when(metaMock.getSpritesHeight()).thenReturn(32);

        DmiDiff dmiDiff = mock(DmiDiff.class);
        when(dmiDiff.getDmiDiffEntries()).thenReturn(Lists.newArrayList(diffMock, diffMock));
        when(dmiDiff.getOldMeta()).thenReturn(metaMock);
        when(dmiDiff.getNewMeta()).thenReturn(metaMock);

        return dmiDiff;
    }
}