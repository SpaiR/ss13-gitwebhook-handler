package io.github.spair.service.image;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.spair.service.RestService;
import io.github.spair.service.config.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class ImageUploaderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUploaderService.class);

    private final RestService restService;
    private final ConfigService configService;

    private static final String UPLOAD_ENDPOINT = "https://img.taucetistation.org/backend.php";
    private static final String BASE64 = "base64";
    private static final String URL = "url";
    private static final String UPLOAD_CODE = "upload_code";
    private static final String DATA_TYPE_PREFIX = "data:image/png;base64,";

    @Autowired
    public ImageUploaderService(final RestService restService, final ConfigService configService) {
        this.restService = restService;
        this.configService = configService;
    }

    public String uploadImage(final String base64image) {
        final String imageUploadCode = configService.getConfig().getImageUploadCode();

        if (imageUploadCode == null || imageUploadCode.isEmpty()) {
            LOGGER.error("Image upload code should be specified to make application work properly");
            throw new IllegalStateException("Empty image upload code");
        }

        MultiValueMap<String, String> reqBody = new LinkedMultiValueMap<>();

        reqBody.add(UPLOAD_CODE, imageUploadCode);
        reqBody.add(BASE64, DATA_TYPE_PREFIX.concat(base64image));

        ObjectNode respJson = restService.postForJson(UPLOAD_ENDPOINT, reqBody, getHttpHeaders());
        return respJson.get(URL).asText();
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        String agentName = configService.getConfig().getRequestAgentName();

        httpHeaders.set(HttpHeaders.USER_AGENT, agentName);
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        return httpHeaders;
    }
}
