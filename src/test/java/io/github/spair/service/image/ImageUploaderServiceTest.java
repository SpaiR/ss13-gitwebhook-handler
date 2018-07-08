package io.github.spair.service.image;

import io.github.spair.service.RestService;
import io.github.spair.service.config.ConfigService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;

import java.awt.image.BufferedImage;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@RestClientTest(RestService.class)
@AutoConfigureWebClient(registerRestTemplate = true)
public class ImageUploaderServiceTest {

    private ImageUploaderService uploaderService;

    @Autowired
    private RestService restService;
    @Autowired
    private MockRestServiceServer server;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigService configService;

    @Before
    public void setUp() {
        when(configService.getConfig().getImageUploadCode()).thenReturn("12345secret");
        when(configService.getConfig().getRequestAgentName()).thenReturn("Agent-Name");
        uploaderService = new ImageUploaderService(restService, configService);
    }

    @Test
    public void testUploadImage() {
        server.expect(requestTo("https://img.taucetistation.org/backend.php"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("User-Agent", "Agent-Name"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.MULTIPART_FORM_DATA))
                .andRespond(withSuccess("{\"url\":\"some.url\"}", MediaType.APPLICATION_JSON));

        String resp = uploaderService.uploadImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));

        assertEquals("some.url", resp);
    }
}