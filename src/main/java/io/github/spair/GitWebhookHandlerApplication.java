package io.github.spair;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GitWebhookHandlerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitWebhookHandlerApplication.class, args);
    }
}
