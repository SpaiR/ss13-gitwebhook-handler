package io.github.spair.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Optional;

public final class JsonCompareUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonCompareUtil.class);

    public static String compareObjects(final Object before, final Object after, final boolean prettyPrint) {
        try {
            JsonNode beforeNode = OBJECT_MAPPER.valueToTree(Optional.ofNullable(before).orElse(Collections.emptyMap()));
            JsonNode afterNode = OBJECT_MAPPER.valueToTree(Optional.ofNullable(after).orElse(Collections.emptyMap()));
            JsonNode patchNode = JsonDiff.asJson(beforeNode, afterNode);

            if (prettyPrint) {
                return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(patchNode);
            } else {
                return OBJECT_MAPPER.writeValueAsString(patchNode);
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("Json processing on objects comparison. Before: {}. After: {}", before, after, e);
            return "";
        }
    }

    private JsonCompareUtil() {
    }
}
