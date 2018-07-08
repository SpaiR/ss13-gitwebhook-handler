package io.github.spair;

import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public final class ResourceHelper {

    public static String readFile(String filePath) {
        try {
            File file = new ClassPathResource(filePath).getFile();
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage readImage(String imagePath) {
        try {
            File file = new ClassPathResource(imagePath).getFile();
            return ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ResourceHelper() {
    }
}
