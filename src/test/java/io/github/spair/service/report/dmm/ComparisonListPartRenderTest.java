package io.github.spair.service.report.dmm;

import io.github.spair.ResourceHelper;
import io.github.spair.service.dmm.entity.DmmChunkDiff;
import io.github.spair.service.dmm.entity.DmmDiffStatus;
import org.assertj.core.util.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ComparisonListPartRenderTest {

    @Test
    public void testRender() {
        DmmChunkDiff dmmChunkDiff1 = DmmChunkDiff.builder()
                .oldChunkImagesLinks(Lists.newArrayList("1-oldChunk1", "1-oldChunk2"))
                .oldChunkAreasImagesLinks(Lists.newArrayList("1-oldChunk-area1", "2-oldChunk-area2"))

                .newChunkImagesLinks(Lists.newArrayList("1- newChunk1", "2-newChunk2"))
                .newChunkAreasImagesLinks(Lists.newArrayList("1-newChunk-area1", "2-newChunk-area2"))
                .build();
        DmmChunkDiff dmmChunkDiff2 = DmmChunkDiff.builder()
                .oldChunkImagesLinks(Lists.newArrayList("2-oldChunk1", "1-oldChunk2"))
                .oldChunkAreasImagesLinks(Lists.newArrayList("2-oldChunk-area1", "1-oldChunk-area2"))

                .newChunkImagesLinks(Lists.newArrayList("2- newChunk1", "2-newChunk2"))
                .newChunkAreasImagesLinks(Lists.newArrayList("2-newChunk-area1", "2-newChunk-area2"))
                .build();

        DmmDiffStatus dmmDiffStatus = new DmmDiffStatus("filename");
        dmmDiffStatus.setDmmDiffChunks(Lists.newArrayList(dmmChunkDiff1, dmmChunkDiff2));

        ComparisonListPartRender partRender = new ComparisonListPartRender();
        String actual = partRender.render(dmmDiffStatus);

        assertEquals(ResourceHelper.readFile("data/render-parts/dmm-comparison-list.txt"), actual);
    }
}