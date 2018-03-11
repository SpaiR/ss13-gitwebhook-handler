package io.github.spair.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BeansConfiguration {

    @Bean
    public RestTemplate template(RestTemplateBuilder builder) {
        return builder.requestFactory(new HttpComponentsClientHttpRequestFactory()).build();
    }
}
