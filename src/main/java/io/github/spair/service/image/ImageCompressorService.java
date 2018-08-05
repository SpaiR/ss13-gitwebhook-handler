package io.github.spair.service.image;

import io.github.spair.util.OSInfoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ImageCompressorService {

    private static final int SUCCESS = 0;
    private static final List<String> DEFAULT_OPTIONS = Arrays.asList(
            "--floyd=0", "--speed=1", "--posterize=2", "--ext=.png", "--force"
    );

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageCompressorService.class);

    public void compressImage(final File imageFile) {
        try {
            Process process = new ProcessBuilder(getCompressCommand(imageFile.getPath())).start();
            int result = process.waitFor();
            if (result != SUCCESS) {
                LOGGER.warn("Image '" + imageFile.getPath() + "' was not compressed. Return value: " + result);
            }
        } catch (Exception e) {
            LOGGER.error("Exception while compressing image {}", imageFile.getPath(), e);
        }
    }

    private List<String> getCompressCommand(final String imageFilePath) {
        String pngquant;
        if (OSInfoUtil.isWindows()) {
            pngquant = "pngquant.exe";
        } else {
            pngquant = "/usr/bin/pngquant";
        }
        return new ArrayList<String>() {{
            add(pngquant);
            addAll(DEFAULT_OPTIONS);
            add(imageFilePath);
        }};
    }
}
