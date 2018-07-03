package io.github.spair.service.image;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.service.RestService;
import io.github.spair.service.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

@Service
public class ImageUploaderService {

    public static final String HOST_PATH = "https://img.taucetistation.org";

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUploaderService.class);

    private final RestService restService;
    private final ConfigService configService;

    private static final String UPLOAD_ENDPOINT = HOST_PATH + "/backend.php";
    private static final String POSTIMAGE = "postimage";
    private static final String URL = "url";
    private static final String UPLOAD_CODE = "upload_code";

    @Autowired
    public ImageUploaderService(final RestService restService, final ConfigService configService) {
        this.restService = restService;
        this.configService = configService;
    }

    public String uploadImage(final BufferedImage image) {
        final String imageUploadCode = configService.getConfig().getImageUploadCode();

        if (imageUploadCode == null || imageUploadCode.isEmpty()) {
            LOGGER.error("Image upload code should be specified to make application work properly");
            throw new IllegalStateException("Empty image upload code");
        }

        try {
            File tmpFile = Files.createTempFile("img", null).toFile();
            tmpFile.deleteOnExit();
            ImageIO.write(image, "png", tmpFile);

            MultiValueMap<String, Object> reqBody = new LinkedMultiValueMap<>();

            reqBody.add(UPLOAD_CODE, imageUploadCode);
            reqBody.add(POSTIMAGE, new FileSystemResource(tmpFile));

            ObjectNode respJson = restService.postForJson(UPLOAD_ENDPOINT, reqBody, getHttpHeaders());

            if (!tmpFile.delete()) {
                throw new IOException("Can't delete temp image file");
            }

            return respJson.get(URL).asText();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        String agentName = configService.getConfig().getRequestAgentName();

        httpHeaders.set(HttpHeaders.USER_AGENT, agentName);
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

        return httpHeaders;
    }
}
