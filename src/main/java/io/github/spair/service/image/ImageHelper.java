package io.github.spair.service.image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public final class ImageHelper {

    public static BufferedImage getDifferenceImage(final BufferedImage img1, final BufferedImage img2) {
        final int width = img1.getWidth();
        final int height = img1.getHeight();
        final int highlight = Color.MAGENTA.getRGB();

        final int[] picture1 = img1.getRGB(0, 0, width, height, null, 0, width);
        final int[] picture2 = img2.getRGB(0, 0, width, height, null, 0, width);

        for (int i = 0; i < picture1.length; i++) {
            if (picture1[i] != picture2[i]) {
                picture1[i] = highlight;
            } else {
                picture1[i] = new Color(picture1[i]).darker().darker().darker().getRGB();
            }
        }

        final BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        out.setRGB(0, 0, width, height, picture1, 0, width);
        return out;
    }

    public static List<BufferedImage> splitImage(final BufferedImage image, final int splitFactor) {
        int chunkWidth = image.getWidth() / splitFactor;
        int chunkHeight = image.getHeight() / splitFactor;

        List<BufferedImage> images = new ArrayList<>();

        for (int x = 0; x < splitFactor; x++) {
            for (int y = 0; y < splitFactor; y++) {
                BufferedImage newImage = new BufferedImage(chunkWidth, chunkHeight, image.getType());

                Graphics g = newImage.createGraphics();
                g.drawImage(image, 0, 0, chunkWidth, chunkHeight,
                        chunkWidth * y, chunkHeight * x,
                        chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight,
                        null
                );
                g.dispose();

                images.add(newImage);
            }
        }

        return images;

    }

    private ImageHelper() {
    }
}
