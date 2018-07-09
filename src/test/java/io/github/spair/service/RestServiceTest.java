package io.github.spair.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@RestClientTest(RestService.class)
@AutoConfigureWebClient(registerRestTemplate = true)
public class RestServiceTest {

    @Autowired
    private RestService restService;
    @Autowired
    private MockRestServiceServer server;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGet() {
        server.expect(requestTo("/test/path"))
                .andExpect(method(HttpMethod.GET)).andExpect(headersMatecher())
                .andRespond(withSuccess());
        restService.get("/test/path", buildHeaders());
    }

    @Test
    public void testGetForEntity() {
        server.expect(requestTo("/test/path"))
                .andExpect(method(HttpMethod.GET)).andExpect(headersMatecher())
                .andRespond(withStatus(HttpStatus.ACCEPTED).body("Response Content").contentType(MediaType.TEXT_PLAIN));

        ResponseEntity<String> resp = restService.getForEntity("/test/path", buildHeaders(), String.class);

        assertEquals(resp.getBody(), "Response Content");
        assertEquals(resp.getStatusCode(), HttpStatus.ACCEPTED);
        assertEquals(resp.getHeaders().getContentType(), MediaType.TEXT_PLAIN);
    }

    @Test
    public void testGetForObject() {
        server.expect(requestTo("/test/path")).andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("Response Content", MediaType.TEXT_PLAIN));

        assertEquals("Response Content", restService.getForObject("/test/path", String.class));
    }

    @Test
    public void testGetForObjectWithHeaders() {
        server.expect(requestTo("/test/path"))
                .andExpect(method(HttpMethod.GET)).andExpect(headersMatecher())
                .andRespond(withSuccess("Response Content", MediaType.TEXT_PLAIN));

        assertEquals("Response Content", restService.getForObject("/test/path", buildHeaders(), String.class));

    }

    @Test
    public void testGetForJson() {
        ObjectNode responseJson = objectMapper.createObjectNode();
        responseJson.set("value", JsonNodeFactory.instance.numberNode(123));

        server.expect(requestTo("/test/path"))
                .andExpect(method(HttpMethod.GET)).andExpect(headersMatecher())
                .andRespond(withSuccess(responseJson.toString(), MediaType.APPLICATION_JSON));

        assertEquals(responseJson, restService.getForJson("/test/path", buildHeaders()));
    }

    @Test
    public void testPut() {
        server.expect(requestTo("/test/path"))
                .andExpect(method(HttpMethod.PUT)).andExpect(headersMatecher())
                .andExpect(content().string("Text body"))
                .andRespond(withSuccess());

        restService.put("/test/path", "Text body", buildHeaders());
    }

    @Test
    public void testPatch() {
        server.expect(requestTo("/test/path"))
                .andExpect(method(HttpMethod.PATCH)).andExpect(headersMatecher())
                .andExpect(content().string("Text body"))
                .andRespond(withSuccess());

        restService.patch("/test/path", "Text body", buildHeaders());
    }

    @Test
    public void testPost() {
        server.expect(requestTo("/test/path"))
                .andExpect(method(HttpMethod.POST)).andExpect(headersMatecher())
                .andExpect(content().string("Text body"))
                .andRespond(withSuccess());

        restService.post("/test/path", "Text body", buildHeaders());
    }

    @Test
    public void testPostForJson() {
        ObjectNode responseJson = objectMapper.createObjectNode();
        responseJson.set("value", JsonNodeFactory.instance.numberNode(123));

        server.expect(requestTo("/test/path"))
                .andExpect(method(HttpMethod.POST)).andExpect(headersMatecher())
                .andExpect(content().string("Text body"))
                .andRespond(withSuccess(responseJson.toString(), MediaType.APPLICATION_JSON));

        assertEquals(responseJson, restService.postForJson("/test/path", "Text body", buildHeaders()));
    }

    @Test
    public void testDelete() {
        server.expect(requestTo("/test/path"))
                .andExpect(method(HttpMethod.DELETE)).andExpect(headersMatecher())
                .andRespond(withSuccess());
        restService.delete("/test/path", buildHeaders());
    }

    @Test
    public void testHead() {
        server.expect(requestTo("/test/path"))
                .andExpect(method(HttpMethod.HEAD)).andExpect(headersMatecher())
                .andRespond(withSuccess());
        restService.head("/test/path", buildHeaders());
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "TestAgent-X");
        return headers;
    }

    private RequestMatcher headersMatecher() {
        return header("User-Agent", "TestAgent-X");
    }
}