package io.github.spair.service.report.dmm;

import io.github.spair.ResourceHelper;
import io.github.spair.service.dmm.entity.DmmChunkDiff;
import io.github.spair.service.dmm.entity.DmmDiffStatus;
import org.assertj.core.util.Lists;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ObjectsTablePartRenderTest {

    @Test
    public void render() {
        DmmChunkDiff dmmChunkDiff1 = DmmChunkDiff.builder()
                .oldChunkImagesLinks(Lists.newArrayList("1-oldChunk1", "1-oldChunk2"))
                .newChunkImagesLinks(Lists.newArrayList("1- newChunk1", "1-newChunk2"))
                .diffImagesLinks(Lists.newArrayList("1-diffImg1", "1-diffImg2"))
                .build();
        DmmChunkDiff dmmChunkDiff2 = DmmChunkDiff.builder()
                .oldChunkImagesLinks(Lists.newArrayList("2-oldChunk1", "2-oldChunk2"))
                .newChunkImagesLinks(Lists.newArrayList("2- newChunk1", "2-newChunk2"))
                .diffImagesLinks(Lists.newArrayList("2-diffImg1", "2-diffImg2"))
                .build();

        DmmDiffStatus dmmDiffStatus = new DmmDiffStatus("filename");
        dmmDiffStatus.setDmmDiffChunks(Lists.newArrayList(dmmChunkDiff1, dmmChunkDiff2));

        ObjectsTablePartRender partRender = new ObjectsTablePartRender();
        String actual = partRender.render(dmmDiffStatus);

        assertEquals(ResourceHelper.readFile("data/render-parts/dmm-objects-table.txt"), actual);
    }
}