package io.github.spair;

import org.springframework.core.io.ClassPathResource;

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

    private ResourceHelper() {
    }
}
