package io.github.spair.service.dmi;

import io.github.spair.byond.dmi.Dmi;
import io.github.spair.byond.dmi.DmiSlurper;
import io.github.spair.service.RestService;
import io.github.spair.service.git.GitHubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
class DmiLoader {

    private final RestService restService;
    private final GitHubService gitHubService;

    private static final Logger LOGGER = LoggerFactory.getLogger(DmiLoader.class);

    @Autowired
    DmiLoader(RestService restService, GitHubService gitHubService) {
        this.restService = restService;
        this.gitHubService = gitHubService;
    }

    @Async
    public CompletableFuture<Optional<Dmi>> loadFromGitHub(String dmiName, String filename) {
        try {
            String base64image = gitHubService.readEncodedFile(filename);
            return CompletableFuture.completedFuture(Optional.of(DmiSlurper.slurpUp(dmiName, base64image)));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                return CompletableFuture.completedFuture(Optional.empty());
            } else {
                LOGGER.error("HTTP error on loading DMI from GitHub. Dmi name: {}. Filename: {}", dmiName, filename, e);
                throw e;
            }
        }
    }

    @Async
    public CompletableFuture<Optional<Dmi>> loadFromUrl(String dmiName, String url) {
        try {
            byte[] imageBytes = restService.getForObject(url, byte[].class);
            String base64image = Base64.getEncoder().encodeToString(imageBytes);
            return CompletableFuture.completedFuture(Optional.of(DmiSlurper.slurpUp(dmiName, base64image)));
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                return CompletableFuture.completedFuture(Optional.empty());
            } else {
                LOGGER.error("HTTP error on loading DMI from URL. Dmi name: {}. URL: {}", dmiName, url, e);
                throw e;
            }
        }
    }
}
