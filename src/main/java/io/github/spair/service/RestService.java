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

    public void get(String path, HttpHeaders headers) {
        restOperations.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), Void.class);
    }

    public <T> ResponseEntity<T> getForEntity(String path, HttpHeaders headers, Class<T> tClass) {
        return restOperations.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), tClass);
    }

    public <T> T getForObject(String path, Class<T> tClass) {
        return restOperations.getForObject(path, tClass);
    }

    public <T> T getForObject(String path, HttpHeaders headers, Class<T> tClass) {
        return restOperations.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), tClass).getBody();
    }

    public ObjectNode getForJson(String path, HttpHeaders headers) {
        return restOperations.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), ObjectNode.class).getBody();
    }

    public void put(String path, Object body, HttpHeaders headers) {
        restOperations.exchange(path, HttpMethod.PUT, new HttpEntity<>(body, headers), Void.class);
    }

    public void post(String path, Object body, HttpHeaders headers) {
        restOperations.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers), Void.class);
    }

    public void delete(String path, HttpHeaders headers) {
        restOperations.exchange(path, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
    }
}
