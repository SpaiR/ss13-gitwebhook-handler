package io.github.spair.service.report.dmm;

import io.github.spair.ResourceHelper;
import io.github.spair.service.dmm.entity.DmmChunkDiff;
import io.github.spair.service.dmm.entity.DmmDiffStatus;
import org.assertj.core.util.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AreasTablePartRenderTest {

    @Test
    public void testRender() {
        DmmChunkDiff dmmChunkDiff1 = DmmChunkDiff.builder()
                .oldChunkAreasImagesLinks(Lists.newArrayList("1-oldChunk-area1", "1-oldChunk-area2"))
                .newChunkAreasImagesLinks(Lists.newArrayList("1- newChunk-area1", "1-newChunk-area2"))
                .diffAreasImagesLinks(Lists.newArrayList("1-diffImg-area1", "1-diffImg-area2"))
                .build();
        DmmChunkDiff dmmChunkDiff2 = DmmChunkDiff.builder()
                .oldChunkAreasImagesLinks(Lists.newArrayList("2-oldChunk-area1", "2-oldChunk-area2"))
                .newChunkAreasImagesLinks(Lists.newArrayList("2- newChunk-area1", "2-newChunk-area2"))
                .diffAreasImagesLinks(Lists.newArrayList("2-diffImg-area1", "2-diffImg-area2"))
                .build();

        DmmDiffStatus dmmDiffStatus = new DmmDiffStatus("filename");
        dmmDiffStatus.setDmmDiffChunks(Lists.newArrayList(dmmChunkDiff1, dmmChunkDiff2));

        AreasTablePartRender partRender = new AreasTablePartRender();
        String actual = partRender.render(dmmDiffStatus);

        assertEquals(ResourceHelper.readFile("data/render-parts/dmm-areas-table.txt"), actual);

    }
}