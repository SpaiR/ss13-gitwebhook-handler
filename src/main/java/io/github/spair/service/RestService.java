package io.github.spair.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

@Service
public class RestService {

    private final RestOperations restOperations;

    @Autowired
    public RestService(final RestOperations restOperations) {
        this.restOperations = restOperations;
    }

    public void get(final String path, final HttpHeaders headers) {
        restOperations.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), Void.class);
    }

    public <T> ResponseEntity<T> getForEntity(final String path, final HttpHeaders headers, final Class<T> tClass) {
        return restOperations.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), tClass);
    }

    public <T> T getForObject(final String path, final Class<T> tClass) {
        return restOperations.getForObject(path, tClass);
    }

    public <T> T getForObject(final String path, final HttpHeaders headers, final Class<T> tClass) {
        return restOperations.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), tClass).getBody();
    }

    public ObjectNode getForJson(final String path, final HttpHeaders headers) {
        return restOperations.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), ObjectNode.class).getBody();
    }

    public void put(final String path, final Object body, final HttpHeaders headers) {
        restOperations.exchange(path, HttpMethod.PUT, new HttpEntity<>(body, headers), Void.class);
    }

    public void patch(final String path, final Object body, final HttpHeaders headers) {
        restOperations.patchForObject(path, new HttpEntity<>(body, headers), Void.class);
    }

    public void post(final String path, final Object body, final HttpHeaders headers) {
        restOperations.postForObject(path, new HttpEntity<>(body, headers), Void.class);
    }

    public ObjectNode postForJson(final String path, final Object body, final HttpHeaders headers) {
        return restOperations.postForObject(path, new HttpEntity<>(body, headers), ObjectNode.class);
    }

    public void delete(final String path, final HttpHeaders headers) {
        restOperations.exchange(path, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
    }
}
