package io.github.spair.service.github;

import com.fasterxml.jackson.databind.node.ArrayNode;
import io.github.spair.service.RestService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some responses from GitHub are divided into pages, where next page link provided in 'link' header.
 * This class process those responses.
 */
final class LinkProcessor {

    private static final Pattern NEXT_PR_FILES = Pattern.compile("<([\\w\\d/?=:.]*)>;\\srel=\"next\"");
    private static final String LINK = "link";

    private final RestService restService;
    private final HttpHeaders httpHeaders;
    private final Consumer<ArrayNode> consumer;

    LinkProcessor(final RestService restService, final HttpHeaders httpHeaders, final Consumer<ArrayNode> consumer) {
        this.restService = restService;
        this.httpHeaders = httpHeaders;
        this.consumer = consumer;
    }

    void recursiveProcess(final String link) {
        ResponseEntity<ArrayNode> resp = restService.getForEntity(link, httpHeaders, ArrayNode.class);

        consumer.accept(resp.getBody());

        String headerLinks = resp.getHeaders().getOrDefault(LINK, Collections.emptyList()).toString();
        Matcher nextLink = NEXT_PR_FILES.matcher(headerLinks);

        if (nextLink.find()) {
            recursiveProcess(nextLink.group(1));
        }
    }
}
