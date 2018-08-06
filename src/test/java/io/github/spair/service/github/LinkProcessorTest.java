package io.github.spair.service.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.github.spair.service.RestService;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@RestClientTest(RestService.class)
@AutoConfigureWebClient(registerRestTemplate = true)
public class LinkProcessorTest {

    @Autowired
    private RestService restService;
    @Autowired
    private MockRestServiceServer server;

    private static final String LINK_1 = "link/1";
    private static final String LINK_2 = "link/2";
    private static final String LINK_3 = "link/3";

    private final ArrayNode CONTENT_1 = createArrayNode("content1");
    private final ArrayNode CONTENT_2 = createArrayNode("content2");
    private final ArrayNode CONTENT_3 = createArrayNode("content3");

    @Before
    public void setUp() {
        server.expect(requestTo(LINK_1))
                .andExpect(method(HttpMethod.GET)).andExpect(headersMatecher())
                .andRespond(withStatus(HttpStatus.OK)
                        .headers(new HttpHeaders() {{
                            setContentType(MediaType.APPLICATION_JSON);
                            set("link", "<" + LINK_2 + ">; rel=\"next\"");
                        }})
                        .body(CONTENT_1.toString())
                );
        server.expect(requestTo(LINK_2))
                .andExpect(method(HttpMethod.GET)).andExpect(headersMatecher())
                .andRespond(withStatus(HttpStatus.OK)
                        .headers(new HttpHeaders() {{
                            setContentType(MediaType.APPLICATION_JSON);
                            set("link", "<" + LINK_3 + ">; rel=\"next\"");
                        }})
                        .body(CONTENT_2.toString())
                );
        server.expect(requestTo(LINK_3))
                .andExpect(method(HttpMethod.GET)).andExpect(headersMatecher())
                .andRespond(withStatus(HttpStatus.OK)
                        .headers(new HttpHeaders() {{
                            setContentType(MediaType.APPLICATION_JSON);
                        }})
                        .body(CONTENT_3.toString())
                );
    }

    @Test
    public void testRecursiveProcess() {
        List<String> resList = new ArrayList<>();
        new LinkProcessor(restService, getHeaders(), node ->
                resList.add(node.get(0).asText())
        ).recursiveProcess(LINK_1);
        assertEquals(Lists.newArrayList("content1", "content2", "content3"), resList);
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("var", "val");
        return headers;
    }

    private RequestMatcher headersMatecher() {
        return header("var", "val");
    }

    private ArrayNode createArrayNode(final String... text) {
        ObjectMapper objectMapper = new ObjectMapper();
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (String s : text) {
            arrayNode.add(s);
        }
        return arrayNode;
    }
}