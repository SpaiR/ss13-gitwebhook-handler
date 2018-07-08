package io.github.spair.service.image;

import io.github.spair.ResourceHelper;
import org.junit.Test;

import java.awt.image.BufferedImage;

import static org.junit.Assert.assertArrayEquals;

public class ImageHelperTest {

    @Test
    public void testGetDifferenceImage() {
        BufferedImage imageToCompare = imageFile("imageToCompare.png");
        BufferedImage imageCompareWith = imageFile("imageCompareWith.png");
        BufferedImage expectedImage = imageFile("imageDifference.png");

        BufferedImage actualImage = ImageHelper.getDifferenceImage(imageToCompare, imageCompareWith);

        assertImages(expectedImage, actualImage);
    }

    @Test
    public void testSplitImage() {
        BufferedImage imageToSplitOrig = imageFile("imageToSplitOrig.png");

        int imageCount = 0;
        for (BufferedImage img : ImageHelper.splitImage(imageToSplitOrig, 2)) {
            BufferedImage splitImg = imageFile("imageSplit-2-" + (++imageCount) + ".png");
            assertImages(splitImg, img);
        }

        imageCount = 0;
        for (BufferedImage img : ImageHelper.splitImage(imageToSplitOrig, 4)) {
            BufferedImage splitImg = imageFile("imageSplit-4-" + (++imageCount) + ".png");
            assertImages(splitImg, img);
        }
    }

    private void assertImages(BufferedImage img1, BufferedImage img2) {
        assertArrayEquals(
                img1.getRGB(0, 0, 100, 75, null, 0, 100),
                img2.getRGB(0, 0, 100, 75, null, 0, 100)
        );
    }

    private BufferedImage imageFile(String imageName) {
        return ResourceHelper.readImage("data/images/" + imageName);
    }
}