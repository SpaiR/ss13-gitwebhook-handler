package io.github.spair.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class WebhookServiceTest {

    @Test
    public void testConvertWebhookToMap() {
        WebhookService webhookService = new WebhookService(new ObjectMapper());
        String jsonMap = "{\"field1\":\"value1\", \"field2\":100, \"field3\": {\"subField\":200}}";

        HashMap resultMap = webhookService.convertWebhookToMap(jsonMap);

        assertEquals(resultMap.get("field1"), "value1");
        assertEquals(resultMap.get("field2"), 100);
        assertEquals(((HashMap) resultMap.get("field3")).get("subField"), 200);
    }
}