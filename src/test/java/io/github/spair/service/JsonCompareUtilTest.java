package io.github.spair.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class JsonCompareUtilTest {

    @Test
    public void testCompareObjects() throws Exception {
        File expectedJson = new ClassPathResource("json-compare-expected.json").getFile();
        assertEquals(
                new ObjectMapper().readValue(expectedJson, ArrayNode.class).toString(),
                JsonCompareUtil.compareObjects(Collections.emptyMap(), Collections.singletonMap("prop", "value"), false));
    }
}