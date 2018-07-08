package io.github.spair.service.dmm;

import io.github.spair.byond.ByondTypes;
import io.github.spair.byond.dmm.MapRegion;
import io.github.spair.byond.dmm.parser.Dmm;
import io.github.spair.byond.dmm.render.DmmRender;
import io.github.spair.service.dmm.entity.DmmChunkDiff;
import io.github.spair.service.image.ImageHelper;
import io.github.spair.service.image.ImageUploaderService;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DmmRender.class, ImageHelper.class})
public class ChunkDiffGeneratorTest {

    @Mock
    private ImageUploaderService imageUploaderService;

    @Mock
    private Dmm oldDmm;
    @Mock
    private Dmm newDmm;

    private ChunkDiffGenerator generator;

    private static final BufferedImage SMALL_IMAGE = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
    private static final BufferedImage MEDIUM_IMAGE = new BufferedImage(5000, 5000, BufferedImage.TYPE_INT_RGB);
    private static final BufferedImage BIG_IMAGE = new BufferedImage(9000, 9000, BufferedImage.TYPE_INT_RGB);

    private static final List<BufferedImage> SPLITTED_IMAGES_MEDIUM = imagesList(4);
    private static final List<BufferedImage> SPLITTED_IMAGES_BIG = imagesList(16);

    private static List<BufferedImage> imagesList(int number) {
        List<BufferedImage> images = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            images.add(SMALL_IMAGE);
        }
        return images;
    }

    @Before
    public void setUp() {
        generator = new ChunkDiffGenerator(imageUploaderService);
        when(imageUploaderService.uploadImage(any(BufferedImage.class))).thenReturn("[link]");

        PowerMockito.mockStatic(DmmRender.class);
        PowerMockito.mockStatic(ImageHelper.class);

        PowerMockito.when(ImageHelper.getDifferenceImage(any(BufferedImage.class), any(BufferedImage.class)))
                .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        PowerMockito.when(ImageHelper.splitImage(any(BufferedImage.class), eq(2))).thenReturn(SPLITTED_IMAGES_MEDIUM);
        PowerMockito.when(ImageHelper.splitImage(any(BufferedImage.class), eq(4))).thenReturn(SPLITTED_IMAGES_BIG);
    }

    @Test
    public void testGenerateWithOldAndNewDmm() {
        mockSmallImagesRender();

        List<DmmChunkDiff> result = generator.generate(regionList(), oldDmm, newDmm);
        assertOldAndNewImages(result, 1);
    }

    @Test
    public void testGenerateWithOnlyOldDmm() {
        mockSmallImagesRender();

        List<DmmChunkDiff> result = generator.generate(regionList(), oldDmm, null);
        assertOnlyOldImages(result, 1);
    }

    @Test
    public void testGenerateWithOnlyNewDmm() {
        mockSmallImagesRender();

        List<DmmChunkDiff> result = generator.generate(regionList(), null, newDmm);
        assertOnlyNewImages(result, 1);
    }

    @Test
    public void testGenerateWithOldAndNewDmmWithMediumSplit() {
        mockMediumImagesRender();

        List<DmmChunkDiff> result = generator.generate(regionList(), oldDmm, newDmm);
        assertOldAndNewImages(result, 4);
    }

    @Test
    public void testGenerateWithOnlyOldDmmWithMediumSplit() {
        mockMediumImagesRender();

        List<DmmChunkDiff> result = generator.generate(regionList(), oldDmm, null);
        assertOnlyOldImages(result, 4);
    }

    @Test
    public void testGenerateWithOnlyNewDmmWithMediumSplit() {
        mockMediumImagesRender();

        List<DmmChunkDiff> result = generator.generate(regionList(), null, newDmm);
        assertOnlyNewImages(result, 4);
    }

    @Test
    public void testGenerateWithOldAndNewDmmWithBigSplit() {
        mockBigImageRender();

        List<DmmChunkDiff> result = generator.generate(regionList(), oldDmm, newDmm);
        assertOldAndNewImages(result, 16);
    }

    @Test
    public void testGenerateWithOnlyOldDmmWithBigSplit() {
        mockBigImageRender();

        List<DmmChunkDiff> result = generator.generate(regionList(), oldDmm, null);
        assertOnlyOldImages(result, 16);
    }

    @Test
    public void testGenerateWithOnlyNewDmmWithBigSplit() {
        mockBigImageRender();

        List<DmmChunkDiff> result = generator.generate(regionList(), null, newDmm);
        assertOnlyNewImages(result, 16);
    }

    private List<MapRegion> regionList() {
        return Lists.newArrayList(MapRegion.of(1, 2), MapRegion.of(2, 3));
    }

    private OngoingStubbing<BufferedImage> mockObjectsRender() {
        return PowerMockito.when(
                DmmRender.renderToImage(any(Dmm.class), any(MapRegion.class), eq(ByondTypes.TURF), eq(ByondTypes.OBJ), eq(ByondTypes.MOB))
        );
    }

    private OngoingStubbing<BufferedImage> mockAreasRender() {
        return PowerMockito.when(DmmRender.renderToImage(any(Dmm.class), any(MapRegion.class), eq(ByondTypes.AREA)));
    }

    private void assertOldAndNewImages(List<DmmChunkDiff> dmmChunkDiffs, int expected) {
        dmmChunkDiffs.forEach(chunk -> {
            assertEquals(expected, chunk.getOldChunkImagesLinks().size());
            assertEquals(expected, chunk.getOldChunkAreasImagesLinks().size());

            assertEquals(expected, chunk.getNewChunkImagesLinks().size());
            assertEquals(expected, chunk.getNewChunkAreasImagesLinks().size());

            assertEquals(expected, chunk.getDiffImagesLinks().size());
            assertEquals(expected, chunk.getDiffAreasImagesLinks().size());
        });
    }

    private void assertOnlyOldImages(List<DmmChunkDiff> dmmChunkDiffs, int expected) {
        dmmChunkDiffs.forEach(chunk -> {
            assertEquals(expected, chunk.getOldChunkImagesLinks().size());
            assertEquals(expected, chunk.getOldChunkAreasImagesLinks().size());

            assertEquals(0, chunk.getNewChunkImagesLinks().size());
            assertEquals(0, chunk.getNewChunkAreasImagesLinks().size());

            assertEquals(0, chunk.getDiffImagesLinks().size());
            assertEquals(0, chunk.getDiffAreasImagesLinks().size());
        });
    }

    private void assertOnlyNewImages(List<DmmChunkDiff> dmmChunkDiffs, int expected) {
        dmmChunkDiffs.forEach(chunk -> {
            assertEquals(0, chunk.getOldChunkImagesLinks().size());
            assertEquals(0, chunk.getOldChunkAreasImagesLinks().size());

            assertEquals(expected, chunk.getNewChunkImagesLinks().size());
            assertEquals(expected, chunk.getNewChunkAreasImagesLinks().size());

            assertEquals(0, chunk.getDiffImagesLinks().size());
            assertEquals(0, chunk.getDiffAreasImagesLinks().size());
        });
    }

    private void mockSmallImagesRender() {
        mockObjectsRender().thenReturn(SMALL_IMAGE);
        mockAreasRender().thenReturn(SMALL_IMAGE);
    }

    private void mockMediumImagesRender() {
        mockObjectsRender().thenReturn(MEDIUM_IMAGE);
        mockAreasRender().thenReturn(MEDIUM_IMAGE);
    }

    private void mockBigImageRender() {
        mockObjectsRender().thenReturn(BIG_IMAGE);
        mockAreasRender().thenReturn(BIG_IMAGE);
    }
}