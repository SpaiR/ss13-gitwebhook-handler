package io.github.spair.service.dmm;

import io.github.spair.byond.ByondTypes;
import io.github.spair.byond.dmm.MapRegion;
import io.github.spair.byond.dmm.Dmm;
import io.github.spair.byond.dmm.render.DmmRender;
import io.github.spair.service.dmm.entity.DmmChunkDiff;
import io.github.spair.util.ImageUtil;
import io.github.spair.service.image.ImageUploaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@Component
final class ChunkDiffGenerator {

    private final ImageUploaderService imageUploaderService;

    private static final int SOFT_SPLIT_LIMIT = 4000;
    private static final int HARD_SPLIT_LIMIT = 8000;

    private static final int SOFT_SPLIT_FACTOR = 2;
    private static final int HARD_SPLIT_FACTOR = 4;

    @Autowired
    ChunkDiffGenerator(final ImageUploaderService imageUploaderService) {
        this.imageUploaderService = imageUploaderService;
    }

    List<DmmChunkDiff> generate(final List<MapRegion> chunks, @Nullable final Dmm oldDmm, @Nullable final Dmm newDmm) {
        List<DmmChunkDiff> dmmDiffChunks = new ArrayList<>();

        chunks.forEach(mapRegion -> {
            final BufferedImage oldChunkImage = renderImage(oldDmm, mapRegion, false);
            final BufferedImage newChunkImage = renderImage(newDmm, mapRegion, false);

            final BufferedImage oldChunkAreaImage = renderImage(oldDmm, mapRegion, true);
            final BufferedImage newChunkAreaImage = renderImage(newDmm, mapRegion, true);

            List<String> oldImagesLinks = new ArrayList<>();
            List<String> oldAreasImagesLinks = new ArrayList<>();

            List<String> newImagesLinks = new ArrayList<>();
            List<String> newAreasImagesLinks = new ArrayList<>();

            List<String> diffImagesLinks = new ArrayList<>();
            List<String> diffAreasImagesLinks = new ArrayList<>();

            if (oldChunkImage != null) {
                oldImagesLinks = getImageLinks(oldChunkImage);
            }
            if (oldChunkAreaImage != null) {
                oldAreasImagesLinks = getImageLinks(oldChunkAreaImage);
            }

            if (newChunkImage != null) {
                newImagesLinks = getImageLinks(newChunkImage);
            }
            if (newChunkAreaImage != null) {
                newAreasImagesLinks = getImageLinks(newChunkAreaImage);
            }

            if (oldChunkImage != null && newChunkImage != null) {
                BufferedImage differenceImage = ImageUtil.getDifferenceImage(oldChunkImage, newChunkImage);
                diffImagesLinks = getImageLinks(differenceImage);
            }
            if (oldChunkAreaImage != null && newChunkAreaImage != null) {
                BufferedImage differenceImage = ImageUtil.getDifferenceImage(oldChunkAreaImage, newChunkAreaImage);
                diffAreasImagesLinks = getImageLinks(differenceImage);
            }

            dmmDiffChunks.add(DmmChunkDiff.builder()
                    .oldChunkImagesLinks(oldImagesLinks).oldChunkAreasImagesLinks(oldAreasImagesLinks)
                    .newChunkImagesLinks(newImagesLinks).newChunkAreasImagesLinks(newAreasImagesLinks)
                    .diffImagesLinks(diffImagesLinks).diffAreasImagesLinks(diffAreasImagesLinks)
                    .build()
            );
        });

        return dmmDiffChunks;
    }

    @Nullable
    private BufferedImage renderImage(@Nullable final Dmm dmm, final MapRegion mapRegion, final boolean areaMode) {
        if (dmm != null) {
            if (areaMode) {
                return DmmRender.renderToImage(dmm, mapRegion, ByondTypes.TURF, ByondTypes.OBJ, ByondTypes.MOB);
            } else {
                return DmmRender.renderToImage(dmm, mapRegion, ByondTypes.AREA);
            }
        }
        return null;
    }

    private List<String> getImageLinks(final BufferedImage image) {
        List<String> imageLinks = new ArrayList<>();
        if (isToSoftSplit(image)) {
            ImageUtil.splitImage(image, SOFT_SPLIT_FACTOR).forEach(img ->
                    imageLinks.add(imageUploaderService.uploadImage(img))
            );
        } else if (isToHardSplit(image)) {
            ImageUtil.splitImage(image, HARD_SPLIT_FACTOR).forEach(img ->
                    imageLinks.add(imageUploaderService.uploadImage(img))
            );
        } else {
            imageLinks.add(imageUploaderService.uploadImage(image));
        }
        return imageLinks;
    }

    // Full map render generates really BIG image, so splitting is necessary.
    private boolean isToSoftSplit(final BufferedImage image) {
        return image.getWidth() >= SOFT_SPLIT_LIMIT && image.getWidth() < HARD_SPLIT_LIMIT
                || image.getHeight() >= SOFT_SPLIT_LIMIT && image.getHeight() < HARD_SPLIT_LIMIT;
    }

    private boolean isToHardSplit(final BufferedImage image) {
        return image.getWidth() >= HARD_SPLIT_LIMIT || image.getHeight() >= HARD_SPLIT_LIMIT;
    }
}
