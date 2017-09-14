package io.github.spair.services;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class WebhookService {

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(WebhookService.class);

    public HashMap convertWebhookToMap(String webhook) {
        try {
            return objectMapper.readValue(webhook, HashMap.class);
        } catch (Exception e) {

            if (e instanceof JsonMappingException || e instanceof JsonParseException) {
                LOGGER.error("JSON parsing error due to webhook conversion", e);
            } else {
                LOGGER.error("Convert webhook to map error", e);
            }

            throw new RuntimeException(e);
        }
    }
}
